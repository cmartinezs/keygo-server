# AGENTS.md — KeyGo Server

> Quick-start guide for AI coding agents. Full context in `AI_CONTEXT.md`, `ARCHITECTURE.md`, `CLAUDE.md`, `ROADMAP.md`.

## Module map & dependency rules

```
keygo-domain   ← pure Java, NO Spring, NO internal deps  [Tenant model ✅]
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

## context-path is always active

All endpoints are served under `/keygo-server`. Local URLs:
- `http://localhost:8080/keygo-server/api/v1/service/info`
- `http://localhost:8080/keygo-server/api/v1/response-codes`
- `http://localhost:8080/keygo-server/api/v1/tenants` (POST — create)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}` (GET — retrieve)
- `http://localhost:8080/keygo-server/api/v1/tenants/{slug}/suspend` (PUT — suspend)
- `http://localhost:8080/keygo-server/actuator/health`

⚠️ **Known bug — `BootstrapAdminKeyFilter`:** uses `request.getRequestURI()` (returns `/keygo-server/api/...`) but path prefixes in `application.yml` are `/api/`, `/actuator/`, `/service/info` (no context-path prefix) → **filter never matches; all routes are currently public**. Fix: use `request.getServletPath()` instead.

The filter has three path categories (see `KeyGoBootstrapProperties`):

| Property | `application.yml` value | Behaviour |
|---|---|---|
| `keygo.bootstrap.api-path-prefix` | `/api/` | Protected — requires `X-KEYGO-ADMIN` |
| `keygo.bootstrap.actuator-path-prefix` | `/actuator/` | Public |
| `keygo.bootstrap.service-info-path-prefix` | `/service/info` | Public |

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

**Existing repositories (packages under `io.cmartinezs.keygo.supabase`):**

| Repository | Package |
|---|---|
| `UserRepository` | `user.repository` |
| `RoleRepository` | `membership.repository` |
| `TenantJpaRepository` | `tenant.repository` |

**Flyway migrations already applied:**
- `V1__initial_schema.sql` — users, roles, user_roles, permissions, role_permissions tables
- `V2__seed_data.sql` — seed data
- `V3__add_oauth_support.sql` — oauth_providers, oauth_tokens tables
- `V4__add_tenants.sql` — tenants table (slug unique, status check constraint)

Next migration must be `V5__...`. **Never reuse or edit existing migration files.**

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
| 1 | Multitenancy (`Tenant`, `TenantRepositoryPort`, resolver) | 🔄 In progress (2026-03-21) |
| 2 | Client app model (`ClientApp`, redirect URIs, grants) | — |
| 3 | User identity per tenant | — |
| 4 | Memberships & roles per app | — |
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

### [2026-03-21] JaCoCo implementado — comando de CI cambiado a `./mvnw verify`
Se configuró JaCoCo 0.8.12 en el POM raíz con tres ejecuciones heredadas a todos los módulos:
`prepare-agent` (antes de tests), `report` (HTML/XML por módulo) y `check` (umbral 60% instrucciones).
Módulos stub (`keygo-infra`, `keygo-common`) marcados con `jacoco.skip=true`.
`keygo-run` genera el reporte agregado consolidado vía `report-aggregate`.
CI actualizado: `./mvnw test` → `./mvnw verify`; se sube `jacoco-reports` como artefacto.
T-016 movida a historial de completadas en ROADMAP.md.

### [2026-03-21] Convenciones de estilo adoptadas — corrección masiva
Se aplicaron las siguientes convenciones de estilo a todo el codebase:
- **JavaDoc sin líneas en blanco**: las líneas en blanco dentro de bloques `/** */` se reemplazan con la etiqueta `<p>` para separar párrafos; las líneas en blanco antes de tags (`@param`, `@return`, `@throws`, `@author`) se eliminan.
- **JavaDoc solo en clases y métodos**: los atributos/campos usan comentario simple `/* */` en vez de `/** */`. Aplica también a componentes de records, constantes de enums y campos de `@ConfigurationProperties`.
- **Lombok en domain**: se agregó Lombok como dependencia `provided` a `keygo-domain`. La clase `Tenant` fue refactorizada: builder manual eliminado, reemplazado por `@Builder` en el constructor privado + `@Getter` en la clase.
- **Entidades JPA sin `@Data`**: `TenantEntity`, `UserEntity`, `RoleEntity` y `PermissionEntity` cambiaron de `@Data` a `@Getter @Setter` para evitar problemas de performance (lazy loading, equals/hashCode sobre colecciones).
- **`@SuppressWarnings("NullableProblems")` en `toString()`**: aplicado en `Tenant`, `TenantId` y `TenantSlug` para resolver el warning de IntelliJ sobre métodos `@Override` que no están anotados con `@NotNull`.
- **Tests sin literales duplicados**: `CreateTenantUseCaseTest` y `TenantTest` extraen los valores de prueba a constantes `private static final` para eliminar alertas de refactorización de IntelliJ.

