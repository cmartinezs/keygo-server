# AGENTS — Registro Histórico (pre 2026-03-25)

> Archivo de entradas archivadas de [`agents-registro.md`](agents-registro.md).
> Contiene el historial de implementación de Fases 0–9b y reorganizaciones estructurales tempranas.

---

### [2026-03-24] Refuerzo gobernanza documental — propuestas + trazabilidad cruzada

- `AI_CONTEXT.md`: sección propuestas futuras corregida para registrar en `docs/ai/propuestas.md` + `ROADMAP.md`
- `AGENTS.md`: agregado `docs/ai/propuestas.md` en sub-documentos; nueva sección explícita de propuestas
- Nuevas propuestas trazadas: `T-049`, `T-050`, `F-040`

---

### [2026-03-24] Fase 9b — Perfil de usuario OIDC extendido + endpoints self-service

- **Migración:** `V13__extend_tenant_user_profile.sql` — 6 campos OIDC en `tenant_users`
- **App:** `GetUserProfileUseCase`, `UpdateUserProfileUseCase`
- **API:** `AccountProfileController`, `UserProfileData`, `ResponseCode.USER_PROFILE_RETRIEVED/UPDATED`
- **Filtro:** `account-profile-path-suffix: "/account/profile"` como ruta pública
- **URLs:** `GET /account/profile`, `PATCH /account/profile` — Bearer usuario
- Fase 9b marcada ✅ Done. Próxima migración (en ese momento): `V14__...`

---

### [2026-03-23] Corrección: Fase 9 marcada como ✅ COMPLETADA en docs

- `AGENTS.md`: tabla de fases dividida (Fase 9 ✅ / Fases 10-11 pendientes)
- `ROADMAP.md`: fase actual 8→9; endpoints 21→24, tests 305+→320+
- `docs/design/IMPLEMENTATION_PLAN.md`: Fase 9 completada con componentes reales

---

### [2026-03-23] Fase 9 — Registro de usuarios con verificación de email

- **Dominio:** `EmailVerification` con `create()`, `isExpired()`, `isUsed()`, `markUsed()`
- **App:** puertos `EmailVerificationRepositoryPort`, `EmailNotificationPort`; use cases `RegisterTenantUserUseCase`, `VerifyEmailUseCase`, `ResendVerificationEmailUseCase`
- **Infra:** `SmtpEmailNotificationAdapter`, `spring-boot-starter-mail`
- **Supabase:** `V12__add_email_verifications.sql`, `EmailVerificationEntity`
- **API:** `RegistrationController` (POST /register, POST /verify-email, POST /resend-verification); 7 nuevos `ResponseCode`
- **Filtro:** 3 nuevos sufijos públicos (`register`, `verify-email`, `resend-verification`)
- Postman: carpeta `Registration` con 3 requests

---

### [2026-03-23] Fase 8 — Client Credentials grant (M2M)

- **App:** `IssueClientCredentialsTokenUseCase`, `IssueClientCredentialsTokenCommand/Result`
- **API:** `AuthorizationController` ampliado con branch `client_credentials`; `ResponseCode.CLIENT_CREDENTIALS_TOKEN_ISSUED`
- `sub` = `clientId` (string); sin `id_token` ni `refresh_token`; solo apps `CONFIDENTIAL`
- Postman: request `Exchange Token — client_credentials grant` con `m2mAccessToken`
- `ROADMAP.md`: Fase 8 ✅, T-039 completada

---

### [2026-03-22] Reorganización de documentos AI a docs/ai/

- `AI_CONTEXT.lecciones.md` → `docs/ai/lecciones.md`
- `AI_CONTEXT.propuestas.md` → `docs/ai/propuestas.md`
- `INCONSISTENCIAS.md` → `docs/ai/inconsistencias.md`
- `AGENTS.registro.md` → `docs/ai/agents-registro.md`
- `docs/README.md`: nueva categoría `ai/`; `docs/ai/README.md` creado

---

### [2026-03-22] Re-auditoría inconsistencias — migración V10 para tablas en plural

- `V10__rename_membership_tables_to_plural.sql`: `app_role`→`app_roles`, `membership`→`memberships`, `membership_role`→`membership_roles`
- `AppRoleEntity.java`, `MembershipEntity.java`: `@Table` actualizado
- `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md`: sincronizados V1–V10

---

### [2026-03-22] Fase 7 — Refresh token, Session, Revocación RFC 7009, UserInfo OIDC §5.3

