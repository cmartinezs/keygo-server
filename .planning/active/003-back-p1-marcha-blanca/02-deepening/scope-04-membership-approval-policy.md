# 🔍 DEEPENING: Scope 04 — Política y contrato de aprobación de memberships

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P1-004 | **Prioridad:** P1 / Condicional

---

## Objective

Definir el camino para memberships en marcha blanca: **simple** (crear memberships directamente ACTIVE, sin PENDING) o **completo** (mantener PENDING y exponer endpoints de aprobación/rechazo). Implementar según decisión. Documentar explícitamente el camino elegido.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Verificar estados reales de membership y si existe flujo PENDING en uso | GENERATE-DOCUMENT | DONE | Camino completo: `CreateMembershipUseCase` crea con `PENDING` |
| 2 | **Si camino completo:** Revisar/ajustar endpoint `approve` existente | GENERATE-DOCUMENT | DONE | `PUT /{membershipId}/approve` ya existe y valida la transición |
| 3 | **Si camino completo:** Agregar endpoint de listado por status `GET /memberships?status=PENDING` si no existe | GENERATE-DOCUMENT | DONE | `status` añadido a `MembershipFilter` + controlador + JPA spec |
| 4 | **Si camino completo:** Definir si existe `reject`; si no, documentar como no implementado | GENERATE-DOCUMENT | DONE | No existe `reject`; solo `revoke` (→ SUSPENDED). No implementado en marcha blanca |
| 5 | **Si camino completo:** Validar que `approve` solo cambia `PENDING` → `ACTIVE`; agregar tests de transición | GENERATE-DOCUMENT | DONE | `ApproveMembershipUseCaseTest` ya cubre PENDING→ACTIVE, ACTIVE→throws, SUSPENDED→throws |
| 6 | **Si camino simple:** Documentar que memberships se crean ACTIVE directamente; no existe flujo PENDING en marcha blanca | UPDATE-TRACEABILITY | N/A | Camino completo elegido |
| 7 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | DONE | Actualizado |

---

## Done Criteria

- [x] Existe decisión explícita documentada: camino completo.
- [x] **Si completo:** se puede listar PENDING (`?status=PENDING`), aprobar PENDING → ACTIVE, no aprobar revocada (throws), tenant mismatch → 404.
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
