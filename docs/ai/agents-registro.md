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

### [2026-03-26] Reorganización de scripts y templates .env — `scripts/switch-env.sh` + `scripts/envs/`

**Motivo:** El script `keygo-supabase/scripts/switch-env.sh` tenía un shebang defectuoso (`!/bin/bash` en lugar de `#!/bin/bash`), lo que causaba que `sh` lo ejecutara en lugar de `bash`. Esto generaba `Bad substitution` en `${BASH_SOURCE[0]}`, rutas incorrectas (`PROJECT_DIR` apuntaba dos niveles arriba del repo) y que `echo -e` no funcionara. Adicionalmente, el script y los templates `.env-*` estaban en `keygo-supabase/` siendo que configuran toda la aplicación (no solo la DB).

**Cambios aplicados:**
- **Nuevo:** `scripts/switch-env.sh` — reescrito con `#!/bin/bash`, `SCRIPT_DIR`/`PROJECT_DIR`/`ENVS_DIR` calculados correctamente; soporta `local`, `desa`, `prod`, `list`, `help`; copia el template a `keygo-supabase/.env`.
- **Nueva carpeta:** `scripts/envs/` con templates `.env-local`, `.env-desa`, `.env-prod`, `.env.example` y `.gitignore` propio.
- **Eliminado:** `keygo-supabase/scripts/switch-env.sh`.
- **Eliminados de `keygo-supabase/`:** `.env-local`, `.env-desa`, `.env-prod` (los templates ahora viven en `scripts/envs/`).
- **Actualizado:** hints en `load-env.sh`, `migrate.sh`, `dev-start.sh` para señalar el nuevo path.
- **Actualizado:** `keygo-supabase/.gitignore` con comentario que aclara el nuevo origen de templates.
- **Actualizado:** `docs/development/ENVIRONMENT_SETUP.md` con la nueva estructura de directorios.

**Estructura resultante:**
```
scripts/
├── switch-env.sh        # ✅ Script general (nuevo destino)
└── envs/
    ├── .gitignore
    ├── .env.example     # ✅ committed
    ├── .env-local       # ⚠️ git ignored
    ├── .env-desa        # ⚠️ git ignored
    └── .env-prod        # ⚠️ git ignored

keygo-supabase/
├── .env                 # ⚠️ activo, generado por switch-env.sh
└── scripts/             # solo scripts de DB
```

### [2026-03-26] Centralización de scripts + menú principal `keygo.sh`

**Motivo:** Los scripts de base de datos estaban dispersos en `keygo-supabase/scripts/` sin punto de entrada unificado. El usuario solicitó una carpeta centralizada y un menú principal interactivo para todas las operaciones del proyecto.

**Cambios aplicados:**
- **Nuevo:** `scripts/keygo.sh` — menú interactivo principal con 20 opciones en 5 categorías (Ambiente, BD, Aplicación, Tests, Setup). Soporta modo interactivo y modo directo (`./scripts/keygo.sh <N>`).
- **Nueva carpeta:** `scripts/db/` con 8 scripts centralizados y un helper interno `_load-env.sh`:
  - `start.sh` (← `dev-start.sh`), `stop.sh` (← `dev-stop.sh`), `migrate.sh`, `info.sh`, `validate.sh`, `repair.sh`, `clean.sh` (con confirmación), `setup.sh` (← `setup-supabase.sh`).
- **Stubs de compatibilidad:** `keygo-supabase/scripts/{dev-start,dev-stop,migrate,clean,repair,validate,info,setup-supabase}.sh` — ahora delegan con `exec` al script centralizado en `scripts/db/`.
- **Actualizado:** `AGENTS.md` — sección "Essential commands" y "Enabling the database" con rutas centralizadas.
- **Actualizado:** `scripts/switch-env.sh` — tips de uso apuntan al menú `keygo.sh`.

**Estructura de scripts resultante:**
```
scripts/
├── keygo.sh              # 🎯 Menú principal (nuevo)
├── switch-env.sh         # Cambio de ambiente
├── quick-start.sh        # Quick start app
├── setup-keygo-tenant.sh # Bootstrap tenant
├── check-ai-docs.sh      # Verificador AI docs
├── test-*.sh             # Smoke tests
├── envs/                 # Templates .env-*
└── db/                   # Scripts de base de datos
    ├── _load-env.sh      # Helper interno (source)
    ├── start.sh          # Docker Compose up
    ├── stop.sh           # Docker Compose down
    ├── migrate.sh        # Flyway migrate
    ├── info.sh           # Flyway info
    ├── validate.sh       # Flyway validate
    ├── repair.sh         # Flyway repair
    ├── clean.sh          # Flyway clean ⚠️
    └── setup.sh          # Setup completo
```

