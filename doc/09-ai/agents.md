# AGENTS - KeyGo Server

Quick-start técnico para agentes que trabajan en este repositorio.

## Leer primero

1. [ai-context.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/ai-context.md)
2. [architecture.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/03-architecture/architecture.md)
3. [doc/README.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/README.md)
4. [agent-operations.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/agent-operations.md)

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
| Política documental | [doc/README.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/README.md) |
| Arquitectura | [03-architecture/architecture.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/03-architecture/architecture.md) |
| Seguridad de rutas | [03-architecture/security/bootstrap-filter.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/03-architecture/security/bootstrap-filter.md) |
| Flujos OAuth2/OIDC | [02-functional/authentication-flow.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/02-functional/authentication-flow.md) |
| Migraciones | [08-reference/data/migrations.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/08-reference/data/migrations.md) |
| Modelo de datos | [08-reference/data/data-model.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/08-reference/data/data-model.md) |
| Relaciones de entidades | [08-reference/data/entity-relationships.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/08-reference/data/entity-relationships.md) |
| Setup local | [07-operations/environment-setup.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/07-operations/environment-setup.md) |
| Guía frontend | [02-functional/frontend/frontend-developer-guide.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/02-functional/frontend/frontend-developer-guide.md) |
| Roadmap | [05-delivery/roadmap.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/05-delivery/roadmap.md) |

## Mantenimiento documental obligatorio

- Nuevo endpoint o cambio de contrato: OpenAPI + Postman + guía frontend.
- Nueva migración Flyway: `migrations.md` + `data-model.md` + `entity-relationships.md`.
- Cambio de reglas o quick-start para agentes: `agents.md` + `agents-change-log.md`.
- Inconsistencia doc-código: `inconsistencies.md`.
