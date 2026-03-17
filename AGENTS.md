# AGENTS.md — KeyGo Server

> Quick-start guide for AI coding agents. Full context in `AI_CONTEXT.md`, `ARCHITECTURE.md`, `CLAUDE.md`.

## Module map & dependency rules

```
keygo-domain   ← pure Java, NO Spring, NO internal deps  [🚧 stub]
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
./mvnw test                            # all tests
./mvnw -pl keygo-api test              # single module
./mvnw spring-boot:run -pl keygo-run   # run locally

# Verificar actividad de retroalimentación del agente AI
./scripts/check-ai-docs.sh            # umbral por defecto: 30 días
./scripts/check-ai-docs.sh --days 60  # umbral personalizado
./scripts/check-ai-docs.sh --quiet    # solo exit code (útil en CI)
# Exit: 0=OK  1=sin actividad reciente  2=sin entradas  3=archivo no encontrado
```

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

## Hexagonal flow: adding a feature

1. **Port OUT** — interface in `keygo-app/src/main/java/.../port/out/`  
   (example: `ServiceInfoProvider`)
2. **UseCase** — plain Java class in `keygo-app/src/main/java/.../usecase/`  
   (example: `GetServiceInfoUseCase`)
3. **Adapter (impl)** — `@ConfigurationProperties` or Spring component in `keygo-run` or `keygo-supabase`  
   (example: `ServiceInfoProperties implements ServiceInfoProvider`)
4. **Wiring** — `@Bean` factory in `keygo-run/config/ApplicationConfig.java`
5. **Controller** — `@RestController` in `keygo-api`, path `/api/v1/<resource>/...`

## context-path is always active

All endpoints are served under `/keygo-server`. Local URLs:
- `http://localhost:8080/keygo-server/api/v1/service/info`
- `http://localhost:8080/keygo-server/actuator/health`

⚠️ **Known bug — `BootstrapAdminKeyFilter`:** uses `request.getRequestURI()` (returns `/keygo-server/api/...`) but path prefixes in `application.yml` are `/api/`, `/actuator/` (no context-path prefix) → **filter never matches; all routes are currently public**. Fix: use `request.getServletPath()` instead.

## Security header

Protected routes require `X-KEYGO-ADMIN: <value of KEYGO_ADMIN_KEY>`.  
Default dev key: `changeMe` — **never use in production**.

## JPA entities (keygo-supabase)

Use `UUID` PK with `@GeneratedValue(strategy = GenerationType.UUID)`, `@CreationTimestamp`/`@UpdateTimestamp` for timestamps, Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`. Schema is managed by Flyway (`db/migration/V<n>__description.sql`). `ddl-auto: validate`.

## Enabling the database

```bash
export SPRING_PROFILES_ACTIVE="supabase,local"
export SUPABASE_URL="jdbc:postgresql://localhost:5432/keygo"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="postgres"
cd keygo-supabase && ./scripts/dev-start.sh   # starts Postgres + PgAdmin via Docker
```

## Testing conventions

- Unit tests: `@ExtendWith(MockitoExtension.class)` + AssertJ + Mockito — **no Spring context**.
- Integration tests (supabase): Testcontainers PostgreSQL.
- Pattern: Given/When/Then comments in every test method.

## Git — never execute directly

List suggested `git` commands for the user; do not run them.

## Docs — only on explicit request

Do not auto-generate or modify `.md` files unless the user explicitly asks. Place docs in `docs/<module>/` or the project root as appropriate.

## Registro de cambios

> Historial de actualizaciones del quick-start. El agente debe agregar una entrada aquí cada vez
> que cambie la estructura de módulos, comandos, patrones o URLs de referencia rápida.
> Formato: `### [YYYY-MM-DD] Descripción del cambio`

### [2026-03-17] Creación inicial + script check-ai-docs.sh
Generación del archivo de guía rápida para agentes AI. Se agregó `check-ai-docs.sh` a los
comandos esenciales (flags `--days`, `--quiet`, `--help`; códigos de salida 0-3). Se extendió
el script para verificar también `AGENTS.md → ## Registro de cambios`.

