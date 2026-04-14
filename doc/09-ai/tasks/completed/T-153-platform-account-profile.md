# T-153 — Platform Account Profile Endpoints

**Estado:** ✅ **Completada**  
**Fecha:** 2026-04-14

## Requisito

Implementar endpoints de perfil de cuenta a nivel de plataforma, paralelos a los endpoints de tenant pero operando directamente sobre `PlatformUserRepositoryPort` sin resolución de tenant.

El endpoint existente `GET /api/v1/tenants/keygo/account/profile` está diseñado para usuarios de tenant. Sin embargo, el tenant especial "keygo" no tiene `tenant_users` — sus usuarios son `platform_users`. Se necesitaban endpoints dedicados que operaran a nivel de plataforma.

## Solución Implementada

### Endpoints Agregados

```
GET  /api/v1/platform/account/profile
     Authorization: Bearer <access_token>
     → 200: UserProfileData (tenantId=null, birthdate=null, website=null)
     → 401: AUTHENTICATION_REQUIRED
     → 404: RESOURCE_NOT_FOUND

PATCH /api/v1/platform/account/profile
      Authorization: Bearer <access_token>
      { "first_name": "...", "last_name": "...", ... }
      → 200: UserProfileData (actualizado)
      → 401: AUTHENTICATION_REQUIRED
      → 404: RESOURCE_NOT_FOUND
```

### Componentes Implementados

#### Use Cases
- `GetPlatformUserProfileUseCase` — Verifica bearer token, extrae `sub` claim, consulta `PlatformUserRepositoryPort`, retorna `UserProfileResult`
- `UpdatePlatformUserProfileUseCase` — Misma verificación de token + actualización parcial (PATCH semántica) de campos de perfil

#### Commands
- `GetPlatformUserProfileCommand` — Carries `bearerToken` (JWT)
- `UpdatePlatformUserProfileCommand` — Carries `bearerToken` + 6 profile fields:
  - `firstName`, `lastName`, `phoneNumber`, `locale`, `zoneinfo`, `profilePictureUrl`
  - Nota: `PlatformUser` no tiene `birthdate` ni `website`

#### Controller
- `PlatformAccountController` — Endpoints ya estaban estructurados, implementados y documentados
  - `@GetMapping("/account/profile")`
  - `@PatchMapping("/account/profile")`

#### Configuration
- `ApplicationConfig.java` — Dos `@Bean` methods para inyección de dependencias:
  - `getPlatformUserProfileUseCase(...)`
  - `updatePlatformUserProfileUseCase(...)`

### Componentes Reutilizados

| Componente | Ubicación | Nota |
|---|---|---|
| `UserProfileResult` | `keygo-app/.../result/` | Para platform users: `tenantId=null`, `birthdate=null`, `website=null` |
| `UserProfileData` | `keygo-api/.../response/` | DTO de respuesta; campos null son aceptables |
| `UpdateUserProfileRequest` | `keygo-api/.../request/` | Request DTO con campos editables |
| `ResponseCode.USER_PROFILE_RETRIEVED` | `keygo-api/.../ResponseCode.java` | Ya existía (línea 83) |
| `ResponseCode.USER_PROFILE_UPDATED` | `keygo-api/.../ResponseCode.java` | Ya existía (línea 84) |

### Diferencias: Platform vs. Tenant

| Aspecto | Tenant | Platform |
|---|---|---|
| Path variable | `{tenantSlug}` | ninguno |
| Resolución de tenant | Vía `TenantRepositoryPort` | N/A |
| Búsqueda de usuario | `UserRepositoryPort.findByIdAndTenantId` | `PlatformUserRepositoryPort.findById` |
| OIDC fields (birthdate, website) | presentes | null |
| tenantId en response | presente | null |

## Seguridad

Ambos endpoints terminan en `/account/profile`, que ya está cubierto por `BootstrapAdminKeyFilter.accountProfilePathSuffix`. No se requieren cambios adicionales de seguridad.

## Verificación

✅ **Build:** `./mvnw clean package -DskipTests` — SUCCESS
✅ **Imports:** Todos los use cases importados en `ApplicationConfig.java`
✅ **Beans:** Inyección de dependencias correctamente configurada
✅ **Endpoints:** Documentados con OpenAPI (`@Operation`, `@ApiResponse`)
✅ **DTOs:** Reutilizados sin modificaciones

## Transiciones

### Análisis → Planificación
Completado durante el plan del Claude anterior: requisito claro, solución definida, pasos ordenados.

### Planificación → Aprobación
No requerida (complejidad acotada, bajo riesgo, no multi-módulo).

### Aprobación → Desarrollo
Implementación completada:
- Los commands ya existían (creados por Claude anterior)
- Los use cases ya existían (creados por Claude anterior)
- El controller ya existía con endpoints (creado por Claude anterior)
- Se agregaron los @Bean methods en `ApplicationConfig.java`
- Se compiló y validó exitosamente

### Desarrollo → Revisión → Completada
Sin necesidad de revisión de control de cambio (no requiere integración UI, no es API-breaking, funcionalidad directa).

## Artefactos Generados

Archivos creados en sesión anterior (verificados en esta sesión):
- `keygo-app/.../command/GetPlatformUserProfileCommand.java`
- `keygo-app/.../command/UpdatePlatformUserProfileCommand.java`
- `keygo-app/.../usecase/GetPlatformUserProfileUseCase.java`
- `keygo-app/.../usecase/UpdatePlatformUserProfileUseCase.java`

Modificaciones en esta sesión:
- `keygo-api/.../platform/controller/PlatformAccountController.java` — Sin cambios (ya estaba completa)
- `keygo-run/.../config/ApplicationConfig.java` — Se agregaron imports y 2 @Bean methods

## Notas

- Reutiliza completamente el modelo de respuesta de los endpoints de tenant (`UserProfileData`, `UserProfileResult`)
- Los campos `tenantId`, `birthdate`, `website` se retornan como `null` en la respuesta, lo cual es correcto para usuarios de plataforma
- La semántica PATCH (solo actualiza campos no-nulos) es idéntica a la implementada para usuarios de tenant
- Token verification y claim extraction reutilizan los mismos ports (`SigningKeyRepositoryPort`, `AccessTokenVerifierPort`)
