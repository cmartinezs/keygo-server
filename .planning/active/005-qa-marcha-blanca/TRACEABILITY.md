# 🔗 Traceability: QA-005 — Marcha Blanca Quality Gates

> [← README.md](README.md) | [← planning/README.md](../../README.md)

---

## Term Matrix

| Término / Concepto | D | R | S | M | P | V | T | B | O | N | F | G | W | Notas |
|-------------------|---|---|---|---|---|---|---|---|---|---|---|---|---|-------|
| Checklist GO/NO-GO | | | | | | | ✅ | | | | | | | scope-01 |
| Condición NO-GO | | ✅ | | | | | ✅ | | | | | | | Criterio de bloqueo |
| Escenario de prueba E2E | | | | | | | ✅ | ✅ | | | | | | | scope-02 |
| Piloto guiado | | ✅ | | | | | | ✅ | ✅ | | | | | | scope-02 |
| `tenant: acme` (datos base piloto) | | | | | | | ✅ | | ✅ | | | | | | scope-02 |

---

## Decisions Made

| ID | Decisión | Justificación | Afecta | Fecha |
|----|----------|--------------|--------|-------|
| D-001 | scope-01 ejecutable al terminar P0 mínimo (BACK-001 + UI-002) | GO/NO-GO cubre condiciones bloqueantes de P0 | scope-01 | 2026-05-17 |
| D-002 | scope-02 requiere P1 completo desplegado | Escenarios cubren flujos de punta a punta | scope-02 | 2026-05-17 |

---

## Residuals

| # | Término / Issue | Bloqueante | Estado | Resolución prevista |
|---|----------------|-----------|--------|-------------------|
| — | *Ninguno* | — | — | — |

---

> [← README.md](README.md) | [← planning/README.md](../../README.md)
