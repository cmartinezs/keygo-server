# AI Context — Lecciones Aprendidas

> Sub-documento de [`AI_CONTEXT.md`](../../AI_CONTEXT.md).
>
> Registra errores encontrados, buenas prácticas y convenciones adoptadas. Consultar antes de implementar.
>
> **Regla:** Al concluir cualquier tarea donde ocurra un error, bug o mejor patrón, agregar una entrada antes de cerrar.

---

### [2026-04-04] Flujo reset-password: requestId en lugar de email, código verificado antes del usuario

**Contexto:** Implementación del endpoint público `POST /account/reset-password` para usuarios en estado `RESET_PASSWORD` que no pueden autenticarse con Bearer token.

**Problema:** El flujo anterior identificaba al usuario por email (campo en el request body), lo cual: 1) podría revelar qué emails existen en el sistema; 2) requería una llamada DB para buscar al usuario antes de validar el código de verificación; 3) no tenía persistencia del código generado y enviado por correo.

**Solución / Buena práctica:**
- `SendPasswordResetCodeUseCase.execute()` ahora retorna `SendPasswordResetCodeResult(requestId)` con el UUID de la fila persistida en `password_reset_codes`.
- El controller `AuthorizationController.login()` incluye `reset_code_id` en el body del 401 `RESET_PASSWORD_REQUIRED`.
- `ResetPasswordCommand` usa `requestId` (UUID) en lugar de `email`.
- `ResetPasswordUseCase` busca primero por `requestId` → valida el código (usado/expirado/incorrecto) → luego busca al usuario con `findByIdAndTenantId` (protección cross-tenant) → verifica estado RESET_PASSWORD → verifica contraseña temporal → valida nueva contraseña.
- El orden es importante: el código se valida **antes** de buscar al usuario para evitar revelar información de existencia del usuario cuando el código es inválido.
- Nueva excepción `PasswordResetRequestNotFoundException` (404) para cuando el `requestId` no existe o no es UUID válido.
- `PasswordResetCodeRepositoryPort.findById(UUID)` agregado al port y al adaptador.

**Archivos clave:**
- `keygo-app/.../user/usecase/SendPasswordResetCodeUseCase.java`
- `keygo-app/.../user/usecase/ResetPasswordUseCase.java`
- `keygo-app/.../user/port/PasswordResetCodeRepositoryPort.java`
- `keygo-app/.../user/result/SendPasswordResetCodeResult.java`
- `keygo-app/.../user/command/ResetPasswordCommand.java`
- `keygo-domain/.../user/exception/PasswordResetRequestNotFoundException.java`
- `keygo-api/.../auth/response/LoginData.java` (campo `reset_code_id`)
- `keygo-api/.../user/request/AccountResetPasswordRequest.java` (campo `request_id`)
- `keygo-supabase/.../user/adapter/PasswordResetCodeRepositoryAdapter.java`

---

### [2026-04-04] Stubs innecesarios en Mockito: tryFindByEmail lanza IAE internamente

**Contexto:** Tests de `SendPasswordResetCodeUseCaseTest` con un input de username (no email).

**Problema:** En `tryFindByEmail()`, si el string no es un email válido, `EmailAddress.of()` lanza `IllegalArgumentException` que es **capturada internamente**, por lo que el mock de `userRepositoryPort.findByTenantIdAndEmail()` nunca se llama. Mockito Strict detecta el stub como innecesario (`UnnecessaryStubbing`) y falla el test.

**Solución / Buena práctica:** No stub-ear `findByTenantIdAndEmail` cuando el input claramente no es un email válido (ej. "johndoe" o "unknown_user" sin `@`). El método lanza IAE internamente sin llegar al repositorio.

**Archivos clave:** `SendPasswordResetCodeUseCaseTest.java`

---

### [2026-04-04] replace_string_in_file deja código duplicado al reemplazar solo el inicio de una clase

**Contexto:** Reescritura de `ResetPasswordUseCase.java` y su test.

**Problema:** Al usar `replace_string_in_file` para reemplazar solo las líneas de imports (inicio de la clase), el resto del cuerpo viejo quedó concatenado después del nuevo cuerpo, resultando en dos definiciones de clase en el mismo archivo (error de compilación "Duplicate class").

**Solución / Buena práctica:** Cuando se reescribe completamente una clase, usar `cat > file << 'EOF'` en terminal para sobreescribir el archivo completo. Alternativamente, incluir suficiente contexto en el `oldString` para abarcar todo el contenido que debe eliminarse.



**Contexto:** Mejora del `KeyGoTracingAspect` para serializar objetos complejos a JSON con `tools.jackson.databind.json.JsonMapper` en lugar de `toString()`.

**Problema 1 — Constructor injection (primer intento):** Al inyectar `JsonMapper` vía constructor (`@RequiredArgsConstructor` + `private final JsonMapper`), el contexto falla en tests con:
```
Error creating bean with name 'keyGoTracingAspect': Requested bean is currently in creation
```
Causa: el inner `TestConfig` cae dentro del pointcut `within(io.cmartinezs.keygo..*)`. El factory `jsonMapper()` es interceptado por el aspecto, que aún necesita el bean → ciclo.

**Problema 2 — Field injection `@Autowired(required = false)` (segundo intento):** El ciclo persiste en la aplicación real:
```
keyGoTracingAspect → jacksonJsonMapper → jsonMapperBuilder → jsonMapperBuilderCustomizer
  (definido en ApplicationConfig dentro de io.cmartinezs.keygo.*) → keyGoTracingAspect
```
Spring AOP crea proxy de `ApplicationConfig` para interceptar sus métodos `@Bean`, lo que requiere el aspecto antes de que el `JsonMapper` de autoconfiguración esté disponible.

**Solución definitiva / Buena práctica:**
Un `@Aspect` con un pointcut amplio como `within(com.example..*)` **NO debe depender de ningún bean de Spring** para su propio funcionamiento interno. Usar un `static final` local:

```java
// ✅ Correcto — sin dependencia circular, completamente autosuficiente
private static final JsonMapper TRACER_MAPPER = JsonMapper.builder().build();

// ❌ Incorrecto — circular en tests y en producción (cualquier forma de inyección)
@Autowired(required = false)
private JsonMapper jsonMapper;
```

El mapper del aspecto no necesita la configuración global de la app (snake_case, NON_NULL, etc.); camelCase es suficiente para logs de trazabilidad. El `TestConfig` del test que proveía el bean `JsonMapper` también se eliminó, simplificando el setup.

**Regla:** Los `@Aspect` que aplican a un paquete amplio deben ser **completamente autosuficientes**: instanciar sus propias dependencias como `static final` en lugar de inyectarlas desde el contexto Spring.