**Opciones del menú `keygo.sh`:**

| Nº | Acción | Categoría |
|---|---|---|
| 1–4 | Cambiar ambiente (local/desa/prod/list) | 🌐 Ambiente |
| 5–6 | Iniciar/Detener DB Docker | 🗄️ BD |
| 7–11 | Migrate/Info/Validate/Repair/Clean Flyway | 🗄️ BD |
| 12–14 | Quick Start / Build / Run servidor | 🚀 App |
| 15–18 | Smoke tests + AI docs + mvnw test | 🧪 Tests |
| 19–20 | Setup tenant / Setup Supabase | ⚙️ Setup |

### [2026-03-26] CORS habilitado — `SecurityFilterChain` + `CorsConfigurationSource`

**Motivo:** Las llamadas XHR del frontend SPA (`http://localhost:5173`) a `GET /keygo-server/api/v1/tenants/{slug}/oauth2/authorize` eran bloqueadas por el navegador con `No 'Access-Control-Allow-Origin' header is present on the requested resource`.

**Cambios aplicados:**
- **Nuevo archivo:** `keygo-run/.../config/properties/KeyGoCorsProperties.java` — `@ConfigurationProperties("keygo.cors")` con `allowedOrigins`, `allowedMethods`, `allowedHeaders`, `allowCredentials` (default `true`), `maxAge` (default 3600).
- **`application.yml`:** nueva sección `keygo.cors` con valores default para desarrollo local (`http://localhost:5173`).
- **`SecurityConfig.java`:** nuevo `@Bean CorsConfigurationSource corsConfigurationSource(KeyGoCorsProperties)` + `http.cors(cors -> cors.configurationSource(corsConfigurationSource))` en `securityFilterChain`.
- **Nuevo test:** `keygo-run/.../config/security/CorsConfigTest.java` — 7 tests unitarios sin Spring context (instancia directa de `SecurityConfig`) que verifican origen, métodos (incluye OPTIONS), credenciales, maxAge y cobertura de rutas.

**Resultado:** Spring Security aplica CORS antes de evaluar el `BootstrapAdminKeyFilter`; el preflight `OPTIONS` y las llamadas reales reciben `Access-Control-Allow-Origin: http://localhost:5173` y `Access-Control-Allow-Credentials: true`.

### [2026-03-25] Seguridad admin Bearer-only + RBAC por endpoint

**Motivo:** El backend deja de aceptar `X-KEYGO-ADMIN` y estandariza seguridad de endpoints admin con JWT Bearer + autorización explícita por endpoint.

**Cambios aplicados:**
- `keygo-run`: `spring-boot-starter-security` + `SecurityConfig` con `@EnableMethodSecurity` y `SecurityFilterChain` stateless.
- `BootstrapAdminKeyFilter`: autenticación **solo** por `Authorization: Bearer`, sin fallback a admin key; pobla `SecurityContext` con authorities `ROLE_*` desde claim `roles`.
- `keygo-api`: `@PreAuthorize` en controllers admin (`PlatformTenantController`, `TenantClientAppController`, `TenantUserController`, `TenantMembershipController`, `TenantAppRoleController`).
- Nuevo evaluador `tenantAuthorizationEvaluator`: valida tenant del token (`tenant_slug` o fallback `iss`) contra `tenantSlug` en path para `ADMIN_TENANT`.
- `OpenApiConfig`: esquema de seguridad migrado de `AdminKeyAuth` a `BearerAuth`.
- `keygo-app`: emisión de claim `tenant_slug` en access tokens (auth code, refresh rotation y client_credentials).

**Resultado:** Endpoints admin alineados a Bearer JWT, con autorización por rol en cada endpoint y control de aislamiento por tenant en capa de autorización.

### [2026-03-24] Refuerzo del punto 6 — Propuestas de mejoras futuras + trazabilidad cruzada

**Motivo:** Alinear la gobernanza documental para que el ciclo de "propuestas futuras" quede explícito y consistente entre documentos AI y roadmap canónico.

