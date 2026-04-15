# T-146 — GET /platform/roles — catálogo de roles de plataforma disponibles

**Estado:** ✅ Completada  
**Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-run`, `postman`, docs

---

## Requerimiento

La UI necesita listar los roles de plataforma disponibles antes de poder asignarlos a un usuario. Hoy existe `GET /platform/users/{userId}/platform-roles` para leer los roles ya asignados, pero no hay un endpoint que exponga el catálogo completo de roles disponibles en la plataforma.

## Solución propuesta

Implementar un endpoint de catálogo dedicado y acotado, reutilizando el stack ya existente de
`PlatformRole` sin tocar esquema ni seeds:

- introducir un use case de lectura simple en `keygo-app`
- exponer un read model/DTO orientado a UI con `id`, `code`, `name`, `description`
- publicar `GET /api/v1/platform/roles` en un `PlatformRoleController` nuevo
- aislar la autorización del catálogo en su propio controller para permitir
  `KEYGO_ADMIN` y `KEYGO_ACCOUNT_ADMIN`
- alinear OpenAPI, Postman y documentación frontend/feedback con la nueva superficie

### Contrato esperado

``` 
GET /api/v1/platform/roles
Authorization: Bearer <platform-token>  (KEYGO_ADMIN o KEYGO_ACCOUNT_ADMIN)

