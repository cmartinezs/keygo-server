# Shared Agent Operations

Política común para agentes AI que trabajan en este repositorio.

## Orden de lectura

1. [agents.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/agents.md)
2. [ai-context.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/09-ai/ai-context.md)
3. [architecture.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/03-architecture/architecture.md)
4. [roadmap.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/05-delivery/roadmap.md)
5. [doc/README.md](/C:/Users/cmartinezs/IdeaProjects/keygo-server/doc/README.md)

## Reglas de trabajo

- Leer documentos canónicos antes de tocar código o contratos.
- Toda documentación nueva debe vivir en `doc/`.
- No usar `99-archive/` como fuente primaria.
- Si una decisión ya es efectiva, dejar ADR o actualizar el existente.
- Si hay drift documental, registrarlo en `inconsistencies.md`.

## Regla de mantenimiento

| Cambio | Actualizar |
|---|---|
| Nuevo endpoint o contrato HTTP | OpenAPI, Postman y guía frontend |
| Nueva migración Flyway | `08-reference/data/migrations.md`, `data-model.md`, `entity-relationships.md` |
| Cambio de quick-start o reglas de agentes | `agents.md` y `agents-change-log.md` |
| Nuevo aprendizaje | `lessons-learned.md` |
| Nueva propuesta | `proposals.md` y, si aplica, `05-delivery/roadmap.md` |

## Checklist rápido

- `keygo-domain` sin Spring
- Nullable del dominio expuesto como `Optional<T>`
- Entidades JPA sin `@Data`
- JSONB con `@JdbcTypeCode(SqlTypes.JSON)`
- Seguridad admin con Bearer JWT
- Documentación viva bajo `doc/`
