# AGENTS.md — KeyGo Server

> Quick-start guide for AI coding agents. Full context in `AI_CONTEXT.md`, `ARCHITECTURE.md`, `CLAUDE.md`, `ROADMAP.md`.
>
> 📖 **Sub-documentos de este archivo (detalle en `docs/ai/`):**
> - [`docs/ai/agents-registro.md`](docs/ai/agents-registro.md) — Historial de cambios al quick-start
> - [`docs/ai/inconsistencias.md`](docs/ai/inconsistencias.md) — Inconsistencias detectadas
> - [`docs/ai/lecciones.md`](docs/ai/lecciones.md) — Lecciones aprendidas
> - [`docs/ai/propuestas.md`](docs/ai/propuestas.md) — Propuestas de mejoras futuras (corto/mediano/largo)

## Module map & dependency rules

```
keygo-domain   ← pure Java, NO Spring, NO internal deps  [Tenant ✅, ClientApp ✅, Auth ✅, SigningKey ✅]
keygo-app      ← usecases + port interfaces (OUT); depends on domain
keygo-infra    ← JWT signer (RSA/Nimbus), JWKS builder, PKCE verifier; depends on app  [✅ Activo]
keygo-api      ← REST controllers + DTOs; depends on app
keygo-supabase ← JPA/Flyway/PostgreSQL; depends on infra
keygo-run      ← main class + Spring wiring (@Bean factories) + application.yml
keygo-bom      ← dependency version management
keygo-common   ← shared utils                               [🚧 stub]
```

**Golden rule:** never add Spring dependencies to `keygo-domain`; never cross module boundaries backwards.

## Essential commands

```bash
./mvnw clean package                   # full build
./mvnw verify                          # all tests + JaCoCo coverage check
./mvnw test                            # tests only (no coverage check)
./mvnw -pl keygo-api test              # single module
./mvnw spring-boot:run -pl keygo-run   # run locally

# ── Menú principal (punto de entrada centralizado) ─────────────────────────────
./docs/scripts/keygo.sh          # menú interactivo (todas las operaciones)
./docs/scripts/keygo.sh <N>      # ejecución directa por número de opción (ej: 7 = migrate)

# ── Ambiente ───────────────────────────────────────────────────────────────────
./docs/scripts/switch-env.sh local   # activar .env-local → .env en raíz del proyecto
./docs/scripts/switch-env.sh desa    # activar .env-desa  → .env en raíz del proyecto
./docs/scripts/switch-env.sh prod    # activar .env-prod  → .env en raíz del proyecto
./docs/scripts/switch-env.sh list    # listar templates disponibles (en envs/)

# ── Base de datos ──────────────────────────────────────────────────────────────
./docs/scripts/db/start.sh       # iniciar Docker Compose (Postgres + PgAdmin)
./docs/scripts/db/stop.sh        # detener Docker Compose
./docs/scripts/db/migrate.sh     # ejecutar migraciones Flyway
./docs/scripts/db/info.sh        # ver estado de migraciones
./docs/scripts/db/validate.sh    # validar migraciones
./docs/scripts/db/repair.sh      # reparar metadatos Flyway
./docs/scripts/db/clean.sh       # ⚠️ limpiar schema completo (pide confirmación)
./docs/scripts/db/setup.sh       # setup inicial Supabase

# ── App / tests ────────────────────────────────────────────────────────────────
./docs/scripts/quick-start.sh              # start DB + env vars + run app
./docs/scripts/test-service-info.sh        # smoke-test GET /api/v1/service/info
./docs/scripts/test-response-codes.sh      # smoke-test GET /api/v1/response-codes
./docs/scripts/setup-keygo-tenant.sh       # bootstrap tenant keygo + keygo-ui

# ── Verificar actividad de retroalimentación del agente AI ─────────────────────
./docs/scripts/check-ai-docs.sh            # umbral por defecto: 30 días
./docs/scripts/check-ai-docs.sh --days 60  # umbral personalizado
./docs/scripts/check-ai-docs.sh --quiet    # solo exit code (útil en CI)
# Exit: 0=OK  1=sin actividad reciente  2=sin entradas  3=archivo no encontrado
```

## Spring Boot 4.x & Jackson 3 (⚠️ import namespace changed)

This project uses **Spring Boot 4.0.3** with **Jackson 3.x** (`tools.jackson.datatype:jackson-datatype-jsr310:3.0.0-rc2`).  
Jackson 3 moved the **databind and datatype** classes to a new Maven group **and** Java package: `tools.jackson.*`.

```java
// ✅ Correct imports (Jackson 3 / Spring Boot 4)
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.datatype.jsr310.JavaTimeModule;

// ❌ Wrong — will not compile (databind moved to tools.jackson.*)
import com.fasterxml.jackson.databind.ObjectMapper;

// ✅ Still OK — annotation namespace did NOT change
import com.fasterxml.jackson.annotation.JsonInclude;  // used in BaseResponse, ApplicationConfig
```

Jackson is customized globally in `keygo-run` via a `JsonMapperBuilderCustomizer` bean in `ApplicationConfig`:  
UTC timezone, `NON_NULL` inclusion, case-insensitive properties, `FAIL_ON_UNKNOWN_PROPERTIES=false`.

## API response pattern (mandatory)

Every controller must return `BaseResponse<T>` using `ResponseHelper` + `ResponseCode`:

```java
// keygo-api: controller
BaseResponse<MyData> response = BaseResponse.<MyData>builder()
    .data(data)
    .success(ResponseHelper.message(ResponseCode.RESOURCE_RETRIEVED))
    .build();
return ResponseEntity.status(HttpStatus.OK).body(response);
```

`ResponseCode` is an enum in `keygo-api`; add new entries there, not ad-hoc strings.  
`BaseResponse` fields: `date` (auto), `success`/`failure` (`MessageResponse`), `data`, optional `debug`/`throwable`.

### Error handling — GlobalExceptionHandler

`keygo-api` has a `@RestControllerAdvice` (`GlobalExceptionHandler`) that wraps exceptions into `BaseResponse<ErrorData>`:

