# Flujo de Gestión de Tenants — Multi-Tenant Isolation

> **Descripción:** Flujos de creación y gestión de tenants, usuarios, apps, roles y memberships con aislamiento completo por tenant.

**Fecha:** 2026-04-05

---

## 1. Creación de Tenant (Onboarding)

```mermaid
sequenceDiagram
    participant Admin as 👨‍💼 Admin KeyGo
    participant API as 🔐 KeyGo API
    participant DB as 🗄️ Database
    participant Email as 📧 Email Service

    Admin->>API: 1. POST /api/v1/tenants<br/>Header: Bearer {admin_token}<br/>body: { name }
    
    API->>API: 2. Validate Bearer token:<br/>• Extract iss, aud<br/>• Verify signature<br/>• Check role=ADMIN

    API->>API: 3. Derivar slug desde `name`

    API->>DB: 4. CHECK slug UNIQUE<br/>SELECT * FROM tenants WHERE slug=?

    API->>DB: 5. BEGIN TRANSACTION

    API->>DB: 6. INSERT tenants<br/>{ slug, name, status=ACTIVE,<br/>  created_at, updated_at }

    API->>DB: 7. COMMIT

    API->>Admin: 8. Return 201<br/>tenantId + slug + name + status
```

---

## 2. Crear Usuario en Tenant

```mermaid
sequenceDiagram
    participant AdminTenant as 👨‍💼 Admin Tenant
    participant API as 🔐 KeyGo API<br/>{slug}/...
    participant DB as 🗄️ Database<br/>(isolated)
    participant Email as 📧 Email

    AdminTenant->>API: 1. POST /api/v1/tenants/{slug}/users<br/>Header: Bearer {tenant_admin_token}<br/>body: { email, name }
    
    API->>API: 2. Validate token:<br/>• iss must match {slug}<br/>• role=ADMIN_TENANT<br/>• tenant match validation

    API->>DB: 3. SELECT tenant_id<br/>FROM tenants WHERE slug={slug}

    API->>DB: 4. SELECT *<br/>FROM tenant_users<br/>WHERE tenant_id=? AND email=?<br/>(check UNIQUE email per tenant)

    API->>API: 5. Validate email format

    API->>DB: 6. BEGIN

    API->>DB: 7. INSERT tenant_users<br/>{ tenant_id, email, name,<br/>  status=UNVERIFIED,<br/>  email_verified=false }

    API->>KeyGo: 8. Issue temporary password<br/>(8 chars random + complexity)

    API->>DB: 9. INSERT password_reset_codes<br/>{ user_id, code_hash, expires_at }

    API->>Email: 10. Send email:<br/>Subject: "Bienvenido a {tenant}"<br/>Temporary password + reset link

    API->>DB: 11. COMMIT

    API->>AdminTenant: 12. Return 201<br/>{ userId, email, status,<br/>  requiresPasswordReset }
```

---

## 3. Crear App (OAuth2) en Tenant

```mermaid
sequenceDiagram
    participant AdminTenant as 👨‍💼 Admin Tenant
    participant API as 🔐 KeyGo API<br/>{slug}/apps/...
    participant DB as 🗄️ Database

    AdminTenant->>API: 1. POST /api/v1/tenants/{slug}/apps<br/>Header: Bearer {admin_tenant_token}<br/>body: { clientId, name, redirectUris }
    
    API->>API: 2. Validate tenant match + role

    API->>DB: 3. SELECT tenant_id WHERE slug=?

    API->>DB: 4. SELECT * FROM client_apps<br/>WHERE tenant_id=? AND clientId=?<br/>(UNIQUE per tenant)

    API->>API: 5. Validate:<br/>• redirectUris are HTTPS<br/>• Port not restricted<br/>• Localhost allowed (dev)

    API->>DB: 6. BEGIN

    API->>DB: 7. INSERT client_apps<br/>{ tenant_id, clientId (UUID),<br/>  clientSecret (random 64),<br/>  redirectUris, status=DRAFT }

    API->>DB: 8. INSERT client_allowed_scopes<br/>(default: openid, profile, email,<br/> offline_access)

    API->>DB: 9. COMMIT

    API->>AdminTenant: 10. Return 201<br/>{ clientId, clientSecret,<br/>  redirectUris, allowedScopes }
```

---

## 4. Crear Rol en App + Asignar a Usuario (Membership)

