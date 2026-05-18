# 🔗 Traceability: BACK-007 — Refactor envío de email de verificación

> [← planning/README.md](../../README.md)

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
|---------------|---|---|---|---|---|---|---|---|---|---|---|---|---|-------|
| `SendVerificationEmailUseCase` | | | ✅ | N/A | | ❌ | ❌ | | | | | | ✅ | Nuevo componente — pendiente crear |
| `RegisterTenantUserUseCase` | | | | N/A | | ⚠️ | ⚠️ | | | | | | ✅ | Refactorizar para delegar envío |
| `ResendVerificationEmailUseCase` | | | | N/A | | ⚠️ | ⚠️ | | | | | | ✅ | Refactorizar para delegar envío |
| Parámetros email verificación | | | ✅ | N/A | | ⚠️ | ⚠️ | | | | | | ✅ | Hoy duplicados en dos use cases |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| — | *Por definir: opción de diseño del componente extraído* | — | Scope 01 | — |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | *Ninguno* | — | — | — |

---

> [← planning/README.md](../../README.md)
