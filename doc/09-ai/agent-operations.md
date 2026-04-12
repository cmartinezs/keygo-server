# Shared Agent Operations

Política común para agentes AI que trabajan en este repositorio.

## Orden de lectura

1. [agents.md](agents.md)
2. [ai-context.md](ai-context.md)
3. [architecture.md](../03-architecture/architecture.md)
4. [roadmap.md](../05-delivery/roadmap.md)
5. [doc/README.md](../README.md)

## Reglas de trabajo

- Leer documentos canónicos antes de tocar código o contratos.
- Toda documentación nueva debe vivir en `doc/`.
- No usar `99-archive/` como fuente primaria.
- Si una decisión ya es efectiva, dejar ADR o actualizar el existente.
- Si hay drift documental, registrarlo en `inconsistencies.md`.
- Todo diagrama debe escribirse en **Mermaid** cuando sea técnicamente viable.
- Si Mermaid no alcanza para el caso, usar **PlantUML**.
- Diagramas ASCII solo se aceptan como último recurso y deben evitarse en documentación canónica.

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
