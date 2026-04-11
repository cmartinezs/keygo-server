# Plan de Implementación — RFC Account & Settings API

- Estado: En progreso
- Fecha inicio: 2026-04-02
- RFC base: `04-rfc-account-backend-contract.md`
- Implementador: AI Agent (Claude Code)
- Revisión: Carlos F. Martínez Sánchez

---

## Resumen de alcance

Implementar los 6 endpoints propuestos en §5 del RFC. Los endpoints §4 (profile, userinfo,
billing) ya existen y no se modifican. El módulo §5.5 (connections) queda fuera del alcance
de este plan — es roadmap.

| Endpoint                                  | RFC §  | Fase |
|-------------------------------------------|--------|------|
| `POST /account/change-password`           | §5.1   | 2    |
| `GET /account/sessions`                   | §5.2   | 3    |
| `DELETE /account/sessions/{sessionId}`    | §5.3   | 3    |
| `GET /account/notification-preferences`   | §5.4.1 | 4    |
| `PATCH /account/notification-preferences` | §5.4.2 | 4    |
| `GET /account/access`                     | §5.6   | 5    |

---

## Convención transversal — Swagger / OpenAPI

Aplica a **toda fase que cree o modifique un endpoint** en `AccountSettingsController`.

| Elemento | Obligatorio | Detalle |
|---|---|---|
| `@Tag` en el controller | Sí | Usar `name = "Account Settings"`, igual que indica el RFC §8 |
| `@Operation(summary, description)` en cada método | Sí | Descripción breve + mención del Bearer token requerido |
| `@ApiResponse` por cada código HTTP posible | Sí | Incluir éxito y todos los errores documentados en el RFC §6 |
| `@Parameter` en path params y headers | Sí | `tenantSlug`, `sessionId`, header `Authorization` |
| `docs/keygo-ui/api-docs.json` | Fase 6 | Regenerar la spec completa una vez finalizada la Fase 5 ejecutando `GET /v3/api-docs` con la app corriendo y reemplazando el archivo |

---

## Fases

### Fase 1 — Infraestructura base (sin nuevos endpoints)

**Objetivo:** Dejar el proyecto compilando con todos los codes de negocio y la configuración
de seguridad necesarios para las fases siguientes. Al finalizar esta fase no hay endpoints
nuevos expuestos, pero la base está lista.

**Módulos afectados:** `keygo-api`, `keygo-run`

**Artefactos a crear/modificar:**

| Artefacto                       | Módulo      | Acción    | Detalle                                                                                                                                                                                                                                                         |
|---------------------------------|-------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ResponseCode.java`             | `keygo-api` | Modificar | Agregar 7 entradas: `ACCOUNT_PASSWORD_CHANGED`, `ACCOUNT_SESSIONS_RETRIEVED`, `ACCOUNT_SESSION_REVOKED`, `ACCOUNT_SESSION_ALREADY_CLOSED`, `ACCOUNT_NOTIFICATION_PREFERENCES_RETRIEVED`, `ACCOUNT_NOTIFICATION_PREFERENCES_UPDATED`, `ACCOUNT_ACCESS_RETRIEVED` |
| `GlobalExceptionHandler.java`   | `keygo-api` | Modificar | Agregar handler para `IncorrectCurrentPasswordException` → HTTP 403, code `BUSINESS_RULE_VIOLATION`, `origin=BUSINESS_RULE`                                                                                                                                     |
| `KeyGoBootstrapProperties.java` | `keygo-run` | Modificar | Agregar 4 propiedades nuevas de sufijos públicos                                                                                                                                                                                                                |
| `application.yml`               | `keygo-run` | Modificar | Registrar los 4 sufijos: `/account/change-password`, `/account/sessions`, `/account/notification-preferences`, `/account/access`                                                                                                                                |
| `BootstrapAdminKeyFilter.java`  | `keygo-run` | Modificar | Agregar condición de paso público para los 4 sufijos nuevos                                                                                                                                                                                                     |

**Criterio de aceptación:**
- `./mvnw test` pasa sin errores
- Los nuevos `ResponseCode` son visibles en `GET /api/v1/response-codes`

---

### Fase 2 — Cambio de contraseña (§5.1)

**Objetivo:** Implementar `POST /api/v1/tenants/{tenantSlug}/account/change-password` completo,
incluyendo política de contraseña, validación del password actual y error 403 tipado.

**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-run`

