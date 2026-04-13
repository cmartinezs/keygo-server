---
name: T-146 — GET /platform/roles — catálogo de roles de plataforma disponibles
description: Endpoint que expone el catálogo de platform roles para que la UI pueda mostrarlos y permitir asignarlos a un usuario.
type: task
status: PENDING
---

# T-146 — GET /platform/roles — catálogo de roles de plataforma disponibles

## Requerimiento

La UI necesita listar los roles de plataforma disponibles antes de poder asignarlos a un usuario. Hoy existe `GET /platform/users/{userId}/platform-roles` para leer los roles ya asignados, pero no hay un endpoint que exponga el catálogo completo de roles disponibles en la plataforma.

## Contrato esperado

```
GET /api/v1/platform/roles
Authorization: Bearer <platform-token>  (KEYGO_ADMIN o KEYGO_ACCOUNT_ADMIN)

200 OK
{
  "status": "SUCCESS",
  "data": [
    {
      "id": "...",
      "code": "KEYGO_ADMIN",
      "name": "Keygo Administrator",
      "description": "..."
    },
    ...
  ]
}
```

## Análisis del estado actual

| Artefacto | Estado |
|---|---|
| `PlatformRole` (dominio) | ✅ Existe — campos: `id`, `code`, `name`, `description` |
| `PlatformRoleRepositoryPort.findAll()` | ✅ Existe |
| `PlatformRoleRepositoryAdapter` (supabase) | Verificar si implementa `findAll()` |
| `GetPlatformRolesCatalogUseCase` | ❌ No existe |
| `PlatformRoleData` (response DTO) | ❌ No existe |
| Endpoint `GET /platform/roles` | ❌ No existe |

## Componentes a crear

| Componente | Módulo | Clase |
|---|---|---|
| Result | `keygo-app` | `GetPlatformRolesCatalogResult` |
| Use case | `keygo-app` | `GetPlatformRolesCatalogUseCase` |
| Response DTO | `keygo-api` | `PlatformRoleData` |
| Controlador (método) | `keygo-api` | `PlatformRoleController#getPlatformRoles` (o método en controlador existente) |

## Pasos de implementación

1. `PENDING` — Verificar si `PlatformRoleRepositoryAdapter` ya implementa `findAll()`.
2. `PENDING` — Crear `GetPlatformRolesCatalogResult` en `keygo-app` (lista de `PlatformRole`).
3. `PENDING` — Crear `GetPlatformRolesCatalogUseCase` en `keygo-app` que invoca `PlatformRoleRepositoryPort.findAll()`.
4. `PENDING` — Crear `PlatformRoleData` DTO en `keygo-api` (campos: `id`, `code`, `name`, `description`).
5. `PENDING` — Exponer endpoint `GET /platform/roles` en controlador apropiado (`PlatformUserController` o nuevo `PlatformRoleController`).
6. `PENDING` — Asegurar `@PreAuthorize` con `KEYGO_ADMIN` o `KEYGO_ACCOUNT_ADMIN`.
7. `PENDING` — Agregar OpenAPI (`@Operation`, `@ApiResponse`) y actualizar Postman.
8. `PENDING` — Actualizar `doc/02-functional/frontend/frontend-developer-guide.md`.
9. `PENDING` — Crear `doc/02-functional/frontend/feedback/out/BE-006-platform-roles-catalog-endpoint.md` notificando a UI la disponibilidad del endpoint y actualizar el índice en `doc/02-functional/frontend/feedback/out/README.md`.

## Guía de verificación

- `GET /api/v1/platform/roles` con bearer válido responde `200` con lista de roles.
- La lista incluye al menos `KEYGO_ADMIN`, `KEYGO_ACCOUNT_ADMIN`, `KEYGO_USER`.
- Sin bearer o con rol insuficiente → `401`/`403`.

## Relaciones

- **Derivada de:** RFC `rbac-multi-scope-alignment` — esta tarea cubre una superficie parcial del RFC que conviene implementar antes del RFC completo para habilitar la UI.
- **Habilitadora de:** flujo UI de asignación de roles (`POST /platform/users/{userId}/platform-roles`).
- **Complementaria de:** T-143 (`GET /platform/users/{userId}/platform-roles`).