| Exception | HTTP | ResponseCode |
|---|---|---|
| `UnauthorizedException` | 401 | `AUTHENTICATION_REQUIRED` |
| `NoResourceFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| `IllegalArgumentException` | 400 | `INVALID_INPUT` |
| `Exception` (catch-all) | 500 | `OPERATION_FAILED` |

To signal an auth error from any layer, throw `UnauthorizedException` (located in `keygo-api/error/`).

> **T-106 — Jerarquía de excepciones tipadas por capa (pendiente):**
> `ErrorData` incluirá campo `layer` (`DOMAIN` / `USE_CASE` / `PORT` / `CONTROLLER`) siempre visible al consumer.
> Las excepciones base `KeyGoException` / `DomainException` / `UseCaseException` / `PortException` estandarizan mensajes.
> Ver diseño completo en [`docs/design/EXCEPTION_HIERARCHY.md`](docs/design/EXCEPTION_HIERARCHY.md).

## Hexagonal flow: adding a feature

1. **Port OUT** — interface in `keygo-app/src/main/java/.../app/<feature>/port/`  
   (example: `platform/port/ServiceInfoProvider`)
2. **UseCase** — plain Java class in `keygo-app/src/main/java/.../app/<feature>/usecase/`  
   (example: `platform/usecase/GetServiceInfoUseCase`)
3. **Adapter (impl)** — `@ConfigurationProperties` or Spring component in `keygo-run` or `keygo-supabase`  
   (example: `ServiceInfoProperties implements ServiceInfoProvider`)
4. **Wiring** — `@Bean` factory in `keygo-run/config/ApplicationConfig.java`
5. **Controller** — `@RestController` in `keygo-api/<feature>/controller/`, path `/api/v1/<resource>/...`  
   Response DTOs go in `keygo-api/<feature>/response/`
6. **Postman** — add or update the request in `docs/postman/KeyGo-Server.postman_collection.json` **before closing the task**.  
   Include: HTTP method, URL with env variables (`{{fullBaseUrl}}/api/v1/...`), required headers, example body (if applicable), and `pm.test()` scripts validating status code, `BaseResponse` structure and business fields.  
   This update **does not require explicit user instruction** — it is a mandatory part of the endpoint workflow.
7. **Frontend Guide** — update section §14 (endpoint inventory) in `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` **before closing the task**.  
   Include: HTTP method, full URL with `context-path`, required auth (Bearer / public), body/params example and `BaseResponse` structure.  
   This update **does not require explicit user instruction** — it is a mandatory part of the endpoint workflow.
8. **Data docs (if Flyway migration added)** — when a new `V{n}__*.sql` migration is created, update **all three** data documents **before closing the task**:
   - `docs/keygo-server/DATA_MODEL.md` — add table dictionary (fields, types, constraints, business rules)
   - `docs/keygo-server/ENTITY_RELATIONSHIPS.md` — update affected context diagrams and relationships
   - `docs/keygo-server/DATA_DICTIONARY.md` — update "Próximas migraciones" section and cross-references
   This update **does not require explicit user instruction** — it is mandatory whenever a new migration is added.

## context-path is always active

All endpoints are served under `/keygo-server`. Local URLs:
- `http://localhost:8080/keygo-server/api/v1/service/info` (GET — info del servicio: title, name, version, **environment**, **status**)
- `http://localhost:8080/keygo-server/api/v1/platform/stats` (GET — **ADMIN** — estadísticas agregadas: tenants/users/apps/signingKeys por estado)
- `http://localhost:8080/keygo-server/api/v1/admin/platform/dashboard` (GET — **ADMIN** — dashboard completo: service summary, security metrics, contadores por estado, topology, rankings top-5, pending actions, recent activity, quick actions — **single-call, GROUP BY queries**)
- `http://localhost:8080/keygo-server/api/v1/response-codes`
- `http://localhost:8080/keygo-server/api/v1/tenants` (POST — create)
- `http://localhost:8080/keygo-server/api/v1/tenants` (GET — **ADMIN** — list all tenants, paginated, with filters `status`, `nameLike`, `page`, `size`)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}` (GET — retrieve)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/suspend` (PUT — suspend)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/activate` (PUT — **activate** — reactiva tenant suspendido o pendiente)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps` (POST — create client app)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps` (GET — list client apps)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{clientId}` (GET — get client app)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{clientId}` (PUT — update client app)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{clientId}/rotate-secret` (POST — rotate secret)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/users` (POST — create user)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/users` (GET — list users)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/users/{userId}` (GET — get user)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/users/{userId}` (PUT — update user)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/users/{userId}/reset-password` (POST — reset password)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/users/validate-credentials` (POST — validate credentials)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{clientId}/register` (POST — **público** — register user with PENDING status + send verification email)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{clientId}/verify-email` (POST — **público** — verify email code → activate user)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{clientId}/resend-verification` (POST — **público** — resend code only if previous one expired)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/oauth2/authorize` (GET — initiate auth)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/login` (POST — login + issue code)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/profile` (GET — **público con Bearer** — perfil propio del usuario autenticado)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/profile` (PATCH — **público con Bearer** — editar perfil propio, PATCH semántica)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/change-password` (POST — **público con Bearer** — cambiar contraseña propia)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/sessions` (GET — **público con Bearer** — listar sesiones activas propias)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/sessions/{sessionId}` (DELETE — **público con Bearer** — revocar sesión propia, idempotente)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/notification-preferences` (GET — **público con Bearer** — preferencias de notificación, retorna defaults si no hay registro)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/notification-preferences` (PATCH — **público con Bearer** — actualizar preferencias de notificación, upsert)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/account/access` (GET — **público con Bearer** — apps y roles propios del usuario autenticado)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{providerClientId}/billing/catalog` (GET — **público** — catálogo de planes públicos, filtro opcional `?subscriberType=TENANT|TENANT_USER`)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{providerClientId}/billing/catalog/{planCode}` (GET — **público** — detalle de un plan público con entitlements)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/apps/{clientId}/billing/plans` (POST — **Bearer ADMIN_TENANT** — crear plan con versión inicial y entitlements)
- `http://localhost:8080/keygo-server/api/v1/billing/contracts` (POST — **público** — iniciar contrato de suscripción; body incluye `clientAppId`; genera código de verificación y envía email)
- `http://localhost:8080/keygo-server/api/v1/billing/contracts/{contractId}` (GET — **público** — estado del contrato)
- `http://localhost:8080/keygo-server/api/v1/billing/contracts/{contractId}/resume` (GET — **público** — restaurar onboarding; devuelve `nextAction` + `verificationCodeExpired` para que el frontend sepa en qué paso está)
- `http://localhost:8080/keygo-server/api/v1/billing/contracts/{contractId}/verify-email` (POST — **público** — verificar código de email → avanza a `PENDING_PAYMENT`; body: `{"code":"123456"}`)
- `http://localhost:8080/keygo-server/api/v1/billing/contracts/{contractId}/resend-verification` (POST — **público** — reenviar código; si código válido lo reenvía igual; si expiró genera uno nuevo)
- `http://localhost:8080/keygo-server/api/v1/billing/contracts/{contractId}/mock-approve-payment` (POST — **público/dev** — simular pago, requiere `keygo.billing.mock-payment-enabled=true`)
- `http://localhost:8080/keygo-server/api/v1/billing/contracts/{contractId}/activate` (POST — **público** — activar contrato → crea tenant/user + suscripción + factura)
- `http://localhost:8080/keygo-server/api/v1/tenants/{subscriberSlug}/apps/{providerClientId}/billing/subscription` (GET — **Bearer ADMIN_TENANT** — suscripción activa; `{subscriberSlug}`=tenant suscriptor, `{providerClientId}`=client_id global del proveedor)
- `http://localhost:8080/keygo-server/api/v1/tenants/{subscriberSlug}/apps/{providerClientId}/billing/subscription/cancel` (POST — **Bearer ADMIN_TENANT** — marcar cancelación al fin del período)
- `http://localhost:8080/keygo-server/api/v1/tenants/{subscriberSlug}/apps/{providerClientId}/billing/invoices` (GET — **Bearer ADMIN_TENANT** — lista de facturas)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/oauth2/token` (POST — exchange code → JWT tokens)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/oauth2/token` (POST — rotate refresh_token grant)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/oauth2/token` (POST — client_credentials grant, M2M, requiere `client_id` + `client_secret`)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/oauth2/revoke` (POST — revoke token, RFC 7009, **público**)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/userinfo` (GET — OIDC userinfo, **público** con Bearer token)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/.well-known/openid-configuration` (GET — OIDC discovery, **público**)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/.well-known/jwks.json` (GET — JWK Set, **público**)
- `http://localhost:8080/keygo-server/actuator/health`
- **`http://localhost:8080/keygo-server/swagger-ui/index.html`** — Swagger UI interactiva (público)
- **`http://localhost:8080/keygo-server/v3/api-docs`** — OpenAPI JSON spec (público)

