# Endpoints — Admin / Platform

Endpoints de administración de plataforma. Todos requieren `Authorization: Bearer <token>` con rol `ADMIN` salvo indicación contraria.

Base path: `/api/v1`

## Tenants

### GET /tenants
Lista todos los tenants con paginación y filtros. Rol: `ADMIN`.

```
GET /api/v1/tenants?page=0&size=20&status=ACTIVE&name_like=key
```

```typescript
// Response 200 — PagedData<TenantData>
{
  "data": {
    "content": [
      {
        "tenantId": "uuid",
        "slug": "acme",
        "name": "ACME Corp",
        "status": "ACTIVE",
        "createdAt": "2026-01-01T00:00:00"
      }
    ],
    "page": 0, "size": 20, "totalElements": 1, "totalPages": 1
  }
}
```

### POST /tenants
Crea un nuevo tenant. Rol: `ADMIN`.

```typescript
POST /api/v1/tenants

{
  "slug": "acme",
  "name": "ACME Corp"
}

// Response 201: tenant creado en estado ACTIVE
// Response 409: slug duplicado
```

### GET /tenants/{slug}
Detalle de un tenant.

### PUT /tenants/{slug}/suspend
Suspende un tenant. Rol: `ADMIN`.

### PUT /tenants/{slug}/activate
Reactiva un tenant suspendido. Rol: `ADMIN`.

## Dashboard de plataforma

### GET /admin/platform/dashboard
Métricas globales de la plataforma. Rol: `ADMIN`.

```typescript
GET /api/v1/admin/platform/dashboard

// Response 200
{
  "data": {
    "tenants": {
      "total": 15,
      "byStatus": { "ACTIVE": 12, "SUSPENDED": 3 }
    },
    "users": {
      "total": 240,
      "byStatus": { "ACTIVE": 200, "SUSPENDED": 40 }
    },
    "apps": { "total": 48 },
    "signingKeys": { "total": 2, "active": 1 }
  }
}
```

## Estadísticas de plataforma

### GET /admin/platform/stats
Snapshot de conteos generales. Rol: `ADMIN`.

```typescript
GET /api/v1/admin/platform/stats

// Response 200
{
  "data": {
    "totalTenants": 15,
    "totalUsers": 240,
    "totalApps": 48,
    "totalActiveSessions": 18
  }
}
```

## Platform Users (usuarios administradores de plataforma)

### GET /platform/users
Lista usuarios de plataforma con paginación. Rol: `ADMIN`.

```typescript
GET /api/v1/platform/users?page=0&size=20&sort=email&order=ASC

// Response 200 — PagedData<PlatformUserData>
{
  "data": {
    "content": [
      {
        "id": "uuid",
        "email": "admin@keygo.io",
        "username": "admin",
        "firstName": "Key",
        "lastName": "Go",
        "status": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

Filtros soportados:

- `status=ACTIVE|SUSPENDED|PENDING|RESET_PASSWORD`
- `username_like=<texto>`
- `email_like=<texto>`
- `sort=username|email|status|createdAt|firstName|lastName`
- `order=ASC|DESC`

### GET /platform/users/{userId}/platform-roles
Lista los roles asignados a un platform user. Rol: `ADMIN`.

```typescript
GET /api/v1/platform/users/{userId}/platform-roles

// Response 200 — PlatformUserRoleData[]
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

### GET /platform/roles
Lista el catálogo de roles de plataforma disponibles para asignación. Rol:
`KEYGO_ADMIN` o `KEYGO_ACCOUNT_ADMIN`.

```typescript
GET /api/v1/platform/roles

// Response 200 — PlatformRoleData[]
{
  "data": [
    {
      "id": "10000000-0000-0000-0000-000000000001",
      "code": "KEYGO_ADMIN",
      "name": "Keygo Admin",
      "description": "Global platform administration"
    },
    {
      "id": "10000000-0000-0000-0000-000000000002",
      "code": "KEYGO_ACCOUNT_ADMIN",
      "name": "Keygo Account Admin",
      "description": "Contractor or tenant scoped account administration"
    }
  ]
}
```

### GET /platform/roles
Lista el catálogo de roles de plataforma disponibles para asignación. Rol:
`KEYGO_ADMIN` o `KEYGO_ACCOUNT_ADMIN`.

```typescript
GET /api/v1/platform/roles

// Response 200 — PlatformRoleData[]
{
  "data": [
    {
      "id": "10000000-0000-0000-0000-000000000001",
      "code": "KEYGO_ADMIN",
      "name": "Keygo Admin",
      "description": "Global platform administration"
    },
    {
      "id": "10000000-0000-0000-0000-000000000002",
      "code": "KEYGO_ACCOUNT_ADMIN",
      "name": "Keygo Account Admin",
      "description": "Contractor or tenant scoped account administration"
    }
  ]
}
```

### POST /platform/users
Crea un usuario de plataforma.

```typescript
POST /api/v1/platform/users

{
  "email": "admin@keygo.io",
  "username": "superadmin"
}
```

### PUT /platform/users/{userId}/suspend
Suspende un platform user. Rol: `ADMIN`.

### PUT /platform/users/{userId}/activate
Reactiva un platform user. Rol: `ADMIN`.

## Platform Account

Endpoints de gestión de cuenta propia para platform users.

### POST /platform/account/check-email
Verifica si un email ya existe como `platform_user`. **Público**, pero requiere la sesión
iniciada previamente por `GET /platform/oauth2/authorize`.

```typescript
POST /api/v1/platform/account/check-email
Content-Type: application/json
Cookie: JSESSIONID=...

{
  "email": "admin@keygo.local"
}

// Response 200: success.code = PLATFORM_USER_EMAIL_FOUND
// Response 404: failure.code = PLATFORM_USER_EMAIL_NOT_FOUND
// Response 401: failure.code = AUTHENTICATION_REQUIRED
// data = null en todos los casos
```

### POST /platform/account/forgot-password
Solicita recovery de contraseña. **Público**, siempre responde 200 (anti-enumeración).

### POST /platform/account/recover-password
Establece nueva contraseña con token de recovery. **Público**.

### POST /platform/account/reset-password
Reset con contraseña temporal. **Público**.

### POST /platform/oauth2/revoke
Revoca un token de plataforma (RFC 7009). **Público**.

## Resumen de roles requeridos

| Superficie | Rol mínimo |
|---|---|
| Dashboard y stats | `ADMIN` |
| CRUD tenants | `ADMIN` |
| CRUD platform users | `ADMIN` |
| CRUD users/apps del tenant | `ADMIN_TENANT` (con tenant scope) |
| Memberships del tenant | `ADMIN_TENANT` |
| Billing de plataforma | `KEYGO_ADMIN` o `KEYGO_TENANT_ADMIN` |
