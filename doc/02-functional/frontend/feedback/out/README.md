# Feedback OUT — Backend → UI

Cambios en el backend que impactan al equipo de UI.

## Índice

| Archivo | Estado | Resumen |
|---|---|---|
| [BE-001-check-email-endpoint.md](BE-001-check-email-endpoint.md) | 🔴 Abierto | Nuevo endpoint `POST /platform/account/check-email` para validar email en onboarding |
| [BE-002-billing-entitlement-limitvalue-decimal.md](BE-002-billing-entitlement-limitvalue-decimal.md) | 🔴 Abierto | `entitlements[].limitValue` de billing se alinea a decimal (`NUMERIC(18,4)`) |
| [BE-003-platform-users-list-endpoint.md](BE-003-platform-users-list-endpoint.md) | 🔴 Abierto | Backend notifica a UI que `GET /platform/users` ya quedó disponible con paginación. |
| [BE-004-platform-user-roles-endpoint.md](BE-004-platform-user-roles-endpoint.md) | 🟢 Confirmado | Backend notificó a UI que `GET /platform/users/{userId}/platform-roles` quedó disponible con scope y contractor resumido, y la revisión fue aprobada. |
| [BE-005-platform-billing-catalog-available.md](BE-005-platform-billing-catalog-available.md) | 🔴 Abierto | Backend notifica a UI que `GET /platform/billing/catalog` ya quedó poblado con el catálogo público de plataforma tras aplicar `V20`. |
| [BE-006-platform-roles-catalog-endpoint.md](BE-006-platform-roles-catalog-endpoint.md) | 🟢 Confirmado | Backend notificó a UI que `GET /platform/roles` quedó disponible y la UI ya lo consume para poblar el catálogo de roles asignables. |
| [BE-007-platform-account-profile.md](BE-007-platform-account-profile.md) | 🟢 Confirmado | `GET`/`PATCH /api/v1/platform/account/profile` — endpoints self-service de perfil para platform users. |
| [BE-008-tenant-app-self-registration.md](BE-008-tenant-app-self-registration.md) | 🔴 Abierto | Flujo completo de self-registro abierto a app de tenant: 3 endpoints públicos, secuencia de pantallas, manejo de errores. |
| [BE-009-tenant-app-invitation-flow.md](BE-009-tenant-app-invitation-flow.md) | 🟡 Pendiente impl. | Flujo de invitación admin a app de tenant: 6 endpoints (admin + públicos), pantallas admin y usuario invitado. Bloqueado hasta T-155. |

## Cómo agregar una entrada

1. Crear `BE-NNN-<slug>.md` en esta carpeta.
2. Usar la plantilla de abajo.
3. Agregar fila en la tabla de índice de este `README.md`.

## Plantilla

```markdown
# BE-NNN — <título corto>

**Fecha:** YYYY-MM-DD  
**Estado:** 🔴 Abierto  
**Plan / RFC:** T-NNN o RFC-NNN (si aplica)

## Cambio

<descripción del cambio en el backend>

## Impacto en UI

<qué debe adaptar el equipo de UI y dónde>

## Confirmación

_Pendiente._

<!-- Completar cuando UI confirme adaptación -->
```