### [2026-03-21] Fase 1 — Núcleo de multitenancy implementado
Se implementó el núcleo de multitenancy en los cinco módulos activos:
- **`keygo-domain`**: entidades de dominio puras `Tenant`, `TenantId`, `TenantSlug`, `TenantStatus`; excepciones `TenantNotFoundException` y `TenantSuspendedException`. Sin Spring ni JPA.
- **`keygo-app`**: puerto `TenantRepositoryPort`, comando `CreateTenantCommand`, casos de uso `CreateTenantUseCase`, `GetTenantBySlugUseCase`, `SuspendTenantUseCase`; `TenantContextHolder` (ThreadLocal sin Spring).
- **`keygo-supabase`**: `TenantEntity` (JPA), `TenantJpaRepository` (Spring Data), `TenantPersistenceMapper`, `TenantRepositoryAdapter` (`@Repository`), migración `V4__add_tenants.sql`.
- **`keygo-api`**: `PlatformTenantController` con 3 endpoints (`POST /api/v1/tenants`, `GET /api/v1/tenants/{slug}`, `PUT /api/v1/tenants/{slug}/suspend`), `CreateTenantRequest` (record con `@Valid`), `TenantData` (Lombok builder), nuevos `ResponseCode` (`TENANT_CREATED`, `TENANT_RETRIEVED`, `TENANT_SUSPENDED`), handlers en `GlobalExceptionHandler` para `TenantNotFoundException` → 404 y `TenantSuspendedException` → 403.
- **`keygo-run`**: wiring de los 3 use cases en `ApplicationConfig`, `TenantResolutionFilter` (header `X-Tenant-Slug` → valida tenant → guarda en `TenantContextHolder`).
- **Tests**: 128+ tests unitarios en total; +39 nuevos (28 domain, 8 app, 4 api, 4 supabase, 4 run).
- **Lección aprendida**: `jakarta.validation-api` debe declararse explícitamente en `keygo-api/pom.xml`.

### [2026-03-21] Cierre de Fase 0 — base de calidad completada
Se completaron los dos entregables pendientes de la Fase 0 (0.4 — base de calidad):
- **CI Pipeline**: creado `.github/workflows/ci.yml` con `./mvnw test` + `./mvnw clean package` en push/PR a `main`/`develop`. Sube artefactos de surefire si el build falla.
- **Maven Enforcer Plugin**: configurado en el `pom.xml` raíz, fase `validate`. Verifica Java 21+, Maven 3.9+, encoding UTF-8 y sin dependencias duplicadas.
- **Convención de estilo documentada**: `docs/keygo-server/CODE_STYLE.md` formaliza indentación 2 espacios, nombres de clases por tipo arquitectónico, orden de imports y reglas de tests. Lint automático (T-023) pendiente de ROADMAP.
- **ROADMAP.md**: T-006 movida a historial de completadas, T-023 agregada a corto plazo, estado del producto actualizado a "Fase 0 ✅ completa".
- **Plan de implementación**: `docs/arch/keygo_server_implementation_plan.md` actualizado con detalle de todos los sub-puntos completados de la Fase 0.