**Cambios aplicados:**
- `AI_CONTEXT.md`: sección `### Propuestas de mejoras futuras` corregida para registrar siempre en `docs/ai/propuestas.md` + `ROADMAP.md` (corto/mediano/largo) y regla práctica de alta con IDs `T-NNN`/`F-NNN`.
- `AGENTS.md`: agregado `docs/ai/propuestas.md` en sub-documentos y nueva sección explícita `## Propuestas de mejoras futuras`.
- `AI_CONTEXT.md`: corrección de link roto hacia lecciones (`AI_CONTEXT.lecciones.md` → `docs/ai/lecciones.md`).
- `docs/ai/propuestas.md` + `ROADMAP.md`: altas sincronizadas de propuestas recientes `T-049`, `T-050` y `F-040`.

**Resultado:** El punto 6 queda referenciado explícitamente en `AI_CONTEXT.md`, `AGENTS.md` y `CLAUDE.md`, con propuestas nuevas ya trazadas en el resumen operativo (`docs/ai/propuestas.md`) y en el registro primario (`ROADMAP.md`).

### [2026-03-24] Fase 9b — Perfil de usuario OIDC extendido + endpoints self-service

**Motivo:** Implementación completa del perfil de usuario OIDC extendido basado en la decisión de diseño:
perfil canónico en `tenant_users`, metadata app-específica en `membership_attributes` (futuro V14).

**Cambios en AGENTS.md:**
- Tabla de filtro: agregado `account-profile-path-suffix: "/account/profile"` como sufijo público
- Lista de migraciones: agregada V13 (`extend_tenant_user_profile`) y actualizado "próxima" a V14
- Tabla de fases: fila Fase 9b marcada como ✅ Done (2026-03-24)
- URLs de referencia rápida: agregados GET y PATCH `/account/profile`

**Nuevos archivos:**
- `V13__extend_tenant_user_profile.sql` — 6 campos OIDC en `tenant_users`
- `GetUserProfileUseCase.java` + `GetUserProfileCommand.java`
- `UpdateUserProfileUseCase.java` + `UpdateUserProfileCommand.java`
- `UserProfileResult.java` (keygo-app result)
- `AccountProfileController.java` (keygo-api)
- `UpdateUserProfileRequest.java` + `UserProfileData.java` (keygo-api)
- `UserProfileUseCaseTest.java` (7 nuevos tests)

**Archivos modificados:**
- `User.java` — 6 nuevos campos + `updateProfile()`
- `TenantUserEntity.java` — 6 nuevas columnas JPA
- `UserPersistenceMapper.java` — mapeo de nuevos campos
- `UpdateUserCommand.java` / `UpdateUserRequest.java` — 6 campos opcionales
- `UpdateUserUseCase.java` — usa `updateProfile()` en vez de `updateName()`
- `UserInfoResult.java` — claims OIDC extendidos
- `GetUserInfoUseCase.java` — retorna claims extendidos
- `ResponseCode.java` — `USER_PROFILE_RETRIEVED`, `USER_PROFILE_UPDATED`
- `UserData.java` — 6 campos de perfil en DTO admin
- `KeyGoBootstrapProperties.java` — `accountProfilePathSuffix`
- `BootstrapAdminKeyFilter.java` — check de sufijo en `isPublicBySuffix()`
- `application.yml` — `account-profile-path-suffix: "/account/profile"`
- `ApplicationConfig.java` — 2 nuevos `@Bean`
- `TenantUserController.java` — pasa nuevos campos al command + toData() extendido
- Postman — carpeta "👤 Account Profile" con GET + PATCH (40 requests total)
- `FRONTEND_DEVELOPER_GUIDE.md` §14 — 2 nuevos endpoints en tabla 14.1

### [2026-03-23] Corrección de documentación — Fase 9 marcada como ✅ COMPLETADA

**Motivo:** La Fase 9 (Self-service de identidad — registro + verificación email) fue implementada el 2026-03-23
pero no se actualizaron los documentos de estado. Se corrigió la discrepancia en:

- `AGENTS.md` — tabla de fases: fila `9–11` dividida en Fase 9 (✅ Done) y Fases 10–11 (pendientes); descripción de Fase 9 corregida de "Token introspection, hardening, observability" a "Self-service de identidad"
- `ROADMAP.md` — "Fase actual" actualizada de Fase 8 → Fase 9; conteo de endpoints (21→24), tests (305+→320+), Postman (29→38)
- `docs/design/IMPLEMENTATION_PLAN.md` — Fase 9 marcada ✅ COMPLETADA con componentes reales implementados; plan original de password reset documentado como pendiente en fase futura