**Artefactos a crear/modificar:**

| Artefacto                                | Módulo         | Acción    | Detalle                                                                                                                                                                                                                                                                                  |
|------------------------------------------|----------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `PasswordPolicy.java`                    | `keygo-domain` | Crear     | Clase utilitaria con método estático `validate(password)`. Reglas: longitud ≥12, al menos 1 mayúscula, 1 minúscula, 1 dígito, 1 carácter especial. Lanza `DomainException` si falla. Sin dependencias Spring.                                                                            |
| `IncorrectCurrentPasswordException.java` | `keygo-app`    | Crear     | En `app/user/exception/`. Extiende `RuntimeException`. Contiene mensaje con causa.                                                                                                                                                                                                       |
| `ChangePasswordCommand.java`             | `keygo-app`    | Crear     | En `app/user/command/`. Record con: `tenantSlug`, `bearerToken`, `currentPassword`, `newPassword`.                                                                                                                                                                                       |
| `ChangePasswordResult.java`              | `keygo-app`    | Crear     | En `app/user/result/`. Record con: `changed` (boolean).                                                                                                                                                                                                                                  |
| `ChangePasswordUseCase.java`             | `keygo-app`    | Crear     | En `app/user/usecase/`. Lógica: (1) verificar token → extraer userId, (2) cargar usuario, (3) `PasswordHasherPort.matches()` → si falla lanzar `IncorrectCurrentPasswordException`, (4) `PasswordPolicy.validate(newPassword)`, (5) validar que nueva ≠ actual, (6) hashear y persistir. |
| `ChangePasswordUseCaseTest.java`         | `keygo-app`    | Crear     | Test unitario con 4 escenarios: éxito, current_password incorrecto, nueva contraseña viola política, nueva igual a la actual.                                                                                                                                                            |
| `ChangePasswordRequest.java`             | `keygo-api`    | Crear     | En `api/user/request/`. Record Jackson con: `current_password`, `new_password`.                                                                                                                                                                                                          |
| `GlobalExceptionHandler.java`            | `keygo-api`    | Modificar | Agregar `@ExceptionHandler(IncorrectCurrentPasswordException.class)` → HTTP 403, code `BUSINESS_RULE_VIOLATION`, `origin=BUSINESS_RULE`. Import de `keygo-app`.                                                                                                                          |
| `AccountSettingsController.java`         | `keygo-api`    | Crear     | En `api/user/controller/`. `@RequestMapping("/api/v1/tenants/{tenantSlug}/account")`. `@Tag(name="Account Settings")`. Endpoint POST `/change-password` con `@Operation` + `@ApiResponse` para 200, 400, 401, 403, 500, 503.                                                             |
| `ApplicationConfig.java`                 | `keygo-run`    | Modificar | Bean para `ChangePasswordUseCase`.                                                                                                                                                                                                                                                       |

**Flujo completo:**

```
POST /account/change-password
  → AccountSettingsController.changePassword()
  → ChangePasswordUseCase.execute(ChangePasswordCommand)
      → AccessTokenVerifierPort.verify(bearerToken) → userId
      → UserRepositoryPort.findByIdAndTenantId()
      → PasswordHasherPort.matches(currentPassword, hash)  [403 si falla]
      → PasswordPolicy.validate(newPassword)               [400 si falla]
      → validar nueva != actual                            [400 si igual]
      → PasswordHasherPort.hash(newPassword)
      → UserRepositoryPort.save(user)
  ← 200 ACCOUNT_PASSWORD_CHANGED { "changed": true }
```

