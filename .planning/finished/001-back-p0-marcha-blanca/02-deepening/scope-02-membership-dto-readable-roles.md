# 🔍 DEEPENING: Scope 02 — DTO memberships con roles legibles y fecha válida

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P0-002 | **Prioridad:** P0 / Bloqueante

---

## Objective

Actualizar el DTO de membership para que exponga `roles` como objetos legibles (`id`, `code`, `display_name`) en lugar de solo `roleIds`, y garantizar que `created_at` nunca sea `null` en memberships persistidas. Aplica a todos los endpoints: crear, listar, aprobar, revocar.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Localizar DTOs de membership y mapper actual que llena `roleIds` y `createdAt` | GENERATE-DOCUMENT | DONE | `MembershipData`, `TenantMembershipController`, mapper |
| 2 | Crear record `MembershipRoleData(UUID id, String code, String displayName)` | GENERATE-DOCUMENT | DONE | `MembershipRoleData` en keygo-api |
| 3 | Agregar `List<MembershipRoleData> roles` al DTO de membership y poblar desde entidad | GENERATE-DOCUMENT | DONE | `MembershipData.roles` via `GetMembershipRolesUseCase` |
| 4 | Garantizar que `createdAt` se puebla desde entidad persistida; corregir caso `null` en create | GENERATE-DOCUMENT | DONE | `Membership.createdAt` + mapper + controller |
| 5 | Actualizar tests de serialización/mapping | GENERATE-DOCUMENT | DONE | 4 tests en `TenantMembershipControllerTest` |
| 6 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | DONE | TRACEABILITY.md actualizado |

---

## Done Criteria

- [x] Response de membership incluye `roles[].id`, `roles[].code`, `roles[].display_name`.
- [x] `created_at` nunca es `null` en memberships persistidas.
- [x] El contrato es consistente en create / list-by-user / list-by-app / approve / revoke.
- [x] Tests de mapper pasan: entidad con dos roles → DTO con dos roles legibles.
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
