# T-130 — Endpoint `POST /api/v1/platform/account/check-email`

**Estado:** 🧩 Pendiente integración UI  
**Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-run`, `postman`, docs

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

## Pasos de Implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Crear `CheckPlatformUserEmailCommand` | `keygo-app/…/user/command/CheckPlatformUserEmailCommand.java` | APPLIED |
| 2 | Crear `CheckPlatformUserEmailUseCase` (usa `PlatformUserRepositoryPort.existsByEmail`) | `keygo-app/…/user/usecase/CheckPlatformUserEmailUseCase.java` | APPLIED |
| 3 | Añadir `PLATFORM_USER_EMAIL_FOUND` y `PLATFORM_USER_EMAIL_NOT_FOUND` a `ResponseCode` | `keygo-api/…/shared/ResponseCode.java` | APPLIED |
| 4 | Crear `CheckPlatformUserEmailRequest` (`@NotBlank @Email String email`) | `keygo-api/…/platform/request/CheckPlatformUserEmailRequest.java` | APPLIED |
| 5 | Añadir `POST /account/check-email` en `PlatformAuthController` | `keygo-api/…/platform/controller/PlatformAuthController.java` | APPLIED |
| 6 | Registrar ruta pública en `application.yml` | `keygo-run/src/main/resources/application.yml` | APPLIED |
| 7 | Añadir campo `platformCheckEmailPathPrefix` en `KeyGoBootstrapProperties` | `keygo-run/…/config/properties/KeyGoBootstrapProperties.java` | APPLIED |
| 8 | Incluir en `isPublicByPrefix()` en `BootstrapAdminKeyFilter` | `keygo-run/…/filter/BootstrapAdminKeyFilter.java` | APPLIED |
| 9 | Registrar bean `CheckPlatformUserEmailUseCase` | `keygo-run/…/config/ApplicationConfig.java` | APPLIED |
| 10 | Actualizar `bootstrap-filter.md` (tabla de prefijos públicos) | `doc/03-architecture/security/bootstrap-filter.md` | APPLIED |
| 11 | Actualizar `authentication-flow.md` (paso entre authorize y login) | `doc/02-functional/authentication-flow.md` | APPLIED |
| 12 | Actualizar docs frontend vivas del flujo/endpoint (`02-authentication.md`, `08-endpoints-admin.md` y `04-error-handling.md` si aplica) | `doc/02-functional/frontend/…` | APPLIED |
| 13 | Actualizar catálogo de endpoints | `doc/08-reference/api/endpoint-catalog.md` | APPLIED |
| 14 | Actualizar colección Postman | `postman/KeyGo-Server.postman_collection.json` | APPLIED |

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

---

## Historial de transiciones

### 2026-04-13 — 🔍 En análisis

- **Reutilización disponible:** `PlatformUserRepositoryPort.existsByEmail(EmailAddress)` ya existe,
  por lo que T-130 no requiere nueva lógica de persistencia ni cambios en `keygo-supabase`.
- **Encaje funcional correcto:** el endpoint calza en `PlatformAuthController`, porque reutiliza
  la sesión HTTP del flujo PKCE y el atributo `platformAuthorizationState` ya definido como
  `SESSION_ATTR_AUTH_STATE`.
- **Impacto real en seguridad de borde:** para que sea público sin Bearer token hay que agregar
  el prefijo en `application.yml`, `KeyGoBootstrapProperties` y `BootstrapAdminKeyFilter`.
- **Contrato consistente con el proyecto:** `AUTHENTICATION_REQUIRED` ya existe y es coherente
  para el caso sin sesión; los códigos nuevos necesarios son solo
  `PLATFORM_USER_EMAIL_FOUND` y `PLATFORM_USER_EMAIL_NOT_FOUND`.
- **Cobertura de pruebas esperable:** el cambio debe extender al menos
  `PlatformAuthControllerTest` y `BootstrapAdminKeyFilterTest`, además de agregar test unitario
  para el nuevo use case.
- **Drift documental detectado:** `doc/06-quality/security-guidelines.md` y
  `doc/02-functional/frontend/feedback/BE-001-check-email-endpoint.md` ya describen este
  endpoint como esperado, aunque todavía no existe en código.
- **Ajuste al plan documental:** la referencia previa a
  `doc/02-functional/frontend/frontend-developer-guide.md` está obsoleta; la documentación viva
  a actualizar está hoy bajo `doc/02-functional/frontend/`.

### 2026-04-13 — 📋 Planificada

- Se consolidó la implementación como cambio **acotado**, sin necesidad de RFC.
- Se amplió el alcance documental para incluir no solo `authentication-flow.md` y
  `bootstrap-filter.md`, sino también la documentación frontend viva en
  `doc/02-functional/frontend/`, el catálogo de endpoints y la colección Postman.
- Se ajustaron los módulos afectados a `keygo-app`, `keygo-api`, `keygo-run`, `postman` y docs.
- El plan quedó estructurado en estos frentes:
  1. **Caso de uso app:** crear `CheckPlatformUserEmailCommand` y
     `CheckPlatformUserEmailUseCase` reutilizando `PlatformUserRepositoryPort.existsByEmail`.
  2. **Contrato y controlador API:** crear `CheckPlatformUserEmailRequest`, agregar
     `PLATFORM_USER_EMAIL_FOUND` / `PLATFORM_USER_EMAIL_NOT_FOUND` e implementar
     `POST /api/v1/platform/account/check-email` en `PlatformAuthController`.
  3. **Wiring de seguridad y beans:** registrar el path público en `application.yml`,
     exponer `platformCheckEmailPathPrefix`, incorporarlo en `BootstrapAdminKeyFilter`
     y registrar el bean en `ApplicationConfig`.
  4. **Pruebas y artefactos:** cubrir el use case, `PlatformAuthControllerTest` y
     `BootstrapAdminKeyFilterTest`, además de actualizar docs, catálogo de endpoints y Postman.

### 2026-04-13 — 🟢 Aprobada

- El usuario aprobó explícitamente la aplicación del plan de T-130.
- El alcance aprobado mantuvo el contrato propuesto: `401 AUTHENTICATION_REQUIRED`,
  `200 PLATFORM_USER_EMAIL_FOUND` y `404 PLATFORM_USER_EMAIL_NOT_FOUND`.

### 2026-04-13 — 🔵 En desarrollo

- Se implementó `CheckPlatformUserEmailCommand` y `CheckPlatformUserEmailUseCase` en `keygo-app`.
- Se agregó `CheckPlatformUserEmailRequest` y los nuevos `ResponseCode` en `keygo-api`.
- Se expuso `POST /api/v1/platform/account/check-email` en `PlatformAuthController`,
  reutilizando `SESSION_ATTR_AUTH_STATE` como prerequisito de sesión.
- Se registró el nuevo path público en `application.yml`, `KeyGoBootstrapProperties` y
  `BootstrapAdminKeyFilter`, además del bean correspondiente en `ApplicationConfig`.
- Se extendieron `CheckPlatformUserEmailUseCaseTest`, `PlatformAuthControllerTest` y
  `BootstrapAdminKeyFilterTest` para cubrir el comportamiento nuevo.
- Se actualizaron `bootstrap-filter.md`, `authentication-flow.md`,
  `doc/02-functional/frontend/02-authentication.md`,
  `doc/02-functional/frontend/08-endpoints-admin.md`,
  `doc/02-functional/frontend/04-error-handling.md`,
  `doc/08-reference/api/endpoint-catalog.md` y la colección Postman.

### 2026-04-13 — 🔄 En revisión

- La implementación quedó completa con los pasos del plan marcados como `APPLIED`.
- Las pruebas focalizadas del cambio pasaron en `keygo-app`, `keygo-api` y `keygo-run`.
- `checkstyle:check` pasó para los módulos tocados usando el runtime Java de IntelliJ (`jbr`).

### 2026-04-13 — 🧩 Pendiente integración UI

- El backend quedó implementado y verificado desde su lado.
- La tarea queda abierta hasta que la UI consuma y confirme la integración del endpoint
  `POST /api/v1/platform/account/check-email` en el flujo hosted login.
- La condición de cierre es la confirmación de integración UI o una verificación explícita
  de que el contrato ya fue adoptado sin requerir ajustes adicionales en backend.
