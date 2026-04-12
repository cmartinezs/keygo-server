# T-130 — Endpoint `POST /api/v1/platform/account/check-email`

**Estado:** 🔲 PENDING  
**Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-run`, docs

---

## Problema / Requisito

La UI necesita saber si un correo electrónico está registrado como `platform_user` **antes**
de avanzar al paso de aceptación de ToS en el flujo de hosted login.
No hay ningún endpoint que cubra esto actualmente.

## Solución Propuesta

Nuevo endpoint **`POST /api/v1/platform/account/check-email`** que:

- Es **público** (no requiere Bearer token).
- Exige que la sesión HTTP tenga el atributo `platformAuthorizationState` creado por
  `GET /platform/oauth2/authorize` (barrera anti-enumeración sin PKCE previo).
- Retorna una respuesta discreta y accionable por la UI.

### Contrato de respuesta

| Condición | HTTP | Campo | Código |
|---|---|---|---|
| Sesión ausente o sin `platformAuthorizationState` | 401 | `failure` | `AUTHENTICATION_REQUIRED` |
| Email registrado | 200 | `success` | `PLATFORM_USER_EMAIL_FOUND` |
| Email no registrado | 404 | `failure` | `PLATFORM_USER_EMAIL_NOT_FOUND` |

---

## Pasos de Implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Crear `CheckPlatformUserEmailCommand` | `keygo-app/…/user/command/CheckPlatformUserEmailCommand.java` | PENDING |
| 2 | Crear `CheckPlatformUserEmailUseCase` (usa `PlatformUserRepositoryPort.existsByEmail`) | `keygo-app/…/user/usecase/CheckPlatformUserEmailUseCase.java` | PENDING |
| 3 | Añadir `PLATFORM_USER_EMAIL_FOUND` y `PLATFORM_USER_EMAIL_NOT_FOUND` a `ResponseCode` | `keygo-api/…/shared/ResponseCode.java` | PENDING |
| 4 | Crear `CheckPlatformUserEmailRequest` (`@NotBlank @Email String email`) | `keygo-api/…/platform/request/CheckPlatformUserEmailRequest.java` | PENDING |
| 5 | Añadir `POST /account/check-email` en `PlatformAuthController` | `keygo-api/…/platform/controller/PlatformAuthController.java` | PENDING |
| 6 | Registrar ruta pública en `application.yml` | `keygo-run/src/main/resources/application.yml` | PENDING |
| 7 | Añadir campo `platformCheckEmailPathPrefix` en `KeyGoBootstrapProperties` | `keygo-run/…/config/properties/KeyGoBootstrapProperties.java` | PENDING |
| 8 | Incluir en `isPublicByPrefix()` en `BootstrapAdminKeyFilter` | `keygo-run/…/filter/BootstrapAdminKeyFilter.java` | PENDING |
| 9 | Registrar bean `CheckPlatformUserEmailUseCase` | `keygo-run/…/config/ApplicationConfig.java` | PENDING |
| 10 | Actualizar `bootstrap-filter.md` (tabla de prefijos públicos) | `doc/03-architecture/security/bootstrap-filter.md` | PENDING |
| 11 | Actualizar `authentication-flow.md` (paso entre authorize y login) | `doc/02-functional/authentication-flow.md` | PENDING |
| 12 | Actualizar `frontend-developer-guide.md` (nuevo endpoint) | `doc/02-functional/frontend/frontend-developer-guide.md` | PENDING |

---

## Notas de Diseño

- **Anti-enumeración:** el requisito de sesión exige que el cliente haya completado
  `GET /oauth2/authorize` (que valida `redirect_uri` contra allowlist), elevando la
  barrera para enumeración masiva. La divulgación del resultado es intencional y
  requerida por producto.
- **Sin payload de respuesta:** el resultado se comunica exclusivamente a través del
  código de respuesta y el HTTP status; el campo `data` es `null`.
- **Reutilización:** `PlatformUserRepositoryPort.existsByEmail(EmailAddress)` ya existe
  en `keygo-app/…/user/port/` — no se requiere nueva lógica de persistencia.
- **Convención de seguridad:** se añade como prefijo exacto (no sufijo) para evitar
  colisión accidental con otras rutas de `account/`.

---

## Verificación

```bash
# 1. Arrancar la app
./mvnw spring-boot:run -pl keygo-run

# 2. Iniciar sesión PKCE (requiere parámetros válidos)
curl -G "http://localhost:8080/keygo-server/api/v1/platform/oauth2/authorize" \
  --data-urlencode "redirect_uri=http://localhost:5173/callback" \
  --data-urlencode "code_challenge=<s256_challenge>" \
  --data-urlencode "code_challenge_method=S256" \
  -c cookies.txt

# 3. Email registrado → 200
curl -X POST "http://localhost:8080/keygo-server/api/v1/platform/account/check-email" \
  -H "Content-Type: application/json" \
  -d '{"email":"existing@example.com"}' \
  -b cookies.txt

# 4. Email no registrado → 404
curl -X POST "http://localhost:8080/keygo-server/api/v1/platform/account/check-email" \
  -H "Content-Type: application/json" \
  -d '{"email":"unknown@example.com"}' \
  -b cookies.txt

# 5. Sin sesión → 401
curl -X POST "http://localhost:8080/keygo-server/api/v1/platform/account/check-email" \
  -H "Content-Type: application/json" \
  -d '{"email":"any@example.com"}'

# 6. Tests de seguridad
./mvnw test -Dtest=BootstrapAdminKeyFilterTest

# 7. Checkstyle
./mvnw checkstyle:check
```