- **Dominio:** `Session`, `RefreshToken` con status machines
- **App:** puertos `SessionRepositoryPort`, `RefreshTokenRepositoryPort`, `AccessTokenVerifierPort`; 5 use cases
- **Infra:** `RsaJwtTokenVerifier`
- **Supabase:** `V11__add_refresh_tokens_and_sessions.sql`
- **API:** `RevocationController` (`POST /oauth2/revoke`), `UserInfoController` (`GET /userinfo`)
- **URLs:** `POST /oauth2/token` (refresh_token grant), `POST /oauth2/revoke`, `GET /userinfo`
- Tests: ~50 nuevos

---

### [2026-03-22] Fase 6 — Firma de tokens y metadata OIDC

- **Dominio:** `SigningKey`, `SigningKeyId`, `SigningKeyStatus`, `SigningKeyAlgorithm`
- **App:** puertos `SigningKeyRepositoryPort`, `TokenSignerPort`, `TokenClaimsFactoryPort`, `JwksBuilderPort`
- **Infra:** `RsaJwtTokenSigner` (Nimbus), `StandardTokenClaimsFactory`, `JwkSetBuilder`; `jacoco.skip` eliminado
- **Supabase:** `V9__add_signing_keys.sql`
- **API:** `JwksController` (`GET /.well-known/jwks.json`), `OidcMetadataController` (`GET /.well-known/openid-configuration`)
- **URLs:** `/.well-known/jwks.json`, `/.well-known/openid-configuration`
- Total: 307 tests ✅

---

### [2026-03-22] Fase 5 — Núcleo OAuth2/OIDC: authorization code flow

- **Dominio:** `AuthorizationCode`, `CodeChallenge`, `ScopeSet`; excepciones PKCE/scope
- **App:** `AuthorizationCodeRepositoryPort`, `ClockPort`; 4 use cases de auth
- **Infra:** `PkceVerifier` (S256 y plain)
- **Supabase:** `V8__add_oauth_authorization_codes.sql`
- **API:** `AuthorizationController` (GET /authorize, POST /login, POST /token); 5 handlers en `GlobalExceptionHandler`
- **Run:** `SystemClockProvider`
- Total: 270+ tests; 23 requests Postman

---

### [2026-03-22] Sincronización de documentos de datos con migraciones V1–V9

- `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md`, `AUTH_FLOW.md` sincronizados
- Regla establecida: nueva migración Flyway → actualizar los 3 docs de datos antes de cerrar
- Sub-documentos AI creados: `lecciones.md`, `propuestas.md`, `agents-registro.md`, `inconsistencias.md`

---

### [2026-03-21] Fase 4 — Memberships y roles por app

- **Dominio:** `Membership`, `AppRole`, `MembershipRole`, `RoleCode`
- **Supabase:** `V7__add_memberships.sql`
- **API:** `TenantMembershipController` (3 endpoints), `TenantAppRoleController` (2 endpoints); 6 `ResponseCode`
- Total: 210+ tests; carpetas Postman `Memberships` + `Roles`

---

### [2026-03-21] Fase 3 — User identity per tenant

- **Dominio:** `TenantUser`, `EmailAddress`, `PasswordHash`, `Username`
- **Supabase:** `V6__add_tenant_users.sql`
- **API:** `TenantUserController` (6 endpoints); 3 `ResponseCode`; `BCryptPasswordHasher`
- Total: 165+ tests; 17 requests Postman

---

### [2026-03-21] Fase 2 — Client app model

- **Dominio:** `ClientApp`, `ClientId`, `ClientType`, `ClientSecret`
- **Supabase:** `V5__add_client_apps.sql`
- **API:** `TenantClientAppController` (5 endpoints + rotate-secret); 5 `ResponseCode`
- 11 requests Postman

---

### [2026-03-21] Fase 1 — Multitenancy

- **Dominio:** `Tenant`, `TenantId`, `TenantSlug`, `TenantStatus`
- **Supabase:** `V4__add_tenants.sql`
- **API:** `TenantController` (3 endpoints); 3 `ResponseCode`
- Postman: carpeta `Tenants` con 3 requests

---

### [2026-03-21] Fase 0 — Hardening estructural

- CI Pipeline: `.github/workflows/ci.yml`
- Maven Enforcer Plugin: Java 21+, Maven 3.9+, UTF-8
- Convenciones: `docs/development/CODE_STYLE.md`
- Bug T-001 corregido: `BootstrapAdminKeyFilter` usa `getServletPath()`
- SpringDoc 3.0.1: Swagger UI en `/keygo-server/swagger-ui/index.html`
- Postman collection + environment creados

---

### [2026-03-17] Reorganización inicial de paquetes por feature

- Paquetes reorganizados de organización técnica genérica a feature-first en `keygo-api`, `keygo-app`, `keygo-run`, `keygo-supabase`

---

### [2026-03-17] Retroalimentación obligatoria establecida como regla

- `AGENTS.md` agregado como documento obligatorio
- Regla de retroalimentación de docs AI establecida en los 4 archivos de instrucciones
