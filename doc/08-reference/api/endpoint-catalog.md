# Endpoint Catalog — Inventario Consolidado de Endpoints

**Propósito:** Mapa centralizado de todos los endpoints API por dominio funcional.

---

## 📌 Nota de Uso

- **Autenticación:** Bearer JWT en header `Authorization: Bearer {token}`
- **Response:** `BaseResponse<T>` con estructura de error estándar (ver [`error-catalog.md`](error-catalog.md))
- **Códigos HTTP:** Mapeados en [`error-catalog.md`](error-catalog.md)

---

## 1. Tenants — Gestión de Tenants

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `GET` | `/api/v1/tenants` | keygo_admin | `BaseResponse<PagedData<TenantData>>` | 200, 400, 401 | Listar tenants con paginación, filtrado, ordenamiento |
| `POST` | `/api/v1/tenants` | keygo_admin | `BaseResponse<TenantData>` | 201, 400, 401, 409 | Crear tenant (slug derivado del nombre) |
| `GET` | `/api/v1/tenants/{slug}` | keygo_admin | `BaseResponse<TenantData>` | 200, 401, 404 | Obtener tenant por slug |
| `PUT` | `/api/v1/tenants/{slug}/suspend` | keygo_admin | `BaseResponse<TenantData>` | 200, 401, 403, 404 | Suspender tenant |
| `PUT` | `/api/v1/tenants/{slug}/activate` | keygo_admin | `BaseResponse<TenantData>` | 200, 401, 404 | Activar tenant |

**Filtros (GET /api/v1/tenants):**
- `status` — ACTIVE, SUSPENDED, PENDING
- `name_like` — búsqueda parcial (case-insensitive)
- `page`, `size` — paginación
- `sort`, `order` — ordenamiento

---

## 2. Users — Gestión de Usuarios (Tenant-scoped)

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `POST` | `/api/v1/tenants/{tenantSlug}/users` | keygo_admin OR tenant role | `BaseResponse<UserData>` | 201, 400, 401, 404, 409 | Crear usuario |
| `GET` | `/api/v1/tenants/{tenantSlug}/users` | keygo_admin OR tenant role | `BaseResponse<PagedData<UserData>>` | 200, 400, 401, 404 | Listar usuarios con paginación |
| `GET` | `/api/v1/tenants/{tenantSlug}/users/{userId}` | keygo_admin OR tenant role | `BaseResponse<UserData>` | 200, 401, 404 | Obtener usuario por UUID |
| `PUT` | `/api/v1/tenants/{tenantSlug}/users/{userId}` | keygo_admin OR tenant role | `BaseResponse<UserData>` | 200, 401, 404 | Actualizar datos de usuario (firstName, lastName, etc.) |
| `POST` | `/api/v1/tenants/{tenantSlug}/users/{userId}/reset-password` | keygo_admin OR tenant role | `BaseResponse<UserData>` | 200, 400, 401, 404 | Admin resetea contraseña de usuario |
| `PUT` | `/api/v1/tenants/{tenantSlug}/users/{userId}/suspend` | keygo_admin OR tenant role | `BaseResponse<UserData>` | 200, 401, 403, 404 | Suspender usuario |
| `PUT` | `/api/v1/tenants/{tenantSlug}/users/{userId}/activate` | keygo_admin OR tenant role | `BaseResponse<UserData>` | 200, 401, 404 | Activar usuario |
| `POST` | `/api/v1/tenants/{tenantSlug}/users/validate-credentials` | — | `BaseResponse<UserData>` | 200, 400, 401, 403, 404 | Validar credenciales (email/username + password) |

**Filtros (GET /api/v1/tenants/{tenantSlug}/users):**
- `status` — ACTIVE, SUSPENDED, PENDING, RESET_PASSWORD
- `username_like`, `email_like` — búsqueda parcial
- `page`, `size`, `sort`, `order`

---

