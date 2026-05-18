# 🔍 DEEPENING: Scope 02 — Endpoint admin de sesiones de usuario

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P1-002 | **Prioridad:** P1 / Condicional

---

## Objective

Implementar `GET /api/v1/tenants/{tenantSlug}/users/{userId}/sessions` si existe persistencia real de sesiones, o documentar explícitamente que no existe para que UI-004/scope-02 use la alternativa B (ocultar feature).

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Verificar si existe entidad/session store con persistencia real de sesiones | GENERATE-DOCUMENT | DONE | Camino A: `SessionEntity` con `tenantUserId`+`tenantId` en `oauth_sessions` |
| 2 | **Si camino A:** Crear service de consulta de sesiones por tenant/user | GENERATE-DOCUMENT | DONE | `ListAdminUserSessionsUseCase` en `keygo-app` |
| 3 | **Si camino A:** Crear endpoint con paginación, aislado por tenant, sin exponer refresh token | GENERATE-DOCUMENT | DONE | `GET /{userId}/sessions` en `TenantUserController` |
| 4 | **Si camino A:** Proteger con `KEYGO_ADMIN` o `KEYGO_ACCOUNT_ADMIN` | GENERATE-DOCUMENT | DONE | `@PreAuthorize("hasRole('KEYGO_ADMIN')")` |
| 5 | **Si camino A:** Agregar tests de aislamiento tenant | GENERATE-DOCUMENT | DONE | `ListAdminUserSessionsUseCaseTest` — 5 tests, verifica `tenantUserId`+`tenantId` |
| 6 | **Si camino B:** Documentar en TRACEABILITY que el endpoint no existe para marcha blanca | UPDATE-TRACEABILITY | N/A | Camino A elegido |
| 7 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | DONE | Actualizado en scope-01 y scope-02 |

---

## Done Criteria

- [x] Existe decisión explícita documentada: camino A (implementar) o camino B (no implementar).
- [x] **Si camino A:** endpoint existe y responde sesiones reales; tenant no puede ver sesiones de otro tenant; no se filtran tokens.
- [x] **Si camino B:** decisión registrada en TRACEABILITY; UI-004/scope-02 puede proceder con alternativa B.
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
