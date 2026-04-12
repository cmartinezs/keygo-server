# Endpoints — Tenant

Recursos scoped al tenant. Todos requieren `Authorization: Bearer <access_token>`.

Base path: `/api/v1/tenants/{tenantSlug}`

## Usuarios

### GET /tenants/{slug}/users
Lista usuarios del tenant con paginación y filtros.

```
GET /api/v1/tenants/{slug}/users?page=0&size=20&status=ACTIVE&email_like=john
```

```typescript
// Response 200 — PagedData<UserData>
{
  "data": {
    "content": [
      {
        "userId": "uuid",
        "email": "john@example.com",
        "username": "johndoe",
        "status": "ACTIVE",
        "createdAt": "2026-04-01T00:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### POST /tenants/{slug}/users
Crea un usuario en el tenant.

```typescript
POST /api/v1/tenants/{slug}/users

{
  "email": "new@example.com",
  "username": "newuser",
  "firstName": "New",
  "lastName": "User"
}

// Response 201: usuario creado
// Response 409: DUPLICATE_EMAIL
```

### GET /tenants/{slug}/users/{userId}
Obtiene un usuario por ID.

### PUT /tenants/{slug}/users/{userId}/suspend
Suspende un usuario.

### PUT /tenants/{slug}/users/{userId}/activate
Reactiva un usuario suspendido.

## Apps (ClientApp)

### GET /tenants/{slug}/apps
Lista apps del tenant.

```
GET /api/v1/tenants/{slug}/apps?page=0&size=20&status=ACTIVE
```

### POST /tenants/{slug}/apps
Registra una nueva app.

```typescript
POST /api/v1/tenants/{slug}/apps

{
  "name": "My App",
  "clientId": "my-app",
  "redirectUris": ["https://myapp.com/callback"],
  "allowedScopes": ["openid", "profile", "email"]
}
```

### GET /tenants/{slug}/apps/{clientId}
Detalle de una app.

## Memberships

### GET /tenants/{slug}/apps/{clientId}/memberships
Lista memberships de una app con paginación.

```
GET /api/v1/tenants/{slug}/apps/{clientId}/memberships?page=0&size=20&status=PENDING
```

```typescript
// Response 200 — PagedData<MembershipData>
{
  "data": {
    "content": [
      {
        "membershipId": "uuid",
        "userId": "uuid",
        "email": "user@example.com",
        "status": "PENDING",
        "createdAt": "2026-04-10T00:00:00"
      }
    ],
    ...
  }
}
```

### POST /tenants/{slug}/apps/{clientId}/memberships
Crea una membership (estado inicial: `PENDING`).

```typescript
POST /api/v1/tenants/{slug}/apps/{clientId}/memberships

{ "userId": "uuid" }

// Response 201: membership creada en estado PENDING
```

### PUT /tenants/{slug}/apps/{clientId}/memberships/{membershipId}/approve
Aprueba una membership pendiente. Envía email de notificación al usuario.

```typescript
PUT /api/v1/tenants/{slug}/apps/{clientId}/memberships/{membershipId}/approve

// Response 200: membership activada
// Response 409: MEMBERSHIP_ALREADY_ACTIVE
```

### PUT /tenants/{slug}/memberships/{membershipId}/suspend
Suspende una membership activa.

### PUT /tenants/{slug}/memberships/{membershipId}/activate
Reactiva una membership suspendida.

## Roles de App

### GET /tenants/{slug}/apps/{clientId}/roles
Lista roles definidos para la app.

```typescript
// Response 200
{
  "data": [
    {
      "roleCode": "EDITOR",
      "name": "Editor",
      "description": "Can edit content",
      "parentCode": "VIEWER"
    }
  ]
}
```

### POST /tenants/{slug}/apps/{clientId}/roles
Crea un nuevo rol en la app.

```typescript
POST /api/v1/tenants/{slug}/apps/{clientId}/roles

{
  "roleCode": "EDITOR",
  "name": "Editor",
  "description": "Can edit content",
  "parentCode": "VIEWER"  // opcional — jerarquía de roles
}
```

### POST /tenants/{slug}/apps/{clientId}/roles/{roleCode}/parent
Asigna un rol padre (jerarquía).

### DELETE /tenants/{slug}/apps/{clientId}/roles/{roleCode}/parent
Elimina el rol padre.

## Roles de Tenant

### GET /tenants/{slug}/roles
Lista roles del tenant.

### POST /tenants/{slug}/roles
Crea un rol de tenant (código en UPPERCASE: `ADMIN_TENANT`, `VIEWER_TENANT`).

## Estadísticas del tenant

### GET /tenants/{slug}/stats
Métricas del tenant: usuarios por estado, apps, memberships, sesiones activas. Requiere rol `ADMIN_TENANT`.
