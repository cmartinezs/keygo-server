# OAuth2 Multi-Domain Contract — Flujos de Autenticación en KeyGo

**Propósito:** Especificar contratos OAuth2 para autenticación platform-level y tenant-scoped, incluyendo PKCE, multi-domain, refresh tokens.

---

## Arquitectura de Dos Niveles

KeyGo implementa **dos flujos OAuth2 independientes**:

### Nivel 1: Platform Auth (Global)

**Endpoint:** `/api/v1/platform/oauth2/...`  
**Scope:** Autenticación de usuario **de plataforma** (admin)  
**Issuer:** `https://keygo.local/api/v1/platform`  
**Audience:** `keygo-platform`, `keygo-console`

**Casos de uso:**
- Admin accede a plataforma general (dashboards, estadísticas)
- Super-admin maneja multiples tenants desde una perspectiva global

### Nivel 2: Tenant Auth (Organizacional)

**Endpoint:** `/api/v1/tenants/{tenantSlug}/oauth2/...`  
**Scope:** Autenticación de usuario **de tenant** (miembro de org)  
**Issuer:** `https://keygo.local/api/v1/tenants/{tenantSlug}`  
**Audience:** `keygo-console`, aplicaciones cliente del tenant

**Casos de uso:**
- Usuario accede a consola del tenant (gestionar usuarios, apps, etc.)
- Usuario inicia login en aplicación cliente (OAuth2 authorization code)

---

## Flujo 1: PKCE Authorization Code (Hosted Login)

**Escenario:** Usuario intenta acceder a aplicación cliente. Aplicación redirige a hosted login de KeyGo.

### 1. Cliente Inicia

```
GET /api/v1/tenants/{tenantSlug}/oauth2/authorize
  ?client_id=app-uuid
  &redirect_uri=https://myapp.local/callback
  &scope=openid profile email
  &code_challenge=E9Mrozoa2owUBdtTvme4Z3IHoWAcvjjvj_VN7CcISGQ
  &code_challenge_method=S256
  &state=abc123xyz
  &nonce=xyz789abc
```

**PKCE:** Cliente genera:
- `code_verifier` — 43-128 caracteres aleatorios
- `code_challenge = base64url(sha256(code_verifier))`
- Envía `code_challenge` en request (mantiene `code_verifier` secreto)

**Validaciones:**
- ¿Tenant existe? → 404 RESOURCE_NOT_FOUND
- ¿App existe en tenant? → 404 RESOURCE_NOT_FOUND
- ¿redirect_uri registrado? → 400 INVALID_INPUT
- ¿code_challenge_method = S256? → 400 (solo S256 soportado)

**Response:**
```json
{
  "data": {
    "authorization_session_id": "sess-uuid",
    "login_url": "https://keygo.local/api/v1/tenants/my-company/account/login",
    "state": "abc123xyz"
  },
  "success": { "code": "AUTHORIZATION_INITIATED" }
}
```

### 2. Usuario Ingresa Credenciales

```
POST /api/v1/tenants/{tenantSlug}/account/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "authorization_session_id": "sess-uuid"
}
```

**Validaciones:**
- ¿Usuario existe en tenant?
- ¿Contraseña correcta?
- ¿Cuenta activa (no suspended)?

**Response:**
```json
{
  "data": {
    "authorization_code": "auth-code-uuid",
    "redirect_uri": "https://myapp.local/callback?code=auth-code-uuid&state=abc123xyz",
    "state": "abc123xyz"
  },
  "success": { "code": "AUTHORIZATION_CODE_ISSUED" }
}
```

**Cliente:**
1. Captura authorization code + state
2. Redirige: `Location: {redirect_uri}`
3. (SPA recibe code en URL, lo guarda en sesión)

### 3. Backend Canjea por Tokens

```
POST /api/v1/tenants/{tenantSlug}/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&client_id=app-uuid
&code=auth-code-uuid
&code_verifier=E9Mrozoa2owUBdtTvme4Z3IHoWAcvjjvj_VN7CcISGQ
&redirect_uri=https://myapp.local/callback
```

**PKCE Validation:**
1. Extrae code → obtiene code_challenge guardado
2. Calcula: `sha256(code_verifier)` → compara con code_challenge
3. Si no coinciden → 401 INVALID_AUTHORIZATION_CODE

**Response:**
```json
{
  "data": {
    "access_token": "eyJ...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_token": "refresh-uuid",
    "id_token": "eyJ...",
    "scope": "openid profile email"
  },
  "success": { "code": "TOKEN_ISSUED" }
}
```

