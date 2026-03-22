# Entity Relationships & Data Flows — KeyGo Server

> Diagramas complementarios de **relaciones de entidades**, **flujos de datos** y **contextos de negocio**.
>
> Fecha de actualización: **2026-03-22**

---

## Tabla de contenidos

1. [Relaciones por contexto de negocio](#relaciones-por-contexto-de-negocio)
2. [Flujos de autenticación (Authorization Code)](#flujos-de-autenticación-authorization-code)
3. [Flujos de token (refresh, revoke)](#flujos-de-token-refresh-revoke)
4. [Ciclo de vida de memberships y roles](#ciclo-de-vida-de-memberships-y-roles)
5. [Modelo de permisos y autorización](#modelo-de-permisos-y-autorización)

---

## Relaciones por contexto de negocio

### Contexto 1: Tenant Management

```mermaid
classDiagram
    class Tenant {
        UUID id
        String slug*
        String name
        Status status
        Timestamp createdAt
        Timestamp updatedAt
    }

    class ClientApp {
        UUID id
        UUID tenantId*
        String clientId*
        ClientType type
        String displayName
        Status status
    }

    class TenantUser {
        UUID id
        UUID tenantId*
        String email*
        String username*
        String displayName
        Status status
    }

    Tenant "1" --> "0..∞" ClientApp : owns
    Tenant "1" --> "0..∞" TenantUser : contains
```

**Invariante:** Todo usuario y app dentro de un tenant debe tener `tenant_id` consistente.

---

### Contexto 2: Client Application Management

```mermaid
classDiagram
    class ClientApp {
        UUID id
        String clientId*
        ClientType type
        String clientSecret
        Status status
    }

    class ClientRedirectUri {
        UUID id
        String redirectUri
    }

    class ClientAllowedGrant {
        UUID id
        GrantType grantType
    }

    class ClientAllowedScope {
        UUID id
        String scope
    }

    ClientApp "1" --> "1..*" ClientRedirectUri : registers
    ClientApp "1" --> "0..*" ClientAllowedGrant : permits
    ClientApp "1" --> "0..*" ClientAllowedScope : permits
```

**Reglas:**
- Redirect URIs validación exacta (sin wildcards).
- Client solo puede usar grants/scopes registrados aquí.
- Secret solo para tipo `CONFIDENTIAL`.

---

### Contexto 3: User Identity & Membership

```mermaid
classDiagram
    class TenantUser {
        UUID id
        String email*
        String username*
        String passwordHash
        Status status
    }

    class Membership {
        UUID id
        UUID userId*
        UUID clientAppId*
        Status status
    }

    class AppRole {
        UUID id
        UUID clientAppId*
        String code*
        String name
        Status status
    }

    class MembershipRole {
        UUID id
        UUID membershipId*
        UUID appRoleId*
    }

    TenantUser "1" --> "0..*" Membership : has
    Membership "1" --> "0..*" MembershipRole : assigned
    AppRole "1" --> "0..*" MembershipRole : grants
```

**Invariantes:**
- Un usuario puede tener 0 o más memberships.
- Una membership = acceso potencial a una app.
- Una membership = 0 o más roles dentro de esa app.
- Roles son específicos por app.

---

### Contexto 4: Authorization & Token Lifecycle

```mermaid
classDiagram
    class AuthorizationCode {
        UUID id
        UUID userId*
        UUID clientAppId*
        String code
        String redirectUri
        String codeChallenge
        Status status
        Timestamp expiresAt
    }

    class RefreshToken {
        UUID id
        UUID userId*
        UUID clientAppId*
        String tokenHash
        Status status
        UUID rotatedFrom
    }

    class Session {
        UUID id
        UUID userId*
        UUID clientAppId*
        String ipAddress
        Status status
    }

    AuthorizationCode "1" --> "0..1" RefreshToken : exchanges-to
    RefreshToken "1" --> "0..1" RefreshToken : rotates-to
    Session "1" --|> "0..1" AuthorizationCode : initiated-by
```

**Flujos:**
1. Authorization Code → canjeable una sola vez → RefreshToken + AccessToken.
2. RefreshToken → renovable múltiples veces → nuevo AccessToken.
3. Session → trazabilidad de login exitoso.

---

## Flujos de autenticación (Authorization Code)

### OAuth2 Authorization Code Flow + PKCE

```mermaid
sequenceDiagram
    participant User as User (Browser)
    participant App as ClientApp (SPA/Mobile)
    participant KeyGo as KeyGo Server
    participant DB as DB

    User->>App: Click "Sign in"
    App->>App: Generate code_challenge (PKCE)
    App->>KeyGo: GET /authorize?client_id=X&redirect_uri=Y&code_challenge=Z&scope=...

    KeyGo->>DB: Validate: client_id, redirect_uri en DB
    alt Valid?
        KeyGo->>User: Render login form
        User->>KeyGo: POST login (email + password)
        KeyGo->>DB: Find user by email in tenant
        alt User found & active?
            KeyGo->>DB: Validate password_hash
            alt Password valid?
                KeyGo->>DB: Check membership in app (status=ACTIVE)
                alt Membership active?
                    KeyGo->>DB: Create AuthorizationCode (ACTIVE, 10min expiry)
                    KeyGo->>KeyGo: Redirect to redirect_uri with code=ABC...
                    KeyGo->>User: Redirect browser
                    User->>App: Browser receives code
                else
                    KeyGo->>User: Error: No membership
                end
            else
                KeyGo->>User: Error: Invalid credentials
            end
        else
            KeyGo->>User: Error: User not found or suspended
        end
    else
        KeyGo->>User: Error: Invalid client or redirect_uri
    end

    App->>KeyGo: POST /token (code=ABC, code_verifier=XYZ, client_id)
    KeyGo->>DB: Find AuthorizationCode where code=ABC
    alt Code valid, not expired, status=ACTIVE?
        KeyGo->>KeyGo: Validate code_verifier vs code_challenge (PKCE)
        alt PKCE valid?
            KeyGo->>DB: Update AuthorizationCode status=CONSUMED
            KeyGo->>DB: Create RefreshToken + emit AccessToken (JWT)
            KeyGo->>KeyGo: Sign JWT with private key (RS256)
            KeyGo->>App: Return {access_token, refresh_token, expires_in}
        else
            KeyGo->>App: Error: Invalid code_verifier
        end
    else
        KeyGo->>App: Error: Code expired/consumed/invalid
    end

    App->>KeyGo: Use access_token in Authorization header
```

---

### Verificación de memberships en login

```mermaid
graph TD
    A["Login form submited"] -->|email + password| B["Buscar user"]
    B -->|¿user found?| C{SÍ}
    B -->|NO| D["Error: User not found"]
    C -->|Validar password| E{¿válido?}
    E -->|NO| F["Error: Invalid credentials"]
    E -->|SÍ| G["Buscar membership: client_app_id, status=ACTIVE"]
    G -->|¿membership found?| H{SÍ}
    G -->|NO| I["Error: User not authorized for this app"]
    H -->|Crear AuthorizationCode| J["Redirect con code"]
    I -->|Error| K["Response 403"]
    D -->|Error| K
    F -->|Error| K
```

---

## Flujos de token (refresh, revoke)

### Refresh Token Flow

```mermaid
sequenceDiagram
    participant App as ClientApp
    participant KeyGo as KeyGo Server
    participant DB as DB

    App->>App: Access token approaching expiry
    App->>KeyGo: POST /token (grant_type=refresh_token, refresh_token=XYZ)

    KeyGo->>DB: Find RefreshToken where token_hash=hash(XYZ)
    alt Token found & status=ACTIVE?
        KeyGo->>DB: Check not expired
        alt Not expired?
            KeyGo->>DB: Check user & app still active
            alt User active & membership active?
                KeyGo->>DB: Create new RefreshToken (rotate)
                KeyGo->>DB: Update old RefreshToken status=USED, rotated_from=NULL
                KeyGo->>KeyGo: Emit new AccessToken (JWT)
                KeyGo->>App: Return {access_token, refresh_token, expires_in}
            else
                KeyGo->>App: Error: User or membership suspended/revoked
            end
        else
            KeyGo->>App: Error: Refresh token expired
        end
    else
        KeyGo->>App: Error: Invalid or revoked refresh token
    end
```

### Token Revocation

```mermaid
graph LR
    A["User logs out"] -->|revoke refresh_token| B["Mark RefreshToken status=REVOKED"]
    B --> C["All future token refreshes fail"]

    D["Admin revokes membership"] -->|revoke ACTIVE| E["Membership status=REVOKED"]
    E --> F["User cannot log in to app"]
    F --> G["Existing tokens still valid until expiry*"]

    H["Session.terminate()"] -->|update status| I["Session status=TERMINATED"]
    I --> J["Logout everywhere: revoke all refresh_tokens of user"]
```

**Nota:** Los access tokens (JWT firmados) no se revocan en DB; solo se revisan en el siguiente refresh. Para revocación inmediata, usar `Session` + lista negra opcional.

---

## Ciclo de vida de memberships y roles

### Creación y transiciones de Membership

```mermaid
stateDiagram-v2
    [*] --> INVITED: CreateMembership
    INVITED --> ACTIVE: User accepts / Admin confirms
    ACTIVE --> SUSPENDED: Admin suspends
    SUSPENDED --> ACTIVE: Admin reactivates
    SUSPENDED --> REVOKED: Admin revokes (irreversible)
    ACTIVE --> REVOKED: Admin revokes
    REVOKED --> [*]
    INVITED --> REVOKED: Invitation expires / Admin revokes

    note right of INVITED
        Usuario invitado a la app;
        no puede loguear aún
    end note

    note right of ACTIVE
        Usuario puede acceder
        a la app
    end note

    note right of SUSPENDED
        Acceso temporalmente
        bloqueado
    end note

    note right of REVOKED
        Acceso permanentemente
        revocado; no reverso
    end note
```

---

### Asignación de roles a un usuario en una app

```mermaid
graph TD
    A["User tiene membership ACTIVE en app X"] -->|tiene roles| B["AppRole:ADMIN"]
    A -->|tiene roles| C["AppRole:USER"]
    A -->|tiene roles| D["AppRole:VIEWER"]

    E["MembershipRole: membership_id=123, app_role_id=101"]
    F["MembershipRole: membership_id=123, app_role_id=102"]
    G["MembershipRole: membership_id=123, app_role_id=103"]

    B -.->|via MembershipRole| E
    C -.->|via MembershipRole| F
    D -.->|via MembershipRole| G

    H["AccessToken JWT claims: roles = [ADMIN, USER, VIEWER]"]
    E -.->|included in| H
    F -.->|included in| H
    G -.->|included in| H
```

---

## Modelo de permisos y autorización

### Matriz de decisión para acceso a app

```mermaid
graph TD
    A["Validar acceso de usuario a app"] --> B{¿Tenant activo?}
    B -->|NO| C["❌ DENY - Tenant suspended/archived"]
    B -->|SÍ| D{¿ClientApp activo?}

    D -->|NO| E["❌ DENY - App disabled"]
    D -->|SÍ| F{¿User activo?}

    F -->|NO| G["❌ DENY - User not active/suspended"]
    F -->|SÍ| H{¿Membership existe?}

    H -->|NO| I["❌ DENY - User not a member"]
    H -->|SÍ| J{¿Membership activo?}

    J -->|NO| K["❌ DENY - Membership suspended/revoked"]
    J -->|SÍ| L["✅ ALLOW - User can log in"]

    L -->|Cargar roles| M["Query MembershipRoles → AppRoles"]
    M -->|Construir claims| N["JWT {sub, roles, scopes, ...}"]
```

---

### Flujo de validación en endpoint protegido

```mermaid
sequenceDiagram
    participant Client as Client App
    participant API as Protected API
    participant KeyGo as KeyGo Server
    participant DB as DB

    Client->>API: GET /resource with Authorization: Bearer JWT

    API->>API: Verify JWT signature (JWKS)
    alt JWT valid & not expired?
        API->>API: Extract claims: sub (user_id), aud (client_id), scope, roles
        API->>KeyGo: Validate claims (optional: cached or local)
        KeyGo->>DB: Check Session status (for revocation)
        alt Session ACTIVE?
            API->>API: Check: required scopes in JWT
            alt Scopes match?
                API->>API: Check: required roles in JWT
                alt Roles match?
                    API->>API: Proceed to business logic
                    API->>Client: 200 OK + Resource
                else
                    API->>Client: 403 Forbidden - Insufficient roles
                end
            else
                API->>Client: 403 Forbidden - Missing scopes
            end
        else
            API->>Client: 401 Unauthorized - Session terminated
        end
    else
        API->>Client: 401 Unauthorized - Invalid or expired JWT
    end
```

---

### Tabla de decisión: Transiciones permitidas de Membership Status

| Estado actual | Acción admin | Nuevo estado | Reversible? |
|---|---|---|---|
| `INVITED` | Confirmar | `ACTIVE` | Sí (volver a INVITED o REVOKED) |
| `INVITED` | Revocar | `REVOKED` | ❌ No |
| `ACTIVE` | Suspender | `SUSPENDED` | Sí (reactivar a ACTIVE) |
| `ACTIVE` | Revocar | `REVOKED` | ❌ No |
| `SUSPENDED` | Reactivar | `ACTIVE` | Sí (volver a SUSPENDED o REVOKED) |
| `SUSPENDED` | Revocar | `REVOKED` | ❌ No |
| `REVOKED` | *Ninguna* | `REVOKED` | ❌ No (permanente) |

---

## Diagrama de capas lógicas de validación

```mermaid
graph TB
    subgraph "Capa 1: Identidad"
        A["TenantResolver"]
        B["TenantRepository"]
        C{¿Tenant activo?}
    end

    subgraph "Capa 2: Aplicación"
        D["ClientAppRepository"]
        E{¿App activa?}
    end

    subgraph "Capa 3: Usuario"
        F["UserRepository"]
        G{¿User activo?}
    end

    subgraph "Capa 4: Membresía"
        H["MembershipRepository"]
        I{¿Membership ACTIVE?}
    end

    subgraph "Capa 5: Autorización"
        J["AppRoleRepository"]
        K["MembershipRoleRepository"]
        L{¿Roles suficientes?}
    end

    A --> B --> C -->|SÍ| D --> E -->|SÍ| F --> G -->|SÍ| H --> I -->|SÍ| J --> K --> L
    C -->|NO| M["❌ Access Denied"]
    E -->|NO| M
    G -->|NO| M
    I -->|NO| M
    L -->|NO| M
    L -->|SÍ| N["✅ Access Allowed"]
```

---

## Índices recomendados (por rendimiento)

```sql
-- Búsqueda rápida por tenant + recurso
CREATE INDEX idx_client_apps_tenant_id ON client_apps(tenant_id, status);
CREATE INDEX idx_tenant_users_tenant_id ON tenant_users(tenant_id, status);
CREATE INDEX idx_memberships_tenant_user_app ON memberships(tenant_id, user_id, client_app_id);
CREATE INDEX idx_memberships_app_status ON memberships(client_app_id, status);
CREATE INDEX idx_app_role_client_app_id ON app_role(client_app_id, status);

-- Búsqueda rápida de tokens
CREATE INDEX idx_authorization_codes_code ON authorization_codes(code);
CREATE INDEX idx_authorization_codes_expires ON authorization_codes(expires_at, status);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_user_app ON refresh_tokens(user_id, client_app_id, status);

-- Búsqueda rápida de sesiones
CREATE INDEX idx_sessions_user_status ON sessions(user_id, status);
CREATE INDEX idx_sessions_tenant_user ON sessions(tenant_id, user_id);

-- Email/username lookup
CREATE INDEX idx_tenant_users_email ON tenant_users(tenant_id, email);
CREATE INDEX idx_tenant_users_username ON tenant_users(tenant_id, username);

-- JWKS lookup
CREATE INDEX idx_signing_keys_status ON signing_keys(status);
```

---

**Última actualización:** 2026-03-22 | **Responsable:** AI Agent | **Estado:** ✅ Completo

