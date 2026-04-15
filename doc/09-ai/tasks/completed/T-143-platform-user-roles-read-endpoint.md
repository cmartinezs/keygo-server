# T-143 — Endpoint `GET /api/v1/platform/users/{userId}/platform-roles`

**Estado:** ✅ Completada  
**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-supabase`, `keygo-run`, `postman`, docs

---

## Problema / Requisito

La UI necesita consultar los roles de un usuario de plataforma mediante
`GET /api/v1/platform/users/{userId}/platform-roles`, pero el backend hoy no expone ese
endpoint. El resultado actual es `HttpRequestMethodNotSupportedException`.

El gap es específico de la superficie HTTP: ya existe soporte de persistencia para consultar
roles asignados a un usuario (`PlatformUserRoleRepositoryPort.findByPlatformUserId(...)` y
`findRoleCodesByPlatformUserId(...)`), por lo que no parece requerir cambios estructurales
de modelo o base de datos.

## Relaciones

- **derivada de:** `doc/02-functional/frontend/feedback/UI-002-platform-user-roles-endpoint.md`

## Solución propuesta

Agregar el endpoint `GET /api/v1/platform/users/{userId}/platform-roles` en
`PlatformUserController`, reutilizando el stack existente de roles de plataforma:

- usar `PlatformUserRoleRepositoryPort.findByPlatformUserId(...)` como fuente principal
- enriquecer la respuesta con `PlatformRoleRepositoryPort.findAll()` para resolver metadata
  (`code`, `name`, `description`) sin agregar queries nuevas en persistencia
- introducir un use case de lectura acotado
- definir DTO/contrato de respuesta consumible por UI
- registrar `ResponseCode` específico de lectura si corresponde
- actualizar Postman y documentación frontend/admin

### Contrato propuesto

La recomendación de análisis es **no paginar** este endpoint: el recurso ya está acotado a un
usuario específico y la cardinalidad esperable de roles de plataforma es baja.

`GET /api/v1/platform/users/{userId}/platform-roles`

```typescript
// Response 200 — List<PlatformUserRoleData>
{
  "data": [
    {
      "assignmentId": "uuid",
      "roleId": "uuid",
      "roleCode": "keygo_admin",
      "roleName": "KeyGo Admin",
      "description": "Full administrative access to the platform",
      "scopeType": "CONTRACTOR",
      "contractorId": "uuid",
      "tenantId": "uuid",
      "contractor": {
        "id": "uuid",
        "displayName": "Acme SpA",
        "billingEmail": "billing@acme.cl"
      },
      "assignedAt": "2026-04-13T08:00:00Z"
    }
  ]
}
```

Campos recomendados:

- `assignmentId`
- `roleId`
- `roleCode`
- `roleName`
- `description`
- `scopeType`
- `contractorId`
- `tenantId`
- `contractor { id, displayName, billingEmail }`
- `assignedAt`

Código recomendado:

- `PLATFORM_ROLE_LIST_RETRIEVED`

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Confirmar la fuente de datos a reutilizar y el shape del response | `keygo-app/.../membership/port/PlatformUserRoleRepositoryPort.java`, `PlatformRoleRepositoryPort.java` | APPLIED |
| 2 | Crear use case de lectura para roles de `PlatformUser`, uniendo asignaciones con catálogo de roles | `keygo-app/.../membership/usecase/...` | APPLIED |
| 3 | Crear DTO de respuesta para roles asignados con metadata de rol, assignment, scope y contractor resumido | `keygo-api/.../platform/response/...` o `membership/response/...` | APPLIED |
| 4 | Agregar `PLATFORM_ROLE_LIST_RETRIEVED` | `keygo-api/.../shared/ResponseCode.java` | APPLIED |
| 5 | Exponer `GET /platform/users/{userId}/platform-roles` en `PlatformUserController` | `keygo-api/.../platform/controller/PlatformUserController.java` | APPLIED |
| 6 | Registrar bean del nuevo use case | `keygo-run/.../config/ApplicationConfig.java` | APPLIED |
| 7 | Cubrir controller + use case con tests | `keygo-api/.../PlatformUserControllerTest.java`, `keygo-app/...` | APPLIED |
| 8 | Actualizar documentación frontend/admin, feedback out y Postman al resolver | `doc/02-functional/frontend/...`, `postman/...` | APPLIED |

---

## Verificación

```bash
./mvnw -pl keygo-run -am test -Dtest=PlatformUserControllerTest
```

## Notas de análisis

- `PlatformUserController` ya maneja asignación y revocación de roles, por lo que el `GET`
  faltante calza naturalmente en la misma superficie.
- La persistencia ya expone métodos de lectura para roles asignados, lo que reduce el alcance
  a wiring de aplicación/API y contrato de respuesta.
- `PlatformUserRole` en dominio originalmente solo traía `platformRoleId` y `assignedAt`; para
  cubrir mejor la necesidad de UI se amplió además con `id`, `scopeType`, `contractorId` y
  `tenantId` conservando la fuente de datos ya existente en persistencia.
- `PlatformRoleRepositoryPort.findAll()` ya expone el catálogo completo de roles con metadata,
  por lo que el enriquecimiento se puede hacer en el use case sin tocar `keygo-supabase`.
- `PlatformUserRoleJpaRepository.findRoleCodesByPlatformUserId(...)` existe, pero para incluir
  `assignedAt` y metadata de asignación conviene usar `findByPlatformUserId(...)` y resolver la
  metadata del rol por `platformRoleId`.
- El ajuste de contrato pidió también contexto de contractor. Como `contractor_id` ya vive en la
  asignación persistida, se reutiliza `ContractorRepositoryPort.findById(...)` para enriquecer la
  respuesta sin agregar nuevas tablas ni migraciones.
- Con el diseño aplicado, **`keygo-supabase` no requirió cambios estructurales ni nuevas queries**;
  solo se amplió el mapping de `PlatformUserRoleEntity` hacia dominio para no perder metadata.
- Si una asignación apunta a un rol inexistente en catálogo, el use case no debería omitirla en
  silencio; debe fallar de forma explícita porque la relación está respaldada por FK y ese caso
  indicaría inconsistencia de datos.

## Historial de transiciones

### 2026-04-13 — 🔍 En análisis

- Se confirmó el gap en `PlatformUserController`: existen `POST /{userId}/platform-roles` y
  `DELETE /{userId}/platform-roles/{roleCode}`, pero no `GET /{userId}/platform-roles`.
- Se confirmó reutilización suficiente en app/persistencia:
  - `PlatformUserRoleRepositoryPort.findByPlatformUserId(UUID)`
  - `PlatformRoleRepositoryPort.findAll()`
  - `PlatformUserRoleJpaRepository.findByPlatformUserId(UUID)`
- Se definió como recomendación de diseño devolver lista simple, no paginada, con metadata del
  rol y `assignedAt`.

### 2026-04-13 — 📋 Planificada

- Con el análisis anterior, T-143 queda delimitada como cambio acotado a `keygo-app`,
  `keygo-api`, `keygo-run`, `postman` y documentación.
- No se observa necesidad actual de RFC ni de cambios en `keygo-supabase`.

### 2026-04-13 — 🟢 Aprobada

- El usuario aprobó explícitamente implementar T-143.
- Se mantuvo el diseño recomendado: lista simple no paginada, enriquecida con metadata del rol.

### 2026-04-13 — 🔵 En desarrollo

- Se implementó `ListPlatformUserRolesUseCase` usando `PlatformUserRepositoryPort`,
  `PlatformUserRoleRepositoryPort` y `PlatformRoleRepositoryPort`.
- Se agregó el read model `PlatformUserRoleResult` en `keygo-app`.
- Se agregó `PlatformUserRoleData` y el endpoint
  `GET /api/v1/platform/users/{userId}/platform-roles` en `PlatformUserController`.
- Se agregó `PLATFORM_ROLE_LIST_RETRIEVED` y el bean correspondiente en `ApplicationConfig`.

### 2026-04-13 — 🔄 En revisión

- Se añadieron pruebas focalizadas para el use case y el controller.
- La validación focalizada pasó con:
  `./mvnw -pl keygo-run -am test -Dtest=PlatformUserControllerTest,ListPlatformUserRolesUseCaseTest -Djacoco.skip=true`

### 2026-04-13 — 🧩 Pendiente integración UI

- El backend ya expone el endpoint requerido por la UI.
- Falta la confirmación de consumo desde la pantalla admin para cerrar completamente el circuito.

### 2026-04-13 — 🛂 Control de cambio

- Desde la integración UI se pidió enriquecer el contrato con más contexto de asignación:
  `assignmentId`, `scopeType`, `contractorId`, `tenantId` y contractor resumido.
- Se aprobó incorporar este ajuste dentro de la misma T-143 en vez de abrir una nueva tarea.

### 2026-04-13 — 🔵 En desarrollo

- Se amplió `PlatformUserRole` para no perder metadata ya presente en persistencia
  (`scopeType`, `contractorId`, `tenantId`) y se expuso `assignmentId` desde el identificador de
  la asignación.
- `ListPlatformUserRolesUseCase` ahora enriquece además el contractor cuando la asignación viene
  scopeada, usando `ContractorRepositoryPort`.

### 2026-04-13 — 🔄 En revisión

- Se ajustaron tests y contrato API para cubrir assignment, scope y contractor resumido.
- Postman y documentación funcional se alinearon al nuevo shape de respuesta.

### 2026-04-13 — 🧩 Pendiente integración UI

- El endpoint sigue disponible para la UI, ahora con metadata de asignación y contractor resumido.
- La condición de cierre vuelve a ser la confirmación de consumo desde la pantalla admin.

### 2026-04-13 — ✅ Completada

- El usuario aprobó la revisión de T-143.
- Se cierra la tarea con el endpoint disponible, contrato enriquecido y artefactos sincronizados
  en código, documentación y Postman.
