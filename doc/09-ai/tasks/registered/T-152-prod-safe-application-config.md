# T-152 — Configuración segura de application.yml para ambientes (develop / test / prod)

**Estado:** ⬜ Registrada
**Módulos afectados:** `keygo-run`, `keygo-supabase`, docs

---

## Problema / Requisito

`application.yml` tiene valores hardcodeados que son peligrosos o incorrectos en producción:

| Propiedad | Valor actual | Riesgo en prod |
|---|---|---|
| `keygo.tracing.method-logging-enabled` | `true` | Overhead de reflexión + fuga de datos de entrada/salida en logs |
| `keygo.billing.mock-payment-enabled` | `true` | Acepta pagos falsos en producción |
| `management.endpoints.web.exposure.include` | `"*"` | Expone todos los endpoints actuator (env, beans, heapdump, etc.) sin restricción |

Además no existen perfiles Spring `develop`, `test` ni `prod`. El único perfil para BD real es
`supabase`, que aplica igual a todos los ambientes no-locales. Esto impide tener comportamientos
distintos por ambiente sin cambiar el código.

---

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| T-151 | habilitadora | T-151 (estrategia CI/CD) no puede definir qué config inyectar en cada ambiente si los valores no son externalizables |

---

## Solución propuesta

### 1. Externalizar propiedades inseguras

Convertir las tres propiedades problemáticas en configurables por env var con default seguro:

```yaml
# application.yml — valores por defecto seguros para producción
keygo:
  tracing:
    method-logging-enabled: ${KEYGO_TRACING_ENABLED:false}
  billing:
    mock-payment-enabled: ${KEYGO_MOCK_PAYMENT_ENABLED:false}
management:
  endpoints:
    web:
      exposure:
        include: "${MANAGEMENT_ENDPOINTS_INCLUDE:health,info}"
```

Los perfiles `local` y `h2` sobreescriben a `true` donde corresponde.

### 2. Crear perfiles de ambiente

| Archivo | Perfil Spring | Propósito |
|---|---|---|
| `application-develop.yml` | `develop` | Activa `supabase` + logging DEBUG + tracing habilitado + mock-payment |
| `application-test.yml` (en `keygo-run`) | `test` | Activa `supabase` + logging INFO + tracing deshabilitado + mock-payment off |
| `application-prod.yml` | `prod` | Activa `supabase` + logging WARN + tracing off + mock-payment off + actuator restringido |

> `application-supabase.yml` sigue siendo el perfil de datasource puro; los nuevos perfiles lo incluyen via `spring.profiles.include`.

### 3. Restricción de actuator en prod

En `application-prod.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info"
  endpoint:
    health:
      show-details: never
```

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Reemplazar `method-logging-enabled: true` por `${KEYGO_TRACING_ENABLED:false}` | `keygo-run/src/main/resources/application.yml` | PENDING |
| 2 | Reemplazar `mock-payment-enabled: true` por `${KEYGO_MOCK_PAYMENT_ENABLED:false}` | `keygo-run/src/main/resources/application.yml` | PENDING |
| 3 | Reemplazar `include: "*"` por `${MANAGEMENT_ENDPOINTS_INCLUDE:health,info}` | `keygo-run/src/main/resources/application.yml` | PENDING |
| 4 | Activar los tres overrides a `true`/`"*"` en `application-local.yml` | `keygo-run/src/main/resources/application-local.yml` | PENDING |
| 5 | Crear `application-develop.yml` con logging DEBUG + tracing + mock-payment | `keygo-run/src/main/resources/application-develop.yml` | PENDING |
| 6 | Crear `application-test.yml` en `keygo-run` con logging INFO, tracing off | `keygo-run/src/main/resources/application-test.yml` | PENDING |
| 7 | Crear `application-prod.yml` con logging WARN, actuator restringido | `keygo-run/src/main/resources/application-prod.yml` | PENDING |
| 8 | Documentar todas las env vars requeridas por ambiente | `doc/07-operations/environment-variables.md` | PENDING |

---

## Guía de verificación

```bash
# Compilación limpia
./mvnw clean package -DskipTests

# Verificar que mock-payment está deshabilitado por defecto
grep -A1 "mock-payment-enabled" keygo-run/src/main/resources/application.yml
# Esperado: mock-payment-enabled: ${KEYGO_MOCK_PAYMENT_ENABLED:false}

# Arrancar con perfil prod y verificar actuator restringido
SPRING_PROFILES_ACTIVE=prod,supabase java -jar keygo-run/target/keygo-run-*.jar &
curl http://localhost:8080/keygo-server/actuator
# Esperado: solo "health" e "info" visibles, sin "env", "beans", "heapdump"
```

---

## Historial de transiciones

- 2026-04-14 — Creada en estado ⬜ Registrada como prerequisito de T-151.