**Access Token contiene:**
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "tenant_slug": "my-company",
  "tenant_id": "t-uuid",
  "roles": ["USER"],
  "aud": "keygo-console",
  "iss": "https://keygo.local/api/v1/tenants/my-company",
  "exp": 1680000000,
  "iat": 1679996400
}
```

---

## Flujo 2: Direct Login (API/CLI)

**Escenario:** CLI tool o backend service necesita token sin user interaction.

### Request

```
POST /api/v1/tenants/{tenantSlug}/account/direct-login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

### Response

```json
{
  "data": {
    "access_token": "eyJ...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_token": "refresh-uuid"
  },
  "success": { "code": "DIRECT_LOGIN_SUCCESSFUL" }
}
```

**Nota:** No se retorna `code` ni `id_token`. Solo access + refresh.

---

## Flujo 3: Refresh Token Rotation

**Escenario:** Access token expirado, cliente usa refresh token para obtener nuevo.

### Request

```
POST /api/v1/tenants/{tenantSlug}/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token=refresh-uuid
&client_id=app-uuid
```

### Response

```json
{
  "data": {
    "access_token": "eyJ...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "refresh_token": "refresh-uuid-new",  // ← Nuevo refresh token
    "scope": "openid profile email"
  },
  "success": { "code": "TOKEN_REFRESHED" }
}
```

**Rotación:** Refresh token anterior se invalida, retorna nuevo.

---

## Flujo 4: Client Credentials (M2M)

**Escenario:** Aplicación cliente necesita token sin usuario final (integración, webhook, etc.).

**Prerequisito:** App cliente debe ser `type=CONFIDENTIAL` con secret.

### Request

```
POST /api/v1/tenants/{tenantSlug}/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic base64(client_id:client_secret)

grant_type=client_credentials
&scope=api:write api:read
```

### Response

```json
{
  "data": {
    "access_token": "eyJ...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "scope": "api:write api:read"
  },
  "success": { "code": "TOKEN_ISSUED" }
}
```

**Access Token contiene:**
```json
{
  "sub": "app-uuid",  // ← Client app, no user
  "client_id": "app-uuid",
  "tenant_slug": "my-company",
  "aud": "api",
  "iss": "https://keygo.local/api/v1/tenants/my-company",
  "exp": 1680000000
}
```

---

## Flujo 5: Token Revocation

**Escenario:** Usuario logout, revocar token.

### Request

```
POST /api/v1/platform/oauth2/revoke
Content-Type: application/x-www-form-urlencoded

token=access-or-refresh-token
&client_id=app-uuid
```

### Response

```json
{
  "data": null,
  "success": { "code": "TOKEN_REVOKED" }
}
```

**Comportamiento:**
- Si token es refresh → invalida todos sus access tokens
- Si token es access → solo revoca ese token

---

## Token Endpoint: Resumen

**Método:** POST  
**Path:** `/api/v1/tenants/{tenantSlug}/oauth2/token`

**Soportados:**
| Grant Type | Soportado | Requisitos |
|---|---|---|
| `authorization_code` | ✅ | PKCE (code_verifier obligatorio) |
| `refresh_token` | ✅ | Refresh token válido |
| `client_credentials` | ✅ | Client secret (Basic auth) |
| `password` | ❌ | (Resource Owner Password — no PKCE, deprecado) |

**Errores Comunes:**
- 400 INVALID_INPUT — parámetro faltante o inválido
- 401 INVALID_AUTHORIZATION_CODE — code expirado o inválido
- 401 INVALID_CLIENT_CREDENTIALS — secret incorrecto
- 409 BUSINESS_RULE_VIOLATION — tenant suspendido o app inactiva

---

## Multi-Domain: Issuer y Audience

### Issuer (iss)

**Platform:**
```
https://keygo.local/api/v1/platform
```

**Tenant:**
```
https://keygo.local/api/v1/tenants/{tenantSlug}
```

**Uso:**
- Token solo válido para issuer que lo emitió
- Si cambias issuer → debe re-autenticarse
- Frontend valida `iss` en JWT para verificar origen

### Audience (aud)

**Platform:**
```json
"aud": "keygo-platform"
```

**Tenant:**
```json
"aud": "keygo-console"  // o app-specific
```

**Uso:**
- Limita a qué API puedes usar token
- Token con aud=keygo-platform no sirve para tenant endpoints
- Client app puede validar aud si quiere restricción adicional

---

## Scopes

### Estándar OpenID Connect

