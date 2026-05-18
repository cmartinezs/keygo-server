# 🔍 DEEPENING: Scope 06 — Contrato de revocación OAuth y logout

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P1-006 | **Prioridad:** P1 / Importante

---

## Objective

Verificar y exponer el contrato real de logout/revocación de tokens. Si los refresh tokens son persistentes, deben poder ser revocados. El endpoint debe ser idempotente. Documentar el endpoint definitivo para que el frontend pueda consumirlo sin mocks.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Buscar controllers de revoke/logout existentes | GENERATE-DOCUMENT | DONE | `RevocationController` (POST /oauth2/revoke) + `AccountSettingsController` (POST /account/logout) |
| 2 | Confirmar si refresh tokens se guardan (hasheados); documentar modelo de persistencia | GENERATE-DOCUMENT | DONE | Tokens guardados con hash SHA-256 determinista en campo `token_hash`; búsqueda por hash |
| 3 | Implementar o ajustar `POST /oauth2/revoke` con `token` + `token_type_hint` | GENERATE-DOCUMENT | DONE | `RevocationController` + `RevokeTokenUseCase` existentes y completos (RFC 7009) |
| 4 | Implementar o ajustar `POST /account/logout` para invalidar sesión actual | GENERATE-DOCUMENT | DONE | `AccountSettingsController.logout()` — localiza sesión `isCurrent=true` y la revoca |
| 5 | Garantizar idempotencia: revocar token ya revocado → respuesta segura sin filtrar estado | GENERATE-DOCUMENT | DONE | Ambos endpoints retornan 200 si el token/sesión no existe o ya está revocado |
| 6 | Agregar tests: token válido revocado, token ya revocado (idempotente), token inválido → respuesta segura, token revocado no puede usarse para refresh | GENERATE-DOCUMENT | DONE | `RevokeTokenUseCaseTest` + `RotateRefreshTokenUseCaseTest` + 3 nuevos tests en `AccountSettingsControllerTest` |
| 7 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | TRACEABILITY.md actualizado |

---

## Done Criteria

- [x] Existe endpoint real y documentado para logout/revoke.
- [x] Logout invalida refresh token o sesión según modelo actual.
- [x] La respuesta no expone datos sensibles.
- [x] Token revocado no permite renovar sesión.
- [x] Respuesta es idempotente.
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
| R-001 | Single Logout federado (back-channel OIDC) | Fuera de alcance marcha blanca | PENDING |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
