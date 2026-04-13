# UI-002 — Missing platform user roles endpoint

**Fecha:** 2026-04-13  
**Estado:** 🔴 Abierto  
**Contexto:** Pantalla admin de plataforma que necesita obtener los roles asignados a un usuario global.

## Problema

La UI intenta consumir `GET /api/v1/platform/users/{userId}/platform-roles`, pero el backend
responde `HttpRequestMethodNotSupportedException` porque `PlatformUserController` hoy solo
expone:

- `POST /api/v1/platform/users/{userId}/platform-roles`
- `DELETE /api/v1/platform/users/{userId}/platform-roles/{roleCode}`

No existe el `GET` de colección para consultar los roles ya asignados al usuario.

## Comportamiento esperado

El backend debe exponer un endpoint de lectura para obtener los roles de plataforma asignados
a un `platform_user`, de forma consumible por la UI.

## Resolución

_Pendiente._

**Tarea/RFC que lo resolvió:** _Pendiente._

<!-- Confirmado en código: PlatformUserController no tiene GET /{userId}/platform-roles, aunque el repositorio ya expone findByPlatformUserId(...) -->
