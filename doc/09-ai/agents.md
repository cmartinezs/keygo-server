# AGENTS - KeyGo Server

Quick-start técnico para agentes que trabajan en este repositorio.

## Leer primero

1. [ai-context.md](ai-context.md)
2. [architecture.md](../03-architecture/architecture.md)
3. [doc/README.md](../README.md)
4. [agent-operations.md](agent-operations.md)

## Mapa de módulos

```text
keygo-domain   <- dominio puro, sin Spring
keygo-app      <- use cases + puertos OUT
keygo-infra    <- JWT signer, JWKS builder, PKCE verifier
keygo-api      <- controllers REST + DTOs + OpenAPI
keygo-supabase <- JPA + Flyway + PostgreSQL
keygo-run      <- main, wiring, seguridad y application.yml
keygo-bom      <- versionado de dependencias
keygo-common   <- utilidades compartidas
```

## Reglas críticas

- Nunca agregar Spring a `keygo-domain`.
- No cruzar dependencias hacia atrás entre módulos.
- En dominio, todo campo nullable debe exponerse como `Optional<T>`.
- Al crear un agregado nuevo persistible, no setear `id`.
- Jackson 3 usa `tools.jackson.databind.*`.
- Entidades JPA: `@Getter @Setter @Builder`, nunca `@Data`.
- Columnas JSONB: `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`.
- `context-path` activo: `/keygo-server`.
- Seguridad admin vigente: `Authorization: Bearer <jwt>`.

## Comandos útiles

```bash
./mvnw clean package
./mvnw test
./mvnw verify
./mvnw -pl keygo-api test
./mvnw spring-boot:run -pl keygo-run
```

## Fuentes de verdad

| Tema | Documento |
|---|---|
| Política documental | [doc/README.md](../README.md) |
| Arquitectura | [03-architecture/architecture.md](../03-architecture/architecture.md) |
| Seguridad de rutas | [03-architecture/security/bootstrap-filter.md](../03-architecture/security/bootstrap-filter.md) |
| Flujos OAuth2/OIDC | [02-functional/authentication-flow.md](../02-functional/authentication-flow.md) |
| Migraciones | [08-reference/data/migrations.md](../08-reference/data/migrations.md) |
| Modelo de datos | [08-reference/data/data-model.md](../08-reference/data/data-model.md) |
| Relaciones de entidades | [08-reference/data/entity-relationships.md](../08-reference/data/entity-relationships.md) |
| Setup local | [07-operations/environment-setup.md](../07-operations/environment-setup.md) |
| Guía frontend | [02-functional/frontend/frontend-developer-guide.md](../02-functional/frontend/frontend-developer-guide.md) |
| Roadmap | [05-delivery/roadmap.md](../05-delivery/roadmap.md) |

## Mantenimiento documental obligatorio

- Nuevo endpoint o cambio de contrato: OpenAPI + Postman + guía frontend.
- Nueva migración Flyway: `migrations.md` + `data-model.md` + `entity-relationships.md`.
- Cambio de reglas o quick-start para agentes: `agents.md` + `agents-change-log.md`.
- Inconsistencia doc-código: `inconsistencies.md`.