### [2026-03-21] Actualización tras análisis del codebase (segunda ronda)
Se identificaron y corrigieron cinco brechas entre el doc y el código real:
- **Jackson 3 annotation namespace**: aclarado que `com.fasterxml.jackson.annotation.*` (ej. `@JsonInclude`) sigue compilando; solo `databind.*` y `datatype.*` se movieron a `tools.jackson.*`. Se actualizó el bloque de código con un ✅ para el annotation import.
- **`BootstrapAdminKeyFilter` — tercer prefijo**: se documentó el nuevo `serviceInfoPathPrefix` (`/service/info`) como ruta pública adicional. La tabla de propiedades ahora refleja las tres categorías que existen en `KeyGoBootstrapProperties` y `application.yml`.
- **`KeyGoBootstrapProperties` `@AssertTrue` validation**: se documentó que la app falla al arrancar si `enabled=true` y `adminKey` es nulo/en blanco. La solución en tests es `keygo.bootstrap.enabled=false`.
- **Tests de integración en keygo-supabase**: corregida afirmación "Testcontainers PostgreSQL" — Testcontainers está en el pom.xml y `application-test.yml` (TC JDBC URL) pero los tests de integración no se han escrito. `UserRepositoryTest` es un test unitario puro con el builder de Lombok.
- **Plan de implementación**: se agregó sección `## Implementation plan` con tabla de las 11 fases (Fase 0 completa, Fase 1 = multitenancy es la siguiente) y referencia a `docs/arch/keygo_server_implementation_plan.md`.

### [2026-03-17] Creación de ROADMAP.md y referencias en docs AI
Se creó `ROADMAP.md` en la raíz del repositorio con 22 propuestas técnicas (T-001 a T-022) y 38 propuestas funcionales (F-001 a F-038) organizadas por horizonte y fase de implementación. Se actualizaron `AI_CONTEXT.md` y `AGENTS.md` para referenciar y mantener el archivo. La sección `## Propuestas de mejoras futuras` de `AI_CONTEXT.md` ahora delega al ROADMAP.md como fuente principal.

### [2026-03-17] Reorganización de paquetes internos por feature
Se reorganizaron los paquetes de cuatro módulos de organización técnica genérica a organización por feature:
- **`keygo-app`**: `port/out/` + `usecase/` → `platform/port/` + `platform/usecase/`
- **`keygo-api`**: `constant/` + `helper/` + `dto/reponse/` + `controller/` + `exception/` → `shared/` + `shared/response/` + `platform/controller/` + `platform/response/` + `error/`  
  Typo histórico `dto/reponse/` corregido a `shared/response/`.
- **`keygo-supabase`**: `entity/` + `repository/` → `user/entity/` + `user/repository/` + `membership/entity/` + `membership/repository/`
- **`keygo-run`**: solo actualización de imports; `KeyGoRunner` renombrado a `KeygoApplication`.
- **`SupabaseJpaConfig`**: `@EntityScan` y `@EnableJpaRepositories` cambiados a `basePackages = "io.cmartinezs.keygo.supabase"`.
- **IntelliJ run config** `.run/KeyGo Runner.run.xml`: referencia a `KeygoApplication` actualizada.

### [2026-03-17] Creación inicial + script check-ai-docs.sh
Generación del archivo de guía rápida para agentes AI. Se agregó `check-ai-docs.sh` a los
comandos esenciales (flags `--days`, `--quiet`, `--help`; códigos de salida 0-3). Se extendió
el script para verificar también `AGENTS.md → ## Registro de cambios`.

### [2026-03-17] Actualización tras análisis del codebase
Se agregaron las siguientes secciones y datos faltantes identificados al comparar el doc con el código real:
- **Spring Boot 4.0.3 + Jackson 3.x**: nueva sección con los imports correctos (`tools.jackson.*`) y los incorrectos (`com.fasterxml.jackson.*`). Crítico para compilación.
- **GlobalExceptionHandler**: tabla de excepciones → HTTP status → `ResponseCode` mapeadas por el `@RestControllerAdvice` en `keygo-api`.
- **`GET /api/v1/response-codes`**: endpoint existente (`ResponseCodeController`) agregado a la lista de URLs locales.
- **Entidades JPA existentes**: `UserEntity`, `RoleEntity`, `PermissionEntity`, `UserRepository`, `RoleRepository`, más migraciones V1/V2/V3 ya aplicadas y `SupabaseJpaConfig`.
- **`keygo.bootstrap.enabled=false`**: forma de deshabilitar el filtro completamente (útil en tests).
- **Scripts de utilidad**: `quick-start.sh`, `test-service-info.sh`, `test-response-codes.sh` y `dev-stop.sh`.

