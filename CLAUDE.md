# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
./mvnw clean package -DskipTests
./mvnw clean install

# Run application
./mvnw spring-boot:run -pl keygo-run

# Run all tests
./mvnw test

# Run tests for a specific module
./mvnw -pl keygo-api test

# Run a single test class
./mvnw test -Dtest=AuthorizationControllerTest

# Run a single test method
./mvnw test -Dtest=AuthorizationControllerTest#methodName

# Full verify with coverage
./mvnw clean verify

# Checkstyle
./mvnw checkstyle:check
```

Local URLs (context-path `/keygo-server`, port 8080):
- Swagger UI: `http://localhost:8080/keygo-server/swagger-ui/index.html`
- Health: `http://localhost:8080/keygo-server/actuator/health`

Local development uses H2 in-memory DB (profile `local` or `h2`). Production targets PostgreSQL via Supabase (profile `supabase`).

## Architecture

KeyGo Server is a multi-tenant IAM (Identity & Access Management) backend following **hexagonal architecture** (ports & adapters). It is a Maven multi-module project on Java 21 + Spring Boot 4.x + Jackson 3.

### Module Dependency Flow

```
keygo-domain ← keygo-app ← keygo-infra
                          ← keygo-api
                          ← keygo-supabase
                                         ← keygo-run (entry point, wiring)
keygo-bom (BOM, no logic)
keygo-common (shared utilities — currently an empty stub, no src/ yet)
```

| Module | Role |
|---|---|
| `keygo-domain` | Pure domain: entities, aggregates, value objects, exceptions. **No Spring.** |
| `keygo-app` | Use cases, commands/results, outbound port interfaces. |
| `keygo-infra` | Cross-cutting adapters: JWT signing, JWKS builder, PKCE verifier, email. |
| `keygo-api` | REST controllers, DTOs, OpenAPI config, exception handling, `BaseResponse<T>` envelope. |
| `keygo-supabase` | JPA entities, Flyway migrations, repository adapters, PostgreSQL. |
| `keygo-run` | `main`, Spring Security filter chain, bean wiring, `application.yml`. |

### Domain Bounded Contexts

`auth`, `user`, `tenant`, `membership`, `clientapp`, `billing`, `role`

### Request Flow

```
HTTP → Filters/Security (keygo-run) → Controller (keygo-api) → Use Case (keygo-app) → Port → Adapter (keygo-supabase / keygo-infra)
```

### Security Model

`BootstrapAdminKeyFilter` (in `keygo-run`) is the single JWT authentication entry point for all protected paths. It validates the Bearer token, extracts the `roles` claim, and sets the `Authentication` in the `SecurityContextHolder`. Spring Security's `SecurityFilterChain` is configured with `anyRequest().permitAll()` — authorization is enforced at the method level via `@PreAuthorize`.

For tenant-scoped controllers, `@PreAuthorize` combines role checks with `@tenantAuthorizationEvaluator.hasTenantAccess(authentication)`, which verifies that the JWT's `tenant_slug` claim (or the tenant slug parsed from the `iss` URL) matches the `{tenantSlug}` path variable.

Platform-scoped endpoints (under `/api/v1/platform/`) issue tokens with `iss` pointing to the platform base URL (not a tenant URL), so they do not pass `hasTenantAccess` for tenant endpoints and must have their own platform equivalents.

### REST Envelope

All endpoints return `BaseResponse<T>` except RFC/OIDC-native endpoints (JWKS, token, OIDC metadata).

## Response Style

Responses must be exact, concise, and precise. Avoid verbosity without sacrificing clarity.

## Mandatory Workflow Before Implementing

### 1. Prior analysis

Before any change, review existing code to determine:
- Equivalent already exists and is proven → **reuse it**.
- Exists but can improve → **refactor applying the appropriate design pattern**.
- Genuinely new → proceed to plan.

### 2. Planning system

This repo uses a planning system under `.planning/`. **Nothing is executed without being inside a planning.**

- Active plannings are tracked in `.planning/active/`
- Before implementing a non-trivial change, identify which active scope covers it or create a new planning entry
- If a task is requested without a planning context and no bypass parameter is given: stop, ask whether it belongs to an existing planning or requires a new one
- Bypass parameters: `--no-plan` (ask for confirmation then execute) / `--no-plan-force` (execute directly)

### 3. Solution plan

Always create a documented plan covering: problem/requirement, proposed solution (components, modules, patterns), ordered implementation steps, and status (`PENDING` | `APPLIED`).

### 4. RFC for large changes

If the change affects multiple modules, public contracts, the data model, or architecture, discuss and document the RFC before implementation.

### 5. Wait for explicit approval

**Do not begin any implementation** until the user explicitly states the plan and/or RFC should be applied.

## Critical Coding Rules

- **No Spring in `keygo-domain`** — zero framework dependencies.
- **No backward module dependencies** — never import from a downstream module.
- **Optional for every nullable field** — any field that can be absent must be declared as `Optional<T>` (not raw `T`) and initialized with `Optional.ofNullable(...)`. This applies in all layers: domain, app, response DTOs, and adapters. Callers must use `.orElse(null)` or `.ifPresent(...)` at the boundary where a raw value is required (e.g., JPA mapping, HTTP response serialization).
- **New JPA entities: do not set `id`** — let JPA generate it.
- **Jackson 3** — the databind API moved to `tools.jackson.databind.*` (e.g. `JsonMapper`, `DeserializationFeature`, `PropertyNamingStrategies`). Jackson *annotations* (`@JsonProperty`, `@JsonInclude`) remain under `com.fasterxml.jackson.annotation.*` and have not moved.
- **JPA entities**: use `@Getter @Setter @Builder`; never `@Data`.
- **JSONB columns**: `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`.
- **Flyway migrations**: `V<N>__<description>.sql` in `keygo-supabase/src/main/resources/db/migration/`. Current: V1–V31; next is V32.
- **Diagrams**: Mermaid first, PlantUML if Mermaid is insufficient, ASCII only as last resort.

## Naming Conventions

| Concept | Convention |
|---|---|
| Use case input | `FooCommand` |
| Use case output | `FooResult` |
| Outbound port | `FooPort` |
| Port implementation | `FooAdapter` |
| Repository adapter | `FooRepositoryAdapter` |
| REST controller | `FooController` |

## Code Quality

- **Checkstyle**: Google Java Format style — 2-space indent, 120-char line limit, no star imports, no `System.out.println`. Warnings enabled; does not fail build.
- **JaCoCo coverage**: 60% instruction coverage threshold by default (15% for `keygo-supabase`). Excludes `*Application`, `*Config`, `*Properties`, `*Entity` classes.

## Documentation

Technical documentation (architecture, data model, API reference, etc.) lives in a **separate repository** (`keygo-docs`) — it does not exist under `doc/` in this repo. Do not create or reference `doc/` paths in this repo.

The `docs/` directory at the root is GitHub Pages output — do not edit it directly.

The `.planning/` directory contains the project's planning system (active and finished plannings, workflows, glossary). It is the source of truth for ongoing work.

## Mandatory Documentation Maintenance

| Change | Update |
|---|---|
| New endpoint or HTTP contract change consumable by UI | OpenAPI spec + Postman collection (`postman/`) + notify frontend team |
| New Flyway migration | Update migration count in this file (Critical Coding Rules section) |
