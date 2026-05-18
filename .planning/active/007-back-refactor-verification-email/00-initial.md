# 🌱 INITIAL: BACK-007 — Refactor envío de email de verificación

> **Status:** DEEPENING
> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Intent

Extraer en un componente reutilizable la lógica de ensamblado y envío del email de verificación, hoy duplicada en `RegisterTenantUserUseCase` y `ResendVerificationEmailUseCase`.

---

## Why

Ambos casos de uso invocan `emailNotificationPort.sendEmail(TYPE_EMAIL_VERIFICATION, ...)` con el mismo conjunto de 7 parámetros (`userUsername`, `userFirstName`, `userLastName`, `verificationCode`, `registration_id`, `client_id`, `expiresInMinutes`). El ensamblado del `Map` está duplicado: cuando se corrigió el resend (planning 006 + bug fix 2026-05-17), la omisión de `registration_id` y `client_id` en el reenvío pasó desapercibida precisamente porque no había una única fuente de verdad para ese contrato.

El flujo debería modelarse como:
- **Registro** → resultado de un flujo → delega el envío al componente compartido.
- **Reenvío** → on-demand bajo parámetros — también delega al mismo componente.

---

## Approximate Scope

- `keygo-app`: nuevo `SendVerificationEmailUseCase` o helper equivalente
- `keygo-app`: `RegisterTenantUserUseCase` — eliminar ensamblado directo del `Map`
- `keygo-app`: `ResendVerificationEmailUseCase` — eliminar ensamblado directo del `Map`
- Tests: actualizar o agregar cobertura del componente extraído

---

## Initiator

- **Requested by:** Carlos Martínez
- **Date:** 2026-05-17
- **Related planning:** 006-back-fix-resend-verification-contract (el bug corregido en ese planning y en el fix posterior expuso la raíz del problema)

---

## Next Step

- [x] Dimensionado → ver `01-expansion.md` y `02-deepening/`

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
