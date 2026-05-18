# SPEC Backend P1 — Política y contrato de aprobación de memberships

| Campo | Valor |
|---|---|
| ID | `BE-P1-004` |
| Tipo | Backend specification |
| Prioridad | P1 / Condicional |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / memberships module |
| Módulo sugerido | Membership approval |
| Estado | Propuesta para implementación |

## Problema

Backend expone `PUT /memberships/{membershipId}/approve`, pero la UI no muestra flujo de aprobación. Si se mantiene estado `PENDING`, debe existir contrato claro para bandeja de solicitudes.

## Decisión funcional

Para marcha blanca se recomiendan dos caminos:

- Camino simple: no usar `PENDING`; crear memberships directamente `ACTIVE` por admin.
- Camino completo: mantener `PENDING` y exponer flujo de aprobación.

Esta specification cubre el camino completo.

## Endpoints recomendados

```http
GET /api/v1/tenants/{tenantSlug}/memberships?status=PENDING&page=0&size=20
PUT /api/v1/tenants/{tenantSlug}/memberships/{membershipId}/approve
PUT /api/v1/tenants/{tenantSlug}/memberships/{membershipId}/reject
```

## Alcance incluido

- Listar memberships pendientes.
- Aprobar membership.
- Rechazar membership si aplica.
- Auditar quién aprobó/rechazó.
- Validar tenant y permisos.

## Fuera de alcance

- Workflow complejo de múltiples aprobadores.
- Notificaciones por email.
- Solicitud pública de acceso si no existe.

## Instrucciones para AI Agent

1. Verificar estados reales de membership.
2. Revisar endpoint `approve` existente.
3. Agregar listado por status si no existe.
4. Definir si existe `reject`; si no, documentar como no implementado.
5. Asegurar que `approve` solo cambia `PENDING` a `ACTIVE`.
6. Agregar tests de transiciones.

## Criterios de aceptación

- Se puede listar `PENDING`.
- Se puede aprobar `PENDING` y queda `ACTIVE`.
- No se puede aprobar una membership revocada o de otro tenant.
- La UI puede construir una bandeja de pendientes.

## Pruebas sugeridas

- Crear pending + approve: OK.
- Approve ya active: idempotente o error claro.
- Approve revoked: error claro.
- Tenant mismatch: 404/403.

## Definition of Done

- El estado `PENDING` queda operable o se decide explícitamente retirarlo del MVP.
