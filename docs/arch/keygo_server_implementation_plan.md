# KEYGO Server Implementation Plan

## 1. Objetivo

Este documento define el **plan de implementación** de Key-go Server a partir de la arquitectura, backlog, modelo de dominio, superficie API y estructura de proyecto ya establecidos.

Su propósito es aterrizar:

- el orden técnico real de construcción,
- qué módulos tocar primero,
- qué componentes crear en cada etapa,
- dependencias entre entregables,
- y una secuencia pragmática para llegar a un MVP funcional sin degradar la arquitectura.

La idea no es construir “todo a la vez”, sino cerrar primero el **núcleo autenticable y operable** del sistema.

---

## 2. Estrategia general de implementación

La implementación debe seguir este criterio:

1. **primero fundamentos estructurales**,  
2. **luego el núcleo OAuth/OIDC usable**,  
3. **después administración de tenant y apps**,  
4. **luego memberships y roles**,  
5. **y finalmente endurecimiento operativo y soporte**.

### Regla principal
No construir UI, endpoints admin ni integraciones accesorias antes de cerrar correctamente:

- tenant resolution,
- user model,
- client app model,
- Authorization Code + PKCE,
- token issuance,
- y seguridad base.

---

## 3. Línea base técnica

## 3.1. Stack asumido

- Java 21
- Spring Boot
- Maven multi-module
- PostgreSQL / Supabase Postgres
- Flyway
- JWT asimétrico
- Testcontainers

## 3.2. Suposiciones de estructura existente

Se asume que ya existe esta base modular:

- `keygo-domain`
- `keygo-app`
- `keygo-api`
- `keygo-infra`
- `keygo-supabase`
- `keygo-run`

El plan no parte de cero: parte desde tu estructura actual y la ordena hacia el MVP.

---

## 4. Orden de implementación recomendado

## Fase 0. Endurecimiento estructural inicial ✅ COMPLETADA (2026-03-21)

### Objetivo
Asegurar que la base técnica no se degrade antes de empezar a meter lógica real.

### Módulos involucrados
- `keygo-domain`
- `keygo-app`
- `keygo-api`
- `keygo-run`
- `keygo-supabase`

### Trabajo

#### 0.1. Validar dependencias Maven entre módulos ✅
- `keygo-api` → `keygo-app` (sin dependencias de persistencia directas)
- `keygo-app` → `keygo-domain` + `keygo-common` (sin Spring)
- `keygo-run` ensambla: `keygo-api` + `keygo-infra` + `keygo-supabase`

#### 0.2. Reorganizar paquetes críticos ✅
Completado el 2026-03-17:
- `keygo-api`: `platform/controller/`, `platform/response/`, `shared/`, `error/`
- `keygo-app`: `platform/port/`, `platform/usecase/`
- `keygo-supabase`: `user/entity/`, `user/repository/`, `membership/entity/`, `membership/repository/`

#### 0.3. Definir convención de nombres ✅
Convenciones establecidas y documentadas en `docs/keygo-server/CODE_STYLE.md`:
- `<Acción><Entidad>UseCase` — caso de uso
- `<Entidad>Provider` / `<Entidad>Port` — puerto OUT
- `<Entidad>Controller` — controlador REST
- `<Entidad>Data` / `<Entidad>Response` — DTO de salida
- `<Entidad>Entity` — entidad JPA
- `<Entidad>Repository` / `<Entidad>RepositoryAdapter` — persistencia

#### 0.4. Configurar base de calidad ✅
- **Pipeline CI**: `.github/workflows/ci.yml` con `./mvnw test` + `./mvnw clean package` en push/PR a `main`/`develop`
- **Format/lint**: Convención documentada en `docs/keygo-server/CODE_STYLE.md` (2 espacios, Google Java Style); enforcement automático como T-023 en ROADMAP
- **Tests unitarios**: 80+ tests con JUnit 5 + Mockito + AssertJ
- **Infraestructura de tests de integración**: Testcontainers configurado en `keygo-supabase/pom.xml`; tests reales pendientes a Fase 1+
- **Perfiles por ambiente**: `supabase`, `local` activos; separación `dev`/`prod` como T-014 en ROADMAP
- **Maven Enforcer Plugin**: valida Java 21+, Maven 3.9+, UTF-8, sin dependencias duplicadas

