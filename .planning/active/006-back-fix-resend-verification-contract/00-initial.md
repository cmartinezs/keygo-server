# 🌱 INITIAL: BACK-006 — Fix contrato resend-verification

> **Status:** DEEPENING
> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Intent

Ajustar el contrato de `resend-verification` para aceptar `registration_id` como identificador principal del usuario, haciendo `email` opcional (legacy), de modo que el frontend pueda reenviar el código de verificación sin necesidad de almacenar el email entre pasos.

---

## Why

El frontend llega al paso de verificación de email con `registration_id` (UUID del usuario devuelto al registrarse), pero sin el email almacenado en estado local. El endpoint actual exige `email` con `@NotBlank`, por lo que rechaza el request con `field_errors: [{ field: "email", message: "Email is required" }]`, bloqueando el flujo de reenvío.

El endpoint simétrico `verify-email` ya soporta `registration_id` como alternativa al `email`. `resend-verification` debe alinearse con ese contrato.

---

## Approximate Scope

- `keygo-api`: `ResendVerificationRequest` (DTO), `RegistrationController`
- `keygo-app`: `ResendVerificationCommand`, `ResendVerificationEmailUseCase`
- Tests: `ResendVerificationEmailUseCaseTest`, test de controlador

---

## Initiator

- **Requested by:** Carlos Martínez
- **Date:** 2026-05-17
- **Related planning:** 003-back-p1-marcha-blanca (scope-05 definió la política de registro; este planning corrige una brecha de contrato en el mismo flujo)

---

## Next Step

- [x] Dimensionado → ver `01-expansion.md` y `02-deepening/`

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
