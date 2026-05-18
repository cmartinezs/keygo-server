# 🔍 DEEPENING: Scope 01 — Contrato resend-verification: registration_id como identificador

> **Status:** PENDING
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** Bug reportado en piloto | **Prioridad:** P1 / Bloqueante para flujo de self-registration

---

## Objective

Hacer que `POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/resend-verification` acepte
`registration_id` (UUID del usuario) como identificador principal. `email` pasa a ser opcional
para compatibilidad legacy. El backend resuelve el email internamente desde el registro.

---

## Análisis de contrato actual

| Elemento | Situación actual | Situación objetivo |
|---|---|---|
| `ResendVerificationRequest.email` | `@NotBlank` — requerido | Opcional; válido si presente |
| `ResendVerificationRequest.registration_id` | No existe | UUID v4 opcional; prioridad sobre `email` |
| Validación de "al menos uno" | No aplica (email era obligatorio) | `@AssertTrue` a nivel de clase |
| `ResendVerificationCommand.registrationId` | No existe | Campo nullable `String` |
| Lookup de usuario en use case | Solo por email (`findByTenantIdAndEmail`) | Por `registrationId` primero, luego `email` |
| Retorno del use case | `void` | `String` (email del usuario) — para construir `NotificationSentData` |
| `RegistrationController` — respuesta | `EmailMasker.mask(request.email())` | `EmailMasker.mask(emailRetornado)` |

### Referencia: patrón de verify-email (ya implementado)

`VerifyEmailUseCase` implementa exactamente el patrón objetivo:
1. Si `registrationId` no es blank → `findByIdAndTenantId` → `UserNotFoundException("id", ...)`
2. Si no, si `email` no es blank → `findByTenantIdAndEmail` → `UserNotFoundException("email", ...)`

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | `ResendVerificationRequest`: quitar `@NotBlank` de `email`; agregar `registration_id` con validación UUID; agregar `@AssertTrue` de clase | GENERATE-DOCUMENT | PENDING | `ResendVerificationRequest.java` actualizado |
| 2 | `ResendVerificationCommand`: agregar campo `registrationId` nullable | GENERATE-DOCUMENT | PENDING | `ResendVerificationCommand.java` actualizado |
| 3 | `ResendVerificationEmailUseCase`: resolver usuario por `registrationId` o `email` (prioridad al ID); retornar `String` email resuelto | GENERATE-DOCUMENT | PENDING | `ResendVerificationEmailUseCase.java` actualizado |
| 4 | `RegistrationController`: pasar `registrationId` al command; usar email retornado por use case para `NotificationSentData` | GENERATE-DOCUMENT | PENDING | `RegistrationController.java` actualizado |
| 5 | Tests use case: agregar casos para `registration_id`; actualizar casos existentes por cambio de firma del command | GENERATE-DOCUMENT | PENDING | `ResendVerificationEmailUseCaseTest.java` actualizado |
| 6 | Tests controlador: agregar casos para `registration_id` y validación de "al menos uno" | GENERATE-DOCUMENT | PENDING | `RegistrationControllerTest.java` actualizado/creado |
| 7 | OpenAPI annotations: actualizar descripción del endpoint | GENERATE-DOCUMENT | PENDING | `RegistrationController.java` — anotaciones |
| 8 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | `TRACEABILITY.md` |

---

## Done Criteria

- [ ] `POST /resend-verification` con solo `registration_id` retorna 200 y email enmascarado.
- [ ] `POST /resend-verification` con solo `email` retorna 200 (compatibilidad legacy no rota).
- [ ] `POST /resend-verification` sin ningún campo retorna 400 con `field_errors`.
- [ ] Use case resuelve usuario por ID primero; email como fallback.
- [ ] Use case retorna el email del usuario (no del request) para enmascarar en respuesta.
- [ ] Tests unitarios cubren: lookup por `registration_id`, lookup por `email`, ambos presentes (prioridad ID), ninguno presente.
- [ ] `TRACEABILITY.md` actualizado.

---

## Decisiones de diseño

| Decisión | Alternativas consideradas | Elección | Razón |
|---|---|---|---|
| Retorno del use case | `void` (actual) vs `String` (email) vs nuevo `ResendVerificationResult` | Retornar `String email` | Mínimo cambio; evita duplicar lookup en el controller; sin necesidad de un result record por un solo campo |
| Prioridad identificadores | `email` primero vs `registration_id` primero | `registration_id` primero | Consistente con `VerifyEmailUseCase`; el ID es más preciso y no editable |
| Validación "al menos uno" | `@AssertTrue` vs constraint personalizada | `@AssertTrue` en el DTO | Simple, sin nueva clase; suficiente para este caso |

---

## Inconsistencies Found

| # | Descripción | Archivos involucrados | Estado | Resolución |
|---|-------------|----------------------|--------|-----------|
| — | *Ninguna* | — | — | — |

---

## Residuals

| # | Descripción | Diferido a | Estado |
|---|-------------|-----------|--------|
| R-01 | `VerifyEmailUseCase` lanza `IllegalArgumentException` cuando no se provee ni `email` ni `registrationId`; debe usar excepción de jerarquía keygo | Planning posterior o fix independiente | PENDING |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
