# API Surface (Consolidated)

⚠️ **This documentation has been consolidated.**

**See:** [`api/ENDPOINT_CATALOG.md`](api/ENDPOINT_CATALOG.md) for complete API endpoint reference with:
- All 60+ endpoints organized by domain
- Authentication requirements
- Request/response examples
- Error codes
- Current implementation status

This file is maintained for backward compatibility only. All updates are made to the canonical `api/ENDPOINT_CATALOG.md`.- autenticación y emisión de tokens,
- administración por tenant,
- administración global de plataforma.

### 2.2. Tenant como contexto obligatorio
Toda operación de tenant debe resolverse dentro de un contexto de tenant explícito o inferido por subdominio.

### 2.3. Estándares donde corresponde
Los flujos OAuth2/OIDC deben parecerse lo máximo posible a la expectativa estándar del ecosistema.

### 2.4. Errores consistentes
Las respuestas de error deben ser predecibles y trazables.

### 2.5. Seguridad contextual
No basta con estar autenticado; el tipo de token, tenant y rol deben ser coherentes con el endpoint accedido.

---

## 3. Planos de la API

## 3.1. Auth Plane
Responsable de:
- login,
- OAuth2/OIDC,
- emisión de tokens,
- metadata OIDC,
- userinfo,
- revocación,
- self-service de identidad.

Base path conceptual:
- `/{tenant}/oauth2/...`
- `/{tenant}/.well-known/...`
- `/{tenant}/account/...`

---

## 3.2. Tenant Plane
Responsable de:
- administración de apps,
- administración de usuarios,
- memberships,
- roles,
- auditoría del tenant.

Base path conceptual:
- `/{tenant}/admin/...`

---

## 3.3. Control Plane
Responsable de:
- administración global de tenants,
- operación del SaaS,
- soporte,
- auditoría global.

Base path conceptual:
- `/platform/...`

---

## 4. Convenciones generales

## 4.1. Formato
- Request/response en JSON para APIs administrativas.
- Endpoints OAuth/OIDC con formato estándar según corresponda.

## 4.2. Versionado
Recomendación inicial:
- versionado a nivel de ruta solo si es necesario más adelante.
- para MVP, mantener rutas limpias y controlar cambios por compatibilidad.

## 4.3. Identificadores
- IDs internos opacos (`uuid` recomendado)
- `client_id` público para clientes OAuth
- `slug` para tenant

## 4.4. Trazabilidad
Toda respuesta de error debería incluir o correlacionarse con un `traceId` o equivalente.

---

## 5. Auth Plane

## 5.1. OpenID Configuration

### Endpoint
`GET /{tenant}/.well-known/openid-configuration`

### Propósito
Exponer metadata OIDC del tenant.

### Respuesta esperada
Incluye al menos:
- issuer
- authorization_endpoint
- token_endpoint
- jwks_uri
- userinfo_endpoint
- revocation_endpoint
- supported_grant_types
- supported_response_types
- supported_scopes
- supported_code_challenge_methods

### Seguridad
Público.

---

## 5.2. JWKS

### Endpoint
`GET /{tenant}/.well-known/jwks.json`

### Propósito
Exponer claves públicas para validación de tokens.

### Seguridad
Público.

---

## 5.3. Authorize

### Endpoint
`GET /{tenant}/oauth2/authorize`

### Propósito
Iniciar Authorization Code Flow.

### Query params mínimos
- `response_type`
- `client_id`
- `redirect_uri`
- `scope`
- `state`
- `code_challenge`
- `code_challenge_method`

### Reglas
- validar tenant activo,
- validar app activa,
- validar redirect URI exacta,
- validar grant permitido,
- validar scopes permitidos,
- preservar contexto para Hosted Login.

### Respuestas posibles
- redirección a login,
- error estándar OAuth si la request es inválida.

### Seguridad
Público, pero validado contextualmente.

---

## 5.4. Hosted Login Submit

### Endpoint conceptual
`POST /{tenant}/account/login`

### Propósito
Recibir credenciales del usuario desde la UI de login de Key-go y continuar el flujo de autorización.

### Request body base
```json
{
  "usernameOrEmail": "user@example.com",
  "password": "******",
  "authorizationContextId": "..."
}
```

### Reglas
- validar credenciales,
- validar estado del usuario,
- validar tenant,
- validar acceso a la app objetivo según membership/policy,
- emitir authorization code y redirigir o responder según flujo UI.