**Criterio de aceptación:**
- Cambio exitoso devuelve 200 `ACCOUNT_PASSWORD_CHANGED`
- Password actual incorrecto devuelve 403 `BUSINESS_RULE_VIOLATION`
- Contraseña débil devuelve 400 `INVALID_INPUT` con `fieldErrors`
- Bearer ausente devuelve 401 `AUTHENTICATION_REQUIRED`

---

### Fase 3 — Gestión de sesiones (§5.2 + §5.3)

**Objetivo:** Implementar listado de sesiones activas y revocación self-service de sesión
individual, incluyendo idempotencia en el DELETE y parsing básico de User-Agent.

**Módulos afectados:** `keygo-app`, `keygo-supabase`, `keygo-api`, `keygo-run`

**Artefactos a crear/modificar:**

| Artefacto                           | Módulo           | Acción    | Detalle                                                                                                                                                                                                                                       |
|-------------------------------------|------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `SessionRepositoryPort.java`        | `keygo-app`      | Modificar | Agregar método: `List<Session> findAllByUserIdAndTenantId(UserId userId, TenantId tenantId)`                                                                                                                                                  |
| `ListUserSessionsCommand.java`      | `keygo-app`      | Crear     | Record con: `tenantSlug`, `bearerToken`.                                                                                                                                                                                                      |
| `SessionInfoResult.java`            | `keygo-app`      | Crear     | Record con todos los campos que expone el RFC §5.2.                                                                                                                                                                                           |
| `ListUserSessionsUseCase.java`      | `keygo-app`      | Crear     | Extrae userId del token, consulta sesiones, mapea a result. Marca `is_current` comparando el sessionId embebido en el access token.                                                                                                           |
| `RevokeUserSessionCommand.java`     | `keygo-app`      | Crear     | Record con: `tenantSlug`, `bearerToken`, `sessionId` (UUID).                                                                                                                                                                                  |
| `RevokeUserSessionResult.java`      | `keygo-app`      | Crear     | Record con: `revoked` (boolean), `sessionId` (UUID), `alreadyClosed` (boolean).                                                                                                                                                               |
| `RevokeUserSessionUseCase.java`     | `keygo-app`      | Crear     | Verifica ownership del token → busca sesión por ID → si no existe o ya terminada: retorna `alreadyClosed=true` sin error → si activa: actualiza status a TERMINATED.                                                                          |
| `ListUserSessionsUseCaseTest.java`  | `keygo-app`      | Crear     | Escenarios: lista con sesión actual + otras, lista vacía.                                                                                                                                                                                     |
| `RevokeUserSessionUseCaseTest.java` | `keygo-app`      | Crear     | Escenarios: revocación exitosa, idempotente (ya revocada), sesión no encontrada.                                                                                                                                                              |
| `SessionJpaRepository.java`         | `keygo-supabase` | Modificar | Agregar query: `findAllByUserIdAndStatusOrderByLastAccessedAtDesc(UUID userId, String status)`                                                                                                                                                |
| `SessionRepositoryAdapter.java`     | `keygo-supabase` | Modificar | Implementar nuevo método del puerto.                                                                                                                                                                                                          |
| `UserAgentParser.java`              | `keygo-supabase` | Crear     | Clase utilitaria en `keygo-supabase/util/`. Parseo básico con regex (sin dependencias externas) para extraer: `browser` (Chrome/Firefox/Safari/Edge/…), `os` (Windows/macOS/Linux/Android/iOS), `deviceType` (desktop/mobile/tablet/unknown). |
| `AccountSessionData.java`           | `keygo-api`      | Crear     | Record con: id, device_label, device_type, browser, os, ip_address, location, created_at, last_seen_at, expires_at, is_current.                                                                                                               |
| `AccountSettingsController.java`    | `keygo-api`      | Modificar | Agregar GET `/sessions` y DELETE `/sessions/{sessionId}` con `@Operation` + `@ApiResponse` para 200, 401, 500, 503 en ambos. DELETE incluye respuesta idempotente (200 con `ACCOUNT_SESSION_ALREADY_CLOSED`).                                 |
| `ApplicationConfig.java`            | `keygo-run`      | Modificar | Beans para `ListUserSessionsUseCase` y `RevokeUserSessionUseCase`.                                                                                                                                                                            |

