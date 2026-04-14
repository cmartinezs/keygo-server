# UI-005 — Integración de check-email para onboarding público

**Fecha:** 2026-04-14  
**Estado:** 🟢 Resuelto  
**Contexto:** Flujo público `/subscribe`, paso `Your details / Tus datos`, antes de mostrar términos y crear contrato.

## Problema

La UI necesitaba diferenciar si el correo ingresado ya existía como `platform_user` antes de avanzar desde el paso de datos del titular. Sin ese pre-check, el wizard seguía a términos y recién más tarde detectaba conflictos o forzaba un flujo incorrecto para usuarios existentes.

## Comportamiento esperado

La UI debe consumir `POST /api/v1/platform/account/check-email` al presionar **Continue** en `Your details / Tus datos` y resolver el resultado por contrato:

- `200 PLATFORM_USER_EMAIL_FOUND` → mantener el paso actual y pedir otro correo
- `404 PLATFORM_USER_EMAIL_NOT_FOUND` → continuar registro
- `401 AUTHENTICATION_REQUIRED` → recrear la sesión previa con `GET /api/v1/platform/oauth2/authorize` y reintentar

## Resolución

Se integró el endpoint en `src/features/auth/register/NewContractPage.tsx` mediante el wrapper `platformCheckEmail()` en `src/features/auth/api.ts`. El wizard ahora reestablece la sesión `authorize` cuando expira, reintenta el pre-check y solo avanza a términos cuando el backend confirma que el correo no existe todavía; si el correo ya existe, el usuario permanece en `Your details / Tus datos` con feedback local para cambiarlo.

**Tarea/RFC que lo resolvió:** T-130
