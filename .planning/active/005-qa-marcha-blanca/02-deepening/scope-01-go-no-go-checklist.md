# 🔍 DEEPENING: Scope 01 — Checklist GO/NO-GO marcha blanca

> **Status:** PENDING
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** QA-001 | **Prioridad:** P0 | **Depende de:** BACK-001 completo + UI-002 completo

---

## Objective

Ejecutar el checklist de validación GO/NO-GO para determinar si KeyGo cumple las condiciones mínimas de marcha blanca. Documentar el resultado (GO / NO-GO), fecha, responsable y observaciones.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Verificar sección Apps: secret disclosure, redirect URIs, scopes, detalle de app, gestión de roles | REVIEW-COHERENCE | PENDING | Ítems del checklist marcados |
| 2 | Verificar sección Roles administrativos: nomenclatura backend/frontend alineada, KEYGO_ACCOUNT_ADMIN sin 403, KEYGO_USER bloqueado | REVIEW-COHERENCE | PENDING | Ítems del checklist marcados |
| 3 | Verificar sección Memberships: app asociada visible, estado visible, roles legibles, `created_at` válido, crear y revocar funcionan | REVIEW-COHERENCE | PENDING | Ítems del checklist marcados |
| 4 | Verificar sección Tokens: `sub`, `tid`, `cid`, `roles`, `scp`/`scopes` presentes; token valida contra JWKS | REVIEW-COHERENCE | PENDING | Ítems del checklist marcados |
| 5 | Verificar sección Mocks: MSW desactivado por defecto, flujos principales sin mock | REVIEW-COHERENCE | PENDING | Ítems del checklist marcados |
| 6 | Registrar resultado final: GO / NO-GO con fecha, responsable y observaciones | GENERATE-DOCUMENT | PENDING | Resultado documentado en este scope o en documento adjunto |
| 7 | Si NO-GO: registrar condiciones bloqueantes y planning de resolución | GENERATE-DOCUMENT | PENDING | Plan de resolución |
| 8 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | TRACEABILITY.md actualizado |

---

## Done Criteria

- [ ] Todos los ítems del checklist evaluados (GO o NO-GO por ítem).
- [ ] Resultado global documentado: GO / NO-GO.
- [ ] Si NO-GO: condiciones bloqueantes listadas con path de resolución.
- [ ] TRACEABILITY.md actualizado.

---

## Checklist de referencia (de QA-001)

### Apps
- [ ] Crear app muestra `client_id` y `client_secret` una sola vez.
- [ ] El secret se puede copiar.
- [ ] La UI advierte que el secret no volverá a mostrarse.
- [ ] La app permite configurar redirect URIs.
- [ ] La app permite configurar scopes base.
- [ ] Backend rechaza Authorization Code sin redirect URI.
- [ ] Existe detalle de app.
- [ ] Existe gestión básica de roles por app.

### Roles administrativos
- [ ] Backend usa `KEYGO_ACCOUNT_ADMIN` como rol tenant/account admin.
- [ ] Frontend usa la misma nomenclatura.
- [ ] `KEYGO_ACCOUNT_ADMIN` puede operar tenant console sin 403 inesperado.
- [ ] `KEYGO_USER` no puede operar tenant console.

### Memberships
- [ ] Membership muestra app asociada.
- [ ] Membership muestra estado.
- [ ] Membership muestra roles legibles, no UUIDs.
- [ ] Membership muestra `created_at` válido.
- [ ] Crear membership funciona para usuario y app del mismo tenant.
- [ ] Revocar membership funciona si está expuesto.

### Tokens
- [ ] Access token incluye `sub`.
- [ ] Access token incluye `tid`.
- [ ] Access token incluye `cid`.
- [ ] Access token incluye roles de app desde membership.
- [ ] Access token incluye scopes (`scp` o `scopes`).
- [ ] Token valida contra JWKS.

### Mocks
- [ ] MSW está desactivado por defecto para marcha blanca.
- [ ] Login, Apps, Users, Memberships, Roles no usan mock.

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