**Nota:** El nombre original de la sección `9–11` en `AGENTS.md` ("Token introspection, hardening, observability")
no correspondía ni al plan original (`IMPLEMENTATION_PLAN.md`) ni a lo que fue implementado como Fase 9.

---

### [2026-03-23] Registro de usuarios con verificación de email

**Dominio (`keygo-domain`)**:
- Nuevo modelo: `EmailVerification` (`keygo-domain/.../user/model/`) — `create()`, `reconstitute()`, `isExpired()`, `isUsed()`, `isValid()`, `markUsed()`
- Nuevo método en `User`: `isPending()` → `UserStatus.PENDING.equals(status)`
- Nuevas excepciones en `keygo-domain/.../user/exception/`:
  - `UserPendingVerificationException` — login con cuenta sin verificar (HTTP 403)
  - `EmailVerificationExpiredException` — código expirado (HTTP 422)
  - `EmailVerificationInvalidException` — código incorrecto o ya usado (HTTP 400)
  - `EmailVerificationStillActiveException` — reenvío bloqueado porque el código aún es válido (HTTP 409)

**Aplicación (`keygo-app`)**:
- Nuevos puertos OUT en `app/user/port/`:
  - `EmailVerificationRepositoryPort` — `save()` + `findLatestByUserIdAndTenantId()`
  - `EmailNotificationPort` — `sendVerificationEmail()`
- Nuevos comandos en `app/user/command/`:
  - `RegisterTenantUserCommand`, `VerifyEmailCommand`, `ResendVerificationCommand`
- Nuevos casos de uso en `app/user/usecase/`:
  - `RegisterTenantUserUseCase` — crea usuario PENDING + genera código 6 dígitos (SecureRandom) + persiste verificación + envía email
  - `VerifyEmailUseCase` — verifica código, activa usuario (PENDING → ACTIVE)
  - `ResendVerificationEmailUseCase` — reenvía solo si el código anterior venció
- Modificado `ValidateUserCredentialsUseCase` — agrega check `isPending()` → `UserPendingVerificationException` antes de verificar `isSuspended()`

**Infraestructura (`keygo-infra`)**:
- Nueva dependencia: `spring-boot-starter-mail`
- Nuevo adaptador: `SmtpEmailNotificationAdapter implements EmailNotificationPort` — texto plano, instanciado como `@Bean`

**Persistencia (`keygo-supabase`)**:
- Migración `V12__add_email_verifications.sql` — tabla `email_verifications` con FK → `tenant_users`, `code VARCHAR(10)`, `expires_at`, `used_at`
- Nueva entidad: `EmailVerificationEntity` (`user.entity`) — `@ManyToOne(LAZY)` → `TenantUserEntity`
- Nuevo repositorio: `EmailVerificationJpaRepository` — `findTopByTenantUserOrderByCreatedAtDesc()`
- Nuevo adaptador: `EmailVerificationRepositoryAdapter` — implementa `EmailVerificationRepositoryPort`

**API (`keygo-api`)**:
- Nuevos `ResponseCode`: `USER_REGISTERED`, `EMAIL_VERIFICATION_SENT`, `EMAIL_VERIFIED`, `EMAIL_VERIFICATION_EXPIRED`, `EMAIL_VERIFICATION_RESENT`, `EMAIL_VERIFICATION_STILL_ACTIVE`, `EMAIL_NOT_VERIFIED`
- Nuevos DTOs en `api/registration/`:
  - Request: `RegisterRequest`, `VerifyEmailRequest`, `ResendVerificationRequest`
  - Response: `RegistrationData`
- Nuevo controller: `RegistrationController` — path `/api/v1/tenants/{tenantSlug}/apps/{clientId}` — sin `@SecurityRequirement`
  - `POST /register` → HTTP 201
  - `POST /verify-email` → HTTP 200
  - `POST /resend-verification` → HTTP 200
- `GlobalExceptionHandler` — 4 nuevos handlers: `UserPendingVerificationException`, `EmailVerificationExpiredException`, `EmailVerificationInvalidException`, `EmailVerificationStillActiveException`

