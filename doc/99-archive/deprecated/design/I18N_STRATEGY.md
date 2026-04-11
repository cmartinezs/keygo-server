# I18N_STRATEGY (Archived)

⚠️ **This document is archived and no longer maintained.**

This was an incomplete design proposal. See:
- [`./RFC_CLOSURE_PROCESS.md`](./RFC_CLOSURE_PROCESS.md) — How design decisions are made
- [`../../rfc/`](../../rfc/) — Active RFCs for future i18n work

Archived in: [`../../archive/deprecated/`](../../archive/deprecated/)
2. **Sin dependencia en tokens:** El locale no se toma del JWT (que es stateless), sino del header `Accept-Language` del request HTTP.
3. **Fallback a inglés (en-US):** Si no hay `Accept-Language` o el idioma no está soportado, mostrar mensaje en inglés por defecto.
4. **Catálogo extensible:** Debe permitir agregar nuevos idiomas sin modificar código, idealmente desde un archivo de propiedades o tabla.

## Opciones consideradas

### Opción A: `messages_XX.properties` (Spring MessageSource)

**Ventajas:**
- Nativo de Spring, bien documentado.
- `ReloadableResourceBundleMessageSource` permite Hot reload en desarrollo.
- Fácil de externalizar a archivos.

**Desventajas:**
- Requiere extraer el locale en cada layer (HTTP → app → error handler).
- `LocaleContextHolder` acoplado a Spring.

**Ejemplo:**
```properties
# i18n/messages_es.properties
AUTHENTICATION_REQUIRED=No pudimos validar tu sesión. Inicia sesión nuevamente.

# i18n/messages_en.properties
AUTHENTICATION_REQUIRED=We couldn't validate your session. Please sign in again.
```

### Opción B: Tabla `localized_messages` (traducción dinámica)

**Ventajas:**
- Permite cambiar mensajes sin redeploy.
- Admin puede gestionar traducciones vía UI.

**Desventajas:**
- Latencia de BD en error path (crítico).
- Complejidad de sincronizar catálogo.

### Opción C: YAML estructurado + caché en memoria

**Ventajas:**
- Flexible, fácil de versionarse.
- Caché elimina latencia.

**Desventajas:**
- Menos estándar que `.properties`.

## Diseño propuesto (Opción A + mejoras)

### 1. Estructura de archivos

```
keygo-api/src/main/resources/i18n/
├── messages.properties          # Fallback en-US (inglés)
├── messages_en_US.properties    # Inglés (Estados Unidos)
├── messages_es.properties       # Español (genérico)
├── messages_es_CL.properties    # Español (Chile)
├── messages_pt_BR.properties    # Portugués (Brasil)
└── messages_fr.properties       # Francés
```

### 2. Resolver locale desde request

```java
@Component
public class LocaleResolver {
  private static final Locale DEFAULT_LOCALE = Locale.US; // en-US por defecto
  
  public Locale resolveLocale(HttpServletRequest request) {
    // 1. Intentar obtener del header Accept-Language
    Locale fromHeader = parseAcceptLanguage(request.getHeader("Accept-Language"));
    if (fromHeader != null && isSupported(fromHeader)) {
      return fromHeader;
    }
    // 2. Fallback a en-US
    return DEFAULT_LOCALE;
  }

  private Locale parseAcceptLanguage(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) return null;
    // Parse "es-CL,es;q=0.9" → [Locale("es", "CL"), Locale("es")]
    String[] parts = headerValue.split(",")[0].split("-");
    return parts.length >= 1 
      ? new Locale(parts[0], parts.length > 1 ? parts[1] : "")
      : null;
  }

  private boolean isSupported(Locale locale) {
    // Soportados: es (es-CL, es-MX, etc.), en (en-US, en-GB, etc.), pt-BR, fr
    return List.of("es", "en", "pt", "fr").contains(locale.getLanguage());
  }
}
```

### 3. Pasar locale a través de request scope

```java
@Component
public class LocaleContextFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, 
      FilterChain chain) {
    Locale locale = localeResolver.resolveLocale(req);
    // Guardar en RequestContextHolder para acceso global sin parámetros
    LocaleContextHolder.setLocale(locale);
    try {
      chain.doFilter(req, res);
    } finally {
      LocaleContextHolder.resetLocaleContext();
    }
  }
}
```

### 4. Refactorizar `ApiErrorDataFactory`

```java
public class ApiErrorDataFactory {
  @Autowired
  private MessageSource messageSource;
  
  private static String clientMessage(ResponseCode responseCode) {
    Locale locale = LocaleContextHolder.getLocale();
    String key = "error." + responseCode.getCode();
    return messageSource.getMessage(key, null, locale);
  }
}
```

### 5. Configuración en `ApplicationConfig`

```java
@Bean
public MessageSource messageSource() {
  ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
  ms.setBasename("classpath:i18n/messages");
  ms.setDefaultEncoding("UTF-8");
  ms.setCacheSeconds(3600); // Cache 1 hora, Hot reload en dev
  return ms;
}

@Bean
public FilterRegistrationBean<LocaleContextFilter> localeContextFilter() {
  FilterRegistrationBean<LocaleContextFilter> bean = new FilterRegistrationBean<>();
  bean.setFilter(new LocaleContextFilter());
  bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // Después de RequestTracingFilter
  return bean;
}
```

## Fases propuestas

| ID | Nombre | Descripción |
|---|---|---|
| T-120 | **Diseño de catálogo i18n** | Crear estructura `i18n/messages_XX.properties` con soporte es, es-CL, en-US, pt_BR, fr; fallback en-US |
| T-121 | **LocaleResolver + LocaleContextFilter** | Implementar resolución de locale desde `Accept-Language` + fallback a en-US |
| T-122 | **Refactorizar `ApiErrorDataFactory`** | Integrar `MessageSource` + `LocaleContextHolder` en generación de `clientMessage` |
| T-123 | **Tests de i18n** | Tests unitarios de `LocaleResolver`, `LocaleContextFilter`, `ApiErrorDataFactory` con múltiples locales |

## Riesgos y consideraciones

### Performance
- **Riesgo:** Caché de MessageSource con TTL puede quedar obsoleto en producción.
- **Mitigación:** `setCacheSeconds(3600)` es un balance; en ambiente con cambios de traducciones muy frecuentes, reducir a 300 s.

### Backwards compatibility
- **Riesgo:** El cambio no rompe contrato de `ErrorData` (solo añade localización).
- **OK:** Cliente existente que no envía `Accept-Language` recibirá respuestas en inglés (en-US) como fallback.

### Exhaustividad del catálogo
- **Riesgo:** Si se agrega un nuevo `ResponseCode` y no se traduce en todos los idiomas, se obtiene "???NOT_FOUND???" de MessageSource.
- **Mitigación:** En dev, usar `throwExceptionOnMissingResource = true`; en prod, loguear warnings con `setFallbackToSystemLocale(true)`.

## Documentación post-implementación

Actualizar:
- `FRONTEND_DEVELOPER_GUIDE.md` §7 — nuevo parámetro `Accept-Language` en requests HTTP
- `AGENTS.md` — patrón de i18n para future tasks
- `docs/ai/lecciones.md` — entrada sobre `LocaleContextHolder` + filter order

---

**Estado:** ✅ APROBADO — Usuario confirmó Opción A (2026-04-03). Comenzar T-120 inmediatamente.
