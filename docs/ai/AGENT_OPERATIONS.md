# Operacion Compartida de Agentes - KeyGo Server

Politica comun para agentes AI que trabajan en este repositorio.

## Leer antes de actuar

1. [`/AGENTS.md`](../../AGENTS.md)
2. [`/AI_CONTEXT.md`](../../AI_CONTEXT.md)
3. [`/ARCHITECTURE.md`](../../ARCHITECTURE.md)
4. [`/ROADMAP.md`](../../ROADMAP.md)
5. [`docs/README.md`](../README.md)
6. La documentacion tematica relevante (`docs/api/`, `docs/data/`, `docs/design/`, `docs/development/`, `docs/operations/`, `docs/product-design/`)

## Comportamiento obligatorio

### Planificar antes de implementar

Antes de escribir codigo o tocar contratos:

1. Leer los documentos canónicos relevantes.
2. Presentar un plan explícito con modulos, archivos, flujo y pruebas.
3. Implementar despues del plan, salvo que la herramienta requiera una confirmacion adicional de su propio flujo.

### Documentacion

- No crear ni actualizar `.md` de forma automatica salvo orden explicita del usuario.
- Excepcion: memoria AI en `docs/ai/` y quick-starts/wrappers de agentes cuando la tarea lo requiera explicitamente o la politica del repo lo exija.
- Toda nueva documentacion debe respetar la politica de ubicacion en [`docs/README.md`](../README.md).

### Git

- No ejecutar `git commit`, `git push`, `git merge`, `git rebase` ni operaciones destructivas sin orden explicita.
- Si hace falta, sugerir los comandos al usuario.

## Regla de mantenimiento al cerrar trabajo tecnico

Si una tarea cambia comportamiento del sistema, revisar si corresponde actualizar:

| Cambio | Actualizar |
|---|---|
| Nuevo endpoint o cambio de contrato HTTP | OpenAPI/controller + `docs/postman/KeyGo-Server.postman_collection.json` + `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` |
| Nueva migracion Flyway | `docs/data/MIGRATIONS.md` + `docs/data/DATA_MODEL.md` + `docs/data/ENTITY_RELATIONSHIPS.md` |
| Cambio de quick-start, comandos o reglas para agentes | `AGENTS.md` + `docs/ai/agents-registro.md` |
| Nueva inconsistencia doc-codigo | `docs/ai/inconsistencias.md` y detalle asociado |
| Nuevo aprendizaje o patron | `docs/ai/lecciones.md` |
| Nueva propuesta o propuesta completada | `ROADMAP.md` + `docs/ai/propuestas.md` |

## Checklist critico

- `keygo-domain` no depende de Spring ni de otros modulos internos.
- Campos nullable del dominio se exponen como `Optional<T>`.
- Objetos nuevos persistidos no deben setear `id`.
- Respuestas REST usan `BaseResponse<T>` salvo endpoints RFC/OIDC nativos.
- Imports Jackson 3 usan `tools.jackson.databind.*`.
- Entidades JPA no usan `@Data`.
- Columnas JSONB usan `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`.
- Seguridad admin vigente: `Authorization: Bearer <jwt>`, no `X-KEYGO-ADMIN`.
- El `context-path` documentado siempre es `/keygo-server`.
- La siguiente migracion Flyway debe usar la siguiente version libre real.

## Fuentes de verdad rapidas

| Tema | Documento |
|---|---|
| Quick-start tecnico | [`/AGENTS.md`](../../AGENTS.md) |
| Snapshot operativo | [`/AI_CONTEXT.md`](../../AI_CONTEXT.md) |
| Arquitectura | [`docs/design/ARCHITECTURE.md`](../design/ARCHITECTURE.md) |
| Seguridad HTTP | [`docs/api/BOOTSTRAP_FILTER.md`](../api/BOOTSTRAP_FILTER.md) |
| Migraciones | [`docs/data/MIGRATIONS.md`](../data/MIGRATIONS.md) |
| Setup local | [`docs/development/ENVIRONMENT_SETUP.md`](../development/ENVIRONMENT_SETUP.md) |
| Politica documental | [`docs/README.md`](../README.md) |
