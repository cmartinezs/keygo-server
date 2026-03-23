# AGENTS — Registro de Cambios

> Sub-documento de [`AGENTS.md`](../../AGENTS.md).
>
> Historial cronológico de actualizaciones al quick-start: módulos, comandos, patrones y URLs.
> Cada entrada corresponde a un cambio que afecta la estructura del repo, los patrones de implementación
> o los flujos de referencia rápida.
>
> **⚠️ Regla de actualización:** Agregar entrada aquí cada vez que cambie la estructura de módulos,
> comandos, patrones o URLs de referencia rápida. No requiere orden explícita del usuario.

---

## Formato de entrada

```markdown
### [YYYY-MM-DD] Descripción del cambio
- Bullet con detalle
```

---

## Registro de cambios

### [2026-03-22] Reorganización de documentos AI a docs/ai/

Se movieron los sub-documentos de contexto AI desde la raíz a `docs/ai/` para mantener la raíz limpia.

**Archivos movidos:**
- `AI_CONTEXT.lecciones.md` → `docs/ai/lecciones.md`
- `AI_CONTEXT.propuestas.md` → `docs/ai/propuestas.md`
- `INCONSISTENCIAS.md` → `docs/ai/inconsistencias.md`
- `INCONSISTENCIAS.datos.md` → `docs/ai/inconsistencias-datos.md`
- `AGENTS.registro.md` → `docs/ai/agents-registro.md`

**Archivos raíz actualizados** (ahora contienen info general + enlaces a `docs/ai/`):
- `AI_CONTEXT.md` — links actualizados a `docs/ai/`
- `AGENTS.md` — links actualizados a `docs/ai/`
- `CLAUDE.md` — tabla de referencias actualizada
- `.github/copilot-instructions.md` — tabla de referencias actualizada

**Otros cambios:**
- `docs/README.md` — nueva categoría `🤖 ai/` agregada
- `scripts/check-ai-docs.sh` — apunta a `docs/ai/lecciones.md` y `docs/ai/agents-registro.md`
- `docs/ai/README.md` — nuevo índice de la categoría AI

---

### [2026-03-22] Re-auditoría de inconsistencias — corrección de tablas en singular via migración V10

Re-revisión de las 12 inconsistencias marcadas como "resueltas" en `docs/ai/inconsistencias-datos.md`. Criterio aplicado: **la documentación manda** para convenciones de nomenclatura; la implementación manda cuando hay razón técnica clara.

**Hallazgo:** Las tablas `app_role`, `membership`, `membership_role` (V7) estaban en singular, violando la convención PostgreSQL de nombres en plural. La corrección anterior solo actualizó los docs para aceptar el singular; ahora se corrige la DB.

**Cambios aplicados:**
- **`V10__rename_membership_tables_to_plural.sql`** (nueva migración):
  - `ALTER TABLE app_role RENAME TO app_roles` + índices y constraints renombrados
  - `ALTER TABLE membership RENAME TO memberships` + índices y constraints renombrados
  - `ALTER TABLE membership_role RENAME TO membership_roles` + PK, FKs e índices renombrados
- **`AppRoleEntity.java`**: `@Table(name = "app_roles")` + nombres de índices/constraints actualizados
- **`MembershipEntity.java`**: `@Table(name = "memberships")` + `@JoinTable(name = "membership_roles")` + nombres actualizados
- **`DATA_MODEL.md`**: sincronizado V1–V10
- **`ENTITY_RELATIONSHIPS.md`**: sincronizado V1–V10
- **`DATA_DICTIONARY.md`**: sincronizado V1–V10
- **`docs/ai/inconsistencias-datos.md`**: inconsistencias #1, #2, #3 actualizadas con criterio de decisión

**Tablas que NO se corrigieron en DB** (la implementación es correcta):
- `membership` sin `tenant_id` (redundante por normalización)
- `app_roles` sin `status` (mejora futura)
- `authorization_codes.status` en lowercase (RFC 7636 / estándar OAuth2)