```mermaid
sequenceDiagram
    participant AdminTenant as 👨‍💼 Admin Tenant
    participant API as 🔐 KeyGo API<br/>{slug}/apps/{appId}/roles
    participant DB as 🗄️ Database

    AdminTenant->>API: 1. POST /api/v1/tenants/{slug}/apps/{appId}/roles<br/>Header: Bearer {admin_tenant_token}<br/>body: { code, name }
    
    API->>API: 2. Validate tenant + app + role

    API->>DB: 3. SELECT tenant_id, client_app_id

    API->>DB: 4. SELECT * FROM app_roles<br/>WHERE client_app_id=? AND code=?<br/>(UNIQUE per app)

    API->>DB: 5. INSERT app_roles<br/>{ client_app_id, tenant_id, code,<br/>  name, parent_role_id=NULL }

    API->>AdminTenant: 6. Return 201<br/>{ roleId, code, name }

    AdminTenant->>API: 7. POST /api/v1/tenants/{slug}/memberships<br/>body: { userId, appId, roles: [roleId] }

    API->>DB: 8. BEGIN

    API->>DB: 9. INSERT memberships<br/>{ tenant_id, user_id, app_id }

    API->>DB: 10. INSERT membership_roles<br/>{ membership_id, role_id }<br/>(N:M relation)

    API->>DB: 11. COMMIT

    API->>AdminTenant: 12. Return 201<br/>{ membershipId, userId, appId,<br/>  roles: [...] }
```

---

## 5. Expansión de Roles Jerárquicos en JWT (T-107)

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant API as 🔐 KeyGo Server
    participant DB as 🗄️ Database<br/>(CTE recursiva)

    User->>API: 1. POST /oauth2/token<br/>(exchange auth code)

    API->>DB: 2. Query user + memberships<br/>SELECT * FROM memberships<br/>WHERE user_id=? AND app_id=?

    API->>DB: 3. Query roles (con jerarquía)<br/>WITH RECURSIVE role_expansion AS (<br/>  SELECT id, code, parent_role_id<br/>  FROM app_roles<br/>  WHERE id IN (membership roles)<br/>  UNION ALL<br/>  SELECT ar.id, ar.code, ar.parent_role_id<br/>  FROM app_roles ar<br/>  INNER JOIN role_expansion re<br/>  ON ar.id = re.parent_role_id<br/>)<br/>SELECT code FROM role_expansion

    API->>API: 4. Obtener lista expandida:<br/>roles=[ADMIN, VIEWER, EDITOR,<br/>  COMMENT_ADMIN] (padres incluidos)

    API->>API: 5. Build JWT claims:<br/>{ sub, iss, aud, scope,<br/>  roles: [ADMIN, VIEWER, ...] }

    API->>User: 6. Return access_token con roles expandidos

    User->>API: 7. API call con Bearer token

    API->>API: 8. Extract roles del JWT<br/>Check @PreAuthorize("hasRole('ADMIN')")<br/>→ true porque roles incluye padre
```

---

## 6. Estados de Transición: Tenant y Usuario

```mermaid
stateDiagram-v2
    [*] --> ONBOARDING: Admin crea tenant<br/>(POST /tenants)
    
    ONBOARDING --> ACTIVE: Tenant ready<br/>Apps registradas
    
    ACTIVE --> ACTIVE: Admin gestiona:<br/>• Crear usuarios<br/>• Crear apps<br/>• Asignar roles
    
    ACTIVE --> SUSPENDED: Admin suspende<br/>(PUT /suspend)
    
    SUSPENDED --> ACTIVE: Admin activa<br/>(PUT /activate)
    
    SUSPENDED --> [*]: Borrado (futuro)
    
    note right of ONBOARDING
        Tenant creado
        Signing keys: ✅
        Default roles: ✅
        Status: ACTIVE
    end note
    
    note right of ACTIVE
        Usuarios pueden loguearse
        Apps autorizan con OAuth2
        Roles asignados a memberships
    end note
    
    note right of SUSPENDED
        Usuarios: 401 bloqueado
        Apps: revocación
        Data: íntegra (no delete)
    end note

    ---

    [*] --> UNVERIFIED: Usuario creado<br/>(POST /users)
    
    UNVERIFIED --> ACTIVE: Email verificado<br/>Password reset
    
    ACTIVE --> ACTIVE: Usuario logueado<br/>Opera normalmente
    
    ACTIVE --> RESET_PASSWORD: Admin resetea<br/>Password temporal enviado
    
    RESET_PASSWORD --> ACTIVE: Usuario cambia password
    
    ACTIVE --> SUSPENDED: Admin suspende
    
    SUSPENDED --> ACTIVE: Admin activa
    
    SUSPENDED --> [*]: Borrado (futuro)
    
    note right of UNVERIFIED
        Email no verificado
        No puede loguearse
        Código 6-dígito (30m)
    end note
    
    note right of RESET_PASSWORD
        Temporary password: 24h TTL
        Reset code: 24h TTL
        Usuario debe cambiar password
    end note
