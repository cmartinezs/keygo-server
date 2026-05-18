# 📋 BACK-001: Marcha Blanca — Backend P0

> **Status:** COMPLETED
> [← active/README.md](../README.md) | [← planning/README.md](../../README.md)

---

| Campo | Valor |
|---|---|
| ID | 001 |
| Prefijo | BACK |
| Prioridad | P0 / Bloqueante para marcha blanca |
| Área | Backend (`keygo-server`) |
| Estado | DEEPENING |

Resuelve las 4 brechas backend P0 identificadas en el diagnóstico de marcha blanca controlada KeyGo. Todas son bloqueantes para habilitar el piloto.

---

## Scopes

| # | Scope | Spec origen | Depende de | Estado |
|---|-------|-------------|-----------|--------|
| 01 | [Unificar nomenclatura roles admin](02-deepening/scope-01-unify-admin-role-naming.md) | BE-P0-001 | — | DONE |
| 02 | [DTO memberships con roles legibles](02-deepening/scope-02-membership-dto-readable-roles.md) | BE-P0-002 | — | DONE |
| 03 | [Claims mínimos en tokens](02-deepening/scope-03-token-claims-minimum-contract.md) | BE-P0-003 | 01 | DONE |
| 04 | [Validación OAuth config de apps](02-deepening/scope-04-app-oauth-config-validation.md) | BE-P0-004 | — | DONE |

---

## Archivos

| Archivo | Descripción |
|---------|-------------|
| [00-initial.md](00-initial.md) | Intent y contexto |
| [01-expansion.md](01-expansion.md) | Scopes y dependencias |
| [02-deepening/](02-deepening/) | Detalle de tareas por scope |
| [TRACEABILITY.md](TRACEABILITY.md) | Matriz de trazabilidad |

---

> [← active/README.md](../README.md) | [← planning/README.md](../../README.md)
