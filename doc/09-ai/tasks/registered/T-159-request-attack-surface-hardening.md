# T-159 — Hardening de superficie de ataque a nivel de request HTTP

**Estado:** ⬜ Registrada  
**Prioridad:** 🔴 Alta  
**Esfuerzo estimado:** 🕐 M  
**Área:** Seguridad / Infraestructura

---

## Problema / Requisito

Los endpoints públicos (registro, verificación de email, reenvío de código, discovery) y los
endpoints de autenticación (login, token refresh) son susceptibles a ataques a nivel HTTP:
abuso por volumen, fuerza bruta, enumeración de usuarios y agotamiento de recursos.

Actualmente no existe ninguna capa de defensa activa frente a estos vectores.

---

## Alcance de la revisión

### 1. Rate limiting por IP / por endpoint

Limitar la cantidad de requests por unidad de tiempo desde la misma IP.

**Endpoints críticos:**

| Endpoint | Riesgo |
|---|---|
| `POST /register` | Creación masiva de cuentas basura |
| `POST /verify-email` | Enumeración de códigos por fuerza bruta |
| `POST /resend-verification` | Spam de emails |
| `POST /oauth2/token` | Fuerza bruta de credenciales |
| `POST /platform/account/check-email` | Enumeración de emails |
| `GET /tenants/public`, `GET /tenants/{slug}/apps/public` | Scraping / crawling masivo |

**Opciones de implementación:**
- **Opción A:** `bucket4j` (in-process, JVM, sin infra externa) — adecuado para fase inicial
- **Opción B:** Redis + `bucket4j-redis` — distribuido, necesario en múltiples instancias
- **Recomendación:** Opción A para desarrollo, preparar migración a B en T-151 (CI/CD)

### 2. Protección contra fuerza bruta en login

Bloqueo temporal de cuenta / IP después de N intentos fallidos consecutivos.

- Lockout temporal (ej. 15 min) tras 5 intentos fallidos en `POST /oauth2/token`
- Resetear contador en login exitoso
- Retorno de `429 Too Many Requests` con `Retry-After` header

### 3. Protección contra enumeración de usuarios

Respuestas de tiempo uniforme en endpoints que podrían revelar si un email existe:

- `POST /platform/account/check-email` — hoy devuelve `true/false` directamente: evaluar si el contrato debe cambiar
- `POST /register` — el error `DUPLICATE_RESOURCE` revela que el email existe; documentar si esto es aceptable dado el contexto (registro público)
- `POST /verify-email` / `POST /resend-verification` — timing attack por diferencia en tiempo de procesamiento

### 4. Limitación de tamaño de payload

Rechazar requests con body excesivamente grande antes de llegar a los controllers.

- Spring Boot ya configura `max-http-request-header-size` y `max-swallow-size`
- Revisar y documentar límites explícitos en `application.yml`
- Considerar límite en `multipart` si se agregan uploads

### 5. Headers de seguridad HTTP

Verificar que los headers de seguridad estándares estén presentes en todas las respuestas:

| Header | Valor recomendado |
|---|---|
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` |
| `Cache-Control` | `no-store` en endpoints de auth |
| `Referrer-Policy` | `no-referrer` |

Spring Security puede inyectarlos con `http.headers(...)`.

### 6. Protección CSRF

Confirmar que el contexto de uso (API pura, Bearer token, sin session cookies) hace CSRF
irrelevante, y documentar explícitamente en la configuración de seguridad.

---

## Exclusiones (fuera de alcance)

- WAF externo (Cloudflare, AWS WAF) — decisión de infraestructura, pertenece a T-151
- Bot detection avanzado (CAPTCHA, fingerprinting) — registrar como tarea derivada si aplica
- DDoS volumétrico — capa de red, fuera del alcance de la aplicación

---

## Pasos de implementación

| # | Paso | Módulo | Estado |
|---|------|--------|--------|
| 1 | Auditoría de configuración actual: headers, limits, security filter chain | keygo-run | PENDING |
| 2 | Agregar `bucket4j` (in-process) con filtros de rate limit en endpoints críticos | keygo-run / keygo-api | PENDING |
| 3 | Implementar lockout temporal tras N intentos fallidos en login | keygo-run / keygo-app | PENDING |
| 4 | Revisar timing en endpoints de enumeración y documentar decisión de contrato | keygo-api | PENDING |
| 5 | Configurar límites explícitos de payload en `application.yml` | keygo-run | PENDING |
| 6 | Habilitar headers de seguridad HTTP via Spring Security | keygo-run | PENDING |
| 7 | Confirmar y documentar exclusión de CSRF en SecurityConfig | keygo-run | PENDING |
| 8 | Tests de integración: verificar 429 en escenarios de abuso | keygo-api / keygo-run | PENDING |

---

## Relaciones

| Artefacto | Tipo | Descripción |
|-----------|------|-------------|
| T-035 | complementaria | Detección de replay attack en refresh token — mismo dominio de seguridad |
| T-038 | complementaria | JTI blacklist en Redis — comparte infraestructura con rate limit distribuido |
| T-061 | complementaria | CORS origins — parte del mismo endurecimiento de headers HTTP |
| T-151 | habilitadora | CI/CD y decisión cloud definen si rate limit debe ser distribuido (Redis) |
| T-154 / T-158 | derivada de | Endpoints públicos de registro y discovery son el principal vector identificado |

---

## Guía de verificación

```bash
# Verificar que Spring Security aplica headers
curl -I http://localhost:8080/keygo-server/api/v1/tenants/public

# Simular rate limit (requiere bucket4j configurado)
for i in $(seq 1 20); do
  curl -s -o /dev/null -w "%{http_code}\n" \
    -X POST http://localhost:8080/keygo-server/api/v1/tenants/test/apps/test/register \
    -H "Content-Type: application/json" \
    -d '{"username":"x","email":"x@x.com","password":"Test1234!"}'
done
# Debe retornar 429 después del umbral configurado
```

---

## Historial de transiciones

- 2026-04-15 → ⬜ Registrada (identificada al analizar superficie de ataque de endpoints públicos T-154/T-158)