### Resultado esperado ✅ ALCANZADO
Base estructural limpia, sin deuda obvia de organización, lista para agregar negocio.

> **Siguiente fase:** Fase 1 — Núcleo de multitenancy

---

## Fase 1. Núcleo de multitenancy ✅ COMPLETADA (2026-03-21)

### Objetivo
Introducir tenant como concepto obligatorio antes de cualquier flujo funcional serio.

### Módulos a tocar primero
- `keygo-domain`
- `keygo-app`
- `keygo-supabase`
- `keygo-api`
- `keygo-run`

### Componentes a crear

## 1.1. Dominio ✅

### `tenant/model`
- `Tenant` ✅
- `TenantId` ✅
- `TenantSlug` ✅
- `TenantStatus` ✅

### `tenant/exception`
- `TenantNotFoundException` ✅
- `TenantSuspendedException` ✅

## 1.2. Aplicación ✅

### Puertos
- `TenantRepositoryPort` ✅

### Casos de uso mínimos
- `CreateTenantUseCase` ✅
- `GetTenantBySlugUseCase` ✅
- `SuspendTenantUseCase` ✅

### Comando
- `CreateTenantCommand` ✅

### Contexto
- `TenantContextHolder` (ThreadLocal, sin Spring) ✅

## 1.3. Persistencia ✅

### En `keygo-supabase`
- `TenantEntity` ✅ (renombrado de `TenantJpaEntity`)
- `TenantJpaRepository` ✅
- `TenantPersistenceMapper` ✅
- `TenantRepositoryAdapter` ✅

### Migración inicial
- tabla `tenants` — `V4__add_tenants.sql` ✅

## 1.4. API ✅

### Control plane básico
- `PlatformTenantController` ✅
  - `POST /api/v1/tenants` — crear tenant ✅
  - `GET /api/v1/tenants/{slug}` — consultar tenant ✅
  - `PUT /api/v1/tenants/{slug}/suspend` — suspender tenant ✅

### DTOs
- `CreateTenantRequest` (record con `@Valid`) ✅
- `TenantData` (Lombok builder) ✅

### Response codes
- `TENANT_CREATED`, `TENANT_RETRIEVED`, `TENANT_SUSPENDED` ✅

### Error handlers en `GlobalExceptionHandler`
- `TenantNotFoundException` → 404 ✅
- `TenantSuspendedException` → 403 ✅

### Seguridad / contexto
- `TenantResolutionFilter` (header `X-Tenant-Slug` → valida tenant → guarda en `TenantContextHolder`) ✅

### Tests
- 39 tests unitarios nuevos: 28 domain, 8 app, 4 api, 4 supabase, 4 run ✅

### Resultado esperado ✅ ALCANZADO
Ya se puede:
- crear tenant,
- consultar tenant,
- resolver tenant por request,
- bloquear operación si tenant está suspendido.

> **Siguiente fase:** Fase 2 — Modelo de aplicaciones cliente

---

## Fase 2. Modelo de aplicaciones cliente ✅ COMPLETADA (2026-03-21)

