# Endpoints — Account (self-service)

Endpoints de gestión de la propia cuenta del usuario autenticado. Todos requieren `Authorization: Bearer <access_token>`.

Base path: `/api/v1/account`

## Perfil

### GET /account/profile
Obtiene el perfil del usuario autenticado.

```typescript
GET /api/v1/account/profile
Authorization: Bearer {token}

// Response 200
{
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+56912345678",
    "locale": "es-CL",
    "zoneinfo": "America/Santiago"
  }
}
```

### PATCH /account/profile
Actualiza campos del perfil. Solo se envían los campos a modificar.

```typescript
PATCH /api/v1/account/profile
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "Carlos",
  "lastName": "Martínez",
  "phoneNumber": "+56911111111",
  "locale": "es-CL"
}

// Response 200: perfil actualizado
```

## Contraseña

### POST /account/change-password
Cambio de contraseña conociendo la actual.

```typescript
POST /api/v1/account/change-password
Authorization: Bearer {token}

{
  "currentPassword": "OldPass1!",
  "newPassword": "NewPass1!"
}

// Response 200: { "data": { "message": "Password changed" } }
// Response 400: INVALID_INPUT (nueva contraseña no cumple política)
// Response 401: INVALID_CREDENTIALS (contraseña actual incorrecta)
```

### POST /account/forgot-password
Solicita envío de email de recuperación. Responde siempre 200 (anti-enumeración).

```typescript
POST /api/v1/account/forgot-password

{ "email": "user@example.com" }

// Response 200 siempre (independiente de si el email existe)
```

### POST /account/recover-password
Establece nueva contraseña usando el token del email de recuperación.

```typescript
POST /api/v1/account/recover-password

{
  "token": "hex32chars",
  "newPassword": "NewPass1!"
}

// Response 200: contraseña restablecida
// Response 400: token inválido o expirado
```

### POST /account/reset-password
Reset con contraseña temporal (flujo iniciado por admin).

```typescript
POST /api/v1/account/reset-password

{
  "email": "user@example.com",
  "temporaryPassword": "TempPass1!",
  "newPassword": "FinalPass1!"
}

// Response 200: contraseña restablecida, status → ACTIVE
// Response 403: RESET_PASSWORD_REQUIRED si el usuario no está en ese estado
```

## Sesiones

### GET /account/sessions
Lista las sesiones activas del usuario.

```typescript
GET /api/v1/account/sessions
Authorization: Bearer {token}

// Response 200
{
  "data": [
    {
      "sessionId": "uuid",
      "createdAt": "2026-04-10T10:00:00",
      "lastActivity": "2026-04-12T08:30:00",
      "userAgent": "Mozilla/5.0...",
      "isCurrent": true
    }
  ]
}
```

### DELETE /account/sessions/{sessionId}
Revoca una sesión específica. Idempotente.

```typescript
DELETE /api/v1/account/sessions/{sessionId}
Authorization: Bearer {token}

// Response 200: sesión revocada
// Response 404: sesión no encontrada
```

## Preferencias de notificación

### GET /account/notification-preferences

```typescript
GET /api/v1/account/notification-preferences
Authorization: Bearer {token}

// Response 200
{
  "data": {
    "emailNotifications": true,
    "marketingEmails": false
  }
}
```

### PATCH /account/notification-preferences

```typescript
PATCH /api/v1/account/notification-preferences
Authorization: Bearer {token}

{ "emailNotifications": false }

// Response 200: preferencias actualizadas
```
