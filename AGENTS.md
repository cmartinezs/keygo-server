# AGENTS.md — KeyGo Server

> Quick-start guide for AI coding agents. Full context in `AI_CONTEXT.md`, `ARCHITECTURE.md`, `CLAUDE.md`, `ROADMAP.md`.

## Module map & dependency rules

```
keygo-domain   ← pure Java, NO Spring, NO internal deps  [Tenant ✅, ClientApp ✅]
keygo-app      ← usecases + port interfaces (OUT); depends on domain
keygo-infra    ← generic port impls; depends on app         [🚧 stub]
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

# Utility/smoke-test scripts
./scripts/quick-start.sh              # start DB + set env vars + run app
./scripts/test-service-info.sh        # smoke-test GET /api/v1/service/info
./scripts/test-response-codes.sh      # smoke-test GET /api/v1/response-codes

# Verificar actividad de retroalimentación del agente AI
./scripts/check-ai-docs.sh            # umbral por defecto: 30 días
./scripts/check-ai-docs.sh --days 60  # umbral personalizado
./scripts/check-ai-docs.sh --quiet    # solo exit code (útil en CI)
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

`keygo-api` has a `@RestControllerAdvice` (`GlobalExceptionHandler`) that wraps exceptions into `BaseResponse<Void>`:

| Exception | HTTP | ResponseCode |
|---|---|---|
| `UnauthorizedException` | 401 | `AUTHENTICATION_REQUIRED` |
| `NoResourceFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| `IllegalArgumentException` | 400 | `INVALID_INPUT` |
| `Exception` (catch-all) | 500 | `OPERATION_FAILED` |

To signal an auth error from any layer, throw `UnauthorizedException` (located in `keygo-api/error/`).

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
6. **Postman** — add or update the request in `postman/KeyGo-Server.postman_collection.json` **before closing the task**.  
   Include: HTTP method, URL with env variables (`{{fullBaseUrl}}/api/v1/...`), required headers, example body (if applicable), and `pm.test()` scripts validating status code, `BaseResponse` structure and business fields.  
   This update **does not require explicit user instruction** — it is a mandatory part of the endpoint workflow.

## context-path is always active

All endpoints are served under `/keygo-server`. Local URLs:
- `http://localhost:8080/keygo-server/api/v1/service/info`
- `http://localhost:8080/keygo-server/api/v1/response-codes`
- `http://localhost:8080/keygo-server/api/v1/tenants` (POST — create)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}` (GET — retrieve)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/suspend` (PUT — suspend)
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
- `http://localhost:8080/keygo-server/actuator/health`
- **`http://localhost:8080/keygo-server/swagger-ui/index.html`** — Swagger UI interactiva (público)
- **`http://localhost:8080/keygo-server/v3/api-docs`** — OpenAPI JSON spec (público)

✅ **Bug T-001 corregido (2026-03-21) — `BootstrapAdminKeyFilter`:** now uses `request.getServletPath()` (strips the context-path) instead of `request.getRequestURI()`. With `context-path=/keygo-server`, `getRequestURI()` returned `/keygo-server/api/...` which never matched the prefixes in `application.yml` (e.g. `/api/`), leaving all routes public. `getServletPath()` returns `/api/...` directly. Tests updated to use `setServletPath()` + 2 regression tests with simulated context-path.

The filter has three path categories (see `KeyGoBootstrapProperties`):

| Property | `application.yml` value | Behaviour |
|---|---|---|
| `keygo.bootstrap.api-path-prefix` | `/api/` | Protected — requires `X-KEYGO-ADMIN` |
| `keygo.bootstrap.actuator-path-prefix` | `/actuator/` | Public |
| `keygo.bootstrap.service-info-path-prefix` | `/service/info` | Public |
| `keygo.bootstrap.swagger-ui-path-prefix` | `/swagger-ui` | Public |
| `keygo.bootstrap.api-docs-path-prefix` | `/v3/api-docs` | Public |

## Security header

Protected routes require `X-KEYGO-ADMIN: <value of KEYGO_ADMIN_KEY>`.  
Default dev key: `changeMe` — **never use in production**.  
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

**Existing repositories (packages under `io.cmartinezs.keygo.supabase`):**

| Repository | Package |
|---|---|
| `UserRepository` | `user.repository` |
| `RoleRepository` | `membership.repository` |
| `TenantJpaRepository` | `tenant.repository` |
| `ClientAppJpaRepository` | `clientapp.repository` |
| `TenantUserJpaRepository` | `user.repository` |

