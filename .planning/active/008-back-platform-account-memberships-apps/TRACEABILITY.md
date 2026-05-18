# 🔗 Traceability: BACK-008 — Platform account /access endpoint

> [← README.md](README.md) | [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Phase Code Reference

| Code | Phase |
|------|-------|
| D | Discovery |
| R | Requirements |
| S | Design |
| M | Data Model |
| P | Planning (SDLC phase 5) |
| V | Development |
| T | Testing |
| B | Deployment |
| O | Operations |
| N | Monitoring |
| F | Feedback |
| G | Guides |
| W | Workflow (planning/) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

| Term / Concept | D | R | S | M | P | V | T | B | O | N | F | G | W | Notes |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `GET /platform/account/access` | | | ✅ | N/A | | ❌ | ❌ | | | | | | ✅ | Nuevo endpoint; V/T pendientes de scope-01 |
| `GetAccountAccessUseCase` | | | ✅ | N/A | | ❌ | ❌ | | | | | | ✅ | Nuevo use case; orquesta membership → app → tenant → roles |
| `TenantAccessResult` / `AppAccessResult` | | | ✅ | N/A | | ❌ | ❌ | | | | | | ✅ | Nuevos result records en `keygo-app` |
| `TenantAccessData` / `AppAccessData` | | | ✅ | N/A | | ❌ | ❌ | | | | | | ✅ | Nuevos DTOs en `keygo-api` |
| `ACCOUNT_ACCESS_RETRIEVED` | | | ✅ | N/A | | ❌ | ❌ | | | | | | ✅ | Nuevo `ResponseCode` |

---

## Decisions Made

| ID | Decisión | Fundamento | Afecta | Fecha |
|----|----------|-----------|--------|-------|
| D-01 | Endpoint único `/access` en lugar de `/memberships` + `/apps` | El endpoint `/apps` sin tenant no tiene semántica propia; el frontend necesita vista agrupada; una sola llamada | `PlatformAccountController` | 2026-05-18 |
| D-05 | `isAuthenticated()` en lugar de `hasAnyRole(...)` | El endpoint muestra el propio acceso del usuario; cualquier plataforma user autenticado puede consultarlo | `PlatformAccountController` | 2026-05-18 |
| D-02 | Respuesta sin paginación | Volumen bajo en el piloto; la agrupación por tenant se pierde con paginación plana | `GET /access` response shape | 2026-05-18 |
| D-03 | `GetAccountAccessUseCase` en `keygo-app`, no en el controller | La orquestación de 3–4 ports es lógica de aplicación, no de presentación | `keygo-app`, `keygo-api` | 2026-05-18 |
| D-04 | Ignorar (log warn) membresías cuya app o tenant no se encuentren | Inconsistencia de datos no debe romper el endpoint | `GetAccountAccessUseCase` | 2026-05-18 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | — | — | — | — |

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
