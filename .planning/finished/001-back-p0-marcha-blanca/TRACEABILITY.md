# 🔗 Traceability: BACK-001 — Marcha Blanca Backend P0

> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Term Matrix

| Término / Concepto | D | R | S | M | P | V | T | B | O | N | F | G | W | Notas |
|-------------------|---|---|---|---|---|---|---|---|---|---|---|---|---|-------|
| `KEYGO_ACCOUNT_ADMIN` | | | | | | ✅ | ✅ | | | | | | | Reemplaza `KEYGO_TENANT_ADMIN` |
| `KEYGO_TENANT_ADMIN` | | | | ⚠️ | | ✅ | ✅ | | | | | | | Eliminar/migrar en scope-01 |
| `MembershipRoleData` | | | ✅ | | | ✅ | ✅ | | | | | | | Nuevo DTO scope-02 |
| Token claim `tid` | | | ✅ | | | ✅ | ✅ | | | | | | | scope-03 |
| Token claim `cid` | | | ✅ | | | ✅ | ✅ | | | | | | | scope-03 |
| Token claim `roles` | | | ✅ | | | ✅ | ✅ | | | | | | | scope-03 |
| `KG-APP-OAUTH-CONFIG-INVALID` | | | ✅ | | | ✅ | ✅ | | | | | | | Error code scope-04 |
| `AUTHORIZATION_CODE` + redirect URI | | | ✅ | | | ✅ | ✅ | | | | | | | Validación scope-04 |

---

## Decisions Made

| ID | Decisión | Justificación | Afecta | Fecha |
|----|----------|--------------|--------|-------|
| D-001 | Usar `KEYGO_ACCOUNT_ADMIN` como rol admin de tenant | Eliminar inconsistencia entre UI y backend | scope-01, scope-03 | 2026-05-17 |
| D-002 | Token incluye `tid`, `cid`, `roles`, `scp` | Claims mínimos para integración externa sin consulta adicional | scope-03 | 2026-05-17 |
| D-003 | App Authorization Code requiere al menos una redirect URI | Evitar apps no integrables | scope-04 | 2026-05-17 |

---

## Residuals

| # | Término / Issue | Bloqueante | Estado | Resolución prevista |
|---|----------------|-----------|--------|-------------------|
| — | *Ninguno* | — | — | — |

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