---

### [2026-03-22] Sincronización de documentos de datos con migraciones reales V1–V9
Se actualizaron los tres documentos de referencia de datos para sincronizarlos con el schema real:
- **`DATA_MODEL.md`** — columnas reales, tabla `membership` (singular), PK compuesta en `membership_role`, `app_role` sin `tenant_id`/`status`, `authorization_codes.status` en minúsculas, `signing_keys.private_material TEXT`, tablas legado V1/V3 documentadas, tablas planificadas marcadas como tales.
- **`ENTITY_RELATIONSHIPS.md`** — class diagrams corregidos, state machine de membership con `PENDING` (no `INVITED`), sin estado `REVOKED`, `role_id` (no `app_role_id`), índices SQL sincronizados con V4–V9.
- **`DATA_DICTIONARY.md`** — `membership` singular, excepciones de convención de status, checklist ampliado, Fases 5/6 completadas.
- **`AUTH_FLOW.md`** — estado "Fases 5 y 6 implementadas ✅"; respuesta Paso 3 con JWT real; status lowercase en diagramas; Fase 7 planificada.
- **Regla nueva:** al crear una migración Flyway, actualizar los 3 docs de datos antes de cerrar la tarea.
- **Sub-documentos creados:** `docs/ai/lecciones.md`, `docs/ai/propuestas.md`, `docs/ai/agents-registro.md`, `docs/ai/inconsistencias.md`, `docs/ai/inconsistencias-datos.md`.

### [2026-03-22] Refactorización de documentos AI en sub-documentos temáticos
Se dividieron los documentos AI principales que estaban creciendo demasiado:
- `AI_CONTEXT.md` → conserva núcleo de referencia rápida; extrae lecciones a `docs/ai/lecciones.md` y propuestas a `docs/ai/propuestas.md`.
- `AGENTS.md` → conserva quick-start; extrae registro de cambios a `docs/ai/agents-registro.md`.
- Nuevo proceso de inconsistencias: `docs/ai/inconsistencias.md` (centralizador) + `docs/ai/inconsistencias-datos.md` (primera instancia documentada).

### [2026-03-22] Fase 6 — Firma de tokens y metadata OIDC completada
- **`keygo-domain`**: `SigningKey`, `SigningKeyId` (record), `SigningKeyStatus`, `SigningKeyAlgorithm`; excepción `NoActiveSigningKeyException`.
- **`keygo-app`**: puertos `SigningKeyRepositoryPort`, `TokenSignerPort`, `TokenClaimsFactoryPort`, `JwksBuilderPort`; casos de uso `IssueTokensUseCase`, `GetJwksUseCase`, `GetOidcConfigurationUseCase`; results `IssueTokensResult`, `OidcConfigurationResult`.
- **`keygo-infra`** (módulo activado — ya no stub): `RsaJwtTokenSigner` (Nimbus via `spring-security-oauth2-jose` transitivo), `StandardTokenClaimsFactory` (at_hash SHA-256), `JwkSetBuilder` (RFC 7517); `PkceVerifier` (Fase 5). `jacoco.skip` eliminado.
- **`keygo-supabase`**: `SigningKeyEntity`, `SigningKeyJpaRepository` (`findFirstByStatus`, `findByStatusIn`), `SigningKeyPersistenceMapper`, `SigningKeyRepositoryAdapter`; migración `V9__add_signing_keys.sql`.
- **`keygo-api`**: `JwksController` (`GET /.well-known/jwks.json`, JSON nativo RFC 7517), `OidcMetadataController` (`GET /.well-known/openid-configuration`, JSON nativo OIDC Discovery 1.0), `AuthorizationController` actualizado para emitir tokens reales en `POST /oauth2/token`; `TokenData` con access_token + id_token + token_type + expires_in; `GlobalExceptionHandler` con handler `NoActiveSigningKeyException` → 503.
- **`keygo-run`**: 3 nuevos `@Bean` en `ApplicationConfig` (`tokenSignerPort`, `tokenClaimsFactoryPort`, `jwksBuilderPort`, `issueTokensUseCase`, `getJwksUseCase`, `getOidcConfigurationUseCase`); property `keygo.info.issuer-base-url` usada en `GetOidcConfigurationUseCase` y `AuthorizationController`.
- **Tests nuevos en esta sesión**: `JwkSetBuilderTest` (4), `JwksControllerTest` (2), `OidcMetadataControllerTest` (2) + tests pre-existentes completados (IssueTokensUseCaseTest, GetJwksUseCaseTest, GetOidcConfigurationUseCaseTest, RsaJwtTokenSignerTest, StandardTokenClaimsFactoryTest, SigningKeyRepositoryAdapterTest, SigningKeyPersistenceMapperTest). **Total proyecto: 307 tests, todos pasan** ✅
- **Postman**: carpeta `🔑 OIDC & JWKS` con 2 requests (`GET OIDC Configuration`, `GET JWKS`) con scripts `pm.test()` completos.
- **ResponseCode**: `TOKEN_ISSUED`, `JWKS_RETRIEVED`, `OIDC_CONFIGURATION_RETRIEVED`.