### Objetivo
Permitir que el tenant registre apps para autenticarse vía Key-go.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-supabase`
- `keygo-api`
- `keygo-run`

### Componentes creados

## 2.1. Dominio ✅

### `clientapp/model`
- `ClientApp` ✅
- `ClientAppId` ✅
- `ClientId` ✅
- `ClientType` ✅ (PUBLIC, CONFIDENTIAL)
- `ClientAppStatus` ✅ (ACTIVE, SUSPENDED, PENDING)
- `AllowedGrant` ✅ (AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN, IMPLICIT)
- `AllowedScope` ✅
- `RedirectUri` ✅
- `AccessPolicy` ✅

### `clientapp/exception`
- `ClientAppNotFoundException` ✅
- `InvalidRedirectUriException` ✅
- `UnsupportedGrantTypeException` ✅

## 2.2. Aplicación ✅

### Puertos
- `ClientAppRepositoryPort` ✅
- `ClientSecretEncoderPort` ✅
- `ClientCredentialGeneratorPort` ✅

### Comandos
- `CreateClientAppCommand` ✅
- `UpdateClientAppCommand` ✅

### Result records
- `CreateClientAppResult` ✅ (devuelve rawSecret solo en creación)
- `RotateSecretResult` ✅ (devuelve newRawSecret solo al rotar)

### Casos de uso mínimos
- `CreateClientAppUseCase` ✅
- `ListClientAppsUseCase` ✅
- `GetClientAppUseCase` ✅
- `UpdateClientAppUseCase` ✅
- `RotateClientSecretUseCase` ✅
- `ResolveClientAppForAuthorizationUseCase` ✅

## 2.3. Persistencia ✅

### Entidades JPA (`keygo-supabase`)
- `ClientAppEntity` ✅
- `ClientRedirectUriEntity` ✅
- `ClientAllowedGrantEntity` ✅
- `ClientAllowedScopeEntity` ✅
- `ClientAppJpaRepository` ✅
- `ClientAppPersistenceMapper` ✅
- `ClientAppRepositoryAdapter` ✅

### Migraciones
- `V5__add_client_apps.sql` ✅ — tablas: `client_apps`, `client_redirect_uris`, `client_allowed_grants`, `client_allowed_scopes`

## 2.4. API ✅

### Controller
- `TenantClientAppController` ✅

### Endpoints implementados
- `POST /api/v1/tenants/{slug}/apps` ✅ — crear app (devuelve clientId + secret)
- `GET /api/v1/tenants/{slug}/apps` ✅ — listar apps
- `GET /api/v1/tenants/{slug}/apps/{clientId}` ✅ — obtener app
- `PUT /api/v1/tenants/{slug}/apps/{clientId}` ✅ — actualizar app
- `POST /api/v1/tenants/{slug}/apps/{clientId}/rotate-secret` ✅ — rotar secret

### DTOs
- `CreateClientAppRequest` ✅
- `UpdateClientAppRequest` ✅
- `ClientAppData` ✅
- `ClientAppSecretData` ✅

### Response codes
- `CLIENT_APP_CREATED`, `CLIENT_APP_RETRIEVED`, `CLIENT_APP_LIST_RETRIEVED`, `CLIENT_APP_UPDATED`, `CLIENT_APP_SECRET_ROTATED` ✅

### Error handlers en `GlobalExceptionHandler`
- `ClientAppNotFoundException` → 404 ✅
- `InvalidRedirectUriException` → 400 ✅
- `UnsupportedGrantTypeException` → 400 ✅

## 2.5. Run ✅
- `BCryptClientSecretEncoder` (`ClientSecretEncoderPort`) ✅
- `UuidClientCredentialGenerator` (`ClientCredentialGeneratorPort`) ✅
- dependencia `spring-security-crypto` ✅
- 8 `@Bean` nuevos en `ApplicationConfig` ✅

### Tests
- ~40 tests unitarios: domain (36), app (16), api (7), supabase (2) ✅

### Resultado alcanzado ✅
El admin del tenant ya puede:
- registrar apps (PUBLIC o CONFIDENTIAL),
- configurar redirect URIs,
- definir grants y scopes,
- obtener `client_id` generado automáticamente,
- obtener el `client_secret` en texto plano una sola vez (CONFIDENTIAL),
- rotar el secret cuando sea necesario.

> **Siguiente fase:** Fase 3 — Identidad de usuario

---

## Fase 3. Identidad de usuario ✅ COMPLETADA (2026-03-21)

### Objetivo
Cerrar la identidad humana única por tenant.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-supabase`
- `keygo-api`
- `keygo-run`

### Componentes creados

## 3.1. Dominio ✅

### `user/model`
- `User` ✅
- `UserId` ✅
- `EmailAddress` ✅
- `Username` ✅
- `PasswordHash` ✅ (`toString()` retorna `"PasswordHash[REDACTED]"` por seguridad)
- `UserStatus` ✅ (ACTIVE, SUSPENDED, PENDING)

### `user/exception`
- `UserNotFoundException` ✅
- `UserSuspendedException` ✅
- `InvalidCredentialsException` ✅
- `DuplicateUserException` ✅

## 3.2. Aplicación ✅

### Puertos
- `UserRepositoryPort` ✅
- `PasswordHasherPort` ✅

### Comandos
- `CreateUserCommand` ✅
- `UpdateUserCommand` ✅
- `ResetUserPasswordCommand` ✅

