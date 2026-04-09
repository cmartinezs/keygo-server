# AI Context - KeyGo Server

Snapshot operativo rápido para agentes. No reemplaza la arquitectura detallada ni el quick-start.

## TL;DR

- Repo: monorepo Maven multi-módulo.
- Stack: Java 21 + Spring Boot 4.x + Jackson 3.
- Arquitectura: hexagonal / ports and adapters.
- Ejecutable: `keygo-run`.
- Persistencia: `keygo-supabase` con JPA, Flyway y PostgreSQL.
- Seguridad admin vigente: `Authorization: Bearer <jwt>`.
- Migraciones reales presentes: `V1` a `V33`; la siguiente debe ser `V34__...`.

## Módulos activos

| Módulo | Rol |
|---|---|
| `keygo-domain` | Dominio puro |
| `keygo-app` | Use cases + puertos |
| `keygo-infra` | JWT, JWKS, PKCE y adaptadores transversales |
| `keygo-api` | REST, DTOs, errores, OpenAPI |
| `keygo-supabase` | JPA, Flyway, repositorios |
| `keygo-run` | Main, wiring, seguridad, `application.yml` |

## Decisiones técnicas activas

- `context-path`: `/keygo-server`
- Envelope REST: `BaseResponse<T>` salvo endpoints RFC/OIDC que devuelven JSON nativo
- Jackson 3 usa `tools.jackson.databind.*`
- `keygo-domain` no debe depender de Spring ni de otros módulos internos
- Campos nullable en dominio deben exponerse como `Optional<T>`
- Nuevos agregados persistidos no deben setear `id`; Hibernate genera UUID
- Columnas JSONB JPA requieren `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`

## Seguridad vigente

- `BootstrapAdminKeyFilter` ya no usa `X-KEYGO-ADMIN`
- Rutas protegidas usan Bearer JWT y authorities desde claim `roles`
- `ADMIN_TENANT` valida alcance contra `tenant_slug` del token o `iss` fallback
- CORS se configura por `keygo.cors.*`

## Comandos esenciales

```bash
./mvnw clean package
./mvnw test
./mvnw verify
./mvnw spring-boot:run -pl keygo-run
./docs/scripts/keygo.sh
./docs/scripts/quick-start.sh
```

## Fuentes de verdad

| Tema | Documento |
|---|---|
| Índice documental y política de ubicación | [`docs/README.md`](docs/README.md) |
| Quick-start técnico para agentes | [`AGENTS.md`](AGENTS.md) |
| Operación compartida de agentes | [`docs/ai/AGENT_OPERATIONS.md`](docs/ai/AGENT_OPERATIONS.md) |
| Arquitectura | [`docs/design/ARCHITECTURE.md`](docs/design/ARCHITECTURE.md) |
| Seguridad de rutas | [`docs/api/BOOTSTRAP_FILTER.md`](docs/api/BOOTSTRAP_FILTER.md) |
| Migraciones Flyway | [`docs/data/MIGRATIONS.md`](docs/data/MIGRATIONS.md) |
| Setup local | [`docs/development/ENVIRONMENT_SETUP.md`](docs/development/ENVIRONMENT_SETUP.md) |
| Inventario humano de endpoints | [`docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`](docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md) |
| Contrato runtime | `/v3/api-docs` |

## Memoria AI

- [`docs/ai/lecciones.md`](docs/ai/lecciones.md)
- [`docs/ai/propuestas.md`](docs/ai/propuestas.md)
- [`docs/ai/inconsistencias.md`](docs/ai/inconsistencias.md)
- [`docs/ai/agents-registro.md`](docs/ai/agents-registro.md)

## Qué no debe vivir aquí

- Inventarios exhaustivos de endpoints
- Detalle completo de migraciones
- Política duplicada de wrappers de agentes
- Historia o RFCs detallados

Para eso, enlazar a los documentos canónicos y no duplicarlos aquí.