### [2026-03-22] Fase 5 — Núcleo OAuth2/OIDC: authorization flow completada
- **`keygo-domain`**: `AuthorizationCode`, `AuthorizationCodeId`, `AuthorizationCodeStatus`, `CodeChallenge`, `ScopeSet`; excepciones `InvalidAuthorizationCodeException`, `AuthorizationCodeExpiredException`, `InvalidPkceVerificationException`, `ScopeNotGrantedException`.
- **`keygo-app`**: puertos `AuthorizationCodeRepositoryPort`, `ClockPort`; 4 comandos; 3 results; 4 casos de uso.
- **`keygo-infra`**: `PkceVerifier` (S256 y plain).
- **`keygo-supabase`**: `AuthorizationCodeEntity`, repositorio, mapper, adapter, `V8__add_oauth_authorization_codes.sql`.
- **`keygo-api`**: `AuthorizationController` (3 endpoints: `GET /authorize`, `POST /login`, `POST /token`), DTOs, 5 handlers en `GlobalExceptionHandler`.
- **`keygo-run`**: `SystemClockProvider`, 6 nuevos `@Bean`.
- Tests: ~60 nuevos. Total: **270+ tests**.
- Postman: carpeta `🔐 OAuth2 Authorization` con 3 requests. **23 requests totales** en 6 carpetas.
- ResponseCode: 4 nuevos (`AUTHORIZATION_INITIATED`, `AUTHORIZATION_CODE_ISSUED`, `AUTHORIZATION_CODE_EXCHANGED`, `LOGIN_SUCCESSFUL`).

### [2026-03-22] Documentación AUTH_FLOW.md generada bajo orden explícita
Documento `docs/api/AUTH_FLOW.md` (~350 líneas): guía completa OAuth 2.0 Authorization Code + PKCE desde perspectiva del cliente. Incluye diagramas Mermaid (prereqs, secuencia, flowcharts de validación por paso).