**Notas de diseño:**
- `is_current`: el access token JWT contiene el `session_id` en claims. Se compara con cada sesión de la lista.
- `location`: campo calculado opcionalmente a partir de IP (puede retornar `null` si no hay servicio de geolocalización configurado).
- `device_label`: se construye como `"${browser} on ${os}"` si no se almacena explícitamente.
- Sin nueva dependencia externa para parsing de UA — se usa regex básico.

**Criterio de aceptación:**
- GET `/sessions` retorna lista ordenada por `last_seen_at` desc, `is_current=true` en exactamente una sesión
- DELETE `/sessions/{id}` retorna 200 en todos los casos (sesión activa, ya revocada o inexistente)
- DELETE de sesión de otro usuario no revoca (ownership check)

---

### Fase 4 — Preferencias de notificación (§5.4)

**Objetivo:** Implementar GET y PATCH de preferencias de notificación con persistencia propia
en nueva tabla. Incluye la migración Flyway V21.

**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-supabase`, `keygo-api`, `keygo-run`

**Artefactos a crear/modificar:**

| Artefacto | Módulo | Acción | Detalle |
|---|---|---|---|
| `NotificationPreferences.java` | `keygo-domain` | Crear | En `domain/user/model/`. Clase con 5 campos boolean + userId + tenantId. Sin Spring. |
| `NotificationPreferencesRepositoryPort.java` | `keygo-app` | Crear | En `app/user/port/`. Métodos: `findByUserIdAndTenantId(UserId, TenantId): Optional<NotificationPreferences>`, `saveOrUpdate(NotificationPreferences): NotificationPreferences` |
| `GetNotificationPreferencesCommand.java` | `keygo-app` | Crear | Record con: `tenantSlug`, `bearerToken`. |
| `GetNotificationPreferencesUseCase.java` | `keygo-app` | Crear | Si no existe registro para el usuario → retorna defaults (security_alerts_email=true, security_alerts_in_app=true, billing_alerts_email=true, product_updates_email=false, weekly_digest=false). |
| `UpdateNotificationPreferencesCommand.java` | `keygo-app` | Crear | Record con `tenantSlug`, `bearerToken`, y los 5 campos boolean. |
| `UpdateNotificationPreferencesUseCase.java` | `keygo-app` | Crear | Upsert: si no existe el registro lo crea; si existe lo actualiza con los 5 valores recibidos. |
| `GetNotificationPreferencesUseCaseTest.java` | `keygo-app` | Crear | Escenarios: con registro, sin registro (defaults). |
| `UpdateNotificationPreferencesUseCaseTest.java` | `keygo-app` | Crear | Escenarios: upsert primera vez, actualizar existente. |
| `V21__user_notification_preferences.sql` | `keygo-supabase` | Crear | Ver DDL abajo. |
| `UserNotificationPreferencesEntity.java` | `keygo-supabase` | Crear | En `supabase/user/entity/`. Mapea tabla `user_notification_preferences`. |
| `NotificationPreferencesJpaRepository.java` | `keygo-supabase` | Crear | Spring Data JPA. Método `findByUserIdAndTenantId(UUID, UUID)`. |
| `NotificationPreferencesRepositoryAdapter.java` | `keygo-supabase` | Crear | Implementa `NotificationPreferencesRepositoryPort`. |
| `NotificationPreferencesData.java` | `keygo-api` | Crear | Record con los 5 campos boolean. |
| `UpdateNotificationPreferencesRequest.java` | `keygo-api` | Crear | Record con los 5 campos boolean (todos requeridos). Con `@JsonIgnoreProperties(ignoreUnknown=false)` para rechazar campos desconocidos. |
| `AccountSettingsController.java` | `keygo-api` | Modificar | Agregar GET y PATCH `/notification-preferences` con `@Operation` + `@ApiResponse` para 200, 400, 401, 500, 503 en ambos. |
| `ApplicationConfig.java` | `keygo-run` | Modificar | Beans para `GetNotificationPreferencesUseCase` y `UpdateNotificationPreferencesUseCase`. |

**DDL migración V21:**

```sql
-- V21__user_notification_preferences.sql
CREATE TABLE user_notification_preferences (
  id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id                 UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
  tenant_id               UUID        NOT NULL REFERENCES tenants(id)      ON DELETE CASCADE,
  security_alerts_email   BOOLEAN     NOT NULL DEFAULT TRUE,
  security_alerts_in_app  BOOLEAN     NOT NULL DEFAULT TRUE,
  billing_alerts_email    BOOLEAN     NOT NULL DEFAULT TRUE,
  product_updates_email   BOOLEAN     NOT NULL DEFAULT FALSE,
  weekly_digest           BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, tenant_id)
);
CREATE INDEX idx_notif_prefs_user_tenant ON user_notification_preferences(user_id, tenant_id);
```

**Criterio de aceptación:**
- GET retorna defaults si el usuario nunca configuró preferencias
- PATCH actualiza todos los campos y retorna el estado actualizado
- PATCH con campo desconocido retorna 400 `INVALID_INPUT`

---

### Fase 5 — Vista de acceso self-service (§5.6)

**Objetivo:** Implementar `GET /account/access` que retorna las membresías del usuario
autenticado con sus apps y roles, sin requerir parámetros admin.

**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-run`