✅ **Bug T-001 corregido (2026-03-21) — `BootstrapAdminKeyFilter`:** now uses `request.getServletPath()` (strips the context-path) instead of `request.getRequestURI()`. With `context-path=/keygo-server`, `getRequestURI()` returned `/keygo-server/api/...` which never matched the prefixes in `application.yml` (e.g. `/api/`), leaving all routes public. `getServletPath()` returns `/api/...` directly. Tests updated to use `setServletPath()` + 2 regression tests with simulated context-path.

✅ **CORS habilitado (2026-03-26) — `SecurityConfig` + `KeyGoCorsProperties`:** `SecurityFilterChain` ahora aplica CORS antes de evaluar `BootstrapAdminKeyFilter`. Configurado vía `keygo.cors.*` en `application.yml` con default `allowedOrigins: [http://localhost:5173]`, `allowCredentials: true` (necesario para `JSESSIONID` entre `/authorize` y `/login`). Nueva clase `KeyGoCorsProperties` (`@ConfigurationProperties("keygo.cors")`); 7 tests unitarios en `CorsConfigTest`.

✅ **Bearer-only admin auth (2026-03-25) — `BootstrapAdminKeyFilter` + `@PreAuthorize`:** protected `/api/**` endpoints now require **only** `Authorization: Bearer <jwt>`. The filter validates signature/expiration and publishes authorities from claim `roles`; authorization is enforced per endpoint with `@PreAuthorize` (`ADMIN` global, `ADMIN_TENANT` scoped by tenant). Tenant scope is validated against `tenant_slug` claim (or `iss` fallback) vs `tenantSlug` in path.

The filter has three path categories (see `KeyGoBootstrapProperties`):

| Property | `application.yml` value | Behaviour |
|---|---|---|
| `keygo.bootstrap.api-path-prefix` | `/api/` | Protected — requires `Authorization: Bearer <jwt>` |
| `keygo.bootstrap.actuator-path-prefix` | `/actuator/` | Public |
| `keygo.bootstrap.service-info-path-prefix` | `/service/info` | Public |
| `keygo.bootstrap.swagger-ui-path-prefix` | `/swagger-ui` | Public |
| `keygo.bootstrap.api-docs-path-prefix` | `/v3/api-docs` | Public |
| `keygo.bootstrap.well-known-path-prefix` | `/.well-known` | Public — OIDC discovery + JWKS |
| `keygo.bootstrap.userinfo-path-suffix` | `/userinfo` | Public — Bearer token validated inside controller |
| `keygo.bootstrap.revocation-path-suffix` | `/oauth2/revoke` | Public — RFC 7009, idempotente |
| `keygo.bootstrap.register-path-suffix` | `/register` | Public — self-registration endpoint |
| `keygo.bootstrap.verify-email-path-suffix` | `/verify-email` | Public — email verification endpoint |
| `keygo.bootstrap.resend-verification-path-suffix` | `/resend-verification` | Public — resend verification code endpoint |
| `keygo.bootstrap.account-profile-path-suffix` | `/account/profile` | Public — Bearer token validated inside controller (GET + PATCH self-service) |
| `keygo.bootstrap.account-change-password-path-suffix` | `/account/change-password` | Public — Bearer token validated inside `ChangePasswordUseCase` |
| `keygo.bootstrap.account-sessions-path-suffix` | `/account/sessions` | Public — uses `hasSegment` to cover `/sessions` and `/sessions/{uuid}` |
| `keygo.bootstrap.account-notification-preferences-path-suffix` | `/account/notification-preferences` | Public — Bearer token validated inside use cases |
| `keygo.bootstrap.account-access-path-suffix` | `/account/access` | Public — Bearer token validated inside `GetUserAccessUseCase` |
| `keygo.bootstrap.authorize-path-suffix` | `/oauth2/authorize` | Public — browser navigates here to start the OAuth2 flow |
| `keygo.bootstrap.login-path-suffix` | `/account/login` | Public — user POSTs credentials during the authorization code flow |
| `keygo.bootstrap.token-path-suffix` | `/oauth2/token` | Public — code exchange (PKCE-protected) and token rotation |

