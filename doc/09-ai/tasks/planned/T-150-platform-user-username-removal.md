# T-150 — Eliminación del concepto `username` de `PlatformUser`

**Estado:** 📋 Planificada
**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-supabase`, `keygo-infra`, docs

---

## Problema / Requisito

`platform_users` nunca tuvo columna `username` (V3__platform_users.sql). Sin embargo, el dominio modeló
`PlatformUser.username: Username` y el onboarding genera un username a partir de `firstName + lastName`
que se informa en el email de verificación. Al intentar iniciar sesión con ese valor, el login falla porque
`PlatformUserJpaRepository.findByUsername` resuelve el username como `split_part(email, '@', 1)` — que no
coincide con lo enviado por email.

Raíz del problema: el concepto de username en `PlatformUser` es un fantasma — no se persiste, no hay
columna, y la derivación en query nunca fue consistente con la generación en onboarding.

El identificador global real y único ya existe: el **email** (`CITEXT UNIQUE`). El username de contexto
tenant ya está correctamente modelado en `tenant_users.local_username`.

---

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| T-128 | absorbida por | T-128 reportaba colisión de username generado en contratos; este refactor lo resuelve eliminando la causa raíz |

---

## Solución propuesta

Eliminar el campo `username` de `PlatformUser` y todo el código que lo soporta. El login siempre será
por email. El campo `email_or_username` en los request DTOs se renombra a `email`.

La lógica de `local_username` en `TenantUser` no se toca — ese concepto es correcto y está scoped
correctamente a cada tenant.

### Componentes a modificar

| Componente | Cambio |
|---|---|
| `PlatformUser` (domain) | Eliminar campo `username: Username` |
| `Username` (value object) | Evaluar si sigue siendo usado fuera de `PlatformUser`; eliminar si no |
| `CreatePlatformUserCommand` | Eliminar parámetro `username` y su validación |
| `CreatePlatformUserUseCase` | Eliminar check `existsByUsername` y asignación al builder |
| `PlatformUserRepositoryPort` | Eliminar `existsByUsername(Username)` |
| `PlatformUserRepositoryAdapter` | Eliminar delegación a `existsByUsername` |
| `PlatformUserJpaRepository` | Eliminar `findByUsername` y `existsByUsername` con `@Query` |
| `AuthenticatePlatformUserCommand` | Renombrar campo `emailOrUsername` → `email` |
| `AuthenticateUserCommand` | Renombrar campo `emailOrUsername` → `email` |
| `AuthenticatePlatformUserUseCase` | Eliminar bifurcación username/email; siempre buscar por email |
| `AuthenticateUserForAuthorizationUseCase` | Ídem |
| `SendPlatformPasswordResetCodeUseCase` | Reemplazar `user.getUsername().value()` por `user.getFirstName()` en variables de email |
| `SendPasswordResetCodeUseCase` | Ídem |
| `LoginRequest` (keygo-api auth) | Renombrar `emailOrUsername` → `email` |
| `PlatformLoginRequest` (keygo-api platform) | Renombrar `emailOrUsername` → `email` |
| Controladores y mappers | Actualizar referencias al campo renombrado |
| Templates de email | Usar `userFirstName` en lugar de `userUsername` como saludo |
| OpenAPI / Postman | Actualizar contratos del login |
| `doc/02-functional/frontend/frontend-developer-guide.md` | Actualizar contrato de login |

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Eliminar campo `username` y getter de `PlatformUser` | `keygo-domain/.../model/PlatformUser.java` | PENDING |
| 2 | Evaluar y eliminar `Username` value object si no tiene otros usos | `keygo-domain/.../model/Username.java` | PENDING |
| 3 | Eliminar parámetro `username` de `CreatePlatformUserCommand` | `keygo-app/.../command/CreatePlatformUserCommand.java` | PENDING |
| 4 | Eliminar `existsByUsername` de `PlatformUserRepositoryPort` | `keygo-app/.../port/PlatformUserRepositoryPort.java` | PENDING |
| 5 | Eliminar check `existsByUsername` y `.username()` builder en use case de creación | `keygo-app/.../usecase/CreatePlatformUserUseCase.java` | PENDING |
| 6 | Renombrar `emailOrUsername` → `email` en `AuthenticatePlatformUserCommand` | `keygo-app/.../command/AuthenticatePlatformUserCommand.java` | PENDING |
| 7 | Renombrar `emailOrUsername` → `email` en `AuthenticateUserCommand` | `keygo-app/.../command/AuthenticateUserCommand.java` | PENDING |
| 8 | Eliminar bifurcación email/username en `AuthenticatePlatformUserUseCase` | `keygo-app/.../usecase/AuthenticatePlatformUserUseCase.java` | PENDING |
| 9 | Eliminar bifurcación email/username en `AuthenticateUserForAuthorizationUseCase` | `keygo-app/.../usecase/AuthenticateUserForAuthorizationUseCase.java` | PENDING |
| 10 | Reemplazar `user.getUsername().value()` por `user.getFirstName()` en reset code | `keygo-app/.../usecase/SendPlatformPasswordResetCodeUseCase.java` | PENDING |
| 11 | Reemplazar `user.getUsername().value()` por `user.getFirstName()` en reset code | `keygo-app/.../usecase/SendPasswordResetCodeUseCase.java` | PENDING |
| 12 | Eliminar `findByUsername` y `existsByUsername` del repositorio JPA | `keygo-supabase/.../repository/PlatformUserJpaRepository.java` | PENDING |
| 13 | Eliminar delegación en el adapter | `keygo-supabase/.../adapter/PlatformUserRepositoryAdapter.java` | PENDING |
| 14 | Renombrar `emailOrUsername` → `email` en `LoginRequest` | `keygo-api/.../auth/request/LoginRequest.java` | PENDING |
| 15 | Renombrar `emailOrUsername` → `email` en `PlatformLoginRequest` | `keygo-api/.../platform/request/PlatformLoginRequest.java` | PENDING |
| 16 | Actualizar controladores y mappers que referencian el campo renombrado | `keygo-api/.../auth/controller/AuthorizationController.java`, `keygo-api/.../platform/controller/PlatformAuthController.java` | PENDING |
| 17 | Actualizar variables de email template (`userUsername` → `userFirstName`) en use cases y configs | `keygo-infra` / `application.yml` | PENDING |
| 18 | Actualizar tests afectados | `keygo-app/.../test/`, `keygo-run/.../test/` | PENDING |
| 19 | Actualizar OpenAPI + colección Postman | `postman/`, OpenAPI annotations | PENDING |
| 20 | Actualizar `doc/02-functional/frontend/frontend-developer-guide.md` | `doc/02-functional/frontend/frontend-developer-guide.md` | PENDING |

---

## Guía de verificación

```bash
# Compilación limpia sin errores
./mvnw clean package -DskipTests

# Suite completa de tests
./mvnw test

# Tests específicos de autenticación
./mvnw test -Dtest=AuthorizationControllerTest
./mvnw test -Dtest=AuthenticatePlatformUserUseCaseTest
./mvnw test -Dtest=CreatePlatformUserUseCaseTest

# Verificar que el repositorio JPA ya no tenga métodos de username
# (no debe haber ningún resultado)
grep -r "findByUsername\|existsByUsername\|emailOrUsername" keygo-*/src/main/java
```

Flujo funcional a verificar:
1. Login en `/auth/token` con email válido → `200 OK` con tokens
2. Login en `/platform/auth/token` con email válido → `200 OK` con tokens
3. Login con username generado → `401` (ya no es un identificador válido)
4. Onboarding completo → email de verificación sin referencia a "username"

---

## Historial de transiciones

- 2026-04-14 — Creada en estado 🔄 En revisión por análisis de inconsistencia entre onboarding y login.