### [2026-03-22] Documentación de modelo de datos — 3 nuevos documentos + índice
Bajo orden explícita: `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `MIGRATIONS.md` en `docs/data/`. 100% Mermaid.

### [2026-03-21] Fase 4 — Memberships y roles por app completada
- **`keygo-domain`**: `Membership`, `AppRole`, `MembershipRole`, `RoleCode`; excepciones de membership.
- **`keygo-app`**: puertos `MembershipRepositoryPort`, `AppRoleRepositoryPort`; 4 casos de uso.
- **`keygo-supabase`**: entidades, repositorios, mapper, adapter, `V7__add_memberships.sql`.
- **`keygo-api`**: `TenantMembershipController` (3 endpoints), `TenantAppRoleController` (2 endpoints), DTOs, 6 nuevos `ResponseCode`.
- **`keygo-run`**: 4 nuevos `@Bean`.
- Tests: ~45 nuevos. Total: **210+ tests**.
- Postman: carpetas `📋 Memberships` y `👥 Roles` con 5 requests.
- `ROADMAP.md`: F-009 completada, Fase 4 marcada.

### [2026-03-21] Fase 3 — User identity per tenant completada
- **`keygo-domain`**: `TenantUser`, `EmailAddress`, `PasswordHash`, `Username`; excepciones `UserNotFoundException`, `UserAlreadyExistsException`.
- **`keygo-app`**: puertos `TenantUserRepositoryPort`, `PasswordHasherPort`; 5 casos de uso (crear, listar, obtener, actualizar, validar credenciales, reset password).
- **`keygo-supabase`**: `TenantUserEntity`, repositorio, mapper, adapter, `V6__add_tenant_users.sql`.
- **`keygo-api`**: `TenantUserController` (6 endpoints), DTOs, 3 nuevos `ResponseCode`.
- **`keygo-run`**: 5 nuevos `@Bean` + `BCryptPasswordHasher`.
- Tests: ~50 nuevos. Total: **165+ tests**.
- Postman: carpeta `👤 Users` con 6 requests. **17 requests totales**.

### [2026-03-21] Fase 2 — Client app model completada
- **`keygo-domain`**: `ClientApp`, `ClientId`, `ClientType`, `ClientSecret`; excepciones.
- **`keygo-app`**: puertos `ClientAppRepositoryPort`; 5 casos de uso.
- **`keygo-supabase`**: `ClientAppEntity`, entidades hijo (redirect URIs, grants, scopes), `V5__add_client_apps.sql`.
- **`keygo-api`**: `TenantClientAppController` (5 endpoints + rotate-secret), DTOs, 5 nuevos `ResponseCode`.
- Postman: carpeta `🔐 Client Apps` con 6 requests. **11 requests totales**.

### [2026-03-22] Fase 7 — Refresh token, Session, Revocación RFC 7009, UserInfo OIDC §5.3

**Dominio (`keygo-domain`)**:
- Nuevas excepciones: `InvalidRefreshTokenException`, `RefreshTokenExpiredException`
- Nuevos modelos: `Session`, `SessionId`, `SessionStatus`, `RefreshToken`, `RefreshTokenId`, `RefreshTokenStatus`

**Aplicación (`keygo-app`)**:
- Nuevos puertos: `SessionRepositoryPort`, `RefreshTokenRepositoryPort`, `AccessTokenVerifierPort`
- Nuevos comandos: `OpenSessionCommand`, `RotateRefreshTokenCommand`, `RevokeTokenCommand`, `GetUserInfoCommand`
- Nuevos resultados: `OpenSessionResult`, `RotateRefreshTokenResult`, `UserInfoResult`
- Nuevos use cases: `OpenSessionUseCase`, `TerminateSessionUseCase`, `RotateRefreshTokenUseCase`, `RevokeTokenUseCase`, `GetUserInfoUseCase`

**Infraestructura (`keygo-infra`)**:
- Nuevo: `RsaJwtTokenVerifier` implementa `AccessTokenVerifierPort` con verificación de firma RSA + expiración

**Persistencia (`keygo-supabase`)**:
- Migración `V11__add_refresh_tokens_and_sessions.sql` (tablas `sessions` + `refresh_tokens` con índices)
- Nuevas entidades: `SessionEntity`, `RefreshTokenEntity`
- Nuevos repositorios JPA: `SessionJpaRepository`, `RefreshTokenJpaRepository`
- Nuevos mappers: `SessionPersistenceMapper`, `RefreshTokenPersistenceMapper`
- Nuevos adapters: `SessionRepositoryAdapter`, `RefreshTokenRepositoryAdapter`

**API (`keygo-api`)**:
- `TokenRequest`: ahora soporta `grant_type` + campos opcionales para `refresh_token` grant
- `TokenData`: agrega campo `refresh_token` en la respuesta
- Nuevos `ResponseCode`: `REFRESH_TOKEN_ROTATED`, `TOKEN_REVOKED`, `USER_INFO_RETRIEVED`
- Nuevo request: `RevokeTokenRequest`
- `AuthorizationController`: agrega branch `refresh_token` en `POST /oauth2/token` + emite RT tras `authorization_code` grant
- Nuevo controller: `RevocationController` → `POST /oauth2/revoke`
- Nuevo controller: `UserInfoController` → `GET /userinfo`
- `GlobalExceptionHandler`: agrega handlers para `InvalidRefreshTokenException` y `RefreshTokenExpiredException`

**Wiring (`keygo-run`)**:
- `ApplicationConfig`: 6 nuevos beans (AccessTokenVerifierPort, OpenSessionUseCase, TerminateSessionUseCase, RotateRefreshTokenUseCase, RevokeTokenUseCase, GetUserInfoUseCase)
- `KeyGoBootstrapProperties`: 2 nuevas propiedades de ruta pública (`userInfoPathSuffix`, `revocationPathSuffix`)
- `BootstrapAdminKeyFilter`: `/userinfo` y `/oauth2/revoke` marcadas como rutas públicas vía sufijos configurables
- `application.yml`: agrega `userinfo-path-suffix` y `revocation-path-suffix`

**Tests**:
- `SessionTest`, `RefreshTokenTest` (domain)
- `OpenSessionUseCaseTest`, `RotateRefreshTokenUseCaseTest`, `RevokeTokenUseCaseTest` (app)

**Postman**: 3 nuevos requests: `Exchange Token — refresh_token grant`, `Revoke Token`, `UserInfo`

**URLs nuevas**:
- `POST /api/v1/tenants/{slug}/oauth2/token` con `grant_type=refresh_token`
- `POST /api/v1/tenants/{slug}/oauth2/revoke` (público, RFC 7009)
- `GET  /api/v1/tenants/{slug}/userinfo` (público, Bearer token)

### [2026-03-21] Fase 1 — Multitenancy completada
- **`keygo-domain`**: `Tenant`, `TenantId`, `TenantSlug`, `TenantStatus`; excepción `TenantNotFoundException`.
- **`keygo-supabase`**: `TenantEntity`, `TenantJpaRepository`, `V4__add_tenants.sql`.
- **`keygo-api`**: `TenantController` (3 endpoints), DTOs, 3 nuevos `ResponseCode`.
- Postman: carpeta `🏢 Tenants` con 3 requests.

### [2026-03-21] Fase 0 — Hardening estructural completado
- CI Pipeline: `.github/workflows/ci.yml` con `./mvnw test` + `./mvnw clean package`.
- Maven Enforcer Plugin: Java 21+, Maven 3.9+, UTF-8, sin dependencias duplicadas.
- Convenciones documentadas: `docs/development/CODE_STYLE.md`.
- Reorganización de paquetes por feature en todos los módulos activos.
- Bug T-001 corregido: `BootstrapAdminKeyFilter` usa `getServletPath()`.
- SpringDoc 3.0.1 integrado, Swagger UI en `/keygo-server/swagger-ui/index.html`.
- Postman collection + environment creados desde cero.

### [2026-03-17] Reorganización inicial de paquetes por feature
Paquetes reorganizados de organización técnica genérica a organización por feature en `keygo-api`, `keygo-app`, `keygo-run`, `keygo-supabase`.

### [2026-03-17] Retroalimentación obligatoria establecida como regla
`AGENTS.md` agregado como documento obligatorio. Regla de retroalimentación de docs AI establecida en los 4 archivos de instrucciones.