### Seguridad
Público en cuanto a token, pero atado a un contexto de autorización válido.

---

## 5.5. Token

### Endpoint
`POST /{tenant}/oauth2/token`

### Propósito
Intercambiar authorization code por tokens o emitir token técnico vía client credentials.

### Casos soportados en MVP
- `authorization_code`
- `refresh_token`
- `client_credentials`

### Request examples

#### Authorization Code
```json
{
  "grant_type": "authorization_code",
  "code": "...",
  "redirect_uri": "https://app.example.com/callback",
  "client_id": "app1",
  "code_verifier": "..."
}
```

#### Refresh Token
```json
{
  "grant_type": "refresh_token",
  "refresh_token": "...",
  "client_id": "app1"
}
```

#### Client Credentials
```json
{
  "grant_type": "client_credentials",
  "client_id": "app1",
  "scope": "api.read api.write"
}
```

### Respuesta base
```json
{
  "access_token": "...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "...",
  "id_token": "...",
  "scope": "openid profile email"
}
```

### Seguridad
- para confidential clients: autenticar client con secret o mecanismo equivalente
- para public clients: PKCE obligatorio en authorization code flow

---

## 5.6. Revoke

### Endpoint
`POST /{tenant}/oauth2/revoke`

### Propósito
Revocar refresh token o token revocable equivalente.

### Request body base
```json
{
  "token": "...",
  "client_id": "app1"
}
```

### Seguridad
Requiere contexto válido del client correspondiente.

---

## 5.7. UserInfo

### Endpoint
`GET /{tenant}/userinfo`

### Propósito
Entregar claims del usuario autenticado.

### Respuesta base
```json
{
  "sub": "user-id",
  "email": "user@example.com",
  "name": "User Example",
  "tenant_id": "tenant-id",
  "client_id": "app1",
  "roles": ["USER"],
  "scope": "openid profile email"
}
```

### Seguridad
Bearer access token válido de usuario.

---

## 5.8. Forgot Password

### Endpoint
`POST /{tenant}/account/forgot-password`

### Propósito
Iniciar recuperación de contraseña.

### Request body
```json
{
  "email": "user@example.com"
}
```

### Reglas
- no filtrar si el usuario existe o no,
- generar token o mecanismo de recuperación.

---

## 5.9. Reset Password

### Endpoint
`POST /{tenant}/account/reset-password`

### Request body
```json
{
  "token": "recovery-token",
  "newPassword": "new-password"
}
```

---

## 5.10. Change Password

### Endpoint
`POST /{tenant}/account/change-password`

### Request body
```json
{
  "currentPassword": "old-password",
  "newPassword": "new-password"
}
```

### Seguridad
Bearer token de usuario autenticado.

---

## 6. Tenant Plane

## 6.1. Apps - Create

### Endpoint
`POST /{tenant}/admin/apps`

### Propósito
Registrar una nueva aplicación cliente dentro del tenant.

### Request body base
```json
{
  "name": "App 1",
  "clientType": "CONFIDENTIAL",
  "allowedGrants": ["authorization_code", "refresh_token", "client_credentials"],
  "allowedScopes": ["openid", "profile", "email"],
  "redirectUris": [
    "https://app1.example.com/callback"
  ],
  "accessPolicy": "CLOSED_APP"
}
```

### Respuesta base
```json
{
  "appId": "...",
  "clientId": "...",
  "clientSecret": "...",
  "name": "App 1"
}
```

### Seguridad
Requiere rol tenant admin o superior.

### Regla importante
`clientSecret` solo debe mostrarse una vez.

---

## 6.2. Apps - List

### Endpoint
`GET /{tenant}/admin/apps`

### Seguridad
Tenant admin.

---

## 6.3. Apps - Detail

### Endpoint
`GET /{tenant}/admin/apps/{appId}`

---

## 6.4. Apps - Update

### Endpoint
`PATCH /{tenant}/admin/apps/{appId}`

### Request body base
```json
{
  "name": "Nuevo nombre",
  "status": "ACTIVE",
  "allowedScopes": ["openid", "profile"],
  "accessPolicy": "OPEN_JOIN"
}
```

---

## 6.5. Apps - Rotate Secret

