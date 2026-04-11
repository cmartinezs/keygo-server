# AI Context - KeyGo Server

Snapshot operativo rápido para agentes.

## TL;DR

- Repo: monorepo Maven multi-módulo.
- Stack: Java 21 + Spring Boot 4.x + Jackson 3.
- Arquitectura: hexagonal / ports and adapters.
- Seguridad: Bearer JWT en rutas protegidas.
- Persistencia: PostgreSQL + JPA + Flyway.

## Modulos activos

| Modulo | Rol |
|---|---|
| `keygo-domain` | Dominio puro |
| `keygo-app` | Use cases + puertos |
| `keygo-infra` | JWT, JWKS, PKCE y adaptadores transversales |
| `keygo-api` | REST, DTOs, errores, OpenAPI |
| `keygo-supabase` | JPA, Flyway, repositorios |
| `keygo-run` | Main, wiring, seguridad, `application.yml` |

## Decisiónes técnicas activas

- `context-path`: `/keygo-server`
- Envelope REST: `BaseResponse<T>` salvo endpoints RFC/OIDC nativos
- `keygo-domain` no depende de Spring
- Los documentos vivos del proyecto viven en `doc/`

## Fuentes de verdad

| Tema | Documento |
|---|---|
| Índice documental | [doc/README.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/README.md) |
| Quick-start agentes | [agents.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/agents.md) |
| Operación compartida | [agent-operations.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/agent-operations.md) |
| Arquitectura | [03-architecture/architecture.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/03-architecture/architecture.md) |
| Seguridad de rutas | [03-architecture/security/bootstrap-filter.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/03-architecture/security/bootstrap-filter.md) |
| Migraciones | [08-reference/data/migrations.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/08-reference/data/migrations.md) |
| Setup local | [07-operations/environment-setup.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/07-operations/environment-setup.md) |

## Memoria AI

- [lessons-learned.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/lessons-learned.md)
- [proposals.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/proposals.md)
- [inconsistencies.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/inconsistencies.md)
- [agents-change-log.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/agents-change-log.md)