## 3. Client Apps — OAuth2 Client Applications (Tenant-scoped)

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `POST` | `/api/v1/tenants/{tenantSlug}/apps` | ADMIN_TENANT+ | `BaseResponse<ClientAppSecretData>` | 201, 400, 401, 404 | Crear app cliente (retorna clientSecret) |
| `GET` | `/api/v1/tenants/{tenantSlug}/apps` | ADMIN_TENANT+ | `BaseResponse<PagedData<ClientAppData>>` | 200, 400, 401, 404 | Listar apps cliente |
| `GET` | `/api/v1/tenants/{tenantSlug}/apps/{clientId}` | ADMIN_TENANT+ | `BaseResponse<ClientAppData>` | 200, 401, 404 | Obtener app cliente por clientId |
| `PUT` | `/api/v1/tenants/{tenantSlug}/apps/{clientId}` | ADMIN_TENANT+ | `BaseResponse<ClientAppData>` | 200, 400, 401, 404 | Actualizar app cliente |
| `POST` | `/api/v1/tenants/{tenantSlug}/apps/{clientId}/rotate-secret` | ADMIN_TENANT+ | `BaseResponse<ClientAppSecretData>` | 200, 401, 404 | Rotar secret de client (retorna nuevo secret) |

**Filtros (GET /api/v1/tenants/{tenantSlug}/apps):**
- `status` — ACTIVE, SUSPENDED, PENDING
- `name_like` — búsqueda parcial
- `page`, `size`, `sort`, `order`

---

## 4. Client App Roles — Roles por Aplicación (Tenant-scoped)

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `POST` | `/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles` | ADMIN_TENANT+ | `BaseResponse<AppRoleData>` | 201, 400, 401, 404, 409 | Crear rol en app |
| `GET` | `/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles` | ADMIN_TENANT+ | `BaseResponse<PagedData<AppRoleData>>` | 200, 400, 401, 404 | Listar roles de app |
| `GET` | `/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles/{roleCode}` | ADMIN_TENANT+ | `BaseResponse<AppRoleData>` | 200, 401, 404 | Obtener rol por código |
| `PUT` | `/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles/{roleCode}` | ADMIN_TENANT+ | `BaseResponse<AppRoleData>` | 200, 401, 404 | Actualizar rol |
| `DELETE` | `/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles/{roleCode}` | ADMIN_TENANT+ | `BaseResponse<Void>` | 204, 401, 404 | Eliminar rol |
| `POST` | `/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles/{roleCode}/parent` | ADMIN_TENANT+ | `BaseResponse<AppRoleData>` | 200, 400, 401, 404 | Asignar rol padre (jerarquía) |
| `DELETE` | `/api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles/{roleCode}/parent` | ADMIN_TENANT+ | `BaseResponse<AppRoleData>` | 200, 401, 404 | Remover rol padre |

---

## 5. Memberships — Asignación de Roles a Usuarios (Tenant-scoped)

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `POST` | `/api/v1/tenants/{tenantSlug}/memberships` | ADMIN_TENANT+ | `BaseResponse<MembershipData>` | 201, 400, 401, 404, 409 | Asignar roles a usuario en una app |
| `GET` | `/api/v1/tenants/{tenantSlug}/memberships` | ADMIN_TENANT+ | `BaseResponse<PagedData<MembershipData>>` | 200, 400, 401, 404 | Listar membresías |
| `GET` | `/api/v1/tenants/{tenantSlug}/memberships/{membershipId}` | ADMIN_TENANT+ | `BaseResponse<MembershipData>` | 200, 401, 404 | Obtener membresía |
| `PUT` | `/api/v1/tenants/{tenantSlug}/memberships/{membershipId}/approve` | ADMIN_TENANT+ | `BaseResponse<MembershipData>` | 200, 401, 404 | Aprobar membresía (si pending) |
| `DELETE` | `/api/v1/tenants/{tenantSlug}/memberships/{membershipId}` | ADMIN_TENANT+ | `BaseResponse<Void>` | 204, 401, 404 | Revocar membresía (soft-delete) |

**Filtros (GET /api/v1/tenants/{tenantSlug}/memberships):**
- `status` — ACTIVE, PENDING, REVOKED
- `userId`, `appId` — filtros opcionales
- `page`, `size`, `sort`, `order`

---

