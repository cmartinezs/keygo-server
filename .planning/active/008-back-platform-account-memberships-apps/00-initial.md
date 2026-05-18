# 🌱 INITIAL: BACK-008 — Platform account /access endpoint

> **Status:** DEEPENING
> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Intent

Agregar `GET /api/v1/platform/account/access` — un único endpoint que, a partir del `sub` del token de plataforma, retorna todos los tenants donde el usuario tiene membresías y las apps a las que tiene acceso en cada uno, con sus roles.

---

## Why

Los tokens de plataforma llevan `iss = /api/v1/platform` (sin `tenant_slug`). `hasTenantAccess(authentication)` extrae el slug de la URL del `iss` o del claim `tenant_slug`; al no encontrarlo, deniega el acceso con 403.

El frontend (keygo-UI) autentica con token de plataforma y necesita saber:
- En qué tenants opera el usuario.
- Qué apps puede gestionar en cada tenant.
- Qué roles tiene en cada app.

Con `/access` el frontend hace una sola llamada autenticada y obtiene la vista completa de acceso, agrupada por tenant → apps → membresía + roles.

---

## Approximate Scope

- `keygo-api`: `PlatformAccountController` — un nuevo método GET
- `keygo-api`: nuevo DTO `AccountAccessData` y `TenantAccessData` / `AppAccessData`
- `keygo-app`: nuevo `GetAccountAccessUseCase` (orquesta: membresías → app info → tenant info → roles)
- Sin cambios en `keygo-supabase` ni `keygo-domain`
- Reutiliza: `MembershipRepositoryPort.findByUserId`, `ClientAppRepositoryPort.findById`, `TenantRepositoryPort.findById`, `GetMembershipRolesUseCase`

---

## Initiator

- **Requested by:** Carlos Martínez
- **Date:** 2026-05-18
- **Related planning:** 005-qa-marcha-blanca (bloqueante para piloto)

---

## Next Step

- [x] Dimensionado → ver `01-expansion.md` y `02-deepening/`

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
