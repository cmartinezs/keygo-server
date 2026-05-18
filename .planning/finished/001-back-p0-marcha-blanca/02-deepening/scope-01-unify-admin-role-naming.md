# 🔍 DEEPENING: Scope 01 — Unificar nomenclatura roles administrativos

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P0-001 | **Prioridad:** P0 / Bloqueante

---

## Objective

Eliminar `KEYGO_TENANT_ADMIN` del backend y reemplazarlo por `KEYGO_ACCOUNT_ADMIN` en todos los contextos (enums, annotations, security matchers, tests). Si existen datos persistidos con ese valor, crear migración Flyway. Garantizar que un token con `KEYGO_ACCOUNT_ADMIN` no reciba 403 en ningún endpoint de tenant console.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Buscar y auditar todos los usos de `KEYGO_TENANT_ADMIN`, `TENANT_ADMIN`, `keygo_tenant_admin` en código, migraciones y tests | GENERATE-DOCUMENT | DONE | Sin datos DB persistidos — reemplazo puro |
| 2 | Determinar si existen datos en DB con valor `KEYGO_TENANT_ADMIN`; definir estrategia (eliminar vs migrar) | GENERATE-DOCUMENT | DONE | No existen — eliminación directa |
| 3 | Reemplazar ocurrencias en enums, `@PreAuthorize`, security matchers y código Java | GENERATE-DOCUMENT | DONE | 7 controladores + application.yml |
| 4 | Crear Flyway migration si hay datos persistidos con `KEYGO_TENANT_ADMIN` | GENERATE-DOCUMENT | DONE | No aplica — sin datos persistidos |
| 5 | Actualizar tests de seguridad: acceso permitido para `KEYGO_ACCOUNT_ADMIN`, rechazado para `KEYGO_USER` | GENERATE-DOCUMENT | DONE | 5 nuevos tests en TenantAuthorizationEvaluatorTest |
| 6 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | DONE | TRACEABILITY.md actualizado |

---

## Done Criteria

- [x] No quedan referencias activas a `KEYGO_TENANT_ADMIN` salvo alias documentado o migración.
- [x] Controllers de tenant console aceptan `KEYGO_ACCOUNT_ADMIN`.
- [x] Usuario con `KEYGO_ACCOUNT_ADMIN` no recibe 403 en endpoints de gestión de apps, usuarios, memberships y roles.
- [x] Usuario con solo `KEYGO_USER` es rechazado en esos endpoints.
- [x] Suite de tests de seguridad pasa.
- [x] TRACEABILITY.md actualizado con nuevos términos.

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
