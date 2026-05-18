# 🔗 Traceability: BACK-006 — Fix contrato resend-verification

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
| `registration_id` en resend-verification | | | ⚠️ | N/A | | ❌ | ❌ | | | | | | ✅ | Contrato no alineado con verify-email; V/T pendientes de scope-01 |
| `ResendVerificationRequest` | | | ⚠️ | N/A | | ❌ | ❌ | | | | | | ✅ | email `@NotBlank` a quitar; agregar `registration_id` |
| `ResendVerificationCommand` | | | | N/A | | ❌ | ❌ | | | | | | ✅ | Falta campo `registrationId` |
| `ResendVerificationEmailUseCase` | | | | N/A | | ❌ | ❌ | | | | | | ✅ | Solo resuelve por email; debe soportar ID |

---

## Decisions Made

| ID | Decisión | Fundamento | Afecta | Fecha |
|----|----------|-----------|--------|-------|
| D-01 | `ResendVerificationEmailUseCase` retorna `String` (email resuelto) en lugar de `void` | Evita duplicar la lógica de lookup en el controller para construir `NotificationSentData` | `ResendVerificationEmailUseCase`, `RegistrationController` | 2026-05-17 |
| D-02 | `registration_id` tiene prioridad sobre `email` cuando ambos están presentes | Consistente con `VerifyEmailUseCase`; el ID es más preciso y no puede ser editado por el cliente | `ResendVerificationEmailUseCase` | 2026-05-17 |
| D-03 | Validación "al menos uno" mediante `@AssertTrue` a nivel de clase en el DTO | Solución mínima sin nueva clase de constraint; suficiente para este caso | `ResendVerificationRequest` | 2026-05-17 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R-01 | `VerifyEmailUseCase` lanza `IllegalArgumentException` (no de jerarquía keygo) | No | PENDING | Planning posterior o fix independiente |

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