**Flyway migrations already applied:**
- `V1__initial_schema.sql` — users, roles, user_roles, permissions, role_permissions tables
- `V2__seed_data.sql` — seed data
- `V3__add_oauth_support.sql` — oauth_providers, oauth_tokens tables
- `V4__add_tenants.sql` — tenants table (slug unique, status check constraint)
- `V5__add_client_apps.sql` — client_apps, client_redirect_uris, client_allowed_grants, client_allowed_scopes tables
- `V6__add_tenant_users.sql` — tenant_users table (unique per tenant: email, username; FK → tenants ON DELETE CASCADE)

Next migration must be `V7__...`. **Never reuse or edit existing migration files.**

**`SupabaseJpaConfig`** (`keygo-supabase`) declares `@EntityScan` + `@EnableJpaRepositories` — required when adding new entities or repositories to this module.

## Enabling the database

```bash
export SPRING_PROFILES_ACTIVE="supabase,local"
export SUPABASE_URL="jdbc:postgresql://localhost:5432/keygo"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="postgres"
cd keygo-supabase && ./scripts/dev-start.sh   # starts Postgres + PgAdmin via Docker
cd keygo-supabase && ./scripts/dev-stop.sh    # stops Postgres + PgAdmin
```

## Testing conventions

- Unit tests: `@ExtendWith(MockitoExtension.class)` + AssertJ + Mockito — **no Spring context**.
- Integration tests (supabase): Testcontainers PostgreSQL is configured in `pom.xml` and `src/test/resources/application-test.yml` (uses TC JDBC URL `jdbc:tc:postgresql:15-alpine:///testdb`) but **no integration tests are written yet** — `UserRepositoryTest` is a pure unit test using the Lombok builder.
- Pattern: Given/When/Then comments in every test method.

## Implementation plan

Full plan: **`docs/arch/keygo_server_implementation_plan.md`** — 11 phases ordered by dependency.

| Phase | Focus | Status |
|---|---|---|
| 0 | Structural hardening (module deps, package org, conventions, CI, quality baseline) | ✅ Done (2026-03-21) |
| 1 | Multitenancy (`Tenant`, `TenantRepositoryPort`, resolver) | ✅ Done (2026-03-21) |
| 2 | Client app model (`ClientApp`, redirect URIs, grants, secret rotation) | ✅ Done (2026-03-21) |
| 3 | User identity per tenant | ✅ Done (2026-03-21) |
| 4 | Memberships & roles per app | ✅ Done (2026-03-21) |
| 5 | OAuth2/OIDC authorization flow (Auth Code + PKCE) | — |
| 6–11 | Token signing, JWKS, refresh, self-service, hardening | — |

**Golden rule from the plan:** never implement `/oauth2/authorize` before tenant, client app, user, and membership are solid.

## Git — never execute directly

List suggested `git` commands for the user; do not run them.

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

> Historial de actualizaciones del quick-start. El agente debe agregar una entrada aquí cada vez
> que cambie la estructura de módulos, comandos, patrones o URLs de referencia rápida.
> Formato: `### [YYYY-MM-DD] Descripción del cambio`