## 6. Platform Auth — Autenticación (Public + Tenant-aware)

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `GET` | `/api/v1/platform/oauth2/authorize` | — | Redirect a login | 302, 400 | Iniciar flujo PKCE OAuth2 |
| `POST` | `/api/v1/platform/account/check-email` | — | `BaseResponse<Void>` | 200, 401, 404 | Verificar si un email ya existe como platform user antes del ToS |
| `POST` | `/api/v1/platform/account/login` | — | `BaseResponse<LoginData>` | 200, 400, 401, 403 | Login con email/password (PKCE) |
| `POST` | `/api/v1/platform/account/direct-login` | — | `BaseResponse<TokenData>` | 200, 400, 401 | Login directo (sin PKCE, para API/CLI) |
| `POST` | `/api/v1/platform/oauth2/token` | — | `BaseResponse<TokenData>` | 200, 400, 401 | Intercambiar código/refresh → tokens |
| `POST` | `/api/v1/platform/oauth2/revoke` | — | `BaseResponse<Void>` | 200, 400 | Revocar token/refresh |
| `GET` | `/api/v1/platform/userinfo` | Bearer | `BaseResponse<UserInfoData>` | 200, 401 | Obtener info de usuario autenticado |

---

## 7. Platform Users — Gestión de Usuarios de Plataforma

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `POST` | `/api/v1/platform/users` | keygo_admin | `BaseResponse<PlatformUserData>` | 201, 400, 401, 409 | Crear usuario de plataforma |
| `GET` | `/api/v1/platform/users` | keygo_admin | `BaseResponse<PagedData<PlatformUserData>>` | 200, 400, 401 | Listar usuarios de plataforma |
| `GET` | `/api/v1/platform/users/{userId}` | keygo_admin | `BaseResponse<PlatformUserData>` | 200, 401, 404 | Obtener usuario por UUID |
| `PUT` | `/api/v1/platform/users/{userId}` | keygo_admin | `BaseResponse<PlatformUserData>` | 200, 401, 404 | Actualizar usuario |
| `PUT` | `/api/v1/platform/users/{userId}/suspend` | keygo_admin | `BaseResponse<PlatformUserData>` | 200, 401, 403, 404 | Suspender usuario |
| `PUT` | `/api/v1/platform/users/{userId}/activate` | keygo_admin | `BaseResponse<PlatformUserData>` | 200, 401, 404 | Activar usuario |
| `POST` | `/api/v1/platform/users/{userId}/reset-password` | keygo_admin | `BaseResponse<PlatformUserData>` | 200, 400, 401, 404 | Admin resetea contraseña |
| `POST` | `/api/v1/platform/users/{userId}/platform-roles` | keygo_admin | `BaseResponse<PlatformUserRoleData>` | 201, 400, 401, 404, 409 | Asignar rol de plataforma a usuario |
| `DELETE` | `/api/v1/platform/users/{userId}/platform-roles/{roleCode}` | keygo_admin | `BaseResponse<Void>` | 204, 401, 404 | Revocar rol de plataforma |

---

## 8. Account Self-Service — Autogestionamiento de Cuenta

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `GET` | `/api/v1/tenants/{tenantSlug}/account/profile` | User | `BaseResponse<AccountProfileData>` | 200, 401, 404 | Obtener perfil de usuario logueado |
| `PATCH` | `/api/v1/tenants/{tenantSlug}/account/profile` | User | `BaseResponse<AccountProfileData>` | 200, 400, 401, 404 | Actualizar perfil propio |
| `POST` | `/api/v1/tenants/{tenantSlug}/account/change-password` | User | `BaseResponse<Void>` | 200, 400, 401, 403, 404 | Cambiar contraseña propia |
| `GET` | `/api/v1/tenants/{tenantSlug}/account/settings/notification-preferences` | User | `BaseResponse<NotificationPreferencesData>` | 200, 401, 404 | Obtener preferencias de notificación |
| `PATCH` | `/api/v1/tenants/{tenantSlug}/account/settings/notification-preferences` | User | `BaseResponse<NotificationPreferencesData>` | 200, 400, 401, 404 | Actualizar preferencias |
| `POST` | `/api/v1/tenants/{tenantSlug}/account/forgot-password` | — | `BaseResponse<Void>` | 200, 400 | Iniciar flujo de recuperación de contraseña |
| `POST` | `/api/v1/tenants/{tenantSlug}/account/recover-password` | — | `BaseResponse<Void>` | 200, 400, 404 | Validar código y resetear contraseña |
| `GET` | `/api/v1/tenants/{tenantSlug}/account/sessions` | User | `BaseResponse<List<SessionData>>` | 200, 401, 404 | Listar sesiones activas |
| `DELETE` | `/api/v1/tenants/{tenantSlug}/account/sessions/{sessionId}` | User | `BaseResponse<Void>` | 204, 401, 404 | Revocar sesión |

