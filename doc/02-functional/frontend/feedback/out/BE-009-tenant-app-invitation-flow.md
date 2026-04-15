# BE-009 — Flujo de invitación de admin a app de tenant

**Fecha:** 2026-04-15
**Estado:** 🟡 Pendiente implementación backend
**Tarea:** [T-155](../../../09-ai/tasks/registered/T-155-tenant-app-invitation-flow.md)

---

## Cambio

Backend implementará un flujo completo de invitación controlada por admin para incorporar
usuarios a una app de tenant. Cubre dos variantes:

- **Usuario nuevo:** el email invitado no tiene cuenta en el tenant → crea contraseña al aceptar.
- **Usuario existente:** el email ya tiene cuenta activa → solo confirma para activar la Membership.

Los endpoints de **aceptación y validación son públicos**. Los de **gestión (invitar, reenviar,
revocar, listar) requieren Bearer JWT con rol `ADMIN_TENANT`**.

> ⚠️ Este documento define el contrato previsto. Los endpoints **aún no existen** en backend.
> UI puede iniciar el diseño de pantallas; la integración real queda bloqueada hasta que T-155
> esté en estado `in-development`.

---

## Endpoints

### Endpoints admin (requieren Bearer JWT — `ADMIN_TENANT`)

#### Invitar usuario

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/memberships/invite
Authorization: Bearer <access_token>
```

**Request:**
```json
{
  "email": "invitado@example.com",
  "role_codes": ["USER", "VIEWER"],
  "expires_in_hours": 72
}
```

| Campo | Requerido | Notas |
|---|---|---|
| `email` | Sí | Email del usuario a invitar |
| `role_codes` | No | Roles de la app a pre-asignar; vacío = sin roles |
| `expires_in_hours` | No | Default 72 h; máximo 168 h (7 días) |

**Response 201:**
```json
{
  "code": "INVITATION_SENT",
  "data": {
    "invitation_id": "uuid",
    "email": "invitado@example.com",
    "status": "SENT",
    "expires_at": "2026-04-18T14:00:00Z"
  }
}
```

**Errores:**
| HTTP | `code` | Acción UI |
|---|---|---|
| 409 | `DUPLICATE_INVITATION` | "Ya existe una invitación activa para ese email" |
| 409 | `USER_ALREADY_MEMBER` | "El usuario ya tiene acceso a esta app" |
| 400 | `INVALID_INPUT` | Errores inline en el formulario |

---

#### Listar invitaciones

```
GET /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations
Authorization: Bearer <access_token>
```

**Query params opcionales:** `status` (SENT / ACCEPTED / EXPIRED / REVOKED), `page`, `size`

**Response 200:**
```json
{
  "code": "INVITATIONS_RETRIEVED",
  "data": {
    "content": [
      {
        "invitation_id": "uuid",
        "email": "invitado@example.com",
        "status": "SENT",
        "roles": ["USER"],
        "invited_at": "2026-04-15T10:00:00Z",
        "expires_at": "2026-04-18T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 1
  }
}
```

---

#### Reenviar invitación

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{invitationId}/resend
Authorization: Bearer <access_token>
```

**Response 200:**
```json
{
  "code": "INVITATION_RESENT",
  "data": {
    "invitation_id": "uuid",
    "expires_at": "2026-04-18T14:00:00Z"
  }
}
```

---

#### Revocar invitación

```
DELETE /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{invitationId}
Authorization: Bearer <access_token>
```

**Response 200:**
```json
{
  "code": "INVITATION_REVOKED",
  "data": null
}
```

---

### Endpoints públicos (sin autenticación)

#### Validar token de invitación

```
GET /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{token}/validate
```

**Response 200:**
```json
{
  "code": "INVITATION_VALID",
  "data": {
    "invitation_id": "uuid",
    "email": "invitado@example.com",
    "tenant_name": "Acme Corp",
    "app_name": "Acme Portal",
    "roles": ["USER"],
    "user_exists": false,
    "expires_at": "2026-04-18T14:00:00Z"
  }
}
```

| Campo | UI usa para |
|---|---|
| `user_exists` | Decidir entre pantalla "crear contraseña" (`false`) o "confirmar acceso" (`true`) |
| `roles` | Mostrar al invitado qué accesos tendrá |
| `expires_at` | Mostrar countdown o badge de urgencia |

**Errores:**
| HTTP | `code` | Acción UI |
|---|---|---|
| 404 | `RESOURCE_NOT_FOUND` | Pantalla "Invitación inválida" |
| 410 | `INVITATION_EXPIRED` | Pantalla "Invitación expirada — contacta al administrador" |
| 409 | `INVITATION_ALREADY_ACCEPTED` | Redirigir a login |

---

#### Aceptar invitación

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{token}/accept
```

**Request — usuario nuevo (`user_exists: false`):**
```json
{
  "password": "Min8Chars!",
  "first_name": "Juan",
  "last_name": "Pérez"
}
```

**Request — usuario existente (`user_exists: true`):**
```json
{}
```

**Response 200:**
```json
{
  "code": "INVITATION_ACCEPTED",
  "data": {
    "user_id": "uuid",
    "username": "invitado",
    "app_name": "Acme Portal",
    "membership_status": "ACTIVE"
  }
}
```

**Errores:**
| HTTP | `code` | Acción UI |
|---|---|---|
| 400 | `INVALID_INPUT` | Error inline en campo contraseña |
| 410 | `INVITATION_EXPIRED` | Pantalla "Invitación expirada" |
| 409 | `INVITATION_ALREADY_ACCEPTED` | Redirigir a login |

---

## Flujo completo para UI

### Panel admin

```
Pantalla A1: Lista de invitaciones
  - GET /invitations (con filtro status)
  - Tabla: email · status · roles · expires_at · acciones
  - Botón "Invitar usuario" → Pantalla A2
  - Por fila: botón "Reenviar" → POST /resend
              botón "Revocar"  → DELETE /{invitationId}

Pantalla A2: Formulario de invitación
  - Campo: email
  - Multiselect: role_codes (roles disponibles en la app)
  - Campo opcional: expires_in_hours
  └─► POST /invite
        ├─ 201 → Pantalla A3
        └─ 409 → error inline

Pantalla A3: Confirmación de envío
  - Mensaje: "Invitación enviada a [email]. Expira el [fecha]."
  - CTA: "Invitar otro" → Pantalla A2
  - CTA: "Volver a la lista" → Pantalla A1
```

### Flujo del usuario invitado

```
(usuario abre link del email: /invitations/{token}/validate)

Pantalla U1: Loading — validando invitación
  └─► GET /invitations/{token}/validate
        ├─ 200 user_exists=false → Pantalla U2a
        ├─ 200 user_exists=true  → Pantalla U2b
        ├─ 404 / 410             → Pantalla U4
        └─ 409 already_accepted  → redirigir a login

Pantalla U2a: Crear contraseña (usuario nuevo)
  - Info: "Fuiste invitado a [app_name] en [tenant_name]"
  - Info: "Tendrás los roles: [roles]"
  - Campo: password (+ confirmar)
  - Campos opcionales: first_name, last_name
  └─► POST /invitations/{token}/accept
        ├─ 200 → Pantalla U3
        └─ 400 → error inline en contraseña

Pantalla U2b: Confirmar acceso (usuario existente)
  - Info: "Tienes una invitación para [app_name]"
  - Info: "Se asignarán los roles: [roles]"
  - Botón: "Aceptar invitación"
  └─► POST /invitations/{token}/accept (body vacío)
        └─ 200 → Pantalla U3

Pantalla U3: Invitación aceptada
  - Mensaje: "Ya tienes acceso a [app_name]."
  - CTA: "Iniciar sesión" → flujo OAuth2/OIDC del tenant

Pantalla U4: Invitación inválida o expirada
  - Mensaje: según código de error (no existe / expirada)
  - Sugerencia: "Contacta al administrador para solicitar una nueva invitación."
```

---

## Impacto en UI

| Aspecto | Detalle |
|---|---|
| **Pantallas admin** | 3 pantallas: lista, formulario, confirmación |
| **Pantallas usuario invitado** | 5 pantallas: loading, crear-contraseña, confirmar-acceso, éxito, error |
| **Token en URL** | El link del email lleva el token en la URL (`/invitations/{token}/validate`); UI lo extrae del path o query param |
| **`user_exists`** | Campo crítico del `GET /validate`; determina qué pantalla mostrar |
| **Sin auth en aceptación** | Todos los endpoints del flujo del invitado son públicos |
| **Membership siempre `ACTIVE`** | Al aceptar, la Membership nace directamente `ACTIVE` (sin aprobación adicional) |

---

## Confirmación

_Pendiente implementación backend (T-155) y confirmación por parte de UI._