### Casos de uso
- `CreateUserUseCase` ✅ — valida tenant activo, unicidad de email y username, hashea password
- `GetUserUseCase` ✅ — busca por UUID dentro del tenant
- `ListUsersUseCase` ✅ — lista todos los usuarios del tenant
- `UpdateUserUseCase` ✅ — actualiza firstName/lastName
- `ResetUserPasswordUseCase` ✅ — admin resets password
- `ValidateUserCredentialsUseCase` ✅ — acepta email o username + password con fallback automático

## 3.3. Persistencia ✅

### En `keygo-supabase`
- `TenantUserEntity` ✅ (restricciones únicas compuestas por tenant)
- `TenantUserJpaRepository` ✅
- `UserPersistenceMapper` ✅
- `UserRepositoryAdapter` ✅

### Migración
- `V6__add_tenant_users.sql` ✅ — tabla `tenant_users` con FK → tenants ON DELETE CASCADE

## 3.4. API ✅

### Controller
- `TenantUserController` ✅ (`/api/v1/tenants/{tenantSlug}/users`)

### Endpoints implementados
- `POST /api/v1/tenants/{slug}/users` ✅ — crear usuario (201)
- `GET /api/v1/tenants/{slug}/users` ✅ — listar usuarios (200)
- `GET /api/v1/tenants/{slug}/users/{userId}` ✅ — obtener usuario (200)
- `PUT /api/v1/tenants/{slug}/users/{userId}` ✅ — actualizar usuario (200)
- `POST /api/v1/tenants/{slug}/users/{userId}/reset-password` ✅ — resetear contraseña (200)
- `POST /api/v1/tenants/{slug}/users/validate-credentials` ✅ — validar credenciales (200)

### DTOs
- `CreateUserRequest`, `UpdateUserRequest`, `ResetPasswordRequest`, `ValidateCredentialsRequest` ✅
- `UserData` ✅ (sin `passwordHash`)

### Response codes
- `USER_CREATED`, `USER_RETRIEVED`, `USER_LIST_RETRIEVED`, `USER_UPDATED`, `USER_PASSWORD_RESET`, `CREDENTIALS_VALID` ✅

### Error handlers en `GlobalExceptionHandler`
- `UserNotFoundException` → 404 ✅
- `UserSuspendedException` → 403 ✅
- `DuplicateUserException` → 409 ✅
- `InvalidCredentialsException` → 401 ✅

## 3.5. Run ✅
- `BCryptPasswordHasher` (`PasswordHasherPort`) ✅
- 7 `@Bean` nuevos en `ApplicationConfig` ✅
- `usersGroup()` en `OpenApiConfig` (grupo `4-users`) ✅

### Tests
- ~30 tests unitarios: domain (11), app (14), api (6), supabase (3) ✅

### Resultado alcanzado ✅
El admin del tenant ya puede:
- crear usuarios con email y username únicos por tenant,
- listar, consultar y actualizar usuarios,
- resetear contraseñas,
- validar credenciales (email o username + password).

> **Siguiente fase:** Fase 4 — Memberships y roles por app

---

## Fase 4. Memberships y roles por app ✅ COMPLETADA (2026-03-21)

