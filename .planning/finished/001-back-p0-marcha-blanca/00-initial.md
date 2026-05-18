# 🌱 INITIAL: BACK-001 — Marcha Blanca Backend P0

> **Status:** DEEPENING
> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Intent

Resolver las 4 brechas backend P0 que bloquean la marcha blanca controlada de KeyGo: inconsistencia en nomenclatura de roles admin, DTO de memberships sin roles legibles, claims de token incompletos, y ausencia de validación contractual en configuración OAuth de apps.

---

## Why

KeyGo no puede entrar a marcha blanca si un usuario `KEYGO_ACCOUNT_ADMIN` recibe 403 por inconsistencia de roles, si las memberships muestran UUIDs, si el token no incluye los claims mínimos para integración externa, o si una app Authorization Code puede quedar sin redirect URI. Estas son condiciones NO-GO definidas en el diagnóstico.

---

## Approximate Scope

- Security layer: enums de roles, annotations, security matchers
- Membership module: DTOs, mappers
- Auth/JWT module: emisión de tokens, claims
- ClientApp module: validación create/update

---

## Initiator

- **Requested by:** Carlos Martínez
- **Date:** 2026-05-17
- **Related planning:** ninguno (primer planning de marcha blanca)

---

## Next Step

- [x] Dimensionado → ver `01-expansion.md` y `02-deepening/`

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