## Security header

Protected routes require `Authorization: Bearer <jwt>`.  
The filter extracts roles from JWT claim `roles` and `@PreAuthorize` enforces endpoint permissions.  
Set `keygo.bootstrap.enabled=false` in `application.yml` (or `KEYGO_BOOTSTRAP_ENABLED=false`) to disable the filter entirely and make all routes public (useful in tests).

`KeyGoBootstrapProperties` has `@AssertTrue` bean validation: **the app fails to start** if `keygo.bootstrap.enabled=true` and `adminKey` is null or blank. Use `keygo.bootstrap.enabled=false` in tests to bypass both the filter and this validation.

## JPA entities (keygo-supabase)

Use `UUID` PK with `@GeneratedValue(strategy = GenerationType.UUID)`, `@CreationTimestamp`/`@UpdateTimestamp` for timestamps, Lombok `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`. **Do NOT use `@Data`** — it generates `equals()`/`hashCode()`/`toString()` over all fields including lazy collections, causing performance issues and potential `LazyInitializationException`. Schema is managed by Flyway (`db/migration/V<n>__description.sql`). `ddl-auto: validate`.

**Existing entities (packages under `io.cmartinezs.keygo.supabase`):**

| Entity | Package | Table | Key relationships |
|---|---|---|---|
| `UserEntity` | `user.entity` | `users` | `@ManyToMany` → `RoleEntity` via `user_roles` |
| `RoleEntity` | `membership.entity` | `roles` | `@ManyToMany` → `PermissionEntity` via `role_permissions` |
| `PermissionEntity` | `membership.entity` | `permissions` | `action` field is `enum Action {CREATE,READ,UPDATE,DELETE,EXECUTE}` |
| `TenantEntity` | `tenant.entity` | `tenants` | `slug` unique index; `status` enum `ACTIVE\|SUSPENDED\|PENDING` |
| `ClientAppEntity` | `clientapp.entity` | `client_apps` | `@ManyToOne` → `TenantEntity`; `@OneToMany(cascade=ALL, orphanRemoval=true)` → redirect URIs, grants, scopes |
| `ClientRedirectUriEntity` | `clientapp.entity` | `client_redirect_uris` | `@ManyToOne(fetch=LAZY)` → `ClientAppEntity` |
| `ClientAllowedGrantEntity` | `clientapp.entity` | `client_allowed_grants` | `@ManyToOne(fetch=LAZY)` → `ClientAppEntity`; `grantType` mapped as `AllowedGrant` enum |
| `ClientAllowedScopeEntity` | `clientapp.entity` | `client_allowed_scopes` | `@ManyToOne(fetch=LAZY)` → `ClientAppEntity` |
| `TenantUserEntity` | `user.entity` | `tenant_users` | `@ManyToOne(fetch=LAZY)` → `TenantEntity`; `UNIQUE(tenant_id, email)`, `UNIQUE(tenant_id, username)` |
| `AppRoleEntity` | `membership.entity` | `app_roles` | `@ManyToOne` → `ClientAppEntity`; `UNIQUE(client_app_id, code)` |
| `MembershipEntity` | `membership.entity` | `memberships` | `@ManyToOne` → `TenantUserEntity`, `ClientAppEntity`; `@ManyToMany` → `AppRoleEntity` via `membership_roles` |
| `AuthorizationCodeEntity` | `auth.entity` | `authorization_codes` | `@ManyToOne(fetch=LAZY)` → `ClientAppEntity`, `TenantEntity`, `TenantUserEntity` |
| `SigningKeyEntity` | `auth.entity` | `signing_keys` | `kid` unique; `status` check `ACTIVE\|RETIRED\|REVOKED`; `algorithm` (RS256/RS384/RS512); `public_material` + `private_material` PEM |
| `SessionEntity` | `auth.entity` | `sessions` | `@ManyToOne(fetch=LAZY)` → `TenantEntity`, `ClientAppEntity`, `TenantUserEntity`; `status` check `ACTIVE\|TERMINATED\|EXPIRED` |
| `RefreshTokenEntity` | `auth.entity` | `refresh_tokens` | `@ManyToOne(fetch=LAZY)` → `SessionEntity`, `TenantEntity`, `ClientAppEntity`, `TenantUserEntity`; `token_hash` unique (SHA-256 hex 64 chars); `status` check `ACTIVE\|USED\|EXPIRED\|REVOKED` |
| `EmailVerificationEntity` | `user.entity` | `email_verifications` | `@ManyToOne(fetch=LAZY)` → `TenantUserEntity`; `code` VARCHAR(10); `expires_at`+`used_at` TIMESTAMPTZ; latest row per user = active verification |

**Existing repositories (packages under `io.cmartinezs.keygo.supabase`):**

| Repository | Package |
|---|---|
| `UserRepository` | `user.repository` |
| `RoleRepository` | `membership.repository` |
| `TenantJpaRepository` | `tenant.repository` |
| `ClientAppJpaRepository` | `clientapp.repository` |
| `TenantUserJpaRepository` | `user.repository` |
| `AuthorizationCodeJpaRepository` | `auth.repository` |
| `SigningKeyJpaRepository` | `auth.repository` |
| `SessionJpaRepository` | `auth.repository` |
| `RefreshTokenJpaRepository` | `auth.repository` |
| `EmailVerificationJpaRepository` | `user.repository` |

