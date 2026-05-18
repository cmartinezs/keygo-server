# SPEC Frontend P1 — Bandeja de solicitudes pendientes de membership

| Campo | Valor |
|---|---|
| ID | `FE-P1-005` |
| Tipo | Frontend specification |
| Prioridad | P1 / Condicional |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Memberships pending requests |
| Estado | Propuesta para implementación |

## Problema

Backend tiene aprobación de memberships, pero UI solo crea y revoca. Si se mantiene estado `PENDING`, debe existir una superficie para aprobar solicitudes.

## Dependencia

Coordinar con `BE-P1-004`.

## Decisión funcional

Si `PENDING` sigue existiendo en MVP, agregar tab o sección “Solicitudes pendientes”. Si no se implementa backend completo, ocultar flujo `PENDING` de marcha blanca.

## Alcance incluido

- Tab `Solicitudes pendientes` en memberships.
- Listado de memberships `PENDING`.
- Acción aprobar.
- Acción rechazar si backend existe.
- Feedback de éxito/error.

## Fuera de alcance

- Notificaciones por email.
- Workflow complejo de aprobación.

## Diseño sugerido

```text
Accesos / Memberships
[Por usuario] [Por aplicación] [Solicitudes pendientes]

Usuario          App              Roles solicitados   Acción
ana@empresa.cl   Portal Clientes  USER                Aprobar
```

## Instrucciones para AI Agent

1. Revisar página memberships.
2. Agregar tab de pendientes si backend disponible.
3. Consumir endpoint de listado `status=PENDING`.
4. Implementar acción aprobar.
5. Si reject no existe, no mostrar botón rechazar.
6. Actualizar listado al aprobar.

## Criterios de aceptación

- Tenant admin puede ver memberships pendientes.
- Tenant admin puede aprobar membership pendiente.
- La membership aprobada pasa a `ACTIVE` en UI.
- No se muestra botón de rechazar si backend no lo soporta.

## Pruebas sugeridas

- Render con lista vacía.
- Render con pending.
- Aprobar actualiza fila.
- Error de approve muestra mensaje.

## Definition of Done

- El estado `PENDING` deja de ser invisible para operación.
