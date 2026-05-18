# SPEC Backend P1 — Endpoint admin para sesiones de usuario

| Campo | Valor |
|---|---|
| ID | `BE-P1-002` |
| Tipo | Backend specification |
| Prioridad | P1 / Importante si UI mantiene sesiones |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / sessions module |
| Módulo sugerido | Tenant admin user sessions |
| Estado | Propuesta para implementación |

## Problema

Frontend declara `getAdminUserSessions` y llama `GET /users/{userId}/sessions`, pero no se observa endpoint equivalente en `TenantUserController`.

## Decisión funcional

Hay dos alternativas válidas:

A. Implementar endpoint admin para sesiones de usuario.
B. Retirar/ocultar temporalmente la UI de sesiones hasta tener backend.

Esta specification cubre la alternativa A.

## Endpoint recomendado

```http
GET /api/v1/tenants/{tenantSlug}/users/{userId}/sessions
Authorization: Bearer <token>
```

## Response sugerido

```json
{
  "items": [
    {
      "session_id": "sess_123",
      "created_at": "2026-05-17T12:00:00Z",
      "last_seen_at": "2026-05-17T12:30:00Z",
      "ip_address": "190.0.0.10",
      "user_agent": "Mozilla/5.0",
      "status": "ACTIVE"
    }
  ]
}
```

## Alcance incluido

- Exponer endpoint read-only para tenant admin.
- Aislar por tenant.
- Paginar si hay muchas sesiones.
- No exponer refresh token ni datos sensibles.

## Fuera de alcance

- Revocar sesiones individuales, salvo que ya exista backend.
- Mostrar geolocalización.
- Fingerprinting avanzado.

## Instrucciones para AI Agent

1. Revisar si existe entidad/session store.
2. Si no existe persistencia real de sesiones, no simular datos falsos; documentar y usar alternativa B en frontend.
3. Si existe, crear service de consulta por tenant/user.
4. Agregar controller endpoint.
5. Proteger con `KEYGO_ADMIN` o `KEYGO_ACCOUNT_ADMIN`.
6. Agregar tests de aislamiento tenant.

## Criterios de aceptación

- Endpoint existe y responde sesiones reales.
- Tenant admin no puede consultar sesiones de usuarios de otro tenant.
- No se filtran tokens ni secrets.
- Frontend puede consumirlo sin mocks.

## Pruebas sugeridas

- User con sesiones: 200.
- User sin sesiones: 200 con lista vacía.
- User inexistente: 404.
- Otro tenant: 404/403.

## Definition of Done

- UI de sesiones puede operar sin mock si se decide mantenerla.
