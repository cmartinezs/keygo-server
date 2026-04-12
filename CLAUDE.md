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
keygo-common (shared utilities)
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

### REST Envelope

All endpoints return `BaseResponse<T>` except RFC/OIDC-native endpoints (JWKS, token, OIDC metadata).

## Response Style

Responses must be exact, concise, and precise. Avoid verbosity without sacrificing clarity.

## Index-Based Navigation

Documentation is hierarchical: every folder has a `README.md` with a section summary and a brief-description index of its documents.

- **Navigate indexes first.** Read the folder's `README.md` before opening individual files to confirm the right source.
- **Always identify a starting document.** If the user does not provide one, ask. If the task has no clear entry point, start from `doc/README.md` and cascade through indexes.
- **Do not explore files blindly.** If the index does not resolve the search, move to the parent level or the most relevant sibling section.
- **Maintain the pattern on every addition.** New file in an existing folder → update that folder's `README.md`. New folder → create its `README.md` with purpose summary and initial index. New top-level section → update `doc/README.md`.

## Mandatory Workflow Before Implementing

### 1. Prior analysis

Before any change, review existing documentation and code to determine:
- Equivalent already exists and is proven → **reuse it**.
- Exists but can improve → **refactor applying the appropriate design pattern**.
- Genuinely new → proceed to plan.

### 2. Solution plan

Always create a documented plan covering: problem/requirement, proposed solution (components, modules, patterns), ordered implementation steps, and status (`PENDING` | `APPLIED`).

**When `/plan` is invoked, persist the plan before closing plan mode — without waiting for explicit instruction** (plans may be implemented in a future conversation):

1. Add entry `T-NNN` in `doc/09-ai/proposals.md` (1–2 lines, status 🔲 Pendiente, link to detail doc).
2. Create `doc/09-ai/tasks/T-NNN-<slug>.md` with: requirement, response contract or component design, ordered steps with `PENDING`/`APPLIED` status, and a verification guide.
3. Register the new entry in `doc/09-ai/tasks/README.md`.

### 3. RFC for large changes

If the change affects multiple modules, public contracts, data model, or architecture, create an RFC in `doc/04-decisions/rfc/` with: context, detailed proposal (what/how/where), impact on modules/migrations/docs, acceptance criteria, and status (`DRAFT` | `APPROVED` | `APPLIED`).

### 4. Wait for explicit approval

**Do not begin any implementation** until the user explicitly states the plan and/or RFC should be applied.

### 5. Record detected future ideas

As a feature is implemented, it may naturally enable other future features. If such an opportunity is detected, **briefly register it** in `doc/09-ai/proposals.md` with: correlative ID (`T-NNN` / `F-NNN`), 1–2 line description (what it enables and why it makes sense), and status `🔲 Pendiente`. Do not develop the full analysis at that point — it stays pending until explicitly picked up.

## Task Closure and Context Compression

At the end of every task, before closing the conversation:

1. **Ask** the user what should be remembered more extensively (decisions, learnings, applied patterns, design changes, problems found).
2. **Compress and persist** — save only what is not derivable from code or git history:

| What | Where |
|---|---|
| Reusable learning or pattern | `lessons-learned.md` |
| Doc/code inconsistency | `inconsistencies.md` |
| Detected future proposal | `proposals.md` |
| Agent rule change | `agents.md` + `agents-change-log.md` |
| Effective architectural decision | ADR in `04-decisions/adr/` |

3. **Update indexes** — if a document was created or modified during closure, update the `README.md` of its folder. Keep content compressed: enough to guide a future search, not an exhaustive report.

## Critical Coding Rules

- **No Spring in `keygo-domain`** — zero framework dependencies.
- **No backward module dependencies** — never import from a downstream module.
- **Nullable domain fields → `Optional<T>`** — never expose raw nulls from domain.
- **New JPA entities: do not set `id`** — let JPA generate it.
- **Jackson 3** uses `tools.jackson.databind.*` (not `com.fasterxml`).
- **JPA entities**: use `@Getter @Setter @Builder`; never `@Data`.
- **JSONB columns**: `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`.
- **Flyway migrations**: `V<N>__<description>.sql` in `keygo-supabase/src/main/resources/db/migration/`. Current: V1–V33; next is V34.
- **Diagrams**: Mermaid first, PlantUML if Mermaid is insufficient, ASCII only as last resort.
- **Documentation**: all new docs go under `doc/`.

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

## Documentation Sources of Truth

| Topic | Document |
|---|---|
| Architecture | `doc/03-architecture/architecture.md` |
| Public/protected routes | `doc/03-architecture/security/bootstrap-filter.md` |
| OAuth2/OIDC flows | `doc/02-functional/authentication-flow.md` |
| Flyway migrations | `doc/08-reference/data/migrations.md` |
| Data model | `doc/08-reference/data/data-model.md` |
| Entity relationships | `doc/08-reference/data/entity-relationships.md` |
| Local setup | `doc/07-operations/environment-setup.md` |
| Frontend guide | `doc/02-functional/frontend/frontend-developer-guide.md` |
| Roadmap | `doc/05-delivery/roadmap.md` |
| AI agent quick-start | `doc/09-ai/agents.md` |

## Mandatory Documentation Maintenance

| Change | Update |
|---|---|
| New endpoint or HTTP contract change consumable by UI | OpenAPI + Postman + `doc/02-functional/frontend/frontend-developer-guide.md` |
| Contract change, `ResponseCode` change, or OAuth flow change affecting UI | `doc/02-functional/frontend/frontend-developer-guide.md` |
| New Flyway migration | `migrations.md` + `data-model.md` + `entity-relationships.md` |
| Agent rule or quick-start change | `agents.md` + `agents-change-log.md` |
| Doc/code inconsistency found | `inconsistencies.md` |
