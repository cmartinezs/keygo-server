# UI-006 — Endpoint para reportar acceso denegado como posible error

**Fecha:** 2026-04-14  
**Estado:** 🔴 Abierto  
**Contexto:** Estados `403 FORBIDDEN` en pantallas protegidas por rol/recurso, comenzando por `/dashboard/tenants`.

## Problema

La UI ya distingue `403 FORBIDDEN` como acceso denegado a recurso y muestra un estado explícito en pantalla. Sin embargo, hoy no existe un endpoint backend para que el usuario pueda indicar que cree que ese rechazo es un error y enviar al equipo de KeyGo el contexto funcional y técnico necesario para investigación.

Actualmente la UI puede reunir:

- comentario del usuario
- `trace_id` del backend
- `code`, `origin`, `detail`, `exception`, `client_message`
- ruta actual, recurso afectado y contexto funcional del bloque
- `sub`, correo, username, rol activo, roles detectados y tenant gestionado

## Comportamiento esperado

El backend debería exponer un endpoint autenticado para recibir este reporte:

- **Método:** `POST`
- **Path propuesto:** `/api/v1/platform/support/access-incidents`
- **Auth:** `BearerAuth`

### Request propuesto

```json
{
  "incident_type": "ACCESS_DENIED",
  "feature_key": "dashboard_tenants",
  "route_path": "/dashboard/tenants",
  "current_url": "http://localhost:5173/dashboard/tenants",
  "resource_path": "/api/v1/tenants",
  "resource_label": "tenants asociados",
  "user_comment": "Soy administrador de cuenta y deberia ver mis tenants asociados.",
  "http_status": 403,
  "error_code": "INSUFFICIENT_PERMISSIONS",
  "client_message": "You don't have permission to perform this action.",
  "error_origin": "BUSINESS_RULE",
  "trace_id": "0fa43e27-1c68-435d-a75f-c75a72660a02",
  "exception": "AuthorizationDeniedException",
  "detail": "Access Denied",
  "actor_sub": "8b231536-870d-4e95-9d37-137b8f495de3",
  "actor_email": "account.admin@keygo.dev",
  "actor_username": "account.admin@keygo.dev",
  "active_role": "keygo_account_admin",
  "detected_roles": ["keygo_account_admin", "keygo_user"],
  "tenant_claim": "keygo",
  "managed_tenant_slug": null,
  "ui_trace_id": "1f6d0b66-26e8-4d7f-8d80-cf6f30d8e4d7",
  "resource_context": {
    "search_query": "",
    "filter_status": "ALL",
    "page": 0,
    "managed_tenant_slug": null
  }
}
```

### Response propuesta

```json
{
  "date": "2026-04-14T15:40:00Z",
  "success": {
    "code": "ACCESS_INCIDENT_REPORTED",
    "message": "OK"
  },
  "data": {
    "incident_id": "incident-001",
    "received_at": "2026-04-14T15:40:00Z",
    "status": "RECEIVED"
  }
}
```

## Resolución

_Pendiente._

La UI quedó preparada con:

- estado visual `AccessDeniedState`
- acción **Reportar posible error**
- wrapper `createAccessIncidentReport()`
- mock MSW temporal para `POST /api/v1/platform/support/access-incidents`

Cuando backend publique el contrato real, la UI solo debe migrar el wrapper y eliminar el mock.

**Tarea/RFC que lo resolvió:** _Pendiente._
