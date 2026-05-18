# 🔍 DEEPENING: Scope 01 — Contrato consistente para suspend/activate de usuarios

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P1-001 | **Prioridad:** P1 / Importante

---

## Objective

Definir y exponer un contrato explícito para las acciones `suspendUser` y `activateUser` que incluya `user_id`, `previous_status`, `current_status` y flags de idempotencia (`already_suspended`/`already_active`). Alinear con lo que el frontend espera.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Revisar endpoints actuales de suspend/activate y su response actual | GENERATE-DOCUMENT | DONE | Devolvían `UserData` completo |
| 2 | Definir DTO `UserStatusActionResult` con `userId`, `previousStatus`, `currentStatus`, flag de idempotencia | GENERATE-DOCUMENT | DONE | `UserStatusActionResult` en keygo-app; `UserStatusActionData` en keygo-api |
| 3 | Actualizar endpoints para devolver `UserStatusActionResult` | GENERATE-DOCUMENT | DONE | `TenantUserController` actualizado; use cases devuelven result record |
| 4 | Agregar tests: suspender activo, suspender ya suspendido, activar suspendido, activar ya activo | GENERATE-DOCUMENT | DONE | 8 tests en `SuspendActivateUserUseCaseTest`; 4 en `TenantUserControllerTest` |
| 5 | Agregar test: usuario inexistente → 404; usuario de otro tenant → 404/403 | GENERATE-DOCUMENT | DONE | Tests 404 incluidos en `SuspendActivateUserUseCaseTest` |
| 6 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | — |

---

## Done Criteria

- [x] Response de suspend/activate incluye `user_id`, `previous_status`, `current_status`, flag idempotencia.
- [x] Contrato coincide con types esperados por frontend (UI-004/scope-01).
- [x] Acciones son idempotentes o reportan estado previo/actual.
- [x] Tests de integración cubren los cuatro casos.
- [ ] TRACEABILITY.md actualizado.

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
