# AGENTS.md - KeyGo Server

Quick-start tecnico resumido para agentes AI.

> Fuentes de verdad relacionadas:
> [`AI_CONTEXT.md`](AI_CONTEXT.md),
> [`ARCHITECTURE.md`](ARCHITECTURE.md),
> [`docs/README.md`](docs/README.md),
> [`docs/ai/AGENT_OPERATIONS.md`](docs/ai/AGENT_OPERATIONS.md)

## Mapa de modulos

```text
keygo-domain   <- dominio puro, sin Spring, sin dependencias internas
keygo-app      <- use cases + puertos OUT
keygo-infra    <- JWT signer, JWKS builder, PKCE verifier
keygo-api      <- controllers REST + DTOs + OpenAPI
keygo-supabase <- JPA + Flyway + PostgreSQL
keygo-run      <- main, wiring, seguridad y application.yml
keygo-bom      <- versionado de dependencias
keygo-common   <- utilidades compartidas
```

## Reglas criticas

- Nunca agregar Spring a `keygo-domain`.
- No cruzar dependencias hacia atras entre modulos.
- En dominio, todo campo nullable debe exponerse como `Optional<T>`.
- Al crear un agregado nuevo persistible, no setear `id`.
- Jackson 3 usa `tools.jackson.databind.*`.
- Entidades JPA: `@Getter @Setter @Builder`, nunca `@Data`.
- Columnas JSONB: `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`.
- `context-path` activo: `/keygo-server`.
- Seguridad admin vigente: `Authorization: Bearer <jwt>`.
- La siguiente migracion debe usar la siguiente version libre real; hoy la ultima presente es `V33`.

## Comandos esenciales

```bash
./mvnw clean package
./mvnw test
./mvnw verify
./mvnw -pl keygo-api test
./mvnw spring-boot:run -pl keygo-run

./docs/scripts/keygo.sh
./docs/scripts/quick-start.sh
./docs/scripts/switch-env.sh local
./docs/scripts/db/start.sh
./docs/scripts/db/migrate.sh
./docs/scripts/test-service-info.sh
./docs/scripts/test-response-codes.sh
```

## Convenciones API

- Controllers REST responden con `BaseResponse<T>` salvo endpoints RFC/OIDC nativos.
- `ResponseCode` es el catalogo de codigos de negocio en `keygo-api`.
- Controllers nuevos o modificados deben mantener OpenAPI completo.
- Si se crea o cambia un endpoint, actualizar tambien Postman y la guia frontend.

## Flujo hexagonal al agregar funcionalidad

1. Puerto OUT en `keygo-app/.../port/`
2. Use case en `keygo-app/.../usecase/`
3. Adapter en `keygo-run` o `keygo-supabase`
4. Wiring en `keygo-run/config/ApplicationConfig.java`
5. Controller y DTOs en `keygo-api`

## Donde esta cada fuente de verdad

| Tema | Documento |
|---|---|
| Politica documental y ubicacion de nuevos `.md` | [`docs/README.md`](docs/README.md) |
| Politica operativa compartida de agentes | [`docs/ai/AGENT_OPERATIONS.md`](docs/ai/AGENT_OPERATIONS.md) |
| Arquitectura | [`docs/design/ARCHITECTURE.md`](docs/design/ARCHITECTURE.md) |
| Seguridad de rutas y filtro bootstrap | [`docs/api/BOOTSTRAP_FILTER.md`](docs/api/BOOTSTRAP_FILTER.md) |
| Flujos OAuth2/OIDC | [`docs/api/AUTH_FLOW.md`](docs/api/AUTH_FLOW.md) |
| Migraciones Flyway | [`docs/data/MIGRATIONS.md`](docs/data/MIGRATIONS.md) |
| Modelo de datos | [`docs/data/DATA_MODEL.md`](docs/data/DATA_MODEL.md) |
| Relaciones de entidades | [`docs/data/ENTITY_RELATIONSHIPS.md`](docs/data/ENTITY_RELATIONSHIPS.md) |
| Setup local y variables | [`docs/development/ENVIRONMENT_SETUP.md`](docs/development/ENVIRONMENT_SETUP.md) |
| Testing | [`docs/development/TEST_STRATEGY.md`](docs/development/TEST_STRATEGY.md) |
| Inventario humano de endpoints | [`docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`](docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md) |
| Contrato runtime | `/v3/api-docs` |
| Roadmap | [`ROADMAP.md`](ROADMAP.md) |

## Actualizaciones documentales obligatorias

- Nuevo endpoint o cambio de contrato: OpenAPI + Postman + `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`.
- Nueva migracion Flyway: `docs/data/MIGRATIONS.md` + `docs/data/DATA_MODEL.md` + `docs/data/ENTITY_RELATIONSHIPS.md`.
- Cambio de comandos, reglas o quick-start para agentes: `AGENTS.md` + `docs/ai/agents-registro.md`.
- Inconsistencia detectada entre docs y codigo: `docs/ai/inconsistencias.md`.

## Referencias AI

- [`docs/ai/lecciones.md`](docs/ai/lecciones.md)
- [`docs/ai/propuestas.md`](docs/ai/propuestas.md)
- [`docs/ai/inconsistencias.md`](docs/ai/inconsistencias.md)
- [`docs/ai/agents-registro.md`](docs/ai/agents-registro.md)

## Que no debe vivir aqui

- Inventario exhaustivo de endpoints
- Historial detallado por fechas
- Ledger completo de migraciones
- Indice historico del repo

Ese detalle vive en los documentos tematicos canónicos.
