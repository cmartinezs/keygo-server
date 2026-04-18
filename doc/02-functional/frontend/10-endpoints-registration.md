# Endpoints — Descubrimiento público y registro

Endpoints **sin autenticación** (no se requiere `Authorization` ni cookie).
Cubren el flujo de descubrimiento de tenants/apps y el auto-registro de nuevos usuarios.

---

## Discovery

### GET /api/v1/tenants/public

Lista tenants activos disponibles para auto-registro.

```
GET /api/v1/tenants/public?page=0&size=50&name_like=acme
```

**Response 200:**
```json
{
  "code": "TENANT_LIST_RETRIEVED",
  "data": {
    "content": [
      { "id": "uuid", "name": "Acme Corp", "slug": "acme" }
    ],
    "page": 0, "size": 50, "totalElements": 1, "totalPages": 1, "last": true
  }
}
```

| HTTP | `code` | Descripción |
|---|---|---|
| 200 | `TENANT_LIST_RETRIEVED` | Lista obtenida |
| 400 | `INVALID_INPUT` | Parámetros de paginación inválidos |

---

### GET /api/v1/tenants/{tenantSlug}/apps/public

Lista apps del tenant que **permiten self-registro con membership**.

Solo retorna apps que cumplan **todos** los siguientes criterios:
- `type = PUBLIC`
- `status = ACTIVE`
- `registration_policy` ∈ `{ OPEN_AUTO_ACTIVE, OPEN_AUTO_PENDING }`

> `INVITE_ONLY` y `OPEN_NO_MEMBERSHIP` quedan excluidas deliberadamente:
> - `INVITE_ONLY`: el registro propio está deshabilitado.
> - `OPEN_NO_MEMBERSHIP`: el registro no crea membership en la app; mostrarla sería engañoso.

**Response 200:**
```json
{
  "code": "CLIENT_APP_LIST_RETRIEVED",
  "data": {
    "content": [
      {
        "id": "uuid",
        "client_id": "acme-web",
        "name": "Acme Web Portal",
        "description": "...",
        "type": "PUBLIC",
        "registration_policy": "OPEN_AUTO_ACTIVE",
        "is_active": true
      }
    ],
    "page": 0, "size": 50, "totalElements": 1, "totalPages": 1, "last": true
  }
}
```

| HTTP | `code` | Descripción |
|---|---|---|
| 200 | `CLIENT_APP_LIST_RETRIEVED` | Lista obtenida |
| 400 | `INVALID_INPUT` | Parámetros de paginación inválidos |
| 404 | `RESOURCE_NOT_FOUND` | Tenant no encontrado o no activo |

---

## Registro

Base path: `/api/v1/tenants/{tenantSlug}/apps/{clientId}`

### POST /register

Crea un nuevo usuario con estado `PENDING` y envía código de verificación por email.

**Request:**
```json
{
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "Min8Chars!",
  "first_name": "John",
  "last_name": "Doe"
}
```

**Response 201:**
```json
{
  "code": "USER_REGISTERED",
  "data": {
    "id": "uuid",
    "username": "jdoe",
    "notification_email": "j***@example.com",
    "status": "PENDING"
  }
}
```

| HTTP | `code` | Acción UI |
|---|---|---|
| 201 | `USER_REGISTERED` | Avanzar a pantalla "Revisa tu email" |
| 400 | `INVALID_INPUT` | Errores inline en el formulario (`data.field_errors`) |
| 403 | `BUSINESS_RULE_VIOLATION` | La app es `INVITE_ONLY` — mostrar mensaje "El registro para esta aplicación es solo por invitación" |
| 404 | `RESOURCE_NOT_FOUND` | App o tenant no encontrado |
| 409 | `DUPLICATE_RESOURCE` | "Ya existe una cuenta con ese email o nombre de usuario" |

> **Nota — Política de membership:** si la app tiene `registration_policy = OPEN_AUTO_ACTIVE`
> o `OPEN_AUTO_PENDING`, el backend crea membership automáticamente al verificar el email.
> Si la app tiene `OPEN_NO_MEMBERSHIP`, el usuario queda activo en el tenant pero sin membership
> en la app — el acceso debe ser otorgado explícitamente por un admin.

---

### POST /verify-email

Valida el código de 6 dígitos recibido por email. Si es válido, activa al usuario y
aplica la política de membership de la app.

**Request:** `{ "email": "jdoe@example.com", "code": "847291" }`

**Response 200:** `{ "code": "EMAIL_VERIFIED", "data": null }`

| HTTP | `code` | Acción UI |
|---|---|---|
| 200 | `EMAIL_VERIFIED` | Avanzar a pantalla de confirmación / CTA de login |
| 400 | `INVALID_INPUT` | Código incorrecto o ya usado — error inline |
| 404 | `RESOURCE_NOT_FOUND` | Usuario o app no encontrado |
| 422 | `EMAIL_VERIFICATION_EXPIRED` | Mostrar aviso de expiración + botón "Reenviar código" |

---

### POST /resend-verification

Genera y envía un nuevo código. Solo permitido si el código anterior ya expiró.

**Request:** `{ "email": "jdoe@example.com" }`

**Response 200:** `{ "code": "EMAIL_VERIFICATION_RESENT", "data": { "notification_email": "j***@example.com" } }`

| HTTP | `code` | Acción UI |
|---|---|---|
| 200 | `EMAIL_VERIFICATION_RESENT` | Reiniciar countdown de 30 minutos |
| 404 | `RESOURCE_NOT_FOUND` | Usuario no encontrado |
| 409 | `BUSINESS_RULE_VIOLATION` | Código aún vigente — "Tu código anterior sigue activo. Revisa tu correo." |

---

## Flujo completo de UI

```
Pantalla 0 — Seleccionar tenant y app
  GET /api/v1/tenants/public               → lista de tenants
  usuario elige tenant
  GET /api/v1/tenants/{slug}/apps/public   → apps que admiten self-registro con membership
  usuario elige app  →  obtiene {tenantSlug, clientId}

Pantalla 1 — Formulario de registro
  POST /register
    ├─ 201  → Pantalla 2
    ├─ 403  → mensaje "solo por invitación"
    └─ 400/409 → errores inline

Pantalla 2 — "Revisa tu email"
  Mostrar notification_email (enmascarado)
  Countdown 30 min (TTL fijo, el backend no lo retorna)
  Botón "Reenviar" (activo solo si countdown = 0)
    POST /resend-verification → reiniciar countdown
  Botón "Ya tengo el código" → Pantalla 3

Pantalla 3 — Ingresar código
  POST /verify-email
    ├─ 200              → Pantalla 4
    ├─ 400 INVALID      → error inline
    └─ 422 EXPIRED      → volver a Pantalla 2 con aviso

Pantalla 4 — Registro completado
  CTA "Iniciar sesión" → flujo OAuth2/PKCE del tenant
```
