# Feedback UI ↔ Backend

Canal de comunicación activa entre los equipos de UI y backend.
Cada ítem es un archivo único que contiene la apertura (quien inicia) y la respuesta (quien cierra).

## Convenciones de nombrado

```
UI-NNN-<slug>.md   → inicia UI
BE-NNN-<slug>.md   → inicia Backend
```

## Estados

| Estado | Significado |
|---|---|
| 🔴 Abierto | Reportado o notificado, sin respuesta |
| 🟡 En revisión | Reconocido, en evaluación o bloqueado |
| 🟢 Resuelto / Confirmado | Implementado o integrado |
| ⬛ Archivado | Ya no aplica |

## Plantilla

```markdown
# [UI|BE]-NNN — <título corto>

**Fecha:** YYYY-MM-DD
**Iniciado por:** UI | Backend
**Estado:** 🔴 Abierto
**Contexto / Plan:** <pantalla o flujo> / T-NNN o RFC-NNN (si aplica)

---

## Apertura _(→ [UI | Backend])_

### Descripción

<UI: gap o inconsistencia detectada / BE: cambio implementado o previsto>

### Expectativa del receptor

<UI: qué debe exponer o cambiar el backend / BE: qué debe adaptar o integrar la UI>

---

## Respuesta _(→ [Backend | UI])_

_Pendiente._

**Referencia:** _Pendiente._
<!-- T-NNN / RFC-NNN / ADR / artefacto que cierra este ítem -->
```

## Índice

| Archivo | Iniciado por | Estado | Resumen |
|---|---|---|---|
| [UI-001-platform-users-list-endpoint.md](UI-001-platform-users-list-endpoint.md) | UI | 🟢 Resuelto | `GET /platform/users` no existía; backend lo habilitó con paginación (T-142). |
| [UI-002-platform-user-roles-endpoint.md](UI-002-platform-user-roles-endpoint.md) | UI | 🟢 Resuelto | `GET /platform/users/{userId}/platform-roles` no existía; backend lo habilitó (T-143). |
| [UI-003-platform-billing-catalog-empty.md](UI-003-platform-billing-catalog-empty.md) | UI | 🟢 Resuelto | `GET /platform/billing/catalog` devolvía `data: []`; se alineó con Flyway V20 (T-145). |
| [UI-004-platform-roles-catalog-endpoint.md](UI-004-platform-roles-catalog-endpoint.md) | UI | 🟢 Resuelto | `GET /platform/roles` para catálogo de roles asignables; UI lo consume en detalle de usuario (T-146). |
| [UI-005-access-incident-reporting-endpoint.md](UI-005-access-incident-reporting-endpoint.md) | UI | 🔴 Abierto | UI necesita `POST /platform/support/access-incidents` para reportar `403` como posible error. |
| [BE-001-check-email-endpoint.md](BE-001-check-email-endpoint.md) | Backend | 🟢 Confirmado | `POST /platform/account/check-email` para onboarding; UI integrado en `NewContractPage.tsx` (T-130). |
| [BE-002-billing-entitlement-limitvalue-decimal.md](BE-002-billing-entitlement-limitvalue-decimal.md) | Backend | 🔴 Abierto | `limitValue` de billing ahora es decimal (`NUMERIC(18,4)`); UI debe adaptar serialización. |
| [BE-007-platform-account-profile.md](BE-007-platform-account-profile.md) | Backend | 🟢 Confirmado | `GET`/`PATCH /platform/account/profile` — perfil self-service de platform user (T-153). |
| [BE-008-tenant-app-self-registration.md](BE-008-tenant-app-self-registration.md) | Backend | 🔴 Abierto | Flujo completo de self-registro público en app de tenant: 3 endpoints (T-154). |
| [BE-009-tenant-app-invitation-flow.md](BE-009-tenant-app-invitation-flow.md) | Backend | 🟡 En revisión | Flujo de invitación admin → app de tenant: 6 endpoints; bloqueado hasta T-155. |