### Objetivo
Modelar correctamente el acceso del usuario a una app concreta.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-supabase`
- `keygo-api`

### Componentes creados

## 4.1. Dominio ✅

### `membership/model`
- `Membership` ✅
- `MembershipId` ✅
- `MembershipStatus` ✅
- `AppRole` ✅
- `AppRoleId` ✅
- `RoleCode` ✅
- `MembershipRole` ✅ (value object)

### `membership/exception`
- `MembershipNotFoundException` ✅
- `MembershipInactiveException` ✅
- `InvalidRoleAssignmentException` ✅

## 4.2. Aplicación ✅

### Puertos
- `MembershipRepositoryPort` ✅
- `AppRoleRepositoryPort` ✅

### Comandos
- `CreateMembershipCommand` ✅

### Casos de uso
- `CreateMembershipUseCase` ✅
- `RevokeMembershipUseCase` ✅
- `ListMembershipsUseCase` ✅
- `ListAppRolesUseCase` ✅

## 4.3. Persistencia ✅
- `MembershipEntity` ✅
- `AppRoleEntity` ✅
- `MembershipJpaRepository` ✅
- `AppRoleJpaRepository` ✅
- `MembershipPersistenceMapper` ✅
- `MembershipRepositoryAdapter` ✅
- `AppRoleRepositoryAdapter` ✅

### Migraciones
- `V7__add_memberships.sql` ✅
  - tabla `app_role`
  - tabla `membership`
  - tabla `membership_role`

## 4.4. API ✅
- `TenantMembershipController` ✅
  - `POST /api/v1/tenants/{slug}/memberships` — crear membresía (201) ✅
  - `GET /api/v1/tenants/{slug}/memberships?userId=...&clientAppId=...` — listar (200) ✅
  - `DELETE /api/v1/tenants/{slug}/memberships/{membershipId}` — revocar (200) ✅
- `TenantAppRoleController` ✅
  - `POST /api/v1/tenants/{slug}/apps/{clientAppId}/roles` — crear rol (201) ✅
  - `GET /api/v1/tenants/{slug}/apps/{clientAppId}/roles` — listar roles (200) ✅

### DTOs
- `CreateMembershipRequest` ✅
- `CreateAppRoleRequest` ✅
- `MembershipData` ✅
- `AppRoleData` ✅

### Response codes
- `MEMBERSHIP_CREATED`, `MEMBERSHIP_RETRIEVED`, `MEMBERSHIP_LIST_RETRIEVED`, `MEMBERSHIP_REVOKED`, `MEMBERSHIP_SUSPENDED` ✅
- `ROLE_CREATED`, `ROLE_RETRIEVED`, `ROLE_LIST_RETRIEVED`, `ROLE_UPDATED`, `ROLE_DELETED`, `ROLE_ASSIGNED` ✅

### Error handlers en `GlobalExceptionHandler`
- `MembershipNotFoundException` → 404 ✅
- `MembershipInactiveException` → 403 ✅
- `InvalidRoleAssignmentException` → 400 ✅

## 4.5. Run ✅
- 4 `@Bean` nuevos en `ApplicationConfig` ✅

### Tests
- Tests unitarios pendientes (serán agregados en commit posterior)

### Resultado esperado ✅
Ya puedes:
- suscribir usuarios a apps,
- revocar acceso,
- asignar roles por app,
- y listar memberships filtradas por usuario o app.

---

## Fase 5. Núcleo OAuth2/OIDC: authorization flow ✅ COMPLETADA (2026-03-22)

### Objetivo
Cerrar el flujo central del producto: login Hosted + Authorization Code + PKCE.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-infra`
- `keygo-supabase`
- `keygo-api`
- `keygo-run`

### Componentes creados

## 5.1. Dominio ✅

### `auth/model`
- `AuthorizationCode` ✅
- `AuthorizationCodeId` ✅ (record con `.value()`)
- `AuthorizationCodeStatus` ✅ (enum: ISSUED, CONSUMED, EXPIRED, REVOKED)
- `CodeChallenge` ✅ (record — S256 y plain PKCE)
- `ScopeSet` ✅

### `auth/exception`
- `InvalidAuthorizationCodeException` ✅
- `AuthorizationCodeExpiredException` ✅
- `InvalidPkceVerificationException` ✅
- `ScopeNotGrantedException` ✅

## 5.2. Aplicación ✅

### Puertos
- `AuthorizationCodeRepositoryPort` ✅
- `ClockPort` ✅

### Comandos
- `InitiateAuthorizationCommand` ✅
- `AuthenticateUserCommand` ✅
- `IssueAuthorizationCodeCommand` ✅
- `ExchangeAuthorizationCodeCommand` ✅

### Results
- `AuthorizationInitiatedResult` ✅
- `AuthorizationCodeIssuedResult` ✅
- `ExchangeAuthorizationCodeResult` ✅

### Casos de uso
- `InitiateAuthorizationUseCase` ✅
- `AuthenticateUserForAuthorizationUseCase` ✅
- `IssueAuthorizationCodeUseCase` ✅
- `ExchangeAuthorizationCodeUseCase` ✅

## 5.3. Infraestructura ✅
- `PkceVerifier` ✅ (validación S256 y plain PKCE)

## 5.4. Persistencia ✅

### En `keygo-supabase`
- `AuthorizationCodeEntity` ✅ (JPA)
- `AuthorizationCodeJpaRepository` ✅ (Spring Data)
- `AuthorizationCodePersistenceMapper` ✅
- `AuthorizationCodeRepositoryAdapter` ✅

### Migración
- `V8__add_oauth_authorization_codes.sql` ✅ — tabla `authorization_codes` con índices