### [2026-03-22] Fase 5 — Núcleo OAuth2/OIDC: authorization flow completada
Se implementó el flujo OAuth 2.0 Authorization Code + PKCE en los cinco módulos activos:
- **`keygo-domain`**: entidades de dominio puras `AuthorizationCode`, `AuthorizationCodeId`, `AuthorizationCodeStatus` (enum), `CodeChallenge`, `ScopeSet`; excepciones `InvalidAuthorizationCodeException`, `AuthorizationCodeExpiredException`, `InvalidPkceVerificationException`, `ScopeNotGrantedException`. Sin Spring ni JPA.
- **`keygo-app`**: puertos `AuthorizationCodeRepositoryPort`, `ClockPort`; 4 comandos (`InitiateAuthorizationCommand`, `AuthenticateUserCommand`, `IssueAuthorizationCodeCommand`, `ExchangeAuthorizationCodeCommand`); 3 results (`AuthorizationInitiatedResult`, `AuthorizationCodeIssuedResult`, `ExchangeAuthorizationCodeResult`); 4 casos de uso (`InitiateAuthorizationUseCase`, `AuthenticateUserForAuthorizationUseCase`, `IssueAuthorizationCodeUseCase`, `ExchangeAuthorizationCodeUseCase`).
- **`keygo-infra`**: `PkceVerifier` (validación S256 y plain PKCE).
- **`keygo-supabase`**: `AuthorizationCodeEntity` (JPA), `AuthorizationCodeJpaRepository` (Spring Data), `AuthorizationCodePersistenceMapper`, `AuthorizationCodeRepositoryAdapter`, migración `V8__add_oauth_authorization_codes.sql` (tabla `authorization_codes` con índices).
- **`keygo-api`**: `AuthorizationController` (3 endpoints: `GET /api/v1/tenants/{slug}/oauth2/authorize`, `POST /api/v1/tenants/{slug}/account/login`, `POST /api/v1/tenants/{slug}/oauth2/token`), 4 DTOs request (`AuthorizationRequest`, `LoginRequest`, `TokenRequest`) y 3 response (`AuthorizationInitiatedData`, `AuthorizationCodeData`, `LoginData`), 5 handlers en `GlobalExceptionHandler`.
- **`keygo-run`**: `SystemClockProvider` (implementación de `ClockPort`), 6 `@Bean` nuevos en `ApplicationConfig` para inyección de dependencias.
- **Tests**: ~60 tests unitarios nuevos. Total proyecto: **270+ tests** (todos pasan).
- **Postman**: carpeta `🔐 OAuth2 Authorization` con 3 requests (authorize, login, token exchange). **23 requests totales** en 6 carpetas.
- **ResponseCode**: 4 nuevos (`AUTHORIZATION_INITIATED`, `AUTHORIZATION_CODE_ISSUED`, `AUTHORIZATION_CODE_EXCHANGED`, `LOGIN_SUCCESSFUL`).
- **Build**: `./mvnw clean package -DskipTests` — **SUCCESS** en todos los módulos.
- **Lección aprendida**: Value objects — usar **records** exclusivamente para todos los value objects (exponen `.value()` como método público automáticamente), no clases con `.getValue()`. Aplicar hoy: reemplazar `AuthorizationCodeId` clase → record.

### [2026-03-21] Fase 4 — Memberships y roles por app completada
Se implementó el núcleo de memberships (acceso del usuario a una app) y roles por app en los cinco módulos activos:
- **`keygo-domain`**: entidades de dominio puras `Membership`, `MembershipId`, `MembershipStatus`, `AppRole`, `AppRoleId`, `RoleCode`, `MembershipRole`; excepciones `MembershipNotFoundException`, `MembershipInactiveException`, `InvalidRoleAssignmentException`. Sin Spring ni JPA.
- **`keygo-app`**: puertos `MembershipRepositoryPort`, `AppRoleRepositoryPort`; comando `CreateMembershipCommand`; casos de uso `CreateMembershipUseCase`, `RevokeMembershipUseCase`, `ListMembershipsUseCase`, `ListAppRolesUseCase`.
- **`keygo-supabase`**: `MembershipEntity`, `AppRoleEntity` (JPA), `MembershipJpaRepository`, `AppRoleJpaRepository` (Spring Data), `MembershipPersistenceMapper`, `MembershipRepositoryAdapter`, `AppRoleRepositoryAdapter`, migración `V7__add_memberships.sql` (tablas `app_role`, `membership`, `membership_role`).
- **`keygo-api`**: `TenantMembershipController` (3 endpoints: POST/GET/DELETE memberships), `TenantAppRoleController` (2 endpoints: POST/GET roles), DTOs (`CreateMembershipRequest`, `CreateAppRoleRequest`, `MembershipData`, `AppRoleData`), 6 nuevos `ResponseCode`, 3 handlers en `GlobalExceptionHandler`.
- **`keygo-run`**: 4 nuevos `@Bean` en `ApplicationConfig`.
- **Tests**: ~45 tests unitarios nuevos distribuidos en domain (18), app (8), api (6), supabase (5). Total proyecto: 210+ tests.
- **Postman**: carpeta `📋 Memberships` y `👥 Roles` con 5 requests (crear, listar, revocar memberships; crear, listar roles).
- **ROADMAP.md**: F-009 completada, Fase 4 marcada como completada en el plan, Sprint 2 cerrado.
- **Lección aprendida**: En controllers REST con DTOs, verificar siempre que el import de `BaseResponse` sea del subpaquete `.response` (`io.cmartinezs.keygo.api.shared.response.BaseResponse`), no directamente de `shared`.

### [2026-03-22] Documentación de flujo de autenticación del cliente — AUTH_FLOW.md
Se generó **bajo orden explícita del usuario** el documento `docs/keygo-server/AUTH_FLOW.md`:

- **`AUTH_FLOW.md`** (~350 líneas): guía completa del flujo OAuth 2.0 Authorization Code + PKCE desde la perspectiva del cliente (SPA/Mobile). Incluye:
  - Diagrama de prerrequisitos del sistema (Mermaid graph)
  - Diagrama de secuencia completo con actor Usuario, WebApp y KeyGo Server (Mermaid sequenceDiagram)
  - Algoritmo PKCE paso a paso con código JavaScript (navegador) y Swift (iOS)
  - HTTP request/response real para cada uno de los 3 endpoints
  - Flowcharts de validación internos por cada paso (Mermaid flowchart)
  - Tabla de errores con excepción, HTTP status y ResponseCode
  - Timeline de evolución Fase 5 → Fase 6 (Mermaid timeline)
  - Guía de implementación en TypeScript (SPA React) y Kotlin (Android/OkHttp)
  - Checklist de seguridad (PKCE, state, cookies, HTTPS)
- **`docs/keygo-server/README.md`** (actualizado): nueva sección "🔐 Seguridad y autenticación" con enlace al documento; nueva entrada de navegación "Cómo implementar el login en mi app".

**Documentación generada bajo orden explícita:** SÍ.

### [2026-03-22] Documentación de modelo de datos — 3 nuevos documentos + índice centralizado
Se generaron **bajo orden explícita del usuario** tres documentos de referencia técnica para el diccionario y modelo de datos:

- **`DATA_MODEL.md`** (1000 líneas): Diccionario completo (12 tablas), diagrama E/R Mermaid, jerarquía de cascade, 8 guías SQL de referencia, tabla de enumeraciones (ENUM), tabla de constraints únicos (PK, UK), índices recomendados con SQL
- **`ENTITY_RELATIONSHIPS.md`** (700 líneas): 5 contextos de negocio (class diagrams), OAuth2 Authorization Code Flow + PKCE (sequence diagram), Refresh Token flow, Token Revocation, ciclo de vida de Membership (state machine), asignación de roles, matriz de decisión de acceso (flowchart), capas lógicas de validación (graph), índices con SQL
- **`DATA_DICTIONARY.md`** (400 líneas): Índice centralizado, mapa de acceso por rol (Dev, Arquitecto, QA), vista de diagramas a nivel de detalle (30k-10k-3k pies), 3 ejemplos de uso reales, convenciones de nomenclatura, checklist de validación, referencias cruzadas con AGENTS.md/ARCHITECTURE.md
- **`README.md`** (actualizado): Índice de todos los documentos técnicos de `docs/keygo-server/`, tabla de referencias cruzadas, preguntas frecuentes, sugerencias de lectura por rol

**Diagramas:** 100% Mermaid (E/R, class, sequence, state, flowchart, graph). Todos natively soportados en GitHub, GitLab, Notion y editores comunes.

**Patrón de referencias cruzadas:** cada documento remite a AGENTS.md (quick-start), ARCHITECTURE.md (diseño), `docs/arch/keygo_server_domain_model.md` (bounded contexts) y `postman/KeyGo-Server.postman_collection.json` (ejemplos HTTP).

**Documentación generada bajo orden explícita:** SÍ. Estos 3 documentos + README son parte del ciclo de trabajo del agente (como `AI_CONTEXT.md`, `AGENTS.md` mismo) porque responden a la necesidad de base de conocimiento compartida, no documentación de producto.

### [2026-03-21] Cierre de Fase 0 — base de calidad completada
Se completaron los dos entregables pendientes de la Fase 0 (0.4 — base de calidad):
- **CI Pipeline**: creado `.github/workflows/ci.yml` con `./mvnw test` + `./mvnw clean package` en push/PR a `main`/`develop`. Sube artefactos de surefire si el build falla.
- **Maven Enforcer Plugin**: configurado en el `pom.xml` raíz, fase `validate`. Verifica Java 21+, Maven 3.9+, encoding UTF-8 y sin dependencias duplicadas.
- **Convención de estilo documentada**: `docs/keygo-server/CODE_STYLE.md` formaliza indentación 2 espacios, nombres de clases por tipo arquitectónico, orden de imports y reglas de tests. Lint automático (T-023) pendiente de ROADMAP.
- **ROADMAP.md**: T-006 movida a historial de completadas, T-023 agregada a corto plazo, estado del producto actualizado a "Fase 0 ✅ completa".
- **Plan de implementación**: `docs/arch/keygo_server_implementation_plan.md` actualizado con detalle de todos los sub-puntos completados de la Fase 0.