### Endpoint
`POST /{tenant}/admin/apps/{appId}/rotate-secret`

### Respuesta base
```json
{
  "clientId": "app1",
  "clientSecret": "new-secret"
}
```

---

## 6.6. Usuarios - Create

### Endpoint
`POST /{tenant}/admin/users`

### Request body base
```json
{
  "email": "user@example.com",
  "displayName": "User Example",
  "username": "userexample"
}
```

### Seguridad
Tenant admin.

---

## 6.7. Usuarios - List

### Endpoint
`GET /{tenant}/admin/users`

### Filtros sugeridos
- status
- email
- appId *(opcional, si quieres filtrar por membership)*

---

## 6.8. Usuarios - Detail

### Endpoint
`GET /{tenant}/admin/users/{userId}`

---

## 6.9. Usuarios - Update

### Endpoint
`PATCH /{tenant}/admin/users/{userId}`

### Request body base
```json
{
  "displayName": "Nuevo nombre",
  "status": "SUSPENDED"
}
```

---

## 6.10. Usuarios - Reset Password Admin

### Endpoint
`POST /{tenant}/admin/users/{userId}/reset-password`

### Propósito
Forzar recuperación o reinicio administrado.

---

## 6.11. Memberships - Create

### Endpoint
`POST /{tenant}/admin/memberships`

### Request body base
```json
{
  "userId": "...",
  "appId": "...",
  "status": "ACTIVE"
}
```

### Seguridad
Tenant admin.

---

## 6.12. Memberships - List

### Endpoint
`GET /{tenant}/admin/memberships`

### Filtros sugeridos
- userId
- appId
- status

---

## 6.13. Memberships - Delete / Revoke

### Endpoint
`DELETE /{tenant}/admin/memberships/{membershipId}`

### Regla
Puede ser delete lógico o revocación según implementación.

---

## 6.14. Membership Roles - Assign

### Endpoint
`POST /{tenant}/admin/memberships/{membershipId}/roles`

### Request body base
```json
{
  "roleCodes": ["USER", "VIEWER"]
}
```

---

## 6.15. App Roles - Create

### Endpoint
`POST /{tenant}/admin/apps/{appId}/roles`

### Request body base
```json
{
  "code": "ADMIN",
  "name": "Administrador"
}
```

---

## 6.16. Tenant Audit

### Endpoint
`GET /{tenant}/admin/audit`

### Propósito
Consultar eventos relevantes del tenant.

### Seguridad
Tenant admin o tenant readonly según política.

---

## 7. Control Plane

## 7.1. Tenants - Create

### Endpoint
`POST /platform/tenants`

### Request body base
```json
{
  "slug": "acme",
  "name": "Acme Ltda",
  "status": "ACTIVE"
}
```

### Seguridad
Platform admin o superior.

---

## 7.2. Tenants - List

### Endpoint
`GET /platform/tenants`

---

## 7.3. Tenants - Detail

### Endpoint
`GET /platform/tenants/{tenantId}`

---

## 7.4. Tenants - Update

### Endpoint
`PATCH /platform/tenants/{tenantId}`

### Request body base
```json
{
  "status": "SUSPENDED"
}
```

---

## 7.5. Global Audit

### Endpoint
`GET /platform/audit`

### Seguridad
Platform admin, support readonly o equivalente.

---

## 7.6. Support Access

### Endpoint
`POST /platform/support-access`

### Propósito
Otorgar acceso explícito y auditable a un tenant para tareas de soporte.

### Request body base
```json
{
  "tenantId": "...",
  "reason": "Investigación de incidente de login",
  "mode": "READ_ONLY",
  "expiresAt": "2026-03-20T12:00:00Z"
}
```

### Seguridad
Platform support o superior.

---

## 8. Seguridad por tipo de token

## 8.1. Bearer token de usuario
Se usa para:
- `/userinfo`
- `/account/change-password`
- operaciones self-service del usuario

## 8.2. Bearer token administrativo de tenant
Se usa para:
- `/admin/...`

Debe contener rol administrativo compatible con el tenant actual.

## 8.3. Bearer token de plataforma
Se usa para:
- `/platform/...`

Debe contener rol de plataforma compatible.

## 8.4. Token técnico M2M
Se usa para consumo de APIs que no impliquen identidad humana.

No debe usarse para acciones de usuario final ni para suplantar identidad humana.

---