---

## 9. Email Verification — Verificación de Email (Public + Tenant-aware)

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `POST` | `/api/v1/tenants/{tenantSlug}/register` | — | `BaseResponse<UserData>` | 201, 400, 409 | Registrar usuario (envía verification email) |
| `POST` | `/api/v1/tenants/{tenantSlug}/resend-verification` | — | `BaseResponse<Void>` | 200, 400, 404 | Reenviar email de verificación |
| `POST` | `/api/v1/tenants/{tenantSlug}/verify-email` | — | `BaseResponse<Void>` | 200, 400, 404 | Verificar email con código |

---

## 10. Billing — Contratos y Suscripciones (Platform-level)

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `GET` | `/api/v1/platform/billing/catalog` | ADMIN | `BaseResponse<List<BillingPlanCatalogData>>` | 200, 401 | Obtener catálogo de planes |
| `GET` | `/api/v1/platform/billing/catalog/{planCode}` | ADMIN | `BaseResponse<BillingPlanCatalogData>` | 200, 401, 404 | Obtener plan específico |
| `POST` | `/api/v1/billing/contracts` | — | `BaseResponse<AppContractData>` | 201, 400, 404, 409 | Crear contrato de facturación |
| `GET` | `/api/v1/billing/contracts/{contractId}` | — | `BaseResponse<AppContractData>` | 200, 404 | Obtener contrato |
| `GET` | `/api/v1/billing/contracts/{contractId}/resume` | — | `BaseResponse<AppContractResumeData>` | 200, 404, 422 | Obtener estado de onboarding para restaurar la UI |
| `POST` | `/api/v1/billing/contracts/{contractId}/verify-email` | — | `BaseResponse<AppContractData>` | 200, 400, 404, 422 | Verificar email del contrato |
| `POST` | `/api/v1/billing/contracts/{contractId}/resend-verification` | — | `BaseResponse<AppContractData>` | 200, 404, 422 | Reenviar verification email |
| `POST` | `/api/v1/billing/contracts/{contractId}/activate` | — | `BaseResponse<AppContractData>` | 200, 404, 422 | Activar contrato |
| `POST` | `/api/v1/billing/contracts/{contractId}/mock-approve-payment` | — | `BaseResponse<AppContractData>` | 200, 404, 422 | (MOCK) Simular aprobación de pago y provisionamiento final |
| `GET` | `/api/v1/platform/billing/subscription` | User | `BaseResponse<SubscriptionData>` | 200, 401 | Obtener suscripción del usuario/tenant |
| `POST` | `/api/v1/platform/billing/subscription/cancel` | User | `BaseResponse<Void>` | 200, 401, 403 | Cancelar suscripción |
| `GET` | `/api/v1/platform/billing/invoices` | User | `BaseResponse<List<InvoiceData>>` | 200, 401 | Listar facturas |

---

## 11. System & Metadata — Información del Sistema

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `GET` | `/api/v1/service/info` | — | `BaseResponse<ServiceInfoData>` | 200 | Obtener info de servicio (versión, build, etc.) |
| `GET` | `/api/v1/response-codes` | ADMIN | `BaseResponse<List<ResponseCodeData>>` | 200, 401 | Listar todos los ResponseCode |
| `GET` | `/.well-known/openid-configuration` | — | OIDC metadata | 200 | OpenID Connect metadata (standard) |
| `GET` | `/.well-known/jwks.json` | — | JWKS | 200 | JSON Web Key Set para validar JWT (standard) |