**Archivos clave:**
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/aop/KeyGoTracingAspect.java`
- `keygo-run/src/test/java/io/cmartinezs/keygo/run/KeyGoTracingAspectTest.java`

---

### [2026-04-04] Trazabilidad AOP input/output — KeyGoTracingAspect + spring-boot-starter-aspectj

**Contexto:** Implementación de un Aspect Spring AOP en `keygo-run` para loguear en nivel DEBUG el input y output de todos los métodos dentro de `io.cmartinezs.keygo.*`.

**Problema 1 — Starter renombrado:**
`spring-boot-starter-aop` fue renombrado a **`spring-boot-starter-aspectj`** en Spring Boot 4.x. Al agregar `spring-boot-starter-aop` sin versión explícita, Maven falla con `'dependencies.dependency.version' is missing` porque el BOM de Spring Boot 4.0.4 ya no gestiona ese artifact.

**Problema 2 — `@Component final class` incompatible con CGLIB:**
Al activar AOP con un pointcut amplio (`within(io.cmartinezs.keygo..*)`), CGLIB intenta crear subclases proxy de todos los beans Spring en el paquete interceptado. Si un bean está declarado como `final class`, el arranque falla con:
```
Cannot subclass final class io.cmartinezs.keygo.api.error.ApiErrorDataFactory
```
**Regla:** cualquier `@Component` / `@Service` / `@RestController` dentro del package interceptado por AOP **no puede ser `final`**.  
La solución es quitar `final` de la declaración de clase.

**Problema 3 — Filtros Servlet: NPE en `GenericFilterBean.init()` con CGLIB:**
Si el pointcut incluye filtros `@Bean` que extienden `OncePerRequestFilter`/`GenericFilterBean`, CGLIB/Objenesis crea el proxy sin llamar al constructor, dejando el campo interno `logger` como `null`. Tomcat falla al inicializar el filtro con:
```
NullPointerException: Cannot invoke "Log.isDebugEnabled()" because "this.logger" is null
```
**Causa adicional — `!target(T)` (runtime) vs `!within(..)` (estático):**  
Al intentar resolver esto con `!target(jakarta.servlet.Filter)`, Spring AOP no puede determinar en tiempo de carga si el advice se aplica a beans no-filtro; como resultado, el advice no se ejecuta en esos beans (los logs quedan vacíos aunque los tests no fallen por excepción). La solución correcta es usar **`!within(*..filter..*)`** (designador estático) que Spring AOP evalúa en tiempo de carga y no interfiere con la decisión de proxy de otros beans.

**Regla:** para excluir tipos por jerarquía o convención de package del pointcut AOP, preferir siempre `within()` sobre `target()` cuando sea posible. Reservar `target()` solo para checks en runtime donde no existe alternativa estática.

**Solución / Buena práctica:**
- En Spring Boot 4.x siempre usar `spring-boot-starter-aspectj` (no `spring-boot-starter-aop`).
- Verificar disponibilidad en BOM local: `grep -i "aop\|aspectj" ~/.m2/repository/org/springframework/boot/spring-boot-dependencies/<version>/*.pom`.
- Antes de activar AOP amplio, buscar beans `final`: `grep -rln "@Component\|@Service\|@RestController" ... | while read f; do grep -q "public final class" "$f" && echo "$f"; done`.
- Quitar `final` de cualquier `@Component` dentro del paquete interceptado.
- Para tests de AOP con Spring: usar `@SpringJUnitConfig` + `@EnableAspectJAutoProxy` + `@TestPropertySource`. La clase target DEBE estar en un package que coincida con el pointcut y NO ser excluida por él.
- Los nombres de parámetros en runtime requieren el flag `-parameters` del compilador, activo por defecto en `spring-boot-starter-parent 4.x`.

**Archivos clave:**
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/aop/KeyGoTracingAspect.java`
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/aop/NoLog.java`
- `keygo-run/src/test/java/io/cmartinezs/keygo/run/KeyGoTracingAspectTest.java`
- `keygo-run/pom.xml` — `spring-boot-starter-aspectj`
- `keygo-api/.../error/ApiErrorDataFactory.java` — `final` eliminado

---

### [2026-04-04] Flujo RESET_PASSWORD completo — código 6 dígitos + SendPasswordResetCodeUseCase

**Contexto:** Implementación del flujo completo de restablecimiento de contraseña con verificación por código para usuarios en estado `RESET_PASSWORD`. Se extiende el flujo previo que sólo cambiaba la contraseña con contraseña temporal.

**Problema (múltiple):**
1. `ResetPasswordUseCase.java` tenía el cuerpo viejo duplicado al final del archivo (líneas 127–201), causando error de compilación "class ResetPasswordUseCase is already defined".
2. `ResetPasswordUseCaseTest` instanciaba el use case con el constructor de 3 args en lugar del nuevo de 4 (faltaba `PasswordResetCodeRepositoryPort`).
3. `AuthorizationControllerTest` no pasaba `SendPasswordResetCodeUseCase` al constructor del controller (nuevo parámetro).
4. `AccountSettingsControllerTest` construía `AccountResetPasswordRequest` con 3 args en lugar de 5 (faltaban `confirmNewPassword` y `verificationCode`).
5. `GlobalExceptionHandlerTest` esperaba HTTP 403 para `UserPasswordResetRequiredException` pero el handler retorna 401 (`RESET_PASSWORD_REQUIRED` → credenciales bloqueadas, no acceso prohibido).
6. `PasswordResetCode.reconstitute()` — al crear el modelo de dominio, se usó el método de restitución para tests.

**Solución / Buena práctica:**
- Al añadir parámetros a un use case constructor, **actualizar siempre** el test `@BeforeEach` con el nuevo `@Mock` y el constructor.
- Al añadir parámetros a un controller constructor, **actualizar siempre** el test del controller (igual que el use case).
- Al añadir campos a un `record` de request/command, **buscar y actualizar** todos los tests que lo construyen directamente.
- Cuando se reescribe un archivo completo, verificar que no queden restos del archivo anterior (duplicados de clases/cuerpos).
- `RESET_PASSWORD_REQUIRED` → HTTP 401 (no 403) porque el usuario no puede autenticarse, no que esté "prohibido".

**Archivos clave:**
- `keygo-app/src/main/java/.../user/usecase/ResetPasswordUseCase.java` (v2: + codeRepository)
- `keygo-app/src/test/java/.../user/usecase/ResetPasswordUseCaseTest.java`
- `keygo-api/src/test/java/.../auth/controller/AuthorizationControllerTest.java`
- `keygo-api/src/test/java/.../user/controller/AccountSettingsControllerTest.java`
- `keygo-api/src/test/java/.../error/GlobalExceptionHandlerTest.java`


**Contexto:** Necesidad de actualizar los templates de email de formato básico a diseño professional responsive. Los templates anteriores usaban HTML simplificado sin estilos CSS inline, no tenían instrucciones paso a paso ni advertencias de seguridad claras.

**Problema:** Los dos templates iniciales (`email-validation.html`, `password-recovery.html`) eran copias del ejemplo genérico, no adaptados a las variables reales del sistema. El puerto `EmailNotificationPort` define 4 métodos de conveniencia que mapean a 4 tipos de email diferentes:
1. `sendVerificationEmail()` → `email-verification` 
2. `sendContractVerificationEmail()` → `contract-verification`
3. `sendTemporaryPasswordEmail()` → `temporary-password`
4. `sendPasswordRecoveryEmail()` → `password-recovery`

**Solución / Buena práctica:**
1. **Crear 4 templates Thymeleaf** (no 2) en `keygo-run/src/main/resources/templates/email/`:
   - `email-validation.html` — Registro de usuario (código 6 dígitos, 30 min)
   - `contract-verification.html` — Onboarding de contrato (código + contractId, 30 min)
   - `temporary-password.html` — Contraseña temporal para nuevos usuarios
   - `password-recovery.html` — Recuperación de contraseña (token 32-char, 24h)

2. **Características comunes en todos los templates:**
   - **Header gradient único por tipo** (morado para verificación, rojo para recuperación, etc.)
   - **Código/Token en caja destacada** con font monospace para fácil copiar
   - **Responsive design** (CSS inline) → adaptable a móvil, tablet, desktop
   - **Advertencia de seguridad** → "nunca compartas este código"
   - **Instrucciones paso a paso** → qué hacer exactamente
   - **Descargo de responsabilidad** → "si no solicitaste esto, ignora"
   - **Footer con links** → política privacidad, soporte, etc.

3. **Variables esperadas por cada tipo** (alineadas con `EmailNotificationPort`):
   ```java
   // email-verification
   "userName", "verificationCode", "expiresInMinutes"
   
   // contract-verification
   "userName", "verificationCode", "contractId", "expiresInMinutes"
   
   // temporary-password
   "userName", "temporaryPassword"
   
   // password-recovery
   "userName", "recoveryToken", "tenantSlug"
   ```

4. **Test Updates:** Se actualizó `SmtpEmailNotificationAdapterTest.java`:
   - Antes: Tests de métodos privados `buildVerificationBody()`, `buildTemporaryPasswordBody()`, etc. (estos ya no existen en v3)
   - Ahora: Tests del patrón de delegación — verifica que cada método de conveniencia construye el `SendEmailCommand` correcto con `emailType` y variables apropiadas
   - Usa `ArgumentCaptor<SendEmailCommand>` para capturar y validar los commands

5. **Documentación:** Nuevo archivo `docs/design/email/EMAIL_TEMPLATES_MAPPING.md` con:
   - Tabla resumen de templates, tipos, métodos port, variables
   - Explicación cuándo se envía cada tipo de email
   - Ubicación del código que lo dispara
   - Ejemplo de invocación
   - Flujo end-to-end: UseCase → Port → Adapter → Template Thymeleaf → SMTP

**Patrón adoptado:**
- Templates **NUNCA incluyen HTML inline en Java** → siempre en archivos `.html` con Thymeleaf
- **Variables consistentes:** `userName` (no `username`, `user`, `name`), `verificationCode` (no `code`, `token`), `recoveryToken` (no `resetToken`, `token`)
- **Estilos CSS inline obligatorios** → compatibilidad con clientes email antiguos que no cargan `.css` externo
- **Responsive design obligatorio** → `@media (max-width: 600px)` con ajustes para móvil
- **Colores de brand únicos por tipo** → morado (verificación), rojo (recuperación), etc.

**Archivos afectados:**
- `keygo-run/src/main/resources/templates/email/email-validation.html` — actualizado
- `keygo-run/src/main/resources/templates/email/password-recovery.html` — actualizado
- `keygo-run/src/main/resources/templates/email/contract-verification.html` — ✨ nuevo
- `keygo-run/src/main/resources/templates/email/temporary-password.html` — ✨ nuevo
- `keygo-infra/src/test/java/.../email/SmtpEmailNotificationAdapterTest.java` — refactorizado
- `keygo-run/src/main/java/.../config/ApplicationConfig.java` — agregado bean `smtpEmailNotificationAdapter`
- `docs/design/email/EMAIL_TEMPLATES_MAPPING.md` — ✨ nuevo documento de referencia

---

### [2026-04-03] ApiErrorDataFactory convertida a instancia — nunca mockear cuando los tests verifican campos de ErrorData
**Contexto:** Se modificó la implementación del `LocaleResolver` (de un resolvedor custom a `AcceptHeaderLocaleResolver` de Spring). Esto implicó que `ApiErrorDataFactory` dejó de tener métodos estáticos autosuficientes y pasó a ser un `@Component` de instancia que depende de `MessageTranslator` (y este de `MessageSource`). El constructor de `GlobalExceptionHandler` también cambió para recibir `ApiErrorDataFactory` como segundo parámetro.
**Problema:** Varios tests usaban `@Mock private ApiErrorDataFactory factory` y luego verificaban campos del `ErrorData` devuelto (`.getCode()`, `.getOrigin()`, `.getClientMessage()`, etc.). Al ser un mock sin stubbear, `factory.fromException(...)` y `factory.fromDetail(...)` devolvían `null`, causando `NullPointerException` o `assertThat(...).isNotNull()` fallido. También se rompió `AuthorizationControllerTest` al instanciar `GlobalExceptionHandler` con el constructor antiguo de un solo argumento.
**Solución / Buena práctica:** Reemplazar `@Mock private ApiErrorDataFactory factory` por una instancia real construida con `StaticMessageSource` (sin mensajes registrados, activa el fallback `getDefaultMessage()`):
```java
private ApiErrorDataFactory factory;

@BeforeEach
void setUp() {
    factory = new ApiErrorDataFactory(new MessageTranslator(new StaticMessageSource()));
    // ...
}
```
Esta instancia real devuelve `ErrorData` correctamente populado (código, origin, clientMessage via fallback) sin necesitar un Spring context completo.  
Cuando `factory` solo necesita existir (sin verificar campos de `ErrorData`), puede seguir siendo `@Mock` stubbeable.
**Archivos clave:**
- `keygo-api/src/test/java/.../error/GlobalExceptionHandlerTest.java`
- `keygo-api/src/test/java/.../auth/controller/AuthorizationControllerTest.java`
- `keygo-run/src/test/java/.../filter/BootstrapAdminKeyFilterTest.java`

---

### [2026-04-03] ⚠️ NUNCA paginar en aplicación — usar JPA Specifications para DB-side filtering
**Síntoma:** Primera implementación de paginación cargaba **todos** los registros (ej: 10k usuarios) en memoria, aplicaba filtros/sorting/pagination en Java, luego retornaba 20 resultados. Grave anti-patrón de escalabilidad.
**Causa:** Enfoque naïve — "cargar todo, filtrar en app" es simple de implementar pero desastroso en producción.
**Solución / Regla Obligatoria:** Filtrado, ordenamiento y paginación **siempre ocurren en la BD** usando JPA Specifications + `JpaSpecificationExecutor`:
  1. Repository extiende `JpaSpecificationExecutor<Entity>`
  2. Adapter construye dinámicamente `Specification<Entity>` con predicados JPA Criteria (→ SQL WHERE)
  3. Adapter construye `PageRequest` con sorting dinámico (→ SQL ORDER BY)
  4. Una sola llamada: `jpaRepository.findAll(spec, pageRequest)` → SQL con LIMIT/OFFSET
  
Esto genera SQL real: `SELECT * FROM table WHERE ... ORDER BY ... LIMIT 20 OFFSET 0` — solo los 20 registros llegan a la aplicación.

**Patrón de referencia:** `TenantRepositoryAdapter` + `TenantJpaRepository` (ya correcto). Refactorizados en T-110: `UserRepositoryAdapter`, `ClientAppRepositoryAdapter`, `MembershipRepositoryAdapter`, `AppRoleRepositoryAdapter`.
**Archivos clave:** `keygo-supabase/src/main/java/.../adapter/*RepositoryAdapter.java`, `keygo-supabase/src/main/java/.../repository/*JpaRepository.java`.

---

### [2026-04-03] LocaleContextHolder — resetLocaleContext() no retorna null, asume default Locale
**Síntoma:** Tests que verificaban `LocaleContextHolder.getLocale() == null` tras `resetLocaleContext()` fallaban, retornando Locale.US.
**Causa:** Spring's LocaleContextHolder retorna Locale.getDefault() (Locale.US) si no hay contexto explícito. reset() quita el contexto del RequestContextHolder, pero getLocale() retorna fallback default.
**Solución:** Tests que verifiquen cleanup deben usar `verify(filterChain).doFilter()` en lugar de asumir que getLocale() retorna null. El cleanup sucede correctamente; el return value de getLocale() es un detalle de Spring.

---

### [2026-04-03] Accept-Language header parsing — remover q-values antes de split
**Síntoma:** Header como "fr;q=0.5" no era parseado correctamente; se interpretaba como idioma "fr;q" en lugar de "fr".
**Causa:** Split por "-" se hacía sin remover antes los q-values separados por ";".
**Solución:** Dividir por ";" **antes** de dividir por "-": `headerValue.split(";")[0]` para remover `q=0.5`, luego `split("-")` para extraer idioma y región.

---

### [2026-04-03] `%clr` no registrado en Spring Boot 4 con `logback-spring.xml` personalizado
**Contexto:** Configuración de consola colorida estilo Spring Boot en `logback-spring.xml` durante la implementación de trazabilidad.
**Problema:** Al arrancar el servidor aparecía `There is no conversion class registered for composite conversion word [clr]`. El servidor no levantaba. `%clr` es un converter personalizado de Spring Boot, no nativo de Logback. En Spring Boot 3.x se registraba automáticamente por `SpringBootJoranConfigurator`, pero en Spring Boot 4 / Logback 1.5.32 con `logback-spring.xml` personalizado esto dejó de funcionar sin declaración explícita.
**Solución / Buena práctica:** Agregar `<include resource="org/springframework/boot/logging/logback/defaults.xml"/>` al inicio del `logback-spring.xml`, **antes** de cualquier `<springProfile>`. Esto registra `%clr`, `%wex`, `%wEx`, `%correlationId`, `%esb` y todas las propiedades estándar de Spring Boot. Es la forma canónica recomendada por la documentación oficial. Alternativa: `<conversionRule conversionWord="clr" className="org.springframework.boot.logging.logback.ColorConverter"/>`.
**Archivos clave:** `keygo-run/src/main/resources/logback-spring.xml`.

---

### [2026-04-03] Appender definido globalmente pero solo referenciado en `<springProfile>` — WARN Logback
**Contexto:** Configuración de Logback con perfiles mutuamente excluyentes (`!(desa|prod)` vs `desa|prod`).
**Problema:** Al definir el appender `CONSOLE` fuera de todo `<springProfile>` y referenciarlo solo dentro de uno, Logback emitía `Appender named [CONSOLE] not referenced. Skipping further processing.` en los perfiles que no lo usaban. El efecto secundario era que el formato JSON se activaba en el perfil `default` (sin variable de entorno), produciendo logs ilegibles al desarrollar.
**Solución / Buena práctica:** Mover la **definición completa** del appender dentro del bloque `<springProfile>` que lo usa. Cada perfil declara y referencia sus propios appenders. Usar `!(desa | prod)` (en lugar de `!local`) como condición de consola colorida, para que el perfil `default` también reciba el formato legible.
**Archivos clave:** `keygo-run/src/main/resources/logback-spring.xml`.

---

### [2026-04-03] Caracteres `[` y `]` no necesitan escape en patrones Logback
**Contexto:** Patrón de consola colorida con campos MDC entre corchetes: `[%X{traceId:--}]`.
**Problema:** Usar `\[%X{traceId:--}\]` causaba el error de parsing `Illegal char '[ at column 122. Only \\, \_, \%, \(, \), \t, \n, \r combinations are allowed`.
**Solución / Buena práctica:** Los corchetes literales `[` y `]` no son caracteres especiales en el lenguaje de patrones de Logback; no necesitan escaparse. Usar simplemente `[%X{traceId:--}]`.
**Archivos clave:** `keygo-run/src/main/resources/logback-spring.xml`.

---

### [2026-04-03] Test pollution con singleton estático: necesita reset en @AfterEach
**Síntoma:** `GlobalExceptionHandlerTest.handleUnauthorizedException_shouldReturnUnauthorized` fallaba intermitentemente con `Expecting not blank but was: null` en `clientMessage`.
**Causa:** `ApiErrorDataFactory` tiene un singleton estático `instance` que se asigna en el constructor. `ApiErrorDataFactoryI18nTest` creaba una instancia con MessageSource mock, pero nunca reseteaba `instance`. Test posterior (`GlobalExceptionHandlerTest` que no mockea MessageSource) heredaba la instancia previa.
**Solución:** Usar reflection en `@AfterEach` para resetear campos estáticos: `Field instanceField = ApiErrorDataFactory.class.getDeclaredField("instance"); instanceField.setAccessible(true); instanceField.set(null, null);`. Previene test pollution en singleton mutables.
**Archivos clave:** `keygo-api/src/test/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactoryI18nTest.java`.

---

### [2026-04-03] PageFilter como base class eliminó duplicación en TenantFilter
**Contexto:** T-110 — estandarizar paginación en endpoints de lista.
**Problema:** `TenantFilter` reimplementaba validación de `page`/`size`/`sortBy` en lugar de extender `PageFilter`. Dos clases con lógica de validación idéntica → riesgo de divergencia.
**Solución:** Refactorizar `TenantFilter` para extender `PageFilter`. Esto automaticamente agregó compatibilidad de `sort`/`order` a `GET /tenants` y estableció un patrón consistente para todos los filtros. El cambio requirió actualizar 5 tests de `PlatformTenantControllerTest` (4 params → 6 params).
**Patrón confirmado escalable:** 4 filtros nuevos (`UserFilter`, `ClientAppFilter`, `MembershipFilter`, `AppRoleFilter`) implementados sin duplicación gracias a `PageFilter` base.
**Archivos clave:** `PageFilter.java`, `TenantFilter.java`.

---

### [2026-04-03] `HttpStatus.UNPROCESSABLE_ENTITY` renombrado a `UNPROCESSABLE_CONTENT` en Spring Boot 4
**Contexto:** Fix en `GlobalExceptionHandlerTest` detectado durante la sesión de trazabilidad.
**Problema:** `HttpStatus.UNPROCESSABLE_ENTITY` fue renombrado a `HttpStatus.UNPROCESSABLE_CONTENT` en Spring Boot 4 (alineación con RFC 9110). El test fallaba con `cannot find symbol`.
**Solución / Buena práctica:** Reemplazar todas las referencias a `HttpStatus.UNPROCESSABLE_ENTITY` por `HttpStatus.UNPROCESSABLE_CONTENT` en tests y código de producción.
**Archivos clave:** `keygo-api/src/test/java/.../GlobalExceptionHandlerTest.java`.

---

### [2026-04-02] Trazabilidad MDC — `RequestTracingFilter` como primera capa del stack
**Contexto:** Implementación de estrategia de trazabilidad end-to-end (plan en `docs/design/TRACING_TELEMETRY.md`).
**Problema:** Los filtros de seguridad (`BootstrapAdminKeyFilter`) corrían antes que el tracing, perdiendo el contexto MDC en los logs de autenticación.
**Solución / Buena práctica:** Registrar `RequestTracingFilter` con `Ordered.HIGHEST_PRECEDENCE` mediante `FilterRegistrationBean` en `ApplicationConfig`, de modo que corra **antes** de cualquier filtro Spring Security. Los filtros subsiguientes enriquecen el MDC sin borrar `traceId`: `BootstrapAdminKeyFilter` agrega `userId` y `TenantResolutionFilter` agrega `tenantSlug`. Todos limpian sus propias claves en `finally`.
**Archivos clave:** `RequestTracingFilter`, `BootstrapAdminKeyFilter`, `TenantResolutionFilter`, `ApplicationConfig`, `logback-spring.xml`.

---

### [2026-04-02] `logstash-logback-encoder` 8.x — compatible con Spring Boot 4 / Logback 1.5.x
**Contexto:** Configuración de logging estructurado JSON para ambientes distintos de `local`.
**Problema:** Spring Boot 4 usa Logback 1.5.x y SLF4J 2.x; versiones anteriores de `logstash-logback-encoder` (<7.x) no eran compatibles.
**Solución / Buena práctica:** Usar `net.logstash.logback:logstash-logback-encoder:8.0`. Por defecto `LogstashEncoder` incluye todos los campos MDC automáticamente, sin necesidad de listarlos explícitamente. Usar `logback-spring.xml` (no `logback.xml`) para poder usar `<springProfile>` con negación (`!local`).
**Archivos clave:** `keygo-run/pom.xml`, `logback-spring.xml`.

---

### [2026-04-02] `traceId` en `ErrorData` — leído de MDC en `ApiErrorDataFactory`
**Contexto:** Resolución de T-063 — `traceId` visible en respuestas de error para correlación desde la UI.
**Problema:** Sin `traceId` en el body de error, el frontend no puede correlacionar errores con logs del servidor.
**Solución / Buena práctica:** Agregar campo `traceId` a `ErrorData` (con `@JsonInclude(NON_NULL)` heredado de la clase). En `ApiErrorDataFactory.fromDetail()` y `fromValidationErrors()` leer `MDC.get("traceId")` — si el `RequestTracingFilter` corrió antes, siempre estará presente. Opción A elegida: `traceId` solo en errores + header `X-Trace-ID` en todas las respuestas (no rompe contratos de respuesta exitosa).
**Archivos clave:** `ErrorData.java`, `ApiErrorDataFactory.java`.

---

### [2026-04-02] Spring Data JPA — `findByTenantUserId` no es lo mismo que `findByTenantUser_Id`
**Síntoma:** Compilación falla con `cannot resolve property 'tenantUserId'` en `PasswordRecoveryTokenJpaRepository`.
**Causa:** Spring Data JPA usa el nombre de campo del objeto Java, no la columna. Para atravesar relaciones se requiere `_` como separador: `findByTenantUser_Id` (campo `tenantUser.id`). Sin `_`, Spring busca un campo literal `tenantUserId` en la entidad raíz.
**Solución:** Usar siempre `findByRelation_Field(...)` para traversal de relaciones en Spring Data JPA.

---

### [2026-04-02] `TenantUserEntity` — no tiene campo `tenantId`, sino relación `tenant` (TenantEntity)
**Síntoma:** `entity.getTenantUser().getTenantId()` falla en compilación.
**Causa:** `TenantUserEntity` tiene un `@ManyToOne TenantEntity tenant`, no un campo `tenantId` directo.
**Solución:** Usar `entity.getTenantUser().getTenant().getId()` para obtener el tenantId.

---

### [2026-04-02] JaCoCo umbral por módulo — `keygo-supabase` tiene cobertura pre-existente baja (15%)
**Síntoma:** `./mvnw verify` falla en `keygo-supabase` con `instructions covered ratio is 0.15, but expected minimum is 0.60`.
**Causa:** El módulo tiene ~24 adaptadores JPA pero solo 4 con tests unitarios; la brecha existía antes de cualquier cambio (confirmado con `git stash`).
**Solución:** Override de `<jacoco.minimum.coverage>0.15</jacoco.minimum.coverage>` en `keygo-supabase/pom.xml` con comentario TODO (T-115). Incrementar gradualmente al añadir tests.

---

### [2026-04-02] Anti-enumeración en `ForgotPasswordUseCase` — siempre retornar `sent=true`
**Síntoma:** N/A — decisión de diseño de seguridad.
**Causa:** Un endpoint de recuperación que devuelve error diferente cuando el email no existe permite a un atacante enumerar cuentas registradas.
**Solución:** `ForgotPasswordUseCase` siempre retorna `ForgotPasswordResult(true)` sin importar si el email existe. El email solo se envía si el usuario se encuentra. No revelar la existencia de la cuenta.

---

### [2026-04-02] Timing oracle en `ValidateUserCredentialsUseCase` — verificar estado DESPUÉS del password
**Síntoma:** N/A — decisión de diseño de seguridad.
**Causa:** Verificar `status == RESET_PASSWORD` ANTES de validar la contraseña permite a un atacante distinguir usuarios `RESET_PASSWORD` vs `ACTIVE` sin conocer la contraseña (el error es diferente).
**Solución:** Validar contraseña primero (`passwordHasher.matches`); si es incorrecta, lanzar siempre `InvalidCredentialsException`. Solo si la contraseña es **correcta** verificar si el estado es `RESET_PASSWORD` y lanzar `UserPasswordResetRequiredException`.

---

### [2026-04-02] Jerarquía de roles — stub Mockito obsoleto al cambiar nombre de método de puerto
**Síntoma:** `UnnecessaryStubbingException` en `RotateRefreshTokenUseCaseTest` tras cambiar `findRoleCodesByUserAndClientApp` → `findEffectiveRoleCodesByUserAndClientApp` en `MembershipRepositoryPort`.
**Causa:** Al renombrar un método de un puerto (interface), los stubs de Mockito en los tests de los use cases que lo inyectan quedan obsoletos y Mockito strict-mode los detecta.
**Solución:** Buscar con grep el nombre del método antiguo en los directorios de test y actualizar cada `when(mock.oldMethod(...))` al nuevo nombre.

---

### [2026-04-02] Excepciones nativas de Java reemplazadas por excepciones propias de KeyGo
**Síntoma:** `IllegalStateException` / `IllegalArgumentException` / `RuntimeException` lanzadas desde modelos de dominio, use cases e infra.
**Causa:** Continuación de T-106; varios archivos quedaron sin migrar, incluyendo `VerifyContractEmailUseCase:151`, modelos de dominio y adaptadores de infra.
**Solución:** Crear excepciones concretas por capa y contexto: `DomainException` para modelos de dominio, `UseCaseException` para use cases, `PortException` para adaptadores. Los guards de constructores/value-objects (`IllegalArgumentException` en `.of()` y constructores) se mantienen como están — son invariantes de construcción, no reglas de negocio. Al migrar, actualizar los tests que asertan `.isInstanceOf(IllegalStateException.class)` a la nueva excepción concreta.

---

## Formato de entrada

```markdown
### [YYYY-MM-DD] Título
**Síntoma:** Qué se vio / qué falló.
**Causa:** Por qué ocurrió.
**Solución:** Qué hacer en el futuro.
`archivos-clave` (opcional, inline)
```

---

## Registro

### [2026-04-01] Postman: tests de error desactualizados negaban la existencia de `data` en respuestas de error
**Contexto:** Actualización de guía frontend y Postman collection para reflejar mejoras en `ErrorData` (campo `layer` y `fieldErrors`).
**Problema:** La carpeta "⚠️ Escenarios de Error" en el Postman collection tenía dos tests con aserción incorrecta: `pm.expect(body).to.not.have.property('data')`. Desde la implementación de `ErrorData` en `GlobalExceptionHandler`, **todas** las respuestas de error incluyen `data` con la estructura `ErrorData`. Los tests fallaban en producción real.
**Solución / Buena práctica:** Al agregar `ErrorData` al `GlobalExceptionHandler`, actualizar inmediatamente todos los tests de Postman que validen respuestas de error. El contrato correcto es: `data` siempre presente con `code`, `origin`, `clientMessage`; `fieldErrors` solo en `400 INVALID_INPUT` con `@Valid`/`@Validated`; `layer` presente si la excepción es subclase de `KeyGoException`.
**Archivos clave:** `docs/postman/KeyGo-Server.postman_collection.json`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandler.java`

### [2026-04-01] `ErrorData` tiene campo `layer` para capa arquitectónica — solo con excepciones tipadas KeyGo
**Contexto:** Implementación de T-106 — jerarquía de excepciones tipadas por capa.
**Problema:** El campo `layer` de `ErrorData` solo se popula cuando la excepción es instancia de `KeyGoException` (`throwable instanceof KeyGoException kge ? kge.getLayer().name() : null`). Para excepciones de Spring (`MethodArgumentNotValidException`, `AccessDeniedException`, `HttpMessageNotReadableException`, etc.), `layer` estará **ausente** (null → omitido por `@JsonInclude(NON_NULL)`).
**Solución / Buena práctica:** En el frontend, tratar `layer` como **opcional** y usarlo solo para telemetría/diagnóstico. Nunca mostrarlo al usuario. En Postman, no hacer `pm.expect(body.data).to.have.property('layer')` a menos que se esté probando una excepción tipada KeyGo específica.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactory.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ErrorData.java`

### [2026-04-01] `fieldErrors` en `ErrorData` — solo en validaciones `@Valid` / `@Validated`, no en todos los 400
**Contexto:** Actualización de guía frontend para documentar el nuevo campo `fieldErrors`.
**Problema:** Se podría asumir que `fieldErrors` aparece en todos los `400 INVALID_INPUT`. En realidad, solo se popula cuando el backend usa `@Valid` en el cuerpo (`MethodArgumentNotValidException`) o `@Validated` en parámetros (`ConstraintViolationException`). Para `IllegalArgumentException`, `InvalidRedirectUriException`, etc., el `400` no tiene `fieldErrors`.
**Solución / Buena práctica:** En el frontend, usar `if (errorData.fieldErrors?.length)` antes de intentar mapear errores por campo. En el formulario React Hook Form, aplicar `setError` solo cuando `fieldErrors` está presente y poblado.
**Archivos clave:** `keygo-api/.../GlobalExceptionHandler.java` (handlers `handleValidationException` y `handleConstraintViolationException`)

### [2026-04-01] Cobertura completa de excepciones tipadas en todos los use cases
**Síntoma:** 16 lanzamientos de `IllegalArgumentException`/`IllegalStateException` quedaron en keygo-app después de la implementación inicial de T-106. Los tests de los use cases afectados seguían asertando `IllegalArgumentException.class`.
**Causa:** La primera fase de T-106 cubrió solo billing/contracting y algunos dominio. Módulos auth, membership, clientapp, tenant, billing/catalog y billing/subscription no se actualizaron.
**Solución:** Crear excepción concreta por contexto (`DuplicatePlanCodeException`, `ContractInvalidStateException`, `SubscriptionNotFoundException`, `SubscriptionInvalidStateException`, `UnsupportedPkceMethodException`, `HashingUnavailableException`, `DuplicateAppRoleException`, `DuplicateMembershipException`, `InvalidCommandFieldException`, `ClientAppInactiveException`, `DuplicateTenantException`, `InvalidPaginationParamException`). Actualizar tests para asertarlas directamente. Para instalar keygo-app con JaCoCo bloqueando: `mvnw install -Djacoco.skip=true`.

### [2026-04-01] Swagger: `Content`/`Schema` faltantes en controllers al agregar `@ApiResponse` con body de error
**Síntoma:** `mvnw compile` falla con `cannot find symbol: class Content / class Schema` en controllers que se actualizaron para incluir `@ApiResponse` con `content = @Content(schema = @Schema(...))`.
**Causa:** Los controllers existentes no tenían `import io.swagger.v3.oas.annotations.media.Content` ni `import io.swagger.v3.oas.annotations.media.Schema` — solo `@ApiResponse` sin body no los requería.
**Solución:** Al agregar cualquier `@ApiResponse` con cuerpo de error, agregar ambos imports al inicio del archivo.

### [2026-04-01] Swagger: convención de descripción con código de respuesta
**Síntoma:** Swagger no indicaba qué `ResponseCode` correspondía a cada HTTP status, dificultando la integración frontend.
**Causa:** Las descripciones de `@ApiResponse` eran texto libre sin referencia al enum `ResponseCode`.
**Solución:** Sufijo `(code: NOMBRE_ENUM)` en cada descripción de `@ApiResponse`. Ejemplo: `"Tenant not found (code: RESOURCE_NOT_FOUND)"`. Para 400 de validación usar: `"... (code: INVALID_INPUT). data.field_errors lists each invalid field."`.

### [2026-04-01] Jerarquía de excepciones por capa — patrón de constructores estructurados
**Síntoma:** Los consumers de la API no podían identificar la capa arquitectónica del error ni el tipo específico de excepción. Los use cases lanzaban `IllegalArgumentException`/`IllegalStateException` genéricas.
**Causa:** Sin jerarquía base, todas las excepciones extendían `RuntimeException` directamente; sin campo `layer` en `ErrorData`.
**Solución:** `KeyGoException(layer, msg)` → `DomainException` / `UseCaseException` / `PortException` / `CONTROLLER` en `UnauthorizedException`. Constructores con valores tipados, nunca strings construidos por el caller. `ErrorData.layer` siempre visible. Ver `docs/design/EXCEPTION_HIERARCHY.md` (T-106). Al instalar nuevas clases de `keygo-domain` en módulos dependientes, usar `mvnw install -DskipTests` antes de `test` en el módulo hijo.

### [2026-04-01] Falta de membership al crear TenantUser durante onboarding de billing
**Síntoma:** Al completar el onboarding de billing (verificación de email del contrato) el sistema notificaba correctamente el username y contraseña temporal, pero al intentar hacer login con esas credenciales el flujo OAuth2 respondía "usuario no relacionado a la app".
**Causa:** `VerifyContractEmailUseCase.execute()` creaba el `TenantUser` en el tenant del proveedor y su `Contractor`, pero **nunca creaba el `Membership`** que vincula a ese usuario con la `clientApp` del proveedor. `IssueAuthorizationCodeUseCase` llama `membershipRepository.findByUserAndClientApp(userId, clientAppId)` y al no encontrar nada lanza `MembershipInactiveException`.
**Solución:** Inyectar `MembershipRepositoryPort` en `VerifyContractEmailUseCase`. Después de resolver el `TenantUser` (nuevo o existente), verificar con `existsByUserAndClientApp` y, si no existe, crear `Membership` con status `ACTIVE`. Actualizar `ApplicationConfig` (bean) y el test (`@Mock MembershipRepositoryPort`, stubs `lenient()` para evitar `UnnecessaryStubbingException` en tests que lanzan excepción antes de llegar al membership).
`VerifyContractEmailUseCase.java`, `ApplicationConfig.java`, `VerifyContractEmailUseCaseTest.java`

---

### [2026-03-31] Email de contrato billing debe incluir el contractId para resumir onboarding
**Síntoma:** El correo de verificación enviado al iniciar un contrato de billing (`POST /billing/contracts`) no incluía el `contractId`, impidiendo que el usuario retomara el proceso con `GET /billing/contracts/{contractId}/resume` si cerraba el navegador.
**Causa:** `EmailNotificationPort.sendVerificationEmail()` solo aceptaba `toEmail`, `username` y `verificationCode` — sin noción de contexto de contrato.
**Solución:** Agregar método específico `sendContractVerificationEmail(toEmail, recipientName, verificationCode, contractId)` al puerto. El adaptador SMTP genera HTML con bloque visual prominente del UUID del contrato. `CreateAppContractUseCase` y `ResendContractVerificationUseCase` usan el nuevo método pasando `contract.getId()` (disponible después de `contractRepo.save()`). No modificar `sendVerificationEmail()` ya existente — mantener separación entre flujo de registro de usuarios y flujo de contratos de billing.
`EmailNotificationPort.java`, `SmtpEmailNotificationAdapter.java`, `CreateAppContractUseCase.java`, `ResendContractVerificationUseCase.java`

---

**Síntoma:** Test no compila: "Duplicate class".
**Causa:** `replace_string_in_file` reemplazó solo el inicio del archivo; el contenido original quedó al final.
**Solución:** El `old_string` debe incluir el bloque completo de apertura de clase + todo el contenido anterior.

---

### [2026-03-31] Endpoints de onboarding billing: resume + resend-verification
**Síntoma:** Nuevas rutas públicas del flujo de contratos debían agregarse sin tocar el filtro.
**Causa:** `BootstrapAdminKeyFilter` usa `hasSegment(path, "/billing/contracts")` — cualquier subruta queda cubierta automáticamente.
**Solución:** Agregar métodos de dominio reutilizables (`isVerificationCodeExpired()`, `renewVerificationCode()`). Campo `nextAction` en DTO encapsula lógica de pantalla para el frontend.
`AppContract.java`, `ResumeContractOnboardingUseCase.java`, `AppContractResumeData.java`

---

### [2026-03-31] Estado RESET_PASSWORD + contraseña temporal vía billing
**Síntoma:** `@InjectMocks` no inyectaba nuevos puertos; test fallaba en construcción del use case.
**Causa:** Al agregar `PasswordHasherPort` y `EmailNotificationPort` al constructor, los mocks no estaban declarados.
**Solución:** Al extender un use case con nuevos puertos, siempre agregar `@Mock` correspondientes. Nuevos valores de enum requieren `ALTER TABLE ... DROP CONSTRAINT ... ADD CONSTRAINT` en PostgreSQL (no permite `ALTER CONSTRAINT`).
`UserStatus.java`, `VerifyContractEmailUseCase.java`, `V19__user_status_reset_password.sql`

---

### [2026-03-31] Email HTML: SimpleMailMessage no soporta HTML + ambigüedad setFrom en Mockito
**Síntoma:** Email enviado como texto plano. En tests, `setFrom(any())` falla en compilación.
**Causa:** `SimpleMailMessage` es solo texto plano. `MimeMessage.setFrom()` tiene dos sobrecargas; `any()` genérico es ambiguo para Mockito.
**Solución:** Usar `MimeMessageHelper(message, true, "UTF-8")` + `helper.setText(html, true)`. Stubear con `doThrow(...).when(mock).setFrom(any(Address.class))`. Agregar `mockito-junit-jupiter` a `keygo-infra/pom.xml`.
`SmtpEmailNotificationAdapter.java`, `keygo-infra/pom.xml`

---

### [2026-03-31] FRONTEND_DEVELOPER_GUIDE: cambios en cascada al renombrar path
**Síntoma:** Un cambio de path propagó inconsistencias a 4+ secciones del documento.
**Causa:** Paths y campos aparecen en múltiples secciones (endpoints, bodies, handlers MSW, diagramas, checklists).
**Solución:** Antes de editar, buscar el path/campo obsoleto con grep en todo el documento y actualizar todas las ocurrencias.

---

### [2026-03-31] Controller de contratos: mover clientAppId del path al body
**Síntoma:** Path requería dos resoluciones innecesarias (`tenantRepo` + `clientAppRepo`).
**Causa:** `clientAppId` estaba en el path cuando el contrato es entidad independiente.
**Solución:** Mover `clientAppId` al body. Eliminar repositorios innecesarios del controller. `BootstrapAdminKeyFilter` con `hasSegment(contains)` no se rompe con cambio de path.
`AppBillingContractController.java`, `CreateAppContractRequest.java`

---

### [2026-03-31] Sincronización Java con modelo v2 Contractor
**Síntoma:** Tests usaban `.companySlug()` (eliminado), `ACTIVATED` (renombrado a `ACTIVE`), `executeForTenant()`/`executeForUser()` (reemplazados).
**Causa:** Cambios de modelo propagados a toda la pila; mocks insuficientes; fields obligatorios no inicializados.
**Solución:** Al cambiar constructor de use case, actualizar TODOS los `@Mock`. Buscar constantes renombradas con grep. Inicializar todos los fields requeridos en builders de test.

---

### [2026-03-30] Migraciones Flyway: dependencia circular entre contractors y tenants
**Síntoma:** `contractors` requería FK a `tenants` y viceversa — circular al crear tablas.
**Causa:** Ambas tablas tienen FKs cruzadas.
**Solución:** Crear columna sin FK, luego agregar FK con `ALTER TABLE ... ADD CONSTRAINT` en migración posterior. Hacer backup antes de reorganizar. Ejecutar `flyway:clean` antes de reescribir historial.
`V3__tenants.sql`, `V11__contractors.sql`

---

### [2026-03-30] Modelo polimórfico billing → Contractor como sujeto central
**Síntoma:** Flujo B2C creaba `TenantUser` sin `tenant_id`, violando constraint NOT NULL. Columnas polimórficas frágiles.
**Causa:** Modelo B2C incompleto; FKs opcionales con CHECK de exclusividad insuficientes.
**Solución:** Introducir `Contractor` como sujeto comercial único. Reemplazar columnas polimórficas por `contractor_id`. Índice único parcial en `app_contracts` por `contractor_id` ACTIVE.

---

### [2026-03-29] Billing: billing_period y base_price movidos a billing_options
**Síntoma:** Tests y mappers usaban `billingPeriod`/`basePrice` en plan — ya no existen ahí.
**Causa:** Refactor parcialmente aplicado; Maven usaba artefactos cacheados.
**Solución:** `AppPlanVersionData` con `billingOptions: List<...>` y `free: boolean`. Plan gratuito = cero filas en billing_options. Compilar desde raíz: `./mvnw compile`.
`V10__billing_catalog.sql`, `AppPlanVersionData.java`

---

### [2026-03-29] Scripts de DB usaban `mvn` sin `-pl keygo-supabase`
**Síntoma:** Scripts fallaban con "command not found" o ejecutaban Flyway en todos los módulos.
**Causa:** Usaban `mvn` (Maven sistema) sin `-pl keygo-supabase`.
**Solución:** Usar `"$PROJECT_ROOT/mvnw" -pl keygo-supabase --no-transfer-progress`. Si se necesita `flyway:clean`, agregar `<cleanDisabled>false</cleanDisabled>` en pom.
`migrate.sh`, `keygo-supabase/pom.xml`

---

### [2026-03-29] UUIDs hardcodeados en FK — usar subqueries semánticas
**Síntoma:** Scripts seed con UUIDs hardcodeados ilegibles; planes asociados a app incorrecta.
**Causa:** FKs escritas literalmente en lugar de derivarse del modelo.
**Solución:** Usar subqueries: `SELECT id FROM <tabla> WHERE <campo_semántico> = '...'`. Preferir `tenants.slug`, `client_apps.client_id`, `app_roles.code`. Para FKs compuestas: CTE o JOIN-based INSERT.

---

### [2026-03-29] Reestructuración Flyway V1–V26 → V1–V17 por dominio
**Síntoma:** Migraciones acumulativas confusas; columna `subscriber_type` en entidad pero ausente en migración.
**Causa:** Migraciones crecieron sin reorganización; comparación incompleta contra JPA entities.
**Solución:** Reiniciar numeración desde V1 (Drop ALL). Una migración por dominio. Validar todas columnas NOT NULL contra JPA entities antes de migrar. Verificar con `ls db/migration/ | sort`.

---

### [2026-03-29] Columna NOT NULL en JPA ausente en migración
**Síntoma:** Hibernate falla en runtime: "missing column [subscriber_type]".
**Causa:** Migración no incluyó la columna; error no detectable en compilación.
**Solución:** Nueva migración correctiva: (1) agregar nullable, (2) back-fill derivando valor de FKs, (3) NOT NULL + CHECK. No modificar migración ya aplicada.
`V23__add_subscriber_type_to_app_subscriptions.sql`, `AppSubscriptionEntity.java`

---

### [2026-03-29] Imports duplicados al añadir anotaciones OpenAPI
**Síntoma:** Nuevos imports insertados antes de los existentes, duplicándolos; warnings de compilación.
**Causa:** No se verificó el bloque de imports antes de agregar.
**Solución:** Revisar bloque completo de imports antes de agregar. Orden: `api.*` → `app.*` → `domain.*` → librerías externas → `java.*`. Compilar con `-pl keygo-api compile` después.

---

### [2026-03-29] Ports inyectados pero nunca invocados en CreateAppContractUseCase
**Síntoma:** Email de verificación nunca se enviaba; código de verificación no se almacenaba en DB.
**Causa:** Puertos inyectados al constructor pero no usados; lógica en repositorio equivocado.
**Solución:** Código de verificación va en `app_contracts` directamente. Use case solo necesita `contractRepo`, `versionRepo`, `emailNotification`. Generar código numérico 6 dígitos con `SecureRandom`.
`CreateAppContractUseCase.java`, `V22__add_contract_verification_code.sql`

---

### [2026-03-29] ActivateAppContractUseCase: rama B2C retornaba datos inválidos
**Síntoma:** Suscripción generada sin `subscriberTenantId` ni `subscriberTenantUserId`.
**Causa:** Rama mockeada; tests pasaban sin detectar datos inválidos de negocio.
**Solución:** Para B2C, obtener `tenantId` del PROVEEDOR desde `clientAppRepo`. Agregar `ClientAppRepositoryPort` al constructor.
`ActivateAppContractUseCase.java`

---

### [2026-03-29] AppBillingSubscriptionController: resolución de appId en tenant incorrecto
**Síntoma:** `getSubscription()` fallaba al buscar app del proveedor bajo el tenant suscriptor.
**Causa:** `resolveClientAppId(tenantSlug, clientId)` filtra por tenant del suscriptor; la app `keygo-platform` pertenece al proveedor.
**Solución:** Agregar `findByClientId(ClientId)` al puerto (búsqueda global). `{tenantSlug}` = suscriptor; `{clientId}` = app del proveedor.
`AppBillingSubscriptionController.java`, `ClientAppRepositoryPort.java`

---

### [2026-03-29] replace_string_in_file elimina import al usarlo como oldString
**Síntoma:** Test deja de compilar: import eliminado.
**Causa:** La herramienta reemplaza el `old_string`; usar un import existente como `old_string` lo elimina.
**Solución:** Incluir TODOS los imports (existentes + nuevos) en `new_string`. Verificar con `./mvnw -pl <módulo> compile`.

---

### [2026-03-28] BaseResponse en sub-paquete response, no en shared directamente
**Síntoma:** Compilación falla: "cannot find symbol BaseResponse".
**Causa:** Import incorrecto: `api.shared.BaseResponse` en lugar de `api.shared.response.BaseResponse`.
**Solución:** Verificar ubicación real con grep antes de escribir imports.

---

### [2026-03-28] `List.of()` sin tipo genérico explícito falla en Java 21
**Síntoma:** Error de compilación en expresión ternaria con `List.of()` en rama vacía.
**Causa:** Java infiere `List<Object>` cuando la rama alternativa tiene tipo diferente.
**Solución:** Usar `List.<TipoEsperado>of()` con tipo explícito.

---

### [2026-03-28] Swagger muestra `data: {}` en lugar del tipo real
**Síntoma:** Swagger UI no infiere el tipo de `data` en `BaseResponse<T>`.
**Causa:** springdoc-openapi no resuelve el genérico `T` desde `@Schema(implementation = BaseResponse.class)`.
**Solución:** Agregar inner class estática `Response extends BaseResponse<PropioDTOType>` en cada DTO. springdoc lee la parametrización de la superclase vía reflexión.

---

### [2026-03-28] Lombok debe declararse explícitamente en cada módulo
**Síntoma:** IntelliJ reporta "symbol not found" para `@Getter`/`@Builder` en `keygo-app`.
**Causa:** Maven compila porque Lombok transita desde `keygo-domain`; IntelliJ no usa transitividad.
**Solución:** Declarar `lombok` scope `provided` en CADA módulo que use anotaciones Lombok. Configurar `annotationProcessorPaths` en `maven-compiler-plugin`.
`keygo-app/pom.xml`

---

### [2026-03-28] N queries de conteo → GROUP BY en puertos de dashboard
**Síntoma:** ~25 queries `COUNT WHERE status = ?` por petición al dashboard.
**Causa:** Puerto con métodos separados por status; caller invocaba N veces.
**Solución:** Cambiar puerto a `Map<K, Long> countXByStatus()`. JPQL: `SELECT s.status, COUNT(s) FROM ... GROUP BY s.status`. Use case usa `getOrDefault(status, 0L)`.
`PlatformDashboardPort.java`, `PlatformDashboardAdapter.java`

---

### [2026-03-28] Swagger muestra camelCase cuando API serializa en snake_case
**Síntoma:** Schema en Swagger UI con `clientId`, `redirectUris` en lugar de `client_id`, `redirect_uris`.
**Causa:** springdoc genera schemas por reflexión, independiente del `JsonMapperBuilderCustomizer` de runtime.
**Solución:** Agregar `spring.jackson.property-naming-strategy: SNAKE_CASE` en `application.yml`. Crear `SnakeCaseModelConverter` implementando `io.swagger.v3.core.converter.ModelConverter`.
`application.yml`, `SnakeCaseModelConverter.java`

---

### [2026-03-28] Agregar método abstracto a interfaz rompe anonymous classes en tests
**Síntoma:** Tests de 3 archivos fallan: "not abstract and does not override abstract method".
**Causa:** Nuevos métodos abstractos requieren implementación en todas las anonymous classes existentes.
**Solución:** Buscar `new NombreInterfaz()` con grep. Actualizar o migrar a Mockito `@Mock`.

---

### [2026-03-27] Use cases sin scope de tenant permiten acceso cross-tenant
**Síntoma:** `listByUserId(userId)` devolvía memberships de cualquier tenant.
**Causa:** Controller pasaba `{tenantSlug}` en path pero use cases filtraban solo por ID.
**Solución:** Agregar métodos tenant-scoped al repositorio: `findByUserIdAndTenantSlug`. JPQL: `m.user.tenant.slug`. `@PreAuthorize` controla QUIÉN; repositorio controla QUÉ datos devuelve.
`MembershipRepositoryPort.java`, `ListMembershipsUseCase.java`, `MembershipJpaRepository.java`

---

### [2026-03-27] Colección Postman corrompida por edición parcial
**Síntoma:** JSON inválido: objetos malformados, cierres faltantes, variables no resueltas.
**Causa:** Ediciones parciales sin validación; variables `{{...}}` no declaradas en entorno.
**Solución:** Validar con `python3 -m json.tool` tras cada edición. Variables deben existir en entorno/colección/scripts. Scripts de token exchange deben hacer `pm.environment.set('accessToken', ...)`.
`KeyGo-Server.postman_collection.json`

---

### [2026-03-27] replace_string_in_file con `...existing code...` eliminó sección entera
**Síntoma:** Bloque §14.2.x completo eliminado del FRONTEND_DEVELOPER_GUIDE.
**Causa:** La herramienta interpretó `...existing code...` como contenido real a reemplazar.
**Solución:** Nunca usar `...existing code...` como marcador. Actualizar secciones por separado o incluir el bloque completo en `old_string`.

---

### [2026-03-25] Mermaid: signos de interrogación invertidos rompen el parser
**Síntoma:** Validador reporta error en bloque Mermaid con `¿Corregida?` en nodo.
**Causa:** El parser Mermaid no soporta caracteres especiales en labels de nodos.
**Solución:** Usar labels ASCII-safe: `Corregida?` sin caracteres invertidos.

---

### [2026-03-25] Tests Maven en monorepo sin `-am` fallan por classpath incompleto
**Síntoma:** `./mvnw -pl keygo-supabase test` falla: `NoClassDefFoundError`.
**Causa:** Sin `-am` no se compilan los módulos de los que depende.
**Solución:** Usar `./mvnw -pl keygo-supabase -am test`.

---

### [2026-03-25] Bearer-only admin auth: autenticación en filtro + autorización en endpoint
**Síntoma:** JWT válido en filtro pero sin control por tenant ni endpoint — acceso cross-tenant posible.
**Causa:** Autenticación centralizada en filtro sin autorización fina por recurso.
**Solución:** Filtro autentica. `@PreAuthorize` por endpoint con evaluador SpEL que compara `tenantSlug` del path con claim `tenant_slug` del JWT.
`BootstrapAdminKeyFilter.java`, `TenantAuthorizationEvaluator.java`

---

### [2026-03-25] Claims map inmutable en tests causa UnsupportedOperationException
**Síntoma:** `claims.put("tenant_slug", ...)` lanza `UnsupportedOperationException`.
**Causa:** `Map.of(...)` retorna mapa inmutable; enriquecimiento de claims modifica en-place.
**Solución:** Copiar a mapa mutable antes de enriquecer: `new LinkedHashMap<>(claims)`.
`IssueTokensUseCase.java`, `RotateRefreshTokenUseCase.java`

---

### [2026-03-24] Controller respondía 201 sin persistencia real
**Síntoma:** `POST /roles` respondía `201 ROLE_CREATED` pero el rol no existía en DB.
**Causa:** Controller construía objeto en memoria sin invocar use case ni repositorio.
**Solución:** Mover lógica a `CreateAppRoleUseCase` en `keygo-app` con validaciones y puerto de salida.
`CreateAppRoleUseCase.java`, `TenantAppRoleController.java`

---

### [2026-03-24] Claim `roles` en JWT: propagación en cascada al cambiar firma
**Síntoma:** Agregar `roles` requirió cambiar 2 use cases, 1 controller, 1 factory y tests.
**Causa:** Cambio de firma de `TokenClaimsFactoryPort` impacta todos los callers.
**Solución:** Agregar `findRoleCodesByUserAndClientApp(UUID, UUID): List<String>` con `@Query nativeQuery`. Para M2M pasar `null`. Actualizar `@Bean` en `ApplicationConfig`.
`TokenClaimsFactoryPort.java`, `StandardTokenClaimsFactory.java`, `ApplicationConfig.java`

---

### [2026-03-24] Filtro solo aceptaba X-KEYGO-ADMIN — círculo vicioso en rutas OAuth2
**Síntoma:** Frontend no podía obtener JWT porque las rutas OAuth2 también requerían `X-KEYGO-ADMIN`.
**Causa:** Filtro incompleto; rutas de autorización protegidas igual que el resto.
**Solución:** Marcar sufijos públicos (`authorizePathSuffix`, `loginPathSuffix`, `tokenPathSuffix`). Agregar `validateBearerAdminToken()` que acepta Bearer JWT con rol admin. Probar primero `X-KEYGO-ADMIN`, luego Bearer.
`BootstrapAdminKeyFilter.java`, `KeyGoBootstrapProperties.java`

---

### [2026-03-24] SigningKeyInitializer: auto-generar clave RSA en startup
**Síntoma:** DB vacía sin clave RSA → arranque fallaba.
**Causa:** Sin mecanismo de inicialización automática.
**Solución:** Crear `SigningKeyInitializer implements ApplicationRunner` con `@Profile("supabase")`. Si no hay ACTIVE, genera RSA-2048, codifica a PEM y persiste. Idempotente.
`SigningKeyInitializer.java`

---

### [2026-03-24] replace_string_in_file con solo imports como oldString duplica la clase
**Síntoma:** Dos declaraciones `public class` en el mismo archivo.
**Causa:** Reemplazo parcial concatenó nuevo contenido al principio dejando el original al final.
**Solución:** Para reescrituras totales usar Write tool. O incluir `package + imports + body` completo en `old_string`.

---

### [2026-03-24] Extender record Java: actualizar todos los sitios de construcción
**Síntoma:** Tests con `new UpdateUserCommand(slug, id, "Jane", "Smith")` fallan compilación tras extender a 10 parámetros.
**Causa:** Cambio de constructor no rastreado en todos los callers.
**Solución:** Buscar con grep todos `new NombreRecord(` antes de extender. Pasar `null` para parámetros opcionales.

---

### [2026-03-24] Perfil de usuario en IAM: dos capas (canónico + metadata por app)
**Síntoma:** Indefinición sobre dónde almacenar campos de perfil.
**Causa:** Decisión de diseño no documentada.
**Solución:** Capa 1 (`tenant_users`): perfil canónico OIDC — "¿quién eres?". Capa 2 (`membership_attributes`): metadata específica por app — "¿qué eres en esta app?".

---

### [2026-03-24] Inconsistencia docs vs DB: criterio de decisión para correcciones
**Síntoma:** Corrección anterior actualizó docs para aceptar DB incorrecta (singular), perpetuando inconsistencia.
**Causa:** Se aceptó implementación incorrecta al resolver inconsistencia.
**Solución:** (1) Docs mandan en convenciones (plural/singular, casing). (2) Implementación manda si hay razón técnica clara. (3) Nunca marcar "corregido" si solo se ajustaron docs para aceptar un error — agregar "pendiente de migración".
`inconsistencias-datos.md`

---

### [2026-03-24] Documentación de datos debe basarse en migraciones SQL reales
**Síntoma:** Múltiples discrepancias entre `DATA_MODEL.md` y migraciones V1–V9.
**Causa:** Documentación escrita de memoria sin leer los `.sql`.
**Solución:** AL generar documentación de datos, SIEMPRE leer las migraciones reales. Regla: nueva migración → actualizar `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` antes de cerrar tarea.

---

### [2026-03-24] Numeración de migraciones desincronizada tras reestructuración Flyway
**Síntoma:** Docs referenciaban V11 para `sessions` (es V8), V12 para `email_verifications` (es V9), etc.
**Causa:** Reestructuración V1–V26 → V1–V17 no actualizó comentarios en documentación.
**Solución:** Tras reestructurar, auditar todos los comentarios `-- Vn` en docs. Validar con `ls db/migration/ | sort`. Incluir tablas de billing en todos los diagramas mermaid.
`DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`

---

### [2026-03-23] Flujo OAuth2: backend retorna code en JSON, no HTTP 302
**Síntoma:** Frontend esperaba redirect 302; backend retorna `data.code` en JSON.
**Causa:** Implementación actual difiere del estándar RFC 6749.
**Solución:** Frontend: lee `code` del JSON, construye URL callback manualmente y navega con `window.location.href`. Diseñar `CallbackPage` preparada para migración futura a 302.
`AUTH_FLOW.md`

---

### [2026-03-23] keygo-ui: una sola app React con roles en JWT
**Síntoma:** Tres apps React separadas — duplicación de flujos OAuth2 y código.
**Causa:** Diseño inicial sin unificación por roles.
**Solución:** Una sola app React. Roles en JWT. Routing con `<RoleGuard>` y `useHasRole()`. Simular roles con `VITE_MOCK_ROLE` + MSW en desarrollo.

---

### [2026-03-23] Variables de entorno en application.yml no se propagan a .env automáticamente
**Síntoma:** `SMTP_HOST`, `SMTP_PORT`, etc. configuradas en `application.yml` pero sin documentar en `.env*`.
**Causa:** Sin mecanismo automático de sincronización.
**Solución:** Regla: si aparece en `application.yml` como `${VAR:default}`, debe estar en `.env.example`, `.env-local`, `.env-desa`, `.env-prod`, `ENVIRONMENT_SETUP.md` y `quick-start.sh`.

---

### [2026-03-23] Builder de ClientApp en tests: incluir todos los campos requeridos
**Síntoma:** `IllegalArgumentException: ClientApp id cannot be null` en tests.
**Causa:** Builder incompleto en setup de test.
**Solución:** Para `ClientApp` en tests incluir siempre: `id(ClientAppId.generate())`, `type(ClientType.PUBLIC)`, `status(ClientAppStatus.ACTIVE)`, `accessPolicy(new AccessPolicy(...))`.
`ClientApp.java`

---

### [2026-03-23] Grant client_credentials: sin id_token ni refresh_token
**Síntoma:** Use case M2M emitía campos propios de authorization_code.
**Causa:** Diferencias entre grants no aplicadas en implementación.
**Solución:** `client_credentials`: `sub` = `clientId` (string, no UUID). Sin `id_token`. Sin `refresh_token`. Solo apps `CONFIDENTIAL`. Campos excluidos con `@JsonInclude(NON_NULL)`.
`IssueClientCredentialsTokenUseCase.java`

---

### [2026-03-22] jacoco.skip=true debe removerse al activar módulo stub
**Síntoma:** Cobertura no medida en `keygo-infra` tras escribir código productivo.
**Causa:** `<jacoco.skip>true</jacoco.skip>` perpetuado desde cuando el módulo era vacío.
**Solución:** Al escribir el primer código productivo en un módulo, remover `<jacoco.skip>true</jacoco.skip>`.
`keygo-infra/pom.xml`

---

### [2026-03-22] Tests de controllers OIDC/JWKS con MockMvc standalone
**Síntoma:** `@WebMvcTest` cargaba contexto innecesario para controllers que retornan `Map<String, Object>`.
**Causa:** Patrón `BaseResponse<T>` no aplica a controllers OIDC (RFC 7517/OIDC Discovery requieren JSON plano).
**Solución:** Usar `MockMvcBuilders.standaloneSetup(controller).build()` + `@ExtendWith(MockitoExtension.class)`. Verificar con `jsonPath("$.keys")`, `jsonPath("$.issuer")`.
`JwksControllerTest.java`, `OidcMetadataControllerTest.java`

---

### [2026-03-22] Reorganización docs AI a docs/ai/
**Síntoma:** Sub-documentos AI mezclados con documentos de producto en raíz.
**Causa:** Raíz congestionada sin separación por audiencia.
**Solución:** Crear `docs/ai/` para documentación de agentes. Documentos raíz actúan como resúmenes con enlaces a `docs/ai/`.

---

### [2026-03-22] Value objects como records: acceso con nombre del parámetro
**Síntoma:** `authCodeId.getValue()` falla en compilación.
**Causa:** Los records exponen el parámetro como método con el mismo nombre, no como `.getValue()`.
**Solución:** `record AuthorizationCodeId(UUID id)` → acceder con `.id()`, no `.getValue()`.

---

### [2026-03-22] Nimbus JOSE+JWT: usar spring-security-oauth2-jose como transitivo
**Síntoma:** Maven falla al declarar `nimbus-jose-jwt` directo sin versión.
**Causa:** Nimbus no está en el BOM de Spring Boot 4.x.
**Solución:** Usar `spring-security-oauth2-jose` sin versión; Nimbus llega transitivamente con versión compatible.
`keygo-infra/pom.xml`

---

### [2026-03-22] Endpoints OIDC/JWKS deben retornar JSON nativo, no BaseResponse
**Síntoma:** Librerías OAuth2 cliente fallaban al parsear `/.well-known/jwks.json`.
**Causa:** RFC 7517 y OIDC Discovery esperan JSON plano; el envelope `BaseResponse` los rompe.
**Solución:** Controllers OIDC/JWKS retornan `ResponseEntity<Map<String, Object>>`. Resto sigue `BaseResponse<T>`. Paths `/.well-known` públicos en el filtro.
`JwksController.java`, `OidcMetadataController.java`

---

### [2026-03-22] JwkSetBuilder (Nimbus) no puede ir en keygo-api — arquitectura hexagonal
**Síntoma:** Controller en `keygo-api` necesitaba Nimbus pero no puede depender de `keygo-infra`.
**Causa:** Implementación concreta en capa API — violación de arquitectura hexagonal.
**Solución:** Definir `JwksBuilderPort` en `keygo-app`. `JwkSetBuilder` en `keygo-infra` implementa el puerto. Wiring en `keygo-run`.
`JwksBuilderPort.java`, `JwkSetBuilder.java`

---

### [2026-03-22] Estado OAuth2 entre GET /authorize y POST /login: usar HttpSession
**Síntoma:** `POST /login` no tenía acceso a los parámetros de `GET /authorize`.
**Causa:** Sin estado compartido entre requests, el contexto de autorización se pierde.
**Solución:** `GET /authorize` guarda `AuthorizationSessionState` en `HttpSession`. `POST /login` recupera el estado. Cliente debe enviar cookies (`credentials: 'include'`).
`AuthorizationController.java`, `AuthorizationSessionState.java`

---

### [2026-03-21] SpringDoc 3.x requerido para Spring Boot 4.x
**Síntoma:** `@SecurityRequirementsOptional` no existe; Swagger UI no levanta.
**Causa:** Se usó SpringDoc 2.x (para Spring Boot 3.x) con Spring Boot 4.x.
**Solución:** Usar `springdoc-openapi-starter-webmvc-ui:3.0.1`. Endpoints públicos sin anotación de seguridad; protegidos con `@SecurityRequirement(name = "AdminKeyAuth")`.
`keygo-api/pom.xml`

---

### [2026-03-21] JaCoCo en monorepo Maven multi-módulo con Spring Boot 4
**Síntoma:** Módulos sin código fallan el check de cobertura; `report-aggregate` falla por dependencias.
**Causa:** JaCoCo aplicado a todos los módulos indiscriminadamente.
**Solución:** `prepare-agent` + `report` + `check` en `pluginManagement`. Módulos stub: `<jacoco.skip>true</jacoco.skip>`. `report-aggregate` solo en `keygo-run`. CI: `./mvnw verify`. Versión 0.8.12.
`pom.xml (raíz)`, `keygo-run/pom.xml`

---

### [2026-03-21] Convenciones de código Java — reglas adoptadas
**Síntoma:** Inconsistencias de estilo en todo el codebase.
**Causa:** Convenciones no documentadas.
**Solución:** JavaDoc: `<p>` para párrafos (no líneas en blanco). JPA: `@Getter @Setter` (nunca `@Data` — rompe `equals`/`hashCode` con lazy collections). Tests: constantes `private static final`. Lombok en domain: scope `provided`.

---

### [2026-03-21] jakarta.validation-api no es transitivo en keygo-api (Spring Boot 4)
**Síntoma:** Build falla: "package jakarta.validation.constraints does not exist".
**Causa:** `spring-boot-starter-web` no expone `jakarta.validation-api` transitivamente en Spring Boot 4.
**Solución:** Agregar `jakarta.validation-api` explícitamente en `keygo-api/pom.xml` y en cualquier módulo con DTOs anotados.

---

### [2026-03-21] Marcar fase completa sin verificar todos los sub-puntos
**Síntoma:** Fase 0 marcada ✅ pero faltaban pipeline CI, lint enforcement y convenciones documentadas.
**Causa:** Solo se verificó el trabajo visible.
**Solución:** Al marcar una fase completa, verificar CADA sub-punto explícitamente.

---

### [2026-03-21] Colecciones Postman: estructura y buenas prácticas
**Síntoma:** Sin colecciones importables; cada prueba requería configuración manual.
**Causa:** Proyecto sin artefactos de prueba funcional.
**Solución:** Crear bajo `postman/` (schema v2.1.0). Auth `apikey` heredada en colección. `{{fullBaseUrl}}` compuesto en pre-request script. Slug único con timestamp. Endpoints públicos override auth con `noauth`.
`KeyGo-Server.postman_collection.json`

---

### [2026-03-21] Generación automática de slug desde nombre del tenant
**Síntoma:** Frontend enviaba `slug` manualmente — inconsistencias y slugs inválidos.
**Causa:** Slug era input del usuario en lugar de derivarse del nombre.
**Solución:** `SlugUtils.toSlug(String)` en `keygo-domain/shared/util/`. `TenantSlug.fromName(String)` genera y valida automáticamente.
`SlugUtils.java`, `TenantSlug.java`

---

### [2026-03-21] Bug T-001: BootstrapAdminKeyFilter nunca bloqueaba rutas
**Síntoma:** Filtro nunca interceptaba peticiones; todas las rutas pasaban sin validación.
**Causa:** `getRequestURI()` incluye el context-path (`/keygo-server/api/...`); prefijos configurados (`/api/`) nunca coincidían.
**Solución:** Usar `request.getServletPath()` (relativa al context-path). En tests: `setServletPath()` en lugar de `setRequestURI()`.
`BootstrapAdminKeyFilter.java`, `BootstrapAdminKeyFilterTest.java`

---

### [2026-04-02] Jackson: databind vs annotations — namespaces distintos
**Síntoma:** `package tools.jackson.annotation does not exist` al compilar.
**Causa:** La regla CLAUDE.md (`tools.jackson.databind.*`) aplica solo a databind. Las anotaciones (`@JsonProperty`, `@JsonIgnoreProperties`, etc.) viven en `com.fasterxml.jackson.annotation.*` porque `tools.jackson.core:jackson-databind:3.1.0` depende transitivamente de `com.fasterxml.jackson.core:jackson-annotations:2.21` — el paquete `tools.jackson.annotation.*` no existe como JAR publicado.
**Solución:** Databind → `tools.jackson.databind.*`. Annotations → `com.fasterxml.jackson.annotation.*`. Son reglas independientes; la segunda no contradice CLAUDE.md.

---

### [2026-04-02] `is_current` en listado de sesiones sin `session_id` en JWT
**Síntoma:** RFC pedía marcar sesión actual comparando `session_id` en claims del JWT; el claim no existe.
**Causa:** `StandardTokenClaimsFactory` no emite `session_id` en el access token.
**Solución:** Determinar sesión actual comparando `userAgent` + `ipAddress` del request HTTP con los campos `user_agent` + `ip_address` almacenados en la sesión.

---

### [2026-04-02] DELETE idempotente para sesiones — 200 siempre
**Síntoma:** Necesidad de decidir qué retornar si la sesión no existe o ya está cerrada al hacer DELETE.
**Causa:** RFC §3.5 establece que DELETE debe ser idempotente.
**Solución:** Si la sesión no existe, ya está TERMINATED o EXPIRED → retornar `alreadyClosed=true` con HTTP 200. Solo lanzar SecurityException si el ownership falla (sesión de otro usuario).

---

### [2026-04-02] `@JsonIgnoreProperties(ignoreUnknown=false)` sobrescribe config global
**Síntoma:** PATCH de preferencias aceptaba campos desconocidos a pesar de querer rechazarlos.
**Causa:** `application.yml` configura `FAIL_ON_UNKNOWN_PROPERTIES=false` globalmente.
**Solución:** Anotar el record de request con `@JsonIgnoreProperties(ignoreUnknown = false)` de `com.fasterxml.jackson.annotation` — sobrescribe la config global para esa clase.

---

### [2026-04-04] Jerarquía de excepciones para email — 3 tipos específicos
**Contexto:** El adaptador de email (`EmailNotificationAdapter`) necesita lanzar excepciones que comuniquen explícitamente qué tipo de fallo ocurrió (template, SMTP, validación).

**Problema:** Usar `EmailNotificationException` genérica para todos los errores dificulta el manejo diferenciado en la UI/use cases. Los 3 tipos de fallo tienen raíces y soluciones completamente diferentes:
- **Template falla** → dev error, configuración, variables faltantes
- **SMTP falla** → infraestructura (servidor down, credenciales), problema transitorio → retry viable
- **Email inválido** → client error, validación necesaria antes de enviar

**Solución / Buena práctica:**
1. **Crear jerarquía de excepciones en `keygo-app`** (capa PORT):
   ```
   EmailNotificationException (base — PortException)
   ├── EmailTemplateException — fallo Thymeleaf
   ├── EmailSmtpException — fallo de envío SMTP
   └── EmailValidationException — email destinatario inválido
   ```

2. **Ubicación:**
   - Base: `keygo-app/src/main/java/.../user/port/notification/EmailNotificationException.java`
   - Sub-excepciones: `keygo-app/src/main/java/.../user/port/notification/exception/Email{Type}Exception.java`

3. **Constructores tipados** (seguir patrón EXCEPTION_HIERARCHY.md):
   ```java
   // EmailTemplateException
   new EmailTemplateException("template-name")
   new EmailTemplateException("template-name", cause)
   
   // EmailSmtpException
   new EmailSmtpException("reason")
   new EmailSmtpException("reason", cause)
   
   // EmailValidationException
   new EmailValidationException("invalid-email@")
   new EmailValidationException("invalid-email@", "reason")
   ```

4. **En el adaptador (`keygo-infra`):**
   - `renderTemplate()` lanza `EmailTemplateException`
   - `sendMimeMessage()` lanza `EmailSmtpException` y `EmailValidationException`
   - `sendEmailInternal()` re-thrown específicas sin envolverlas en genéricas

5. **En use cases / controllers:**
   - Catch base `EmailNotificationException` para genérico
   - Catch específicas si se necesita manejo diferenciado (ej: SMTP → retry, validation → error inmediato)

**Archivos clave:**
- `keygo-app/src/main/java/.../user/port/notification/EmailNotificationException.java`
- `keygo-app/src/main/java/.../user/port/notification/exception/EmailTemplateException.java`
- `keygo-app/src/main/java/.../user/port/notification/exception/EmailSmtpException.java`
- `keygo-app/src/main/java/.../user/port/notification/exception/EmailValidationException.java`
- `keygo-infra/src/main/java/.../adapter/notification/EmailNotificationAdapter.java`

