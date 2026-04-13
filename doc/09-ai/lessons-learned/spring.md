# Lecciones — Spring Boot / Framework

Spring Boot 4.x, AOP, Logback, i18n y comportamientos del framework.

---

### [2026-04-06] i18n: `LocaleContextFilter` es obligatorio para que filtros de seguridad respeten `Accept-Language`

**Contexto:** Peticiones con `Accept-Language: en-US` recibían `client_message` en español en errores generados en filtros de seguridad (401/403 de `BootstrapAdminKeyFilter`, 403 de `TenantResolutionFilter`).

**Problema (dos causas):**
1. **`LocaleContextFilter` nunca fue implementado:** Sin el filtro, `LocaleContextHolder.getLocale()` retorna `Locale.getDefault()` del JVM. En un servidor Linux en Chile (`LANG=es_CL.UTF-8`), eso devuelve `Locale(es, CL)` → mensajes en español. Los errores generados *antes* del `DispatcherServlet` (filtros de seguridad) nunca pasan por el `localeResolver` bean del Servlet.
2. **`I18nConfig.defaultLocale = es-CL`:** Incorrecto per diseño — el fallback debe ser `en-US`.

**Nota:** El header llega como `en-US` (hyphen, BCP 47 estándar), NO con underscore. La normalización `_` → `-` incluida en `KeyGoLocaleResolver` es código defensivo para clientes no estándares, pero no era la causa del bug.

**Solución / Buena práctica:**
- Crear `KeyGoLocaleResolver implements LocaleResolver` que lee el header crudo `Accept-Language`, normaliza `_` → `-` con `String.replace('_', '-')` (defensivo), y usa `Locale.LanguageRange.parse()` + `Locale.lookup()` para matching contra los locales soportados.
- Registrar como bean `localeResolver` (nombre que usa el `DispatcherServlet`) con `defaultLocale = Locale.US`.
- Crear `LocaleContextFilter extends OncePerRequestFilter` con `Ordered.HIGHEST_PRECEDENCE + 1`. Garantiza que errores de filtros también respetan `Accept-Language`.
- Para tests de limpieza: usar `es-CL` como header (no `en-US`) para verificar que el contexto fue efectivamente seteado y reseteado.

**Archivos clave:**
- `keygo-api/.../api/shared/KeyGoLocaleResolver.java` (nuevo)
- `keygo-api/.../api/shared/I18nConfig.java` (defaultLocale corregido)
- `keygo-run/.../run/filter/LocaleContextFilter.java` (nuevo — fix principal)
- `keygo-run/.../run/config/ApplicationConfig.java` (registro del filtro)
- Tests: `KeyGoLocaleResolverTest` (16 tests), `LocaleContextFilterTest` (8 tests)

---

### [2026-04-04] `@Aspect` con pointcut amplio NO debe depender de beans del contexto Spring

**Contexto:** Mejora del `KeyGoTracingAspect` para serializar objetos complejos a JSON con `tools.jackson.databind.json.JsonMapper`.

**Problema 1 — Constructor injection:** `@RequiredArgsConstructor` + `private final JsonMapper` → el contexto falla en tests:
```
Error creating bean with name 'keyGoTracingAspect': Requested bean is currently in creation
```
Causa: el inner `TestConfig` cae dentro del pointcut `within(io.cmartinezs.keygo..*)`. El factory `jsonMapper()` es interceptado por el aspecto, que aún necesita el bean → ciclo.

**Problema 2 — Field injection `@Autowired(required = false)`:** El ciclo persiste en la aplicación real:
```
keyGoTracingAspect → jacksonJsonMapper → jsonMapperBuilder → jsonMapperBuilderCustomizer
  (definido en ApplicationConfig dentro de io.cmartinezs.keygo.*) → keyGoTracingAspect
```

**Solución definitiva / Buena práctica:**
```java
// ✅ Correcto — sin dependencia circular, completamente autosuficiente
private static final JsonMapper TRACER_MAPPER = JsonMapper.builder().build();

// ❌ Incorrecto — circular en tests y en producción
@Autowired(required = false)
private JsonMapper jsonMapper;
```
El mapper del aspecto no necesita la configuración global de la app; camelCase es suficiente para logs de trazabilidad.

**Regla:** Los `@Aspect` que aplican a un paquete amplio deben ser **completamente autosuficientes**: instanciar sus propias dependencias como `static final`.

**Archivos clave:**
- `keygo-run/.../aop/KeyGoTracingAspect.java`
- `keygo-run/src/test/.../KeyGoTracingAspectTest.java`

---

### [2026-04-04] Trazabilidad AOP — `spring-boot-starter-aspectj` + pitfalls con `final class` y filtros Servlet

**Contexto:** Implementación de un Aspect Spring AOP en `keygo-run`.

**Problema 1 — Starter renombrado:**
`spring-boot-starter-aop` fue renombrado a **`spring-boot-starter-aspectj`** en Spring Boot 4.x. Maven falla con `'dependencies.dependency.version' is missing` porque el BOM ya no gestiona ese artifact.

**Problema 2 — `@Component final class` incompatible con CGLIB:**
```
Cannot subclass final class io.cmartinezs.keygo.api.error.ApiErrorDataFactory
```
**Regla:** cualquier `@Component` dentro del package interceptado por AOP **no puede ser `final`**.

**Problema 3 — Filtros Servlet: NPE en `GenericFilterBean.init()` con CGLIB:**
CGLIB/Objenesis crea el proxy sin llamar al constructor, dejando el campo interno `logger` como `null`. Tomcat falla con `NullPointerException: Cannot invoke "Log.isDebugEnabled()"`.