---

## 12. Platform Dashboard & Analytics

| Método | Path | Auth | Respuesta | Códigos | Propósito |
|---|---|---|---|---|---|
| `GET` | `/api/v1/platform/dashboard` | ADMIN | `BaseResponse<DashboardData>` | 200, 401 | Obtener dashboard de plataforma |
| `GET` | `/api/v1/platform/stats` | ADMIN | `BaseResponse<StatsData>` | 200, 401 | Obtener estadísticas globales |

---

## Patrones Generales

### Autenticación

**Platform Roles (bien-conocidos):**
- `keygo_admin` — Administrador global de plataforma (acceso a todos los tenants)
- `keygo_account_admin` — Admin de cuenta de plataforma (gestión de platform users)
- `keygo_user` — Usuario regular de plataforma

**Tenant Roles (custom, definidos por tenant):**
- Cada tenant define sus propios roles (ej: `ADMIN_ORG`, `MANAGER`, `VIEWER`)
- Código de rol: uppercase, única dentro del tenant

**App Roles (custom, definidos por app cliente):**
- Cada aplicación cliente define sus roles (ej: `EDITOR`, `VIEWER`)

**Tenant-scoped:**
Endpoints bajo `/api/v1/tenants/{tenantSlug}/...` validan que:
1. El usuario tenga `keygo_admin` (acceso global), O
2. El tenant en el token coincida con el de la URL

### Errores

**4xx (cliente):**
- 400 INVALID_INPUT — validación fallida
- 401 AUTHENTICATION_REQUIRED — token faltante/expirado/inválido
- 403 BUSINESS_RULE_VIOLATION — rol insuficiente o estado inválido
- 404 RESOURCE_NOT_FOUND — recurso no existe

**5xx (servidor):**
- 500 OPERATION_FAILED — excepción no mapeada, BD inaccesible, etc.

### Paginación

Endpoints con `*List*` u endpoint collection soportan:
```
?page=0&size=20&sort=createdAt&order=DESC
```

Response es `PagedData<T>` con campos:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 1234,
  "totalPages": 62,
  "last": false
}
```

### Response Structure

Todos los endpoints retornan `BaseResponse<T>`:
```json
{
  "data": {...},
  "success": {
    "code": "TENANT_CREATED",
    "message": "Tenant created successfully"
  },
  "error": null
}
```

En caso de error:
```json
{
  "data": null,
  "success": null,
  "error": {
    "code": "INVALID_INPUT",
    "message": "Request validation failed",
    "origin": "CLIENT_REQUEST",
    "layer": "HTTP",
    "traceId": "abc123...",
    "fieldErrors": [
      {"field": "email", "message": "email must be a valid email address"}
    ]
  }
}
```

---

## Checklist: Nuevo Endpoint

Al agregar un nuevo endpoint:

- [ ] ¿Ubicación correcta? (controller temático bajo `/api/v1/{domain}/...`)
- [ ] ¿Autenticación? (@PreAuthorize con roles correctos)
- [ ] ¿Validación? (@Valid en request DTO)
- [ ] ¿Response type? (BaseResponse<T> con T apropriado)
- [ ] ¿Errores documentados? (@ApiResponse para cada código HTTP)
- [ ] ¿Actualizar `error-catalog.md`? (nuevos `ResponseCode`)
- [ ] ¿Actualizar este catálogo? (agregar fila en tabla)

---

## Referencias

| Recurso | Ubicación |
|---|---|
| **Validación** | [`../../03-architecture/patterns/validation-strategy.md`](../../03-architecture/patterns/validation-strategy.md) |
| **Errores** | [`error-catalog.md`](error-catalog.md) |
| **Debugging** | [`../../06-quality/debug-guide.md`](../../06-quality/debug-guide.md) |
| **OpenAPI** | `/v3/api-docs` (Swagger UI en `/swagger-ui.html`) |
| **Controllers** | `keygo-api/src/main/java/io/cmartinezs/keygo/api/{domain}/controller/` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 1

