# INC-H05 — Drift documental post-reorganización

**Categoría:** docs
**Criticidad:** 🟡 No crítica
**Estado:** ✅ Resuelta
**Detectada:** 2026-04-09
**Resuelta:** 2026-04-09
**Tarea relacionada:** —

## Descripción

Tras la reorganización de la estructura de documentación (aplanamiento de carpetas,
renombrado de archivos canónicos), varios documentos de operación del agente y de
arquitectura mantenían referencias a rutas o nombres de archivo que ya no existían.
Incluye wrappers de compatibilidad que apuntaban a ubicaciones antiguas.

## Esperado vs. Real

| Documento | Problema |
|---|---|
| `README.md` principal | Referencias a rutas antiguas post-reorganización |
| `agents.md` | Rutas de módulos y comandos con paths previos a la reorganización |
| `architecture.md` | Links internos rotos tras renombrado de archivos |
| `bootstrap-filter.md` | Referenciado por nombre antiguo en varios documentos |
| Wrappers de compatibilidad | Apuntaban a ubicaciones de archivos ya movidos |

## Corrección

Auditoría completa de referencias cruzadas y actualización masiva de todos los
documentos afectados: `README.md`, `agents.md`, `agent-operations.md`,
`architecture.md`, `bootstrap-filter.md` y sus wrappers. Aplicado el 2026-04-09.
