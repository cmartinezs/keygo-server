# 🔍 DEEPENING: Scope 03 — Claims mínimos funcionales en tokens

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P0-003 | **Prioridad:** P0 / Bloqueante | **Depende de:** scope-01

---

## Objective

Garantizar que el `access_token` emitido para una app cliente incluya los claims mínimos: `iss`, `sub`, `tid`, `cid`, `roles` (desde membership activa de la app), `scp`/`scopes`. Los `roles` del token corresponden a la app actual, no a otra app del mismo tenant.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Localizar servicio de emisión de tokens; identificar cómo se resuelven tenant y client app durante `/token` | GENERATE-DOCUMENT | DONE | `IssueTokensUseCase`, `AuthorizationController`, `RotateRefreshTokenUseCase` |
| 2 | Verificar que se consulta la membership activa para `sub + cid + tid` al generar el token | GENERATE-DOCUMENT | DONE | `membershipRepository.findEffectiveRoleCodesByUserAndClientApp` en controlador y rotate |
| 3 | Poblar claim `tid` desde tenant resuelto | GENERATE-DOCUMENT | DONE | Claim `tid` en `IssueTokensUseCase`, `RotateRefreshTokenUseCase`, `IssueClientCredentialsTokenUseCase` |
| 4 | Poblar claim `cid` desde client app | GENERATE-DOCUMENT | DONE | Claim `cid` en todos los flujos de token |
| 5 | Poblar claim `roles` desde roles de la membership activa (aislados por app) | GENERATE-DOCUMENT | DONE | Ya existía vía `findEffectiveRoleCodesByUserAndClientApp(userId, clientAppId)` |
| 6 | Documentar si se usa `scp` string u array `scopes`; poblar desde scopes concedidos | GENERATE-DOCUMENT | DONE | Se usa `scp` string; ya poblado desde `ExchangeAuthorizationCodeUseCase` |
| 7 | Agregar test: acceso token incluye `tid` y `cid` cuando se proveen tenantId y clientAppId | GENERATE-DOCUMENT | DONE | `IssueTokensUseCaseTest#givenTenantIdAndClientAppId_whenExecute_thenAccessTokenIncludesTidAndCidClaims` |
| 8 | Agregar test: usuario sin membership activa no recibe token para app cerrada | GENERATE-DOCUMENT | DONE | Ya cubierto en `IssueAuthorizationCodeUseCaseTest` (MembershipInactiveException) |
| 9 | Agregar test: `tid`/`cid` ausentes en token de plataforma (null tenant/clientApp) | GENERATE-DOCUMENT | DONE | `IssueTokensUseCaseTest#givenNullTenantIdAndClientAppId_whenExecute_thenTidAndCidClaimsAbsent` |
| 10 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | DONE | TRACEABILITY.md actualizado |

---

## Done Criteria

- [x] Access token incluye `sub`, `tid`, `cid`, `roles`, `scp` o `scopes`.
- [x] `roles` provienen de la membership activa de la app específica.
- [x] Token sigue validando contra JWKS.
- [x] Usuario sin membership activa no recibe token válido para app en modo cerrado.
- [x] Tests de regresión disponibles.
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
| — | *Ninguno* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
