# T-142 — Endpoint `GET /api/v1/platform/users`

**Estado:** 🧩 Pendiente integración UI  
**Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-supabase`, `keygo-run`, `postman`, docs  
**Tipo de registro:** retroactivo

---

## Problema / Requisito

La UI necesitaba consumir `GET /api/v1/platform/users?page=0&size=20`, pero el backend
respondía `HttpRequestMethodNotSupportedException` porque `PlatformUserController` no
exponía el `GET` de colección, solo creación y detalle por `userId`.

Además, la documentación funcional de frontend ya declaraba este endpoint como existente,
por lo que había drift entre contrato documentado y comportamiento real del backend.

## Relaciones

- **derivada de:** `doc/02-functional/frontend/feedback/UI-001-platform-users-list-endpoint.md`

## Solución aplicada

Se implementó `GET /api/v1/platform/users` reutilizando el patrón existente de listados
paginados del proyecto:

- filtro de paginación/ordenamiento en `keygo-app`
- `PagedResult<PlatformUser>` en caso de uso y persistencia
- `PagedData<PlatformUserData>` en la API
- soporte JPA paginado en `keygo-supabase`
- bean registrado en `ApplicationConfig`
- actualización de docs frontend y colección Postman

### Contrato de respuesta

| Condición | HTTP | Campo | Código |
|---|---|---|---|
| Listado obtenido | 200 | `success` | `PLATFORM_USER_LIST_RETRIEVED` |
| Parámetros de paginación inválidos | 400 | `failure` | `INVALID_INPUT` |
| Token ausente o inválido | 401 | `failure` | `AUTHENTICATION_REQUIRED` |

### Query params soportados

| Parámetro | Tipo | Obligatorio | Observación |
|---|---|---|---|
| `page` | `int` | no | default `0` |
| `size` | `int` | no | default `20` |
| `status` | `UserStatus` | no | filtro exacto |
| `username_like` | `string` | no | filtro parcial |
| `email_like` | `string` | no | filtro parcial |
| `sort` | `string` | no | `username`, `email`, `status`, `createdAt`, `firstName`, `lastName` |
| `order` | `ASC\|DESC` | no | default `ASC` |

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Crear `PlatformUserFilter` | `keygo-app/.../user/filter/PlatformUserFilter.java` | APPLIED |
| 2 | Crear `ListPlatformUsersUseCase` | `keygo-app/.../user/usecase/ListPlatformUsersUseCase.java` | APPLIED |
| 3 | Extender `PlatformUserRepositoryPort` con `findAllPaged` | `keygo-app/.../user/port/PlatformUserRepositoryPort.java` | APPLIED |
| 4 | Agregar soporte paginado en `PlatformUserRepositoryAdapter` | `keygo-supabase/.../user/adapter/PlatformUserRepositoryAdapter.java` | APPLIED |
| 5 | Habilitar `JpaSpecificationExecutor` en `PlatformUserJpaRepository` | `keygo-supabase/.../user/repository/PlatformUserJpaRepository.java` | APPLIED |
| 6 | Agregar `PLATFORM_USER_LIST_RETRIEVED` | `keygo-api/.../shared/ResponseCode.java` | APPLIED |
| 7 | Exponer `GET /platform/users` en `PlatformUserController` | `keygo-api/.../platform/controller/PlatformUserController.java` | APPLIED |
| 8 | Registrar bean `ListPlatformUsersUseCase` | `keygo-run/.../config/ApplicationConfig.java` | APPLIED |
| 9 | Agregar tests del use case y controller | `keygo-app/.../ListPlatformUsersUseCaseTest.java`, `keygo-api/.../PlatformUserControllerTest.java` | APPLIED |
| 10 | Actualizar docs frontend, feedback y Postman | `doc/02-functional/frontend/...`, `postman/KeyGo-Server.postman_collection.json` | APPLIED |

---

## Verificación

```bash
./mvnw -pl keygo-run -am test -Dtest=PlatformUserControllerTest,ListPlatformUsersUseCaseTest -Djacoco.skip=true
```

## Nota de trazabilidad

Este documento se registra **retroactivamente** porque la corrección se implementó de forma
directa para desbloquear a la UI antes de crear la tarea formal. Se mantiene el feedback UI→Backend
como origen del requerimiento, y esta tarea deja la trazabilidad técnica que faltaba en `doc/09-ai/tasks`.
