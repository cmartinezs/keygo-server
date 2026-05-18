# 🔍 DEEPENING: Scope 04 — Validación contractual OAuth config de apps

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P0-004 | **Prioridad:** P0 / Bloqueante

---

## Objective

Agregar validaciones condicionales al crear/actualizar apps: `AUTHORIZATION_CODE` requiere al menos una redirect URI válida, app `PUBLIC` con Authorization Code debe exigir PKCE, `CLIENT_CREDENTIALS` en app `PUBLIC` se rechaza. Los errores deben ser claros y mostrar el campo afectado.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Revisar DTOs de create/update client app y validaciones actuales | GENERATE-DOCUMENT | DONE | `CreateClientAppUseCase`, `UpdateClientAppUseCase` |
| 2 | Agregar validación: `AUTHORIZATION_CODE` sin redirect URI → `400` con `APP_OAUTH_CONFIG_INVALID` | GENERATE-DOCUMENT | DONE | En ambos use cases + `GlobalExceptionHandler` |
| 3 | Agregar validación: redirect URI malformada → `400` | GENERATE-DOCUMENT | DONE | Ya existía en `RedirectUri` value object |
| 4 | Agregar validación: app `PUBLIC` + `AUTHORIZATION_CODE` sin PKCE requerido → rechazar o forzar PKCE | GENERATE-DOCUMENT | PENDING | Diferido — requiere campo `requirePkce` en dominio (residual) |
| 5 | Agregar validación: `CLIENT_CREDENTIALS` en app `PUBLIC` → `400` | GENERATE-DOCUMENT | DONE | En ambos use cases |
| 6 | Verificar serialización snake_case/camelCase en response de error | GENERATE-DOCUMENT | DONE | `GlobalExceptionHandler` retorna `APP_OAUTH_CONFIG_INVALID` |
| 7 | Agregar tests de validaciones críticas (create + update) | GENERATE-DOCUMENT | DONE | 3 nuevos tests en `CreateClientAppUseCaseTest` + 1 en `UpdateClientAppUseCaseTest` |
| 8 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | DONE | TRACEABILITY.md actualizado |

---

## Done Criteria

- [x] No se puede crear app Authorization Code sin redirect URI.
- [x] No se puede dejar app pública con CLIENT_CREDENTIALS.
- [x] Errores incluyen `code` `APP_OAUTH_CONFIG_INVALID` y `message`.
- [x] No se rompe creación de app confidential con grant válido.
- [x] Tests de validaciones críticas pasan.
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
| 1 | Validación PUBLIC + AUTHORIZATION_CODE requiere `requirePkce = true` — campo no existe aún en dominio | BACK-003 P1 | PENDIENTE |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