**Artefactos a crear/modificar:**

| Artefacto | Módulo | Acción | Detalle |
|---|---|---|---|
| `UserAccessEntry.java` | `keygo-domain` | Crear | En `domain/membership/model/`. Record con: clientAppId, clientAppName, membershipId, status, roles (List<String>). |
| `GetUserAccessCommand.java` | `keygo-app` | Crear | En `app/user/command/`. Record con: `tenantSlug`, `bearerToken`. |
| `GetUserAccessUseCase.java` | `keygo-app` | Crear | Extrae userId del token → `MembershipRepositoryPort.findByUserIdAndTenantSlug()` → mapea a lista de `UserAccessEntry`. Reutiliza el puerto existente, sin cambios en persistence. |
| `GetUserAccessUseCaseTest.java` | `keygo-app` | Crear | Escenarios: con membresías, sin membresías (lista vacía). |
| `UserAccessData.java` | `keygo-api` | Crear | Record con: client_app_id, client_app_name, membership_id, status, roles. |
| `AccountSettingsController.java` | `keygo-api` | Modificar | Agregar GET `/access` con `@Operation` + `@ApiResponse` para 200, 401, 500, 503. |
| `ApplicationConfig.java` | `keygo-run` | Modificar | Bean para `GetUserAccessUseCase`. |

**Nota:** `MembershipRepositoryPort.findByUserIdAndTenantSlug(UUID userId, String tenantSlug)`
ya existe en `keygo-app` — no requiere cambios en persistencia.

**Criterio de aceptación:**
- Retorna lista con las apps donde el usuario tiene membresía activa en el tenant
- Sin membresías retorna lista vacía (no 404)
- Cada entrada incluye roles directos asignados (no jerarquía expandida)

---

### Fase 6 — Documentación y cierre

**Objetivo:** Actualizar todos los documentos obligatorios al concluir la implementación.
No implica código nuevo.

**Artefactos a actualizar:**