## 9. Errores esperados

## 9.1. Error envelope sugerido para APIs JSON
```json
{
  "code": "USER_NOT_FOUND",
  "message": "The requested user was not found",
  "traceId": "...",
  "details": {}
}
```

## 9.2. Categorías comunes
- `TENANT_NOT_FOUND`
- `TENANT_SUSPENDED`
- `CLIENT_NOT_FOUND`
- `CLIENT_DISABLED`
- `INVALID_REDIRECT_URI`
- `INVALID_SCOPE`
- `INVALID_CREDENTIALS`
- `USER_SUSPENDED`
- `MEMBERSHIP_NOT_FOUND`
- `MEMBERSHIP_NOT_ACTIVE`
- `ACCESS_DENIED`
- `UNSUPPORTED_GRANT_TYPE`
- `INVALID_AUTHORIZATION_CODE`
- `INVALID_REFRESH_TOKEN`
- `TOKEN_REVOKED`
- `ROLE_NOT_FOUND`
- `FORBIDDEN`
- `VALIDATION_ERROR`

## 9.3. OAuth/OIDC errors
En endpoints estándar conviene responder con semántica estándar cuando corresponda:
- `invalid_request`
- `unauthorized_client`
- `access_denied`
- `unsupported_response_type`
- `invalid_scope`
- `server_error`
- `temporarily_unavailable`

---

## 10. Reglas de seguridad por endpoint

### Auth Plane
- públicos o semipúblicos, pero siempre validados por tenant/client/contexto

### Tenant Plane
- requieren identidad autenticada con rol de tenant apropiado
- nunca deben operar fuera del tenant contextual

### Control Plane
- requieren rol de plataforma
- acceso altamente auditado

### Reglas adicionales
- rate limiting en login y token endpoint
- validación estricta de redirect URI
- secrets y tokens revocables nunca en texto plano persistido

---

## 11. Headers y contexto recomendados

## 11.1. Correlation / Trace
- `X-Request-Id` o equivalente
- opcional: `X-Correlation-Id`

## 11.2. Tenant Context
Idealmente inferido desde subdominio.

En ambientes locales o internos, opcionalmente se puede permitir estrategia alternativa temporal, pero sin romper el modelo conceptual.

## 11.3. Authorization
- `Authorization: Bearer <token>`

---

## 12. Orden recomendado de implementación de la API

### Primero
- `/.well-known/openid-configuration`
- `/.well-known/jwks.json`
- `/oauth2/authorize`
- `/account/login`
- `/oauth2/token`

### Luego
- `/admin/apps`
- `/admin/users`
- `/admin/memberships`
- `/userinfo`
- `/account/forgot-password`
- `/account/reset-password`
- `/account/change-password`

### Después
- `/platform/tenants`
- `/platform/audit`
- `/platform/support-access`

---

## 13. Corte MVP recomendado de API

El MVP de API queda suficientemente útil si incluye al menos:

### Auth Plane
- openid-configuration
- jwks
- authorize
- login submit
- token
- userinfo
- forgot/reset/change password

### Tenant Plane
- create/list/update apps
- rotate secret
- create/list/update users
- create/list/delete memberships
- assign roles a membership

### Control Plane
- create/list/update tenants
- audit global básico

---

## 14. Decisiones ya fijadas por esta superficie API

Con este documento, quedan consolidadas las siguientes decisiones:

- separación por planos,
- endpoints OAuth/OIDC alineados al estándar,
- tenant admin API independiente del control plane,
- memberships como pieza central del acceso por app,
- self-service mínimo del usuario final,
- soporte auditable desde plataforma,
- JWT + JWKS como mecanismo base de interoperabilidad.

---

## 15. Próximo paso recomendado

Con Architecture, Backlog, Domain Model y API Surface definidos, el siguiente paso natural es crear uno de estos dos artefactos:

1. **`KEYGO_SERVER_IMPLEMENTATION_PLAN.md`**
   - orden técnico real de desarrollo
   - módulos a crear primero
   - dependencias entre componentes
   - hitos por sprint

2. **`KEYGO_SERVER_PROJECT_STRUCTURE.md`**
   - estructura exacta de paquetes/módulos
   - naming de clases base
   - convención para controllers, use cases, ports y adapters

La secuencia más útil ahora sería:

**API Surface → Project Structure → Implementation Plan → código.**