## 5.5. API ✅

### Controller
- `AuthorizationController` ✅ (3 endpoints)

### Endpoints implementados
- `GET /api/v1/tenants/{slug}/oauth2/authorize` ✅ — iniciar autorización
- `POST /api/v1/tenants/{slug}/account/login` ✅ — autenticación del usuario
- `POST /api/v1/tenants/{slug}/oauth2/token` ✅ — intercambio authorization_code → tokens

### DTOs request
- `AuthorizationRequest` ✅
- `LoginRequest` ✅
- `TokenRequest` ✅

### DTOs response
- `AuthorizationInitiatedData` ✅
- `AuthorizationCodeData` ✅
- `LoginData` ✅

### Response codes
- `AUTHORIZATION_INITIATED`, `AUTHORIZATION_CODE_ISSUED`, `AUTHORIZATION_CODE_EXCHANGED`, `LOGIN_SUCCESSFUL` ✅

### Error handlers en `GlobalExceptionHandler`
- `InvalidAuthorizationCodeException` → 400 ✅
- `AuthorizationCodeExpiredException` → 400 ✅
- `InvalidPkceVerificationException` → 400 ✅
- `ScopeNotGrantedException` → 403 ✅
- (+ handler genérico para flujo OAuth2 estándar) ✅

## 5.6. Run ✅
- `SystemClockProvider` (`ClockPort`) ✅
- 6 `@Bean` nuevos en `ApplicationConfig` ✅

### Tests
- ~60 tests unitarios nuevos. Total proyecto: **270+ tests** (todos pasan) ✅

### Postman
- Carpeta `🔐 OAuth2 Authorization` con 3 requests (authorize, login, token exchange) ✅
- **23 requests totales** en 6 carpetas ✅

### Resultado alcanzado ✅
Ya tienes el corazón de Key-go funcionando:
- una app inicia el flujo OAuth2 Authorization Code + PKCE,
- el usuario se autentica en Key-go (login hosted),
- se genera un authorization code de un solo uso,
- y se canjea por tokens (access_token + id_token placeholder).

> **Siguiente fase:** Fase 6 — Firma de tokens y metadata OIDC

---

## Fase 6. Firma de tokens y metadata OIDC

### Objetivo
Emitir tokens interoperables y validables externamente.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-infra`
- `keygo-api`
- `keygo-supabase`

### Componentes a crear

## 6.1. Dominio
- `SigningKey`
- `SigningKeyStatus`

## 6.2. Aplicación
### Puertos
- `SigningKeyRepositoryPort`
- `TokenSignerPort`
- `TokenClaimsFactoryPort`

### Casos de uso
- `IssueAccessTokenUseCase`
- `IssueIdTokenUseCase`
- `GetJwksUseCase`
- `GetOidcConfigurationUseCase`

## 6.3. Infraestructura
- implementación JWT signer
- construcción de claims
- publicación de JWKS

## 6.4. Persistencia
- `SigningKeyJpaEntity`
- `SigningKeyJpaRepository`

### Migración
- tabla `signing_key`

## 6.5. API
- `OidcMetadataController`
- `JwksController`

### Endpoints
- `GET /{tenant}/.well-known/openid-configuration`
- `GET /{tenant}/.well-known/jwks.json`

### Resultado esperado
Los tokens emitidos por Key-go ya pueden ser validados por terceros mediante estándar OIDC/JWKS.

---

## Fase 7. Refresh token y userinfo

### Objetivo
Completar sesiones renovables y claims consultables.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-infra`
- `keygo-supabase`
- `keygo-api`

### Componentes a crear

## 7.1. Dominio
- `RefreshToken`
- `RefreshTokenStatus`
- `Session`
- `SessionStatus`

## 7.2. Aplicación
### Puertos
- `RefreshTokenRepositoryPort`
- `SessionRepositoryPort`

### Casos de uso
- `RotateRefreshTokenUseCase`
- `RevokeTokenUseCase`
- `GetUserInfoUseCase`
- `OpenSessionUseCase`
- `TerminateSessionUseCase`

## 7.3. Persistencia
- `RefreshTokenJpaEntity`
- `SessionJpaEntity`
- repositorios y adapters

### Migraciones
- tabla `refresh_token`
- tabla `session`

## 7.4. API
- ampliar `TokenController`
- `UserInfoController`
- `RevocationController`