| Artefacto                                           | Tipo        | Detalle                                                                       |
|-----------------------------------------------------|-------------|-------------------------------------------------------------------------------|
| `docs/keygo-ui/api-docs.json`                       | Obligatorio | Regenerar spec OpenAPI completa: con la app corriendo en local, ejecutar `GET /keygo-server/v3/api-docs` y reemplazar el contenido del archivo. Confirma §10 del RFC. |
| `docs/postman/KeyGo-Server.postman_collection.json` | Obligatorio | Agregar los 6 nuevos requests con `pm.test()` scripts                         |
| `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`         | Obligatorio | Agregar los 6 endpoints en §14                                                |
| `AGENTS.md`                                         | Obligatorio | Actualizar próxima migración (V21 → V22), agregar nuevos endpoints al listado |
| `docs/ai/agents-registro.md`                        | Obligatorio | Entrada por cambios en AGENTS.md                                              |
| `docs/data/DATA_MODEL.md`                           | Obligatorio | Diccionario de tabla `user_notification_preferences`                          |
| `docs/data/ENTITY_RELATIONSHIPS.md`                 | Obligatorio | Diagrama actualizado con nueva tabla                                          |
| `docs/data/MIGRATIONS.md`                           | Obligatorio | Registrar V21                                                                 |
| `docs/ai/lecciones.md`                              | Obligatorio | Entradas por aprendizajes de la implementación                                |
| `ROADMAP.md` + `docs/ai/propuestas.md`              | Obligatorio | Propuestas técnicas derivadas + marcar §5.5 como F-NNN pendiente              |

---

## Árbol de dependencias entre fases

```
Fase 1 (base)
  └── Fase 2 (change-password)
        └── Fase 3 (sessions)
              └── Fase 4 (notification-preferences)
                    └── Fase 5 (account-access)
                          └── Fase 6 (documentación)
```

Cada fase es autocontenida: el proyecto compila y los tests pasan al finalizar cada una.

---

## Decisiones de diseño documentadas

| Decisión | Alternativa descartada | Razón |
|---|---|---|
| Parseo de User-Agent con regex básico en `keygo-supabase` | Agregar librería `yauaa` / `ua-parser-java` | Evitar dependencia externa para parsing no crítico; regex cubre casos principales (Chrome, Firefox, Safari, Edge, Windows, macOS, Linux, Android, iOS) |
| `location` retorna null (no geoIP) | Integrar servicio de geolocalización por IP | Fuera de alcance del RFC; se puede extender en T-NNN futuro |
| GET preferences retorna defaults si no hay registro | Crear registro al primer login | Evita migración de datos retroactiva; UX transparente |
| `AccountSettingsController` separado de `AccountProfileController` | Extender el controller existente | Separación de responsabilidades: profile vs settings |
| Idempotencia en DELETE sessions: 200 siempre | 404 si no encontrada | Requerimiento explícito del RFC §3.5 |
| `is_current` derivado del `session_id` en claims del JWT | Campo extra en sesión | El access token ya contiene `session_id`; no requiere estado extra |

---

## Estado de fases

| Fase | Descripción | Estado |
|---|---|---|
| 1 | Infraestructura base | ✅ Completada 2026-04-02 |
| 2 | Cambio de contraseña | ✅ Completada 2026-04-02 |
| 3 | Gestión de sesiones | ✅ Completada 2026-04-02 |
| 4 | Preferencias de notificación | ✅ Completada 2026-04-02 |
| 5 | Vista de acceso self-service | ✅ Completada 2026-04-02 |
| 6 | Documentación y cierre | ✅ Completada 2026-04-02 |

### Notas de implementación — Fase 1

- Handler `GlobalExceptionHandler` para `IncorrectCurrentPasswordException` movido a Fase 2
  (la excepción no existe aún; agregar el import antes causaría error de compilación).
- `DELETE /account/sessions/{sessionId}` usa `hasSegment` en el filtro (no `hasSuffix`) —
  el path termina en UUID, no en sufijo fijo. Mismo patrón que `billingContractsPathSuffix`.
- 6 tests de regresión agregados en `BootstrapAdminKeyFilterTest` (3 sufijos + 2 sessions).
