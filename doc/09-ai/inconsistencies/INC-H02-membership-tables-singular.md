# INC-H02 — Tablas V7 en singular requieren migración V10

**Categoría:** datos
**Criticidad:** 🔴 Crítica
**Estado:** ✅ Resuelta
**Detectada:** 2026-03-22
**Resuelta:** 2026-03-22
**Tarea relacionada:** —

## Descripción

Re-auditoría de los ítems "resueltos" de [INC-H01](INC-H01-data-model-v1-v9.md) reveló que
corregir los nombres de tablas solo en documentación era insuficiente: las tablas
`membership`, `membership_role` y `app_role` en DB (V7) debían renombrarse físicamente
para ser consistentes con la convención plural de todo el schema.

## Esperado vs. Real

| Tabla en V7 | Nombre correcto | Corrección |
|---|---|---|
| `membership` | `memberships` | ✅ Renombrada en V10 |
| `membership_role` | `membership_roles` | ✅ Renombrada en V10 |
| `app_role` | `app_roles` | ✅ Renombrada en V10 |

## Corrección

`V10__rename_membership_tables_to_plural.sql` — migración que renombra las tres tablas.
Entidades JPA actualizadas (`@Table(name = "memberships")`, etc.).
Documentación actualizada en `data-model.md` y `entity-relationships.md`.