| Scope | Claims en ID Token | Propósito |
|---|---|---|
| `openid` | sub | ID token presence |
| `profile` | name, picture, zoneinfo | User profile |
| `email` | email, email_verified | Email contact |

### Custom Scopes

| Scope | Propósito | Ejemplo |
|---|---|---|
| `api:read` | Leer datos (M2M) | Client credentials |
| `api:write` | Escribir datos (M2M) | Client credentials |
| `platform` | Acceso global | Platform auth |

---

## Checklist: Nuevo OAuth2 Endpoint

Al agregar nuevo endpoint OAuth2:

- [ ] **¿Qué nivel?** (Platform `/api/v1/platform/...` vs Tenant `/api/v1/tenants/{tenantSlug}/...`)
- [ ] **¿Qué grant soporta?** (authorization_code, refresh_token, client_credentials)
- [ ] **¿PKCE obligatorio?** (Sí para authorization_code público)
- [ ] **¿Validar tenant?** (Sí para tenant-scoped)
- [ ] **¿Validar app?** (Sí si requiere client_id)
- [ ] **JWT estructura:** ¿sub, aud, iss, roles, tenant_slug?
- [ ] **Documentar en ENDPOINT_CATALOG.md**
- [ ] **Documentar en ERROR_CATALOG.md** (errores OAuth2)

---

## Anti-Patterns: Evitar

### ❌ No implementar PKCE

```java
// MAL: Authorization code sin PKCE
GET /oauth2/authorize?client_id=...&redirect_uri=...&scope=...
// → Token puede ser robado en redirect
```

### ✅ PKCE siempre

```java
// BIEN: PKCE obligatorio para public clients
code_challenge = base64url(sha256(random_verifier))
GET /oauth2/authorize?...&code_challenge=...&code_challenge_method=S256
```

---

### ❌ Tokens sin tenant scope

```json
// MAL
{
  "sub": "user-uuid",
  "roles": ["ADMIN"]
  // Sin tenant_slug → ¿Qué tenant administra?
}
```

### ✅ Tenant siempre en token

```json
// BIEN
{
  "sub": "user-uuid",
  "tenant_slug": "my-company",
  "roles": ["ADMIN"]
}
```

---

### ❌ Misma URL para platform y tenant auth

```java
// MAL: Confunde niveles
GET /oauth2/authorize?...
// ¿Es platform o tenant?
```

### ✅ URLs explícitas por nivel

```java
// BIEN: Claro el nivel
GET /api/v1/platform/oauth2/authorize?...     // Platform
GET /api/v1/tenants/{slug}/oauth2/authorize?... // Tenant
```

---

## Token Lifetimes

| Token | TTL | Rotable | Revocable |
|---|---|---|---|
| **Access Token** | 1 hora | No (crear nuevo) | Sí |
| **Refresh Token** | 30 días | Sí (cada uso) | Sí |
| **Authorization Code** | 10 minutos | N/A | Sí (one-time) |

---

## Diagnosticando OAuth2

### ¿Token inválido o expirado?

```bash
# Decodificar token
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson'

# Verificar exp claim
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .exp'
date +%s  # ¿exp > now?
```

### ¿PKCE inválido?

```bash
# Calcular challenge del verifier
verifier="abc123..."
echo -n "$verifier" | sha256sum | xxd -r -p | base64 -w 0 | tr '+/' '-_' | tr -d '='
# Comparar con code_challenge enviado
```

### ¿Refresh token rechazado?

```bash
# Verificar que refresh_token aún es válido
# Refresh tokens se invalidan cuando se rotan
# Si intentas reusar viejo → 401 INVALID_REFRESH_TOKEN
```

---

## Referencias

| Recurso | Ubicación |
|---|---|
| **Controllers** | `keygo-api/src/main/java/io/cmartinezs/keygo/api/auth/` |
| **Platform Auth** | `keygo-api/src/main/java/io/cmartinezs/keygo/api/platform/controller/PlatformAuthController.java` |
| **Use Cases** | `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/` |
| **Tokens** | `docs/design/AUTHORIZATION_PATTERNS.md` (JWT claims) |
| **Endpoints** | `docs/design/api/ENDPOINT_CATALOG.md` (OAuth2 endpoints) |
| **RFC origen** | `docs/rfc/restructure-multitenant/06-jwt-y-autorizacion.md` |
| **Errores** | `docs/design/api/ERROR_CATALOG.md` (401, 409 codes) |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 2  
**RFC Closure:** Absorbe decisiones de restructure-multitenant §06 + §04