200 OK
{
  "success": {
    "code": "PLATFORM_ROLE_LIST_RETRIEVED",
    "message": "Platform role list retrieved successfully"
  },
  "data": [
    {
      "id": "...",
      "code": "KEYGO_ADMIN",
      "name": "Keygo Admin",
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
| `PlatformRoleRepositoryAdapter` (supabase) | ✅ Existe — implementa `findAll()` sobre JPA |
| `GetPlatformRolesCatalogUseCase` | ✅ Existe |
| `PlatformRoleData` (response DTO) | ✅ Existe |
| `ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED` | ✅ Existe — puede reutilizarse para la lectura del catálogo |
| Endpoint `GET /platform/roles` | ✅ Existe |

## Componentes a crear

| Componente | Módulo | Clase |
|---|---|---|
| Result | `keygo-app` | `GetPlatformRolesCatalogResult` |
| Use case | `keygo-app` | `GetPlatformRolesCatalogUseCase` |
| Response DTO | `keygo-api` | `PlatformRoleData` |
| Controlador (método) | `keygo-api` | `PlatformRoleController#getPlatformRoles` |

## Pasos de implementación

1. `APPLIED` — Se verificó que `PlatformRoleRepositoryAdapter` ya implementa `findAll()`.
2. `APPLIED` — Crear `GetPlatformRolesCatalogResult` en `keygo-app` (lista de `PlatformRole`).
3. `APPLIED` — Crear `GetPlatformRolesCatalogUseCase` en `keygo-app` que invoca `PlatformRoleRepositoryPort.findAll()`.
4. `APPLIED` — Crear `PlatformRoleData` DTO en `keygo-api` (campos: `id`, `code`, `name`, `description`).
5. `APPLIED` — Exponer endpoint `GET /platform/roles` en un nuevo `PlatformRoleController`.
6. `APPLIED` — Asegurar `@PreAuthorize` con `KEYGO_ADMIN` o `KEYGO_ACCOUNT_ADMIN`.
7. `APPLIED` — Agregar OpenAPI (`@Operation`, `@ApiResponse`) y actualizar Postman.
8. `APPLIED` — Actualizar `doc/02-functional/frontend/08-endpoints-admin.md`.
9. `APPLIED` — Crear `doc/02-functional/frontend/feedback/BE-006-platform-roles-catalog-endpoint.md` notificando a UI la disponibilidad del endpoint y actualizar el índice en `doc/02-functional/frontend/feedback/README.md`.

## Notas de análisis

- La tarea es **acotada**: reutiliza catálogo ya existente en dominio, puerto y adaptador, por lo que
  no requiere cambios de modelo ni migraciones Flyway.
- El seed actual ya contiene `KEYGO_ADMIN`, `KEYGO_ACCOUNT_ADMIN` y `KEYGO_USER`, por lo que la
  verificación funcional puede hacerse sobre datos canonicos ya presentes.
- No conviene agregar este `GET` dentro de `PlatformUserController`: ese controller está montado en
  `/api/v1/platform/users` y además tiene `@PreAuthorize("hasRole('KEYGO_ADMIN')")` a nivel clase,
  lo que impediría cumplir el contrato de ruta `/api/v1/platform/roles` y el acceso
  `KEYGO_ADMIN` o `KEYGO_ACCOUNT_ADMIN`.
- La opción correcta es un `PlatformRoleController` nuevo, con su propio `@RequestMapping` y
  `@PreAuthorize("hasAnyRole('KEYGO_ADMIN','KEYGO_ACCOUNT_ADMIN')")`.
- Aunque T-146 no necesita corregir todo el drift RBAC pendiente, sí debe evitar heredarlo:
  hoy existen superficies que aún usan `KEYGO_TENANT_ADMIN` o restringen de más a `KEYGO_ADMIN`,
  mientras el catálogo canonico vigente usa `KEYGO_ACCOUNT_ADMIN`.
- `ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED` ya existe y puede reutilizarse; no se justifica crear
  un nuevo código solo para esta lectura de catálogo.

## Guía de verificación

- `GET /api/v1/platform/roles` con bearer válido responde `200` con lista de roles.
- La lista incluye al menos `KEYGO_ADMIN`, `KEYGO_ACCOUNT_ADMIN`, `KEYGO_USER`.
- Sin bearer o con rol insuficiente → `401`/`403`.

## Relaciones

- **Derivada de:** RFC `rbac-multi-scope-alignment` — esta tarea cubre una superficie parcial del RFC que conviene implementar antes del RFC completo para habilitar la UI.
- **Habilitadora de:** flujo UI de asignación de roles (`POST /platform/users/{userId}/platform-roles`).
- **Complementaria de:** T-143 (`GET /platform/users/{userId}/platform-roles`).

## Historial de transiciones

### 2026-04-13 — 🔍 En análisis

- Se confirmó que el catálogo base ya existe en dominio y persistencia:
  - `PlatformRole`
  - `PlatformRoleRepositoryPort.findAll()`
  - `PlatformRoleRepositoryAdapter.findAll()`
- Se confirmó que el seed canonico ya expone `KEYGO_ADMIN`, `KEYGO_ACCOUNT_ADMIN` y `KEYGO_USER`.
- Se confirmó que `ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED` ya existe y puede reutilizarse.
- Se descartó reutilizar `PlatformUserController` para este endpoint porque su path base
  (`/api/v1/platform/users`) y su `@PreAuthorize("hasRole('KEYGO_ADMIN')")` a nivel clase no
  calzan con el contrato requerido.
- Se dejó como recomendación de diseño crear `PlatformRoleController` nuevo con
  `@RequestMapping("/api/v1/platform/roles")` y
  `@PreAuthorize("hasAnyRole('KEYGO_ADMIN','KEYGO_ACCOUNT_ADMIN')")`.

### 2026-04-13 — 📋 Planificada

- Con el análisis anterior, T-146 queda delimitada como cambio acotado a `keygo-app`,
  `keygo-api`, `keygo-run`, `postman` y documentación.
- Se consolidó como solución propuesta un `PlatformRoleController` dedicado, evitando reutilizar
  `PlatformUserController` por incompatibilidad de path y autorización.
- No se observa necesidad de RFC ni de cambios en `keygo-supabase` o Flyway para implementar esta
  tarea.

### 2026-04-13 — 🟢 Aprobada

- El usuario aprobó explícitamente el plan de T-146.
- Se mantuvo el alcance aprobado: endpoint dedicado de catálogo, reuse de `findAll()` y sin cambios
  de esquema.

### 2026-04-13 — 🔵 En desarrollo

- Se agregó `GetPlatformRolesCatalogResult` y `GetPlatformRolesCatalogUseCase` en `keygo-app`.
- Se agregó `PlatformRoleData` y el nuevo `PlatformRoleController` con
  `GET /api/v1/platform/roles`.
- Se registró el bean del use case en `ApplicationConfig`.
- Se añadieron pruebas focalizadas para use case y controller.
- Se actualizaron Postman y documentación frontend/admin, incluyendo feedback out `BE-006`.

### 2026-04-13 — 🔄 En revisión

- El alcance implementado expone el catálogo de roles de plataforma como lista simple consumible
  por UI con `id`, `code`, `name` y `description`.
- La autorización del endpoint quedó aislada en un controller propio para permitir
  `KEYGO_ADMIN` y `KEYGO_ACCOUNT_ADMIN` sin heredar restricciones de `PlatformUserController`.
- La validación focalizada pasó en `keygo-app` y `keygo-api`; la corrida completa del repo siguió
  fallando por tests preexistentes de `RecoverPasswordUseCaseTest` y `ResetPasswordUseCaseTest`,
  fuera del alcance de T-146.

### 2026-04-13 — 🧩 Pendiente integración UI

- El backend ya expone `GET /api/v1/platform/roles` y entregó documentación + Postman + feedback
  out para su consumo.
- La condición de cierre queda sujeta a la confirmación de integración desde la UI admin.

### 2026-04-13 — ✅ Completada

- La UI aprobó T-146 y confirmó el consumo efectivo de `GET /api/v1/platform/roles`.
- Se cierra la tarea con backend, documentación y feedback UI↔Backend ya alineados.
- La referencia de confirmación queda trazada en `BE-006-platform-roles-catalog-endpoint.md`.