### Endpoints
- `POST /{tenant}/oauth2/token` *(refresh token branch)*
- `POST /{tenant}/oauth2/revoke`
- `GET /{tenant}/userinfo`

### Resultado esperado
Ya existe:
- refresh token con rotación,
- revoke,
- userinfo,
- y trazabilidad básica de sesión.

---

## Fase 8. Client Credentials

### Objetivo
Habilitar machine-to-machine y cerrar el segundo grant esencial del MVP.

### Módulos principales
- `keygo-app`
- `keygo-infra`
- `keygo-api`

### Componentes a crear

### Casos de uso
- `AuthenticateClientUseCase`
- `IssueClientAccessTokenUseCase`

### API
- extender `TokenController` con rama `client_credentials`

### Resultado esperado
Una app confidential puede autenticarse y obtener token técnico sin usuario final.

---

## Fase 9. Self-service de identidad

### Objetivo
Dar funciones mínimas de identidad al usuario final sin depender del tenant admin.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-infra`
- `keygo-api`
- `keygo-supabase`

### Componentes a crear

### Casos de uso
- `RequestPasswordResetUseCase`
- `ConfirmPasswordResetUseCase`
- `ChangePasswordUseCase`

### Puertos
- `PasswordResetTokenRepositoryPort`
- `MailSenderPort`

### API
- `AccountRecoveryController`

### Endpoints
- `POST /{tenant}/account/forgot-password`
- `POST /{tenant}/account/reset-password`
- `POST /{tenant}/account/change-password`

### Resultado esperado
El usuario puede recuperar o cambiar su contraseña de forma estándar.

---

## Fase 10. Control plane y soporte

### Objetivo
Cerrar operación básica del SaaS por parte del equipo Key-go.

### Módulos principales
- `keygo-domain`
- `keygo-app`
- `keygo-api`
- `keygo-supabase`

### Componentes a crear

### Casos de uso
- `ListTenantsUseCase`
- `UpdateTenantStatusUseCase`
- `GrantSupportAccessUseCase`
- `GetPlatformAuditUseCase`

### API
- `PlatformTenantController`
- `PlatformAuditController`
- `PlatformSupportController`

### Resultado esperado
El equipo Key-go puede:
- operar tenants,
- auditar globalmente,
- y acceder a soporte controlado.

---

## Fase 11. Auditoría, seguridad operacional y hardening

### Objetivo
No dejar el MVP vulnerable o ciego operativamente.

### Módulos principales
- todos, con foco en `keygo-infra`, `keygo-api`, `keygo-app`

### Trabajo

#### 11.1. Auditoría de eventos críticos
Eventos mínimos:
- tenant creado/suspendido
- app creada/secret rotado
- usuario creado/suspendido
- membership creada/revocada
- login exitoso/fallido
- code emitido/consumido
- token emitido/revocado

#### 11.2. Rate limiting
Aplicar al menos en:
- login submit
- token endpoint

#### 11.3. Error handling consistente
- envelope JSON consistente en admin APIs
- errores OAuth estándar donde corresponda

#### 11.4. Tests críticos de seguridad
- redirect URI exacta
- code single-use
- refresh token rotation
- tenant isolation

### Resultado esperado
El producto ya es defendible para pruebas reales con terceros.

---

## 5. Orden de clases y componentes a crear primero

Si quieres una guía todavía más concreta, este sería el orden base de creación:

### Primer bloque
1. `Tenant`
2. `TenantRepositoryPort`
3. `TenantJpaEntity`
4. `TenantRepositoryAdapter`
5. `TenantResolver`
6. `PlatformTenantController`

### Segundo bloque
7. `ClientApp`
8. `ClientAppRepositoryPort`
9. `ClientAppJpaEntity`
10. `ClientAppRepositoryAdapter`
11. `TenantAdminAppController`

### Tercer bloque
12. `User`
13. `UserRepositoryPort`
14. `UserJpaEntity`
15. `UserRepositoryAdapter`
16. `PasswordHasherPort`
17. `TenantAdminUserController`

### Cuarto bloque
18. `Membership`
19. `AppRole`
20. repositorios/entidades/adapters asociados
21. `TenantAdminMembershipController`

### Quinto bloque
22. `AuthorizationCode`
23. `AuthorizationController`
24. `AccountController`
25. `TokenController`
26. `SigningKey`
27. `OidcMetadataController`
28. `JwksController`

Este orden minimiza retrabajo.

---

## 6. Dependencias entre entregables

### Dependencia fuerte
No tiene sentido hacer:
- `/oauth2/authorize`
- `/oauth2/token`

si todavía no existen bien:
- tenant,
- client app,
- user,
- membership.

### Dependencia importante
No tiene sentido hacer:
- refresh tokens,
- support access,
- auditoría refinada,

si todavía no está sólido el authorization flow.

### Dependencia práctica
La UI admin debería empezar a integrarse recién cuando:
- CRUD apps,
- CRUD users,
- memberships,

ya existan y estén razonablemente estables.

---

## 7. Plan sugerido por sprint

## Sprint 0 ✅ COMPLETADO (2026-03-21)
- Fase 0 completa ✅
- Fase 1 completa ✅
- Fase 2 completa ✅

## Sprint 1 ✅ COMPLETADO (2026-03-21)
- Fase 3 completa ✅

## Sprint 2 ✅ COMPLETADO (2026-03-21)
- Fase 4 completa ✅

## Sprint 3 ✅ COMPLETADO (2026-03-22)
- Fase 5 completa ✅

## Sprint 4
- completar Fase 6
- completar Fase 7

## Sprint 5
- completar Fase 8
- completar Fase 9

## Sprint 6
- completar Fase 10
- completar Fase 11

Este orden asume un MVP serio, no una demo rápida. Si necesitas cortar antes, puedes parar al final de Sprint 4 con un MVP ya bastante usable.

---

## 8. Corte recomendado de MVP funcional real

Si necesitas cortar la implementación en un punto que ya permita probar el producto de extremo a extremo, el corte más inteligente es:

### MVP Core
- Fase 0
- Fase 1
- Fase 2
- Fase 3
- Fase 4
- Fase 5
- Fase 6
- Fase 7

Eso te deja con:
- multi-tenant,
- apps,
- usuarios,
- memberships,
- login estándar,
- JWT firmados,
- JWKS,
- refresh token,
- userinfo,
- administración básica.

### MVP+ comercial
Agregar luego:
- Fase 8
- Fase 9
- Fase 10
- Fase 11

---

## 9. Riesgos técnicos a vigilar durante implementación

### Riesgo 1
Meter demasiada lógica en controllers.

### Riesgo 2
Permitir que `keygo-api` hable directo con persistencia.

### Riesgo 3
Meter clases comodín en `common`.

### Riesgo 4
No modelar bien el tenant context y luego tener fugas de aislamiento.

### Riesgo 5
Intentar implementar demasiadas features enterprise antes de cerrar el flujo estándar de login.

### Riesgo 6
Volver a meter el concepto de “app token previo” en vez de mantener el flujo estándar OAuth/OIDC.

---

## 10. Checklist de definición de avance por fase

Cada fase debería considerarse cerrada sólo si cumple:

### Diseño
- clases base creadas en el módulo correcto
- nombres coherentes con convención

### Persistencia
- migraciones listas
- entidades persistentes alineadas con dominio

### Aplicación
- casos de uso funcionando
- puertos definidos claramente

### API
- endpoint expuesto
- payload validado
- errores consistentes

### Testing
- unit tests donde corresponde
- integration tests en persistencia y flujo crítico

---

## 11. Decisión ejecutiva final

La implementación de Key-go no debe arrancar “por la UI” ni “por endpoints sueltos”.

Debe avanzar en esta lógica:

> **tenant → client app → user → membership → authorization flow → tokens → admin/support/hardening**

Ese orden protege la arquitectura, evita retrabajo y asegura que el MVP nazca con el núcleo correcto.

---

## 12. Próximo paso recomendado

Con este plan ya definido, el siguiente paso más útil no es otro documento general, sino algo operativo y concreto. Tienes dos buenas opciones:

1. **`KEYGO_SERVER_FIRST_ITERATION_TASKS.md`**  
   Lista accionable de tareas iniciales, casi como tablero técnico.

2. **`KEYGO_SERVER_MODULE_BOOTSTRAP_GUIDE.md`**  
   Guía de qué clases/paquetes crear primero en cada módulo, con orden muy práctico.

La mejor secuencia ahora sería:

**Implementation Plan → First Iteration Tasks → código.**

