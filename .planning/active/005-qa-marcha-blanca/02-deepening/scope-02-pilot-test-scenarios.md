# 🔍 DEEPENING: Scope 02 — Escenarios de prueba para piloto guiado

> **Status:** PENDING
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** QA-002 | **Prioridad:** P0/P1 | **Depende de:** BACK-003 completo + UI-004 completo

---

## Objective

Ejecutar los 10 escenarios de prueba E2E que validan el comportamiento funcional del piloto guiado. Registrar resultado (PASS/FAIL) por escenario. Cualquier FAIL debe resolverse antes de habilitar la marcha blanca con usuarios reales.

---

## Datos base del piloto

- Tenant: `acme`
- Admin global: `KEYGO_ADMIN`
- Admin tenant: `KEYGO_ACCOUNT_ADMIN`
- Usuario final: `KEYGO_USER`
- App pública: `Portal Clientes`
- App confidential: `Backoffice API`
- Roles de app: `ADMIN`, `USER`, `VIEWER`

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Preparar ambiente de piloto con datos base | GENERATE-DOCUMENT | PENDING | Ambiente configurado |
| 2 | Ejecutar Escenario 1: Crear app pública integrable (secret disclosure, redirect URI, scopes) | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 3 | Ejecutar Escenario 2: Bloquear app sin redirect URI (validación local o 400 backend) | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 4 | Ejecutar Escenario 3: Crear roles de app (code, nombre legible, no UUIDs) | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 5 | Ejecutar Escenario 4: Crear usuario y asignar app (autocomplete, membership con rol legible) | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 6 | Ejecutar Escenario 5: Login con membership activa y validar claims del token | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 7 | Ejecutar Escenario 6: Denegar usuario sin membership en app cerrada → pantalla KG-NO-MEMBERSHIP | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 8 | Ejecutar Escenario 7: Validar roles administrativos (KEYGO_ACCOUNT_ADMIN opera, KEYGO_USER bloqueado) | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 9 | Ejecutar Escenario 8: Desactivar mocks (VITE_ENABLE_MSW=false, Network tab muestra llamadas reales) | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 10 | Ejecutar Escenario 9: Suspender y activar usuario con mensajes correctos | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 11 | Ejecutar Escenario 10: Logout real (llamada backend, refresh token revocado) | REVIEW-COHERENCE | PENDING | PASS/FAIL |
| 12 | Documentar resultados; registrar FAILs con descripción y planning de resolución | GENERATE-DOCUMENT | PENDING | Informe de resultados |
| 13 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | TRACEABILITY.md actualizado |

---

## Done Criteria

- [ ] Los 10 escenarios ejecutados con resultado documentado (PASS/FAIL).
- [ ] Todos los FAILs tienen un path de resolución registrado.
- [ ] La marcha blanca puede habilitarse cuando todos los escenarios sean PASS.
- [ ] TRACEABILITY.md actualizado.

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