**Flyway migrations already applied:**
- `V1__drop_all.sql` — **Drop ALL** (pizarrón limpio, idempotente; incluye `contractors`)
- `V2__foundation.sql` — Extensión `uuid-ossp` + función trigger `update_updated_at_column()`
- `V3__tenants.sql` — Tabla `tenants` (slug, status check `ACTIVE|SUSPENDED|PENDING|DELETED`, `contractor_id` sin FK aún)
- `V4__client_apps.sql` — Tablas `client_apps`, `client_redirect_uris`, `client_allowed_grants`, `client_allowed_scopes`
- `V5__tenant_users.sql` — Tabla `tenant_users` (con 6 campos OIDC 5.3 de perfil)
- `V6__memberships.sql` — Tablas `app_roles`, `memberships`, `membership_roles` (PK compuesta)
- `V7__auth_codes_and_signing_keys.sql` — Tablas `authorization_codes` (PKCE), `signing_keys` (RSA PEM)
- `V8__sessions_and_refresh_tokens.sql` — Tablas `sessions`, `refresh_tokens` (hash SHA-256, rotación)
- `V9__email_verifications.sql` — Tabla `email_verifications` (código 6 dígitos, TTL 30 min)
- `V10__billing_catalog.sql` — Tablas `app_plans` (con `subscriber_type`), `app_plan_versions`, `app_plan_billing_options`, `app_plan_entitlements`
- `V11__contractors.sql` — **NUEVA** tabla `contractors` (1:1 con `tenant_users`; status `PENDING|ACTIVE|SUSPENDED`) + FK `tenants.contractor_id → contractors(id)`
- `V12__billing_contracts.sql` — Tabla `app_contracts` (modelo v2: `contractor_id`, sin `subscriber_*` ni `company_slug`; estados ampliados: `SUPERSEDED`, `FINALIZED`)
- `V13__billing_subscriptions.sql` — Tablas `app_subscriptions` (`contractor_id`, sin `subscriber_*`), `payment_transactions`
- `V14__billing_invoices_and_usage.sql` — Tablas `invoices`, `usage_counters` (`contractor_id`, sin `subscriber_*`)
- `V15__billing_support_tables.sql` — Tablas `payment_methods`, `tenant_billing_profiles`
- `V16__seed_foundation.sql` — Seed: tenants `keygo`+`demo`, apps, usuarios (contraseñas correctas), roles, memberships
- `V17__seed_billing_plans.sql` — Seed: planes FREE/PERSONAL/TEAM/BUSINESS/FLEX/ENTERPRISE + versiones v1.0 + billing options + entitlements (escalera completa)
- `V18__seed_contractors.sql` — Seed: `keygo_contractor` (TenantUser en keygo), `contractors` record ACTIVE, contrato ACTIVE plan PERSONAL, suscripción ACTIVE, tenant `acme` vinculado

- `V19__user_status_reset_password.sql` — Columna `status=RESET_PASSWORD` en `tenant_users`; tabla `password_reset_tokens`
- `V20__add_app_role_hierarchy.sql` — Tabla `app_role_hierarchy` (parent/child, restricciones de ciclo, profundidad ≤5), índices, CTE recursiva para expansión de roles en JWT
- `V21__user_notification_preferences.sql` — Tabla `user_notification_preferences` (5 flags boolean, UNIQUE `user_id+tenant_id`)

Next migration must be `V22__...`. **Never reuse or edit existing migration files.**

**Seed convention — foreign keys via subquery (mandatory):**  
When a seed row references a parent table's PK, **never hardcode the UUID**. Always use a `SELECT` subquery with a `WHERE` on a unique, human-readable field:

```sql
-- ❌ Bad: hardcoded UUID (fragile, unreadable)
INSERT INTO client_apps (tenant_id, name)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'my-app');

-- ✅ Good: subquery by semantic field
INSERT INTO client_apps (tenant_id, name)
VALUES ((SELECT id FROM tenants WHERE slug = 'keygo'), 'my-app');

-- ✅ Good: chained subqueries
INSERT INTO memberships (tenant_user_id, client_app_id)
VALUES (
  (SELECT tu.id FROM tenant_users tu
   JOIN tenants t ON t.id = tu.tenant_id
   WHERE t.slug = 'keygo' AND tu.username = 'keygo_admin'),
  (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
);
```

Preferred semantic fields by parent table:

| Parent table | Preferred field |
|---|---|
| `tenants` | `slug` |
| `client_apps` | `client_id` |
| `tenant_users` | `username` or `email` (combined with `tenant_id` subquery) |
| `app_roles` | `code` (combined with `client_app_id` subquery) |
| `app_plans` | `code` (combined with `client_app_id` subquery) |
| `app_plan_versions` | `version_tag` (combined with `plan_id` subquery) |

**Seed credentials (dev/local ONLY — never use in production):**

| Tabla | Usuario | Email | Contraseña | Tenant | Rol |
|---|---|---|---|---|---|
| `tenant_users` | `keygo_admin` | `admin@keygo.local` | `Admin1234!` | `keygo` | admin |
| `tenant_users` | `keygo_tenant_admin` | `tenant-admin@keygo.local` | `Admin1234!` | `keygo` | admin_tenant |
| `tenant_users` | `keygo_user` | `user@keygo.local` | `Admin1234!` | `keygo` | user_tenant |
| `tenant_users` | `keygo_contractor` | `contractor@keygo.local` | `Admin1234!` | `keygo` | user_tenant |
| `tenant_users` | `demo_admin` | `admin@demo.local` | `DevAdmin1!` | `demo` | demo_admin |
| `tenant_users` | `demo_user` | `user@demo.local` | `DevUser1!` | `demo` | demo_user |

**Seed contractors (modelo billing v2):**

| Tabla | ID estable | Tenant user | Status | Tenant(s) creados |
|---|---|---|---|---|
| `contractors` | `88888888-8888-8888-8888-000000000001` | `keygo_contractor` | `ACTIVE` | `acme`, `demo` |

