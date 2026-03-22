# AGENTS — Registro de Cambios

> Sub-documento de [`AGENTS.md`](AGENTS.md).
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

### [2026-03-22] Re-auditoría de inconsistencias — corrección de tablas en singular via migración V10

Re-revisión de las 12 inconsistencias marcadas como "resueltas" en `INCONSISTENCIAS.datos.md`. Criterio aplicado: **la documentación manda** para convenciones de nomenclatura; la implementación manda cuando hay razón técnica clara.

**Hallazgo:** Las tablas `app_role`, `membership`, `membership_role` (V7) estaban en singular, violando la convención PostgreSQL de nombres en plural. La corrección anterior solo actualizó los docs para aceptar el singular; ahora se corrige la DB.

**Cambios aplicados:**
- **`V10__rename_membership_tables_to_plural.sql`** (nueva migración):
  - `ALTER TABLE app_role RENAME TO app_roles` + índices y constraints renombrados
  - `ALTER TABLE membership RENAME TO memberships` + índices y constraints renombrados
  - `ALTER TABLE membership_role RENAME TO membership_roles` + PK, FKs e índices renombrados
- **`AppRoleEntity.java`**: `@Table(name = "app_roles")` + nombres de índices/constraints actualizados
- **`MembershipEntity.java`**: `@Table(name = "memberships")` + `@JoinTable(name = "membership_roles")` + nombres actualizados
- **`DATA_MODEL.md`**: encabezados, diagrama Mermaid ER, grafo de cascade, queries de ejemplo, tabla de enumeraciones, tabla de constraints únicos, tabla de próximas migraciones (V10 aplicada, próxima V11) → sincronizado V1–V10
- **`ENTITY_RELATIONSHIPS.md`**: notas de contexto 3, diagrama de roles, state machine → sincronizado V1–V10
- **`DATA_DICTIONARY.md`**: caso de uso, convenciones de nomenclatura (se eliminó la excepción de singular), Siguientes pasos → sincronizado V1–V10
- **`INCONSISTENCIAS.datos.md`**: inconsistencias #1, #2, #3 actualizadas con criterio de decisión; criterio de corrección documentado
- **`AGENTS.md`**: tabla de entidades JPA (`app_roles`, `memberships`, `membership_roles`) y lista de migraciones aplicadas

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
- **Regla nueva:** al crear una migración Flyway, actualizar los 3 docs de datos antes de cerrar la tarea (agregada en `AI_CONTEXT.md`, `CLAUDE.md`, `AGENTS.md`, `.github/copilot-instructions.md`).
- **Sub-documentos creados:** `AI_CONTEXT.lecciones.md`, `AI_CONTEXT.propuestas.md`, `AGENTS.registro.md`, `INCONSISTENCIAS.md`, `INCONSISTENCIAS.datos.md`.

### [2026-03-22] Refactorización de documentos AI en sub-documentos temáticos
Se dividieron los documentos AI principales que estaban creciendo demasiado:
- `AI_CONTEXT.md` → conserva núcleo de referencia rápida; extrae lecciones a `AI_CONTEXT.lecciones.md` y propuestas a `AI_CONTEXT.propuestas.md`.
- `AGENTS.md` → conserva quick-start; extrae registro de cambios a `AGENTS.registro.md`.
- Nuevo proceso de inconsistencias: `INCONSISTENCIAS.md` (centralizador) + `INCONSISTENCIAS.datos.md` (primera instancia documentada).

### [2026-03-22] Fase 6 — Firma de tokens y metadata OIDC completada
- **`keygo-domain`**: `SigningKey`, `SigningKeyId` (record), `SigningKeyStatus`, `SigningKeyAlgorithm`; excepción `NoActiveSigningKeyException`.
- **`keygo-app`**: puertos `SigningKeyRepositoryPort`, `TokenSignerPort`, `TokenClaimsFactoryPort`, `JwksBuilderPort`; casos de uso `IssueTokensUseCase`, `GetJwksUseCase`, `GetOidcConfigurationUseCase`.
- **`keygo-infra`**: `RsaJwtTokenSigner` (Nimbus via `spring-security-oauth2-jose`), `StandardTokenClaimsFactory`, `JwkSetBuilder`.
- **`keygo-supabase`**: `SigningKeyEntity`, `SigningKeyJpaRepository`, mapper, adapter, migración `V9__add_signing_keys.sql`.
- **`keygo-api`**: `JwksController` (`GET /.well-known/jwks.json`), `OidcMetadataController` (`GET /.well-known/openid-configuration`), `AuthorizationController` actualizado, `TokenData` ampliado.
- **`keygo-run`**: `SigningKeyBootstrapService` (auto-genera par RSA 2048 al arrancar sin clave ACTIVE, solo perfil `supabase`), 6 nuevos `@Bean`.
- Tests: ~29 nuevos. Total: **299 tests**.
- Postman: carpeta `🔑 OIDC & JWKS` con 2 requests. **25 requests totales** en 7 carpetas.
- `application.yml`: `keygo.info.issuer-base-url`, `keygo.bootstrap.well-known-path-prefix`.

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
Documento `docs/keygo-server/AUTH_FLOW.md` (~350 líneas): guía completa OAuth 2.0 Authorization Code + PKCE desde perspectiva del cliente. Incluye diagramas Mermaid (prereqs, secuencia, flowcharts de validación por paso).

### [2026-03-22] Documentación de modelo de datos — 3 nuevos documentos + índice
Bajo orden explícita: `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md`, `README.md` (actualizado). 100% Mermaid.

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

### [2026-03-21] Fase 1 — Multitenancy completada
- **`keygo-domain`**: `Tenant`, `TenantId`, `TenantSlug`, `TenantStatus`; excepción `TenantNotFoundException`.
- **`keygo-supabase`**: `TenantEntity`, `TenantJpaRepository`, `V4__add_tenants.sql`.
- **`keygo-api`**: `TenantController` (3 endpoints), DTOs, 3 nuevos `ResponseCode`.
- Postman: carpeta `🏢 Tenants` con 3 requests.

### [2026-03-21] Fase 0 — Hardening estructural completado
- CI Pipeline: `.github/workflows/ci.yml` con `./mvnw test` + `./mvnw clean package`.
- Maven Enforcer Plugin: Java 21+, Maven 3.9+, UTF-8, sin dependencias duplicadas.
- Convenciones documentadas: `docs/keygo-server/CODE_STYLE.md`.
- Reorganización de paquetes por feature en todos los módulos activos.
- Bug T-001 corregido: `BootstrapAdminKeyFilter` usa `getServletPath()`.
- SpringDoc 3.0.1 integrado, Swagger UI en `/keygo-server/swagger-ui/index.html`.
- Postman collection + environment creados desde cero.

### [2026-03-17] Reorganización inicial de paquetes por feature
Paquetes reorganizados de organización técnica genérica a organización por feature en `keygo-api`, `keygo-app`, `keygo-run`, `keygo-supabase`.

### [2026-03-17] Retroalimentación obligatoria establecida como regla
`AGENTS.md` agregado como documento obligatorio. Regla de retroalimentación de docs AI establecida en los 4 archivos de instrucciones.