```

---

## 7. Aislamiento Multi-Tenant en BD

```
┌─────────────────────────────────────────────────┐
│            PostgreSQL Database                  │
├─────────────────────────────────────────────────┤
│                                                 │
│  TENANTS TABLE                                  │
│  ├─ id=1, slug='acme'         ← Tenant A      │
│  └─ id=2, slug='globex'       ← Tenant B      │
│                                                 │
│  TENANT_USERS TABLE                             │
│  ├─ id=10, tenant_id=1, email='john@acme'     │
│  ├─ id=11, tenant_id=1, email='alice@acme'    │
│  ├─ id=20, tenant_id=2, email='john@globex'   │  (different person!)
│  └─ id=21, tenant_id=2, email='bob@globex'    │
│                                                 │
│  CLIENT_APPS TABLE                              │
│  ├─ id=100, tenant_id=1, clientId='....'      │ (Acme's app)
│  └─ id=200, tenant_id=2, clientId='....'      │ (Globex's app)
│                                                 │
│  APP_ROLES TABLE                                │
│  ├─ id=1000, tenant_id=1, client_app_id=100   │
│  └─ id=2000, tenant_id=2, client_app_id=200   │
│                                                 │
│  MEMBERSHIPS TABLE                              │
│  ├─ id=9000, tenant_id=1, user_id=10, app_id=100
│  └─ id=9001, tenant_id=2, user_id=20, app_id=200
│                                                 │
└─────────────────────────────────────────────────┘

QUERY RULE:
SELECT * FROM tenant_users 
WHERE tenant_id = 1  ← SIEMPRE filtrar por tenant_id
    AND email = 'john@...'

Nunca:
SELECT * FROM tenant_users WHERE email = 'john@...'
              ↑ puede retornar usuario de otro tenant!
```

---

## 8. Path Variable Validation (Tenant Resolution)

```mermaid
graph TD
    A["GET /api/v1/tenants/acme/users<br/>Header: Bearer JWT{iss: .../tenants/acme}"] 
    --> B["BootstrapAdminKeyFilter"]
    
    B --> B1["1. Extract token"]
    B --> B2["2. Decode JWT (no signature check en este punto)"]
    B --> B3["3. Extract iss claim<br/>= 'https://keygo.local/tenants/acme'"]
    B --> B4["4. Resolve slug<br/>= 'acme'"]
    
    B4 --> B5["5. Compare path slug<br/>path: 'acme' vs JWT: 'acme'<br/>✅ MATCH"]
    
    B5 --> C["✅ Allow request<br/>Continue to controller"]
    
    D["❌ MISMATCH<br/>path: 'globex' vs JWT: 'acme'"]
    --> E["🚫 Reject: 403 Forbidden<br/>'Tenant slug mismatch'"]

    style B fill:#e1f5ff
    style C fill:#c8e6c9
    style E fill:#ffcdd2
```

---

## 9. Flujo: Validación de Token por Tenant

```mermaid
sequenceDiagram
    participant Client as 🖥️ Client
    participant Filter as 🔒 BootstrapAdminKeyFilter
    participant Controller as 📍 UserController<br/>{slug}
    participant UseCase as 🎯 ListTenantUsersUseCase
    participant DB as 🗄️ Database

    Client->>Filter: 1. GET /api/v1/tenants/{slug}/users<br/>Header: Authorization: Bearer JWT

    Filter->>Filter: 2. Extract JWT<br/>Decode claims (no verify yet)

    Filter->>Filter: 3. Extract iss + path slug<br/>iss: https://keygo.local/tenants/acme<br/>slug: acme

    Filter->>Filter: 4. Validate iss=slug<br/>✅ Match → continue<br/>❌ Mismatch → 403

    Filter->>Controller: 5. Forward request

    Controller->>Controller: 6. @PreAuthorize("hasRole('ADMIN_TENANT')")<br/>Extract role from JWT<br/>Check if ADMIN_TENANT

    Controller->>UseCase: 7. ListTenantUsersUseCase.execute<br/>(tenantSlug='acme')

    UseCase->>DB: 8. SELECT * FROM tenant_users<br/>WHERE tenant_id = (SELECT id FROM tenants WHERE slug='acme')

    DB->>UseCase: 9. Return only acme's users<br/>(filtered by tenant_id)

    UseCase->>Controller: 10. UserData[]

    Controller->>Client: 11. 200 OK { users: [...] }<br/>(only acme users)
```

---

**Última actualización:** 2026-04-05  
**Próximo:** FLUJO_BILLING.md (suscripciones y facturas)