**Causa de `!target(T)` vs `!within(..)`:** `!target(jakarta.servlet.Filter)` (runtime) hace que el advice no se ejecute en esos beans. La solución correcta es `!within(*..filter..*)` (estático), que Spring AOP evalúa en tiempo de carga.

**Regla:** Para excluir tipos por package del pointcut AOP, preferir siempre `within()` sobre `target()`. Reservar `target()` solo para checks en runtime donde no existe alternativa estática.

**Solución:**
- En Spring Boot 4.x siempre usar `spring-boot-starter-aspectj`.
- Verificar en BOM: `grep -i "aop\|aspectj" ~/.m2/repository/org/springframework/boot/spring-boot-dependencies/<version>/*.pom`.
- Antes de activar AOP amplio, buscar beans `final`: `grep -rln "@Component\|@Service\|@RestController" ... | while read f; do grep -q "public final class" "$f" && echo "$f"; done`.

**Archivos clave:**
- `keygo-run/.../aop/KeyGoTracingAspect.java`
- `keygo-run/pom.xml` — `spring-boot-starter-aspectj`
- `keygo-api/.../error/ApiErrorDataFactory.java` — `final` eliminado

---

### [2026-04-03] `Accept-Language` header parsing — remover q-values antes de split

**Síntoma:** Header como `"fr;q=0.5"` no era parseado correctamente; se interpretaba como idioma `"fr;q"`.

**Causa:** Split por `"-"` se hacía sin remover antes los q-values separados por `";"`.

**Solución:** Dividir por `";"` **antes** de dividir por `"-"`: `headerValue.split(";")[0]` para remover `q=0.5`, luego `split("-")` para extraer idioma y región.

---

### [2026-04-03] `%clr` no registrado en Spring Boot 4 con `logback-spring.xml` personalizado

**Problema:** `%clr` es un converter personalizado de Spring Boot, no nativo de Logback. En Spring Boot 4 / Logback 1.5.32 con `logback-spring.xml` personalizado dejó de registrarse automáticamente.

**Solución / Buena práctica:** Agregar al inicio del `logback-spring.xml`, **antes** de cualquier `<springProfile>`:
```xml
<include resource="org/springframework/boot/logging/logback/defaults.xml"/>
```
Esto registra `%clr`, `%wex`, `%correlationId` y todas las propiedades estándar de Spring Boot.

**Archivos clave:** `keygo-run/src/main/resources/logback-spring.xml`

---

### [2026-04-03] Appender definido globalmente pero solo referenciado en `<springProfile>` — WARN Logback

**Problema:** Logback emitía `Appender named [CONSOLE] not referenced. Skipping further processing.` en los perfiles que no lo usaban. El formato JSON se activaba en el perfil `default`, produciendo logs ilegibles al desarrollar.

**Solución / Buena práctica:** Mover la **definición completa** del appender dentro del bloque `<springProfile>` que lo usa. Usar `!(desa | prod)` (no `!local`) como condición de consola colorida, para que el perfil `default` también reciba el formato legible.

**Archivos clave:** `keygo-run/src/main/resources/logback-spring.xml`

---

### [2026-04-03] Caracteres `[` y `]` no necesitan escape en patrones Logback

**Problema:** Usar `\[%X{traceId:--}\]` causaba error de parsing: `Illegal char '['`.

**Solución:** Los corchetes literales no son caracteres especiales en Logback; no necesitan escaparse. Usar simplemente `[%X{traceId:--}]`.

---

### [2026-04-03] `HttpStatus.UNPROCESSABLE_ENTITY` renombrado a `UNPROCESSABLE_CONTENT` en Spring Boot 4

**Problema:** `HttpStatus.UNPROCESSABLE_ENTITY` fue renombrado en Spring Boot 4 (alineación con RFC 9110). Falla con `cannot find symbol`.

**Solución:** Reemplazar todas las referencias a `UNPROCESSABLE_ENTITY` por `UNPROCESSABLE_CONTENT` en tests y código de producción.

---

### [2026-04-02] Trazabilidad MDC — `RequestTracingFilter` como primera capa del stack

**Problema:** Los filtros de seguridad (`BootstrapAdminKeyFilter`) corrían antes que el tracing, perdiendo el contexto MDC en los logs de autenticación.

**Solución / Buena práctica:** Registrar `RequestTracingFilter` con `Ordered.HIGHEST_PRECEDENCE` mediante `FilterRegistrationBean` en `ApplicationConfig`, de modo que corra **antes** de cualquier filtro Spring Security. Los filtros subsiguientes enriquecen el MDC sin borrar `traceId`: `BootstrapAdminKeyFilter` agrega `userId` y `TenantResolutionFilter` agrega `tenantSlug`. Todos limpian sus propias claves en `finally`.

**Archivos clave:** `RequestTracingFilter`, `BootstrapAdminKeyFilter`, `TenantResolutionFilter`, `ApplicationConfig`, `logback-spring.xml`

---

### [2026-04-02] `logstash-logback-encoder` 8.x — compatible con Spring Boot 4 / Logback 1.5.x

**Problema:** Spring Boot 4 usa Logback 1.5.x y SLF4J 2.x; versiones anteriores de `logstash-logback-encoder` (<7.x) no eran compatibles.

**Solución:** Usar `net.logstash.logback:logstash-logback-encoder:8.0`. Por defecto `LogstashEncoder` incluye todos los campos MDC automáticamente. Usar `logback-spring.xml` (no `logback.xml`) para poder usar `<springProfile>` con negación (`!local`).

**Archivos clave:** `keygo-run/pom.xml`, `logback-spring.xml`