**`SupabaseJpaConfig`** (`keygo-supabase`) declares `@EntityScan` + `@EnableJpaRepositories` — required when adding new entities or repositories to this module.

## Enabling the database

```bash
# Cambiar al ambiente local (copia template → .env en la raíz del proyecto)
./docs/scripts/switch-env.sh local
# Cargar variables en el shell actual
set -a; source .env; set +a

# Iniciar / detener Postgres + PgAdmin via Docker
./docs/scripts/db/start.sh    # o: ./docs/scripts/keygo.sh 5
./docs/scripts/db/stop.sh     # o: ./docs/scripts/keygo.sh 6

# Alternativamente, usar el menú principal
./docs/scripts/keygo.sh       # menú interactivo
./docs/scripts/keygo.sh 5     # iniciar DB directamente
```

## Naming conventions

| Artifact | Pattern | Example |
|---|---|---|
| Port OUT | `<Action><Entity>Port` | `FindTenantPort`, `SaveContractPort` |
| UseCase | `<Action><Entity>UseCase` | `CreateAppContractUseCase` |
| Controller | `<Module><Resource>Controller` | `AppBillingContractController` |
| Response DTO | `<Entity>Data` | `AppContractData`, `AppContractResumeData` |
| Request DTO | `<Action><Entity>Request` | `CreateContractRequest` |
| JPA Entity | `<Entity>Entity` | `AppContractEntity` |
| JPA Repository | `<Entity>JpaRepository` | `AppContractJpaRepository` |
| Adapter (port impl) | `<Technology><Port>Adapter` | `SupabaseContractRepositoryAdapter` |

## Environment variables

| Variable | Description | Default |
|---|---|---|
| `PORT` | Server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profiles (`supabase`, `local`, `desa`, `prod`) | `default` |
| `SUPABASE_URL` | PostgreSQL JDBC URL | — |
| `SUPABASE_USER` | DB user | — |
| `SUPABASE_PASSWORD` | DB password | — |
| `KEYGO_BOOTSTRAP_ENABLED` | Enable security filter | `true` |

Profiles: `supabase` activates JPA + Flyway; combine with `local` / `desa` / `prod` for env-specific config.  
Switch env: `./docs/scripts/switch-env.sh <local|desa|prod>`.

## Testing conventions

- Unit tests: `@ExtendWith(MockitoExtension.class)` + AssertJ + Mockito — **no Spring context**.
- UseCases: constructor injection; add `@Mock` for every port — `@InjectMocks` resolves via constructor automatically. When adding a new port to a use case, always add the corresponding `@Mock` in the test.
- Controllers: `@WebMvcTest` or plain Mockito without full Spring context.
- Integration tests (supabase): Testcontainers PostgreSQL is configured in `pom.xml` and `src/test/resources/application-test.yml` (uses TC JDBC URL `jdbc:tc:postgresql:15-alpine:///testdb`) but **no integration tests are written yet** — prefer Testcontainers over repository mocks when possible.
- `mockito-junit-jupiter` must be in the module's `pom.xml` when using `@ExtendWith(MockitoExtension.class)`.
- Pattern: Given/When/Then comments in every test method.

## Email notifications — Thymeleaf templates

For email notifications, use **Thymeleaf templates** (not HTML inline in Java). This ensures:
- ✅ Separation of concerns (templates separate from code)
- ✅ No recompilation for design changes
- ✅ Designer-friendly (edit `.html` directly)
- ✅ XSS protection (Thymeleaf auto-escapes values)
- ✅ i18n support (locale-aware rendering)
- ✅ Testeable independently

**Architecture:**
1. **Port OUT** — `EmailNotificationPort` interface in `keygo-infra/port/notification/`
2. **Strategy** — `EmailStrategy` implementations per email type (validation, recovery, etc.)
3. **Adapter** — `EmailNotificationAdapter` in `keygo-infra/adapter/notification/`
4. **Config** — `ThymeleafTemplateConfig` bean in `keygo-run/config/`
5. **Templates** — `.html` files in `keygo-run/src/main/resources/templates/email/`

**Documentation:**
- **Index (start here!):** [`docs/design/email/EMAIL_TEMPLATES_INDEX.md`](docs/design/email/EMAIL_TEMPLATES_INDEX.md) — Navigation guide for all email docs
- **Quick start (5 steps):** [`docs/design/email/EMAIL_TEMPLATES_QUICKSTART.md`](docs/design/email/EMAIL_TEMPLATES_QUICKSTART.md)
- **Complete guide:** [`docs/design/email/EMAIL_TEMPLATES_THYMELEAF.md`](docs/design/email/EMAIL_TEMPLATES_THYMELEAF.md) — architecture, implementations, best practices
- **Pattern analysis:** [`docs/design/email/EMAIL_PATTERNS_ANALYSIS.md`](docs/design/email/EMAIL_PATTERNS_ANALYSIS.md) — HTML inline vs Thymeleaf, industry best practices 2024-2026
- **Visual reference:** [`docs/design/email/EMAIL_TEMPLATES_VISUAL.md`](docs/design/email/EMAIL_TEMPLATES_VISUAL.md) — diagrams, checklists, quick lookup

**Key points:**
- Depends on: `spring-boot-starter-thymeleaf` + `spring-boot-starter-mail`
- Use `@Bean(name = "emailTemplateEngine")` to avoid conflicts with web tier
- Template cache: `false` in dev, `true` in prod (configured via `KeyGoEmailProperties`)
- Render with: `emailTemplateEngine.process(templateName, context)`
- Send with: `JavaMailSender.send(mimeMessage)` with `MimeMessageHelper` (UTF-8, HTML mode)

## Implementation plan

Full plan: **`docs/arch/keygo_server_implementation_plan.md`** — 11 phases ordered by dependency.

