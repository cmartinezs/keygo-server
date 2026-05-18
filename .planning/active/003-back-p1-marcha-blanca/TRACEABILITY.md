# 🔗 Traceability: BACK-003 — Marcha Blanca Backend P1

> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Term Matrix

| Término / Concepto | D | R | S | M | P | V | T | B | O | N | F | G | W | Notas |
|-------------------|---|---|---|---|---|---|---|---|---|---|---|---|---|-------|
| `UserStatusActionResult` | | | ✅ | | | ✅ | ✅ | | | | | | | DTO scope-01 |
| `already_suspended` / `already_active` | | | ✅ | | | ✅ | ✅ | | | | | | | Flags idempotencia scope-01 |
| `/users/{userId}/sessions` | | | ✅ | | | ✅ | ✅ | | | | | | | Endpoint scope-02 |
| Query param `q` (search) | | | ✅ | | | ✅ | ✅ | | | | | | | scope-03 |
| `/memberships?status=PENDING` | | | ✅ | | | ✅ | ✅ | | | | | | | scope-04 |
| `access_policy` | | | ✅ | ✅ | | ✅ | ✅ | | | | | | | Campo nuevo en ClientApp, scope-05 |
| `KG-NO-MEMBERSHIP` | | | ✅ | | | ✅ | ✅ | | | | | | | Error code scope-05 |
| `CLOSED` / `OPEN_JOIN` / `SELF_SIGNUP` | | | ✅ | | | ✅ | ✅ | | | | | | | Enum access_policy scope-05 |
| `/oauth2/revoke` | | | ✅ | | | ✅ | ✅ | | | | | | | Endpoint revocación scope-06 |

---

## Decisions Made

| ID | Decisión | Justificación | Afecta | Fecha |
|----|----------|--------------|--------|-------|
| D-001 | Default `access_policy = CLOSED` en creación de app | Evitar auto-join no deseado en marcha blanca | scope-05 | 2026-05-17 |
| D-002 | Error `KG-NO-MEMBERSHIP` para usuario sin membership en app cerrada | Error funcional detectable por UI, no 403 genérico | scope-05 | 2026-05-17 |
| D-003 | Logout invalida refresh token, no solo limpieza local | Seguridad real en marcha blanca | scope-06 | 2026-05-17 |

---

## Residuals

| # | Término / Issue | Bloqueante | Estado | Resolución prevista |
|---|----------------|-----------|--------|-------------------|
| R-001 | scope-02: si no existe persistencia real de sesiones → alternativa B (ocultar UI) | No | PENDIENTE decisión | UI-004/scope-02 |
| R-002 | scope-04: definir camino simple (ACTIVE directo) vs completo (PENDING + aprobación) | No | PENDIENTE decisión | inicio scope-04 |

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
