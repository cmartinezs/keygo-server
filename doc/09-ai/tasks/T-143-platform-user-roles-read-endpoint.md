# T-143 — Endpoint `GET /api/v1/platform/users/{userId}/platform-roles`

**Estado:** 📋 Planificada  
**Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-run`, `postman`, docs

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

- **derivada de:** `doc/02-functional/frontend/feedback/in/UI-002-missing-platform-user-roles-endpoint.md`

## Solución propuesta

Agregar el endpoint `GET /api/v1/platform/users/{userId}/platform-roles` en
`PlatformUserController`, reutilizando el stack existente de roles de plataforma:

- usar `PlatformUserRoleRepositoryPort` como fuente principal
- introducir un use case de lectura acotado
- definir DTO/contrato de respuesta consumible por UI
- registrar `ResponseCode` específico de lectura si corresponde
- actualizar Postman y documentación frontend/admin

### Contrato propuesto

Pendiente de implementación, pero debería devolver una colección de roles asignados al usuario
en un contrato estable para UI, idealmente con al menos:

- `roleCode`
- nombre legible / display name si ya está disponible sin duplicar lógica

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Confirmar la fuente de datos a reutilizar (`findByPlatformUserId` o `findRoleCodesByPlatformUserId`) y el shape del response | `keygo-app/.../membership/port/PlatformUserRoleRepositoryPort.java` | APPLIED |
| 2 | Crear use case de lectura para roles de `PlatformUser` | `keygo-app/.../membership/usecase/...` | PENDING |
| 3 | Crear DTO de respuesta para roles de plataforma asignados | `keygo-api/.../platform/response/...` o `membership/response/...` | PENDING |
| 4 | Exponer `GET /platform/users/{userId}/platform-roles` en `PlatformUserController` | `keygo-api/.../platform/controller/PlatformUserController.java` | PENDING |
| 5 | Agregar `ResponseCode` de lectura si el contrato no reutiliza uno existente | `keygo-api/.../shared/ResponseCode.java` | PENDING |
| 6 | Registrar bean del nuevo use case | `keygo-run/.../config/ApplicationConfig.java` | PENDING |
| 7 | Cubrir controller + use case con tests | `keygo-api/.../PlatformUserControllerTest.java`, `keygo-app/...` | PENDING |
| 8 | Actualizar documentación frontend, feedback out y Postman al resolver | `doc/02-functional/frontend/...`, `postman/...` | PENDING |

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
- No se implementa todavía: queda esperando aprobación explícita del usuario.