**Configuración (`keygo-run`)**:
- `application.yml` — sección `spring.mail` con `${SMTP_HOST}`, `${SMTP_PORT:587}`, `${SMTP_USERNAME}`, `${SMTP_PASSWORD}`; sección `keygo.mail.from`, `keygo.mail.app-name`; 3 nuevos path sufijos: `register-path-suffix`, `verify-email-path-suffix`, `resend-verification-path-suffix`
- `KeyGoBootstrapProperties` — 3 nuevos campos: `registerPathSuffix`, `verifyEmailPathSuffix`, `resendVerificationPathSuffix`
- `BootstrapAdminKeyFilter.isPublicPath()` — agrega los 3 nuevos sufijos
- `ApplicationConfig` — nuevos `@Bean`: `emailNotificationPort`, `registerTenantUserUseCase`, `verifyEmailUseCase`, `resendVerificationEmailUseCase`

**Tests**:
- `RegisterTenantUserUseCaseTest` — 6 casos: éxito (PENDING + email enviado), tenant no encontrado, tenant suspendido, app no pertenece al tenant, email duplicado, username duplicado
- `VerifyEmailUseCaseTest` — 4 casos: éxito (ACTIVE), código expirado, código incorrecto, código ya usado
- `ResendVerificationEmailUseCaseTest` — 3 casos: éxito (código vencido), éxito (sin código previo), bloqueado (código vigente)

**Postman** — carpeta `Registration` con 3 requests: `POST Register User`, `POST Verify Email`, `POST Resend Verification Email`

**Lección aprendida**: `ClientApp.builder()` requiere `id`, `type`, `status` y `accessPolicy` — siempre pasar todos los campos obligatorios del dominio en tests, incluso si el objeto se usa solo como valor de retorno de un mock.

---

### [2026-03-23] Fase 8 — Client Credentials grant (M2M) completada

**Dominio (`keygo-domain`)**:
- Reutiliza modelos existentes: `ClientApp`, `ClientType`, `AllowedGrant.CLIENT_CREDENTIALS`, `SigningKey`
- `ClientAuthenticationException` ya existía — se usa cuando la app es PUBLIC o el secret es incorrecto

**Aplicación (`keygo-app`)**:
- Nuevo comando: `IssueClientCredentialsTokenCommand` (`tenantSlug`, `clientId`, `rawClientSecret`, `scope`)
- Nuevo result: `IssueClientCredentialsTokenResult` (`accessToken`, `tokenType`, `expiresIn`, `scope`)
- Nuevo use case: `IssueClientCredentialsTokenUseCase` — flujo: valida tenant → busca app → verifica CONFIDENTIAL → valida grant CLIENT_CREDENTIALS → verifica secret → obtiene signing key activa → resuelve scopes (intersección o todos) → firma token JWT

**API (`keygo-api`)**:
- `AuthorizationController` ampliado: nuevo método privado `handleClientCredentialsGrant` en `POST /api/v1/tenants/{slug}/oauth2/token`
- Respuesta: `access_token`, `token_type=Bearer`, `expires_in=3600`, `scope`; sin `id_token` ni `refresh_token` (excluidos por `NON_NULL` Jackson)
- Nuevo `ResponseCode`: `CLIENT_CREDENTIALS_TOKEN_ISSUED`

**Wiring (`keygo-run`)**:
- `@Bean IssueClientCredentialsTokenUseCase` en `ApplicationConfig`

**Postman**:
- Nuevo request `Exchange Token — client_credentials grant` en carpeta `🔑 OIDC & JWKS`
- 7 `pm.test()`: status 200, `CLIENT_CREDENTIALS_TOKEN_ISSUED`, `access_token`, ausencia de `refresh_token`, ausencia de `id_token`, `token_type=Bearer`, `expires_in > 0`
- Guarda `m2mAccessToken` en variable de entorno

**Documentación actualizada**:
- `ROADMAP.md`: T-039 → historial, F-025 → completada (~~tachado~~), estado del producto actualizado a Fase 8 ✅
- `AGENTS.md`: Fase 8 marcada como Done, endpoint `client_credentials` en lista de URLs, entrada en registro reciente
- `docs/design/IMPLEMENTATION_PLAN.md`: Fases 7 y 8 documentadas con todos los componentes implementados; Sprint 4 marcado completo
- `docs/data/DATA_MODEL.md`: sección "Tablas planificadas" corregida (eliminadas `refresh_tokens` y `tenant_sessions` que ya son V11); diagrama E/R actualizado con `SESSIONS` y `REFRESH_TOKENS`
- `docs/ai/lecciones.md`: lección sobre `sub=clientId`, ausencia de `id_token`/`refresh_token` y resolución de scopes en M2M

**URLs nuevas / actualizadas**:
- `POST /api/v1/tenants/{slug}/oauth2/token` con `grant_type=client_credentials` (requiere `client_id` + `client_secret`)

---

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