| Phase | Focus | Status |
|---|---|---|
| 0 | Structural hardening (module deps, package org, conventions, CI, quality baseline) | ✅ Done (2026-03-21) |
| 1 | Multitenancy (`Tenant`, `TenantRepositoryPort`, resolver) | ✅ Done (2026-03-21) |
| 2 | Client app model (`ClientApp`, redirect URIs, grants, secret rotation) | ✅ Done (2026-03-21) |
| 3 | User identity per tenant | ✅ Done (2026-03-21) |
| 4 | Memberships & roles per app | ✅ Done (2026-03-21) |
| 5 | OAuth2/OIDC authorization flow (Auth Code + PKCE) | ✅ Done (2026-03-22) |
| 6 | Token signing RS256 + JWKS + OIDC Discovery | ✅ Done (2026-03-22) |
| 7 | Refresh token (rotation + SHA-256 hash), Session, Revocation (RFC 7009), UserInfo (OIDC §5.3) | ✅ Done (2026-03-22) |
| 8 | Client Credentials grant (M2M) — `IssueClientCredentialsTokenUseCase` | ✅ Done (2026-03-23) |
| 9 | Self-service de identidad: registro de usuario (`RegisterTenantUserUseCase`), verificación email (`VerifyEmailUseCase`), reenvío código (`ResendVerificationEmailUseCase`), `EmailVerificationEntity` (V12), `SmtpEmailNotificationAdapter`, `RegistrationController` (3 endpoints públicos) | ✅ Done (2026-03-23) |
| 9b | Perfil de usuario OIDC extendido (V13): 6 campos en `tenant_users`, `GetUserProfileUseCase`, `UpdateUserProfileUseCase`, `AccountProfileController` (GET+PATCH `/account/profile`), campo `accountProfilePathSuffix` en filtro | ✅ Done (2026-03-24) |
| 10–11 | Control plane y soporte, auditoría, hardening operacional, observabilidad | — |

**Golden rule from the plan:** never implement `/oauth2/authorize` before tenant, client app, user, and membership are solid.

## Git — never execute directly

List suggested `git` commands for the user; do not run them.

## Propuestas de mejoras futuras

Al cerrar cualquier tarea, incluir propuestas accionables en 3 horizontes (corto, mediano y largo plazo) y registrarlas en:

- `docs/ai/propuestas.md` (resumen operativo para agentes)
- `ROADMAP.md` (registro canónico con IDs `T-NNN` / `F-NNN`)

Si una propuesta es recurrente o de alto valor, también reflejarla en `AI_CONTEXT.md`.

## Docs — only on explicit request

Do not auto-generate or modify `.md` files unless the user explicitly asks. Place docs in `docs/<module>/` or the project root as appropriate.

### Diagrams in documentation

When a diagram is needed, use this priority order:

| Priority | Tool | When to use |
|---|---|---|
| 1 | **Mermaid** | Always first — natively supported in GitHub, GitLab, Notion and most Markdown editors |
| 2 | **PlantUML** | If the diagram type cannot be expressed in Mermaid (e.g. complex component diagrams, C4, timing) |
| 3 | **ASCII art** | Last resort — only if neither Mermaid nor PlantUML is viable in the context |

## ROADMAP.md — propuestas de mejoras (excepción a la regla anterior)

`ROADMAP.md` en la raíz del repositorio **es parte del ciclo de trabajo del agente**, al igual que `AI_CONTEXT.md` y `AGENTS.md`.  
Actualizarlo **no requiere orden explícita** del usuario cuando se cumpla alguna de estas condiciones:

| Evento | Acción en ROADMAP.md |
|---|---|
| Se completa una propuesta técnica (T-NNN) o funcional (F-NNN) | Mover fila a **"Historial de propuestas completadas"** con fecha y referencia |
| Se genera nueva propuesta técnica al concluir una tarea | Agregar en tabla **Propuestas técnicas** (horizonte correspondiente) con ID correlativo `T-NNN` |
| Se decide descartar o posponer una propuesta | Mover a **"Features fuera del MVP v1"** o eliminar con justificación |
| Se cambia el horizonte temporal de una propuesta | Mover la fila a la tabla del nuevo horizonte |

**Regla de IDs:** `T-NNN` para técnicas, `F-NNN` para funcionales. Continuar desde el último ID existente.

## Registro de cambios

> El historial detallado se encuentra en **[`docs/ai/agents-registro.md`](docs/ai/agents-registro.md)**.
>
> Agregar ahí una entrada completa cada vez que cambie la estructura de módulos, comandos, patrones
> o URLs de referencia rápida. No requiere orden explícita del usuario.

### Entradas recientes (resumen)

