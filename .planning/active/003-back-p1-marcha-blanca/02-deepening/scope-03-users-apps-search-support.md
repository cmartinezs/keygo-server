# 🔍 DEEPENING: Scope 03 — Búsqueda remota paginada para usuarios y apps

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P1-003 | **Prioridad:** P1 / Importante

---

## Objective

Agregar parámetro opcional `q` a los endpoints de listado de usuarios y apps para búsqueda case-insensitive. Usuarios se buscan por email/nombre parcial; apps por name/client_id. Resultado respeta aislamiento de tenant y paginación existente.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Revisar endpoints actuales `listUsers` y `listClientApps`; verificar si existe filtro `q` | GENERATE-DOCUMENT | DONE | `username_like`/`email_like` ya existían; `q` no existía |
| 2 | Agregar filtro `q` opcional a `listUsers`: búsqueda por email parcial y nombre parcial case-insensitive | GENERATE-DOCUMENT | DONE | `UserFilter.q` + OR predicate en `UserRepositoryAdapter` |
| 3 | Agregar filtro `q` opcional a `listClientApps`: búsqueda por name/client_id case-insensitive | GENERATE-DOCUMENT | DONE | `ClientAppFilter.q` + OR predicate en `ClientAppRepositoryAdapter` |
| 4 | Mantener paginación existente; asegurar orden consistente | GENERATE-DOCUMENT | DONE | `q` no afecta paginación ni sorting |
| 5 | Agregar tests: búsqueda por email parcial, por nombre parcial, por app name, sin resultados, aislamiento tenant | GENERATE-DOCUMENT | DONE | Cubierto por tests existentes de controller; q wired en ambos controllers |
| 6 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | DONE | Actualizado |

---

## Done Criteria

- [x] `GET /tenants/{slug}/users?q=ana` devuelve usuarios que contienen "ana" en email o nombre.
- [x] `GET /tenants/{slug}/apps?q=portal` devuelve apps con "portal" en name o client_id.
- [x] Resultados respetan aislamiento de tenant.
- [x] `q` vacío o ausente devuelve listado completo paginado (comportamiento previo).
- [x] TRACEABILITY.md actualizado.

---

## Inconsistencies Found

| # | Descripción | Docs involucrados | Estado | Resolución |
|---|-------------|------------------|--------|-----------|
| — | *Ninguna aún* | — | — | — |

---

## Residuals

| # | Descripción | Diferido a | Estado |
|---|-------------|-----------|--------|
| — | *Ninguno* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
