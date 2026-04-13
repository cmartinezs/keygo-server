# INC-H01 — 12 inconsistencias modelo de datos V1–V9

**Categoría:** datos
**Criticidad:** 🔴 Crítica
**Estado:** ✅ Resuelta
**Detectada:** 2026-03-22
**Resuelta:** 2026-03-22
**Tarea relacionada:** —

## Descripción

Auditoría completa del schema DB (migraciones Flyway V1–V9) contra documentación
(`data-model.md`, `entity-relationships.md`, `auth_flow.md`). Se detectaron 12 inconsistencias
en nombres de tablas, columnas, tipos de datos, constraints y valores de enums.

El detalle completo de cada ítem está en la sección **Esperado vs. Real** a continuación.

## Esperado vs. Real (resumen)

| # | Área | Problema |
|---|---|---|
| 1 | `membership`, `membership_role`, `app_role` | Tablas en singular en DB vs. plural en docs — requirió V10 (ver INC-H02) |
| 2 | `membership_roles` | PK documentada como UUID independiente; real es PK compuesta `(membership_id, role_id)` |
| 3 | `app_roles` | Columnas `tenant_id` y `status` documentadas pero inexistentes; `name` → `display_name` |
| 4 | `authorization_codes` | 7 diferencias: nombres de columnas, tamaños, status en minúsculas, `used_at` no documentada |
| 5 | `signing_keys` | `private_material_ref` → `private_material TEXT`; `created_at` no documentada |
| 6 | `tenants` | `owner_email` no documentada; status values distintos |
| 7 | `client_apps` | `display_name` → `name`; `client_secret` → `hashed_secret`; `client_type` → `type` |
| 8 | `client_redirect_uris` | `redirect_uri` → `uri`; tamaño `2000` → `2048` |
| 9 | `tenant_users` | `display_name` → `first_name`+`last_name`; username no era nullable |
| 10 | Tablas legado V1/V3 | `users`, `roles`, `oauth_providers`, etc. existían en DB sin estar en docs |
| 11 | `refresh_tokens`, `sessions` | Documentadas como implementadas; eran planificadas |
| 12 | `AUTH_FLOW.md` Fase 6 | Estado incorrecto; respuesta y status values erróneos |

## Corrección

`data-model.md`, `entity-relationships.md` y `AUTH_FLOW.md` actualizados en su totalidad.
Criterio aplicado: la documentación manda en convenciones de nomenclatura; la implementación
manda cuando la razón técnica es clara. Los items #1 (tablas en singular) requirieron
migración `V10` adicional — ver [INC-H02](INC-H02-membership-tables-singular.md).