| Fecha | Cambio |
|---|---|
| 2026-04-03 | **Corrección `logback-spring.xml`:** 3 bugs: `%clr` no registrado → `<include resource="org/springframework/boot/logging/logback/defaults.xml"/>` al inicio del XML; appender `CONSOLE` movido dentro de `<springProfile>`; `\[` / `\]` → `[` / `]` (no se escapan en Logback). Condición cambiada de `!local` a `!(desa \| prod)`. `management.metrics.web.server.request.autotime` eliminado (deprecado SB4). `HttpStatus.UNPROCESSABLE_ENTITY` → `UNPROCESSABLE_CONTENT` en `GlobalExceptionHandlerTest`. |
| 2026-04-02 | **Trazabilidad end-to-end:** `RequestTracingFilter` (MDC traceId/method/path + header `X-Trace-ID` simétrico); MDC enriquecido en `BootstrapAdminKeyFilter` (userId) y `TenantResolutionFilter` (tenantSlug); `traceId` en `ErrorData` + `ApiErrorDataFactory`; `logback-spring.xml` (consola colorida en `!(desa\|prod)`, JSON Logstash a archivo rotativo en `desa\|prod`); `logstash-logback-encoder:8.0` en `keygo-run/pom.xml`; 9 tests unitarios `RequestTracingFilterTest`; plan en `docs/design/TRACING_TELEMETRY.md`; sección §13.4 en `FRONTEND_DEVELOPER_GUIDE.md` |
| 2026-03-31 | **Simplificación de `AppBillingContractController`:** path cambiado a `/api/v1/billing/contracts` (eliminado `{tenantSlug}/{clientId}` del path). `clientAppId` ahora va en el body del POST. Removidas dependencias `tenantRepo`/`clientAppRepo` del controller. `CreateAppContractRequest` agrega campo `clientAppId`. Tests actualizados. |
| 2026-03-31 | **Sincronización modelo v2 Contractor (V1-V18):** JPA entities, repos, puertos, adaptadores, use cases y controllers actualizados. Nuevas entidades: `ContractorEntity`, `PaymentTransactionEntity`, `TenantBillingProfileEntity`, `PaymentMethodEntity`. `ContractStatus.ACTIVATED`→`ACTIVE`. `executeForTenant`→`executeForContractor`. 5 test files actualizados. |
| 2026-03-30 | **Reestructuración total de migraciones Flyway — modelo v2 contractors:** V1-V18 desde cero. Backup en `backup_20260330/`. V3 agrega `contractor_id`+`DELETED`; V10 agrega `subscriber_type`; V11=contractors (nueva); V12=billing_contracts (sin subscriber_*, nuevo modelo); V13=billing_subscriptions (contractor_id); V14=billing_invoices+usage (contractor_id); V17=seed billing plans (escalera completa FREE/PERSONAL/TEAM/BUSINESS/FLEX/ENTERPRISE); V18=seed contractors (keygo_contractor → contractor → acme_contract → acme_sub → tenants acme+demo vinculados). Próxima migración: `V19__...` |
| 2026-03-29 | **Reestructuración total de migraciones Flyway:** V1–V26 (acumulativo con parches) reemplazado por **V1–V17** organizado por dominio. V1=Drop ALL, V2=Foundation, V3=Tenants, V4=ClientApps, V5=TenantUsers (OIDC 5.3 incluido), V6=Memberships, V7=AuthCodes+SigningKeys, V8=Sessions+RefreshTokens, V9=EmailVerifications, V10=BillingCatalog (con `subscriber_type`), V11=BillingContracts (con `verification_code`), V12=BillingSubscriptions, V13=Invoices+UsageCounters, V14=BillingSupport, V15=SeedFoundation, V16=SeedBillingPlatformApp, V17=SeedBillingPlans. Próxima migración: `V18__...` |
| 2026-03-28 | Billing model B-1→B-8: dominio de billing (11 enums, 6 modelos), puertos/use cases catálogo+contratación+suscripción+facturación+uso, entidades JPA (7), repositorios JPA (7), adaptadores (7), mapper, 3 controllers REST (`AppBillingPlanController`, `AppBillingContractController`, `AppBillingSubscriptionController`), 18 ResponseCodes de billing, `KeyGoBillingProperties`, 2 sufijos públicos (`billing-catalog`, `billing-contracts`), 25 tests unitarios nuevos; 89 tests totales pasan |
| 2026-03-28 | Dashboard admin: nuevo `GET /api/v1/admin/platform/dashboard` (`PlatformDashboardController`, `GetPlatformDashboardUseCase`, `PlatformDashboardAdapter`, `PlatformDashboardPort`, `PlatformDashboardResult`, `PlatformDashboardData`); refactorización GROUP BY — 9 métodos `countX(status)` → `Map<K,Long> countX()` eliminando ~16 queries individuales; `PLATFORM_DASHBOARD_RETRIEVED` `ResponseCode` |
| 2026-03-28 | Dashboard endpoints: `ServiceInfoData` extendido con `environment`+`status`; nuevo `GET /api/v1/platform/stats` (`PlatformStatsController`, `GetPlatformStatsUseCase`, `PlatformStatsAdapter`, `PlatformStatsPort`, `PlatformStatsResult`, `PlatformStatsData`); nuevo `PUT /api/v1/tenants/{slug}/activate` (`ActivateTenantUseCase`); 3 nuevos `ResponseCode`: `PLATFORM_STATS_RETRIEVED`, `TENANT_ACTIVATED` |
| 2026-03-27 | Endpoint `GET /api/v1/tenants` — listado paginado de tenants con filtros `status`/`nameLike`: `PagedResult<T>`, `TenantFilter`, `ListTenantsUseCase`, `PagedData<T>`, `TenantJpaRepository+JpaSpecificationExecutor`, `TENANT_LIST_RETRIEVED` |
| 2026-03-23 | Corrección de documentación — Fase 9 marcada como ✅ COMPLETADA: tabla de fases corregida, ROADMAP actualizado (endpoints 21→24, tests 305+→320+, Postman 29→38), IMPLEMENTATION_PLAN.md actualizado con componentes reales |
| 2026-03-23 | Registro con verificación email — `RegisterTenantUserUseCase`, `VerifyEmailUseCase`, `ResendVerificationEmailUseCase`, `EmailVerificationEntity` (V12), `SmtpEmailNotificationAdapter`, `RegistrationController` (3 endpoints públicos), 3 nuevos sufijos en filtro, `lecciones.md` actualizado |
| 2026-03-23 | Fase 8 — Client Credentials grant (M2M): `IssueClientCredentialsTokenUseCase`, rama `client_credentials` en `POST /oauth2/token`, `CLIENT_CREDENTIALS_TOKEN_ISSUED` `ResponseCode`, Postman request |
| 2026-03-22 | Fase 7 — Refresh token (rotación SHA-256), Session, Revocación RFC 7009, UserInfo OIDC §5.3 |
| 2026-03-22 | Reorganización de documentos AI a `docs/ai/` |
| 2026-03-22 | Re-auditoría de inconsistencias — corrección de tablas en singular via V10 |
| 2026-03-22 | Refactorización de docs AI en sub-documentos temáticos |
| 2026-03-22 | Sincronización de documentos de datos con migraciones V1–V9 |
| 2026-03-22 | Fase 6 — Firma de tokens (RS256) + JWKS + OIDC Discovery completados |
| 2026-03-22 | Fase 5 — Flujo OAuth2 Authorization Code + PKCE completado |
| 2026-03-21 | Fase 4 — Memberships y roles por app completados |
| 2026-03-21 | Fase 3 — User identity per tenant completado |
| 2026-03-21 | Fase 2 — Client app model completado |
| 2026-03-21 | Fase 1 — Multitenancy completado |
| 2026-03-21 | Fase 0 — Hardening estructural + CI + Enforcer completados |

Ver historial completo en [`docs/ai/agents-registro.md`](docs/ai/agents-registro.md).

