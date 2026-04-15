# T-155 — Flujo de invitación de admin a app de tenant

**Estado:** ⬜ Registrada
**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-supabase`, `keygo-run`, docs, Postman

---

## Problema / Requisito

Diseñar e implementar el flujo completo de invitación de un usuario por parte de un admin a
una app de tenant. El flujo cubre dos variantes:

- **Usuario nuevo:** el email invitado no tiene cuenta en el tenant → debe crear contraseña.
- **Usuario existente:** el email ya tiene cuenta activa en el tenant → solo acepta la invitación.

El objetivo es que tanto backend como UI tengan una especificación clara y sin ambigüedad para
implementar las pantallas y endpoints correspondientes.

**Estado actual:** ninguno de los endpoints de invitación existe en backend. Las tablas de
base de datos tampoco existen. Esta tarea requiere RFC previo por impacto multi-módulo.

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| T-154 | complementaria | T-154 cubre el registro libre; este cubre la invitación controlada por admin |
| T-044 | habilitadora | `membership_attributes` puede necesitarse para transportar datos de invitación |

---

## Flujo A: Usuario nuevo

### Diagrama de secuencia

```
Admin            UI-Admin              Backend               Email
  │── invita email ──►│                   │                    │
  │                   │── POST /invite ──►│ crea invitation SENT + envía email
  │                   │◄── 201 {invitationId, email, status:SENT} ──┤  ──► correo al invitado
  │
  (Usuario invitado recibe email, hace clic en link)
  │
  Usuario         UI-Tenant             Backend
  │── abre link ──►│                   │
  │                │── GET /invitations/{token}/validate ──►│
  │                │◄── 200 {email, tenant_name, app_name, roles, expires_at} ──┤
  │◄── pantalla "crear contraseña" ──────────────────────────────────────────│
  │── ingresa password ──►│            │
  │                │── POST /invitations/{token}/accept ──►│
  │                │                  │  crea tenant_user ACTIVE
  │                │                  │  crea app_membership ACTIVE con roles
  │                │◄── 200 INVITATION_ACCEPTED ──┤
  │◄── pantalla "listo, ya puedes iniciar sesión" ─────────────────────────│
```

---

## Flujo B: Usuario existente

```
(mismo inicio que Flujo A hasta GET /validate)
  │                │── GET /invitations/{token}/validate ──►│
  │                │◄── 200 {email, tenant_name, app_name, roles, user_exists: true} ──┤
  │◄── pantalla "confirmar acceso a app" ────────────────────────────────────│
  │── confirma ──►│                   │
  │                │── POST /invitations/{token}/accept ──►│
  │                │                  │  crea app_membership ACTIVE con roles
  │                │◄── 200 INVITATION_ACCEPTED ──┤
  │◄── pantalla "ya tienes acceso a [app_name]" ──────────────────────────│
```

---

## Especificación de Endpoints

### 1. Invitar usuario a app (admin)

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/memberships/invite
Authorization: Bearer <access_token>   (rol ADMIN_TENANT)
Content-Type: application/json
```

**Path params:**
| Param | Tipo | Descripción |
|---|---|---|
| `tenantSlug` | string | Identificador del tenant |
| `clientId` | string | OAuth2 `client_id` de la app |

**Request body:**
```json
{
  "email": "invitado@example.com",
  "role_codes": ["USER", "VIEWER"],
  "expires_in_hours": 72
}
```

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `email` | string | Sí | Formato email válido |
| `role_codes` | string[] | No | Roles existentes en la app; vacío = sin roles |
| `expires_in_hours` | integer | No | Default 72h; máximo 168h (7 días) |

**Response exitosa — 201 Created:**
```json
{
  "code": "INVITATION_SENT",
  "data": {
    "invitation_id": "uuid",
    "email": "invitado@example.com",
    "status": "SENT",
    "expires_at": "2026-04-17T14:00:00Z"
  }
}
```

**Respuestas de error:**
| HTTP | `code` | Cuándo ocurre |
|---|---|---|
| 400 | `INVALID_INPUT` | Email inválido, `role_codes` inexistentes en la app |
| 401 | `AUTHENTICATION_REQUIRED` | Sin Bearer token |
| 403 | `INSUFFICIENT_PERMISSIONS` | Token válido pero sin rol ADMIN_TENANT |
| 404 | `RESOURCE_NOT_FOUND` | `tenantSlug` o `clientId` no existen |
| 409 | `DUPLICATE_INVITATION` | Ya existe invitación activa (SENT) para ese email en la app |
| 409 | `USER_ALREADY_MEMBER` | El email ya tiene Membership ACTIVE en la app |

**Estado UI al recibir 201:**
- Mostrar confirmación: "Invitación enviada a [email]."
- Agregar fila en la lista de invitaciones pendientes con estado `SENT` y fecha de expiración.

---

### 2. Validar token de invitación (público)

```
GET /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{token}/validate
Authorization: ninguna (endpoint público)
```

**Path params:**
| Param | Tipo | Descripción |
|---|---|---|
| `tenantSlug` | string | Del tenant que envió la invitación |
| `clientId` | string | De la app a la que se invita |
| `token` | string | Token opaco del link del email (64 chars, URL-safe) |

**Response exitosa — 200 OK:**
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
    "expires_at": "2026-04-17T14:00:00Z"
  }
}
```

| Campo | Descripción | UI usa para |
|---|---|---|
| `user_exists` | `true` si el email ya tiene cuenta ACTIVE en el tenant | Decidir si mostrar form de contraseña o pantalla de confirmación |
| `email` | Email del invitado | Precompletar campos, mostrar confirmación |
| `roles` | Roles que se asignarán | Mostrar al usuario qué accesos tendrá |
| `expires_at` | Expiración de la invitación | Mostrar countdown / badge de urgencia |

**Respuestas de error:**
| HTTP | `code` | Cuándo ocurre |
|---|---|---|
| 404 | `RESOURCE_NOT_FOUND` | Token no existe o ya fue eliminado |
| 410 | `INVITATION_EXPIRED` | Token existe pero ya expiró |
| 409 | `INVITATION_ALREADY_ACCEPTED` | Token ya fue usado (invitación aceptada) |

**Estado UI al recibir 404/410:**
- Mostrar pantalla de error: "Esta invitación no es válida o ha expirado."
- Ofrecer opción de contactar al administrador.

---

### 3. Aceptar invitación (público)

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{token}/accept
Authorization: ninguna (endpoint público)
Content-Type: application/json
```

**Request body — usuario nuevo (`user_exists: false`):**
```json
{
  "password": "Min8Chars!",
  "first_name": "Juan",
  "last_name": "Pérez"
}
```

**Request body — usuario existente (`user_exists: true`):**
```json
{}
```
_(body vacío; el sistema identifica al usuario por el token)_

| Campo | Tipo | Requerido si `user_exists=false` | Validación |
|---|---|---|---|
| `password` | string | Sí | Mínimo 8 caracteres |
| `first_name` | string | No | — |
| `last_name` | string | No | — |

**Response exitosa — 200 OK:**
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

**Respuestas de error:**
| HTTP | `code` | Cuándo ocurre |
|---|---|---|
| 400 | `INVALID_INPUT` | Contraseña inválida (usuario nuevo) |
| 404 | `RESOURCE_NOT_FOUND` | Token no existe |
| 410 | `INVITATION_EXPIRED` | Token expirado |
| 409 | `INVITATION_ALREADY_ACCEPTED` | Token ya usado |

**Estado UI al recibir 200:**
- Mostrar pantalla de éxito: "Ya tienes acceso a [app_name]. Inicia sesión."
- CTA: iniciar sesión OAuth2/OIDC del tenant.

---

### 4. Reenviar invitación (admin)

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{invitationId}/resend
Authorization: Bearer <access_token>   (rol ADMIN_TENANT)
Content-Type: application/json
```

**Response exitosa — 200 OK:**
```json
{
  "code": "INVITATION_RESENT",
  "data": {
    "invitation_id": "uuid",
    "expires_at": "2026-04-17T14:00:00Z"
  }
}
```

---

### 5. Revocar invitación (admin)

```
DELETE /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations/{invitationId}
Authorization: Bearer <access_token>   (rol ADMIN_TENANT)
```

**Response exitosa — 200 OK:**
```json
{
  "code": "INVITATION_REVOKED",
  "data": null
}
```

---

### 6. Listar invitaciones (admin)

```
GET /api/v1/tenants/{tenantSlug}/apps/{clientId}/invitations
Authorization: Bearer <access_token>   (rol ADMIN_TENANT)
```

**Query params opcionales:**
| Param | Tipo | Descripción |
|---|---|---|
| `status` | string | `SENT`, `ACCEPTED`, `EXPIRED`, `REVOKED` |
| `page` | integer | Default 0 |
| `size` | integer | Default 20 |

**Response exitosa — 200 OK:**
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
        "invited_at": "2026-04-14T10:00:00Z",
        "expires_at": "2026-04-17T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 1
  }
}
```

---

## Modelo de datos requerido

### Nueva tabla: `app_membership_invitations` (V24)

```sql
CREATE TABLE app_membership_invitations (
    id                   UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id            UUID        NOT NULL,
    client_app_id        UUID        NOT NULL,
    email                CITEXT      NOT NULL,
    token_hash           VARCHAR(64) NOT NULL UNIQUE,
    role_codes           JSONB       NOT NULL DEFAULT '[]',
    status               VARCHAR(20) NOT NULL DEFAULT 'SENT',
    invited_by_user_id   UUID,
    invited_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at           TIMESTAMPTZ NOT NULL,
    accepted_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_membership_invitations PRIMARY KEY (id),
    CONSTRAINT fk_ami_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_ami_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE,
    CONSTRAINT fk_ami_invited_by
        FOREIGN KEY (invited_by_user_id) REFERENCES platform_users(id) ON DELETE SET NULL,
    CONSTRAINT uq_ami_active_email_app
        UNIQUE (client_app_id, email) WHERE status = 'SENT'
);
```

**Notas de diseño:**
- `token_hash` almacena el hash SHA-256 del token opaco entregado al usuario; nunca el token en claro.
- `role_codes` es JSONB para evitar una tabla join con roles en el momento de la invitación.
- El índice parcial `WHERE status = 'SENT'` previene invitaciones duplicadas activas sin bloquear las históricas.

---

## Pantallas UI requeridas

### Panel admin (UI administración del tenant)

| # | Pantalla | Trigger | Datos disponibles |
|---|---|---|---|
| A1 | **Lista de invitaciones** | Admin navega a la sección de invitaciones de la app | `GET /invitations` — tabla con email, status, fecha, roles, botones reenviar/revocar |
| A2 | **Formulario invitar** | Admin hace clic en "Invitar usuario" | Lista de roles de la app para multiselect; campo email |
| A3 | **Confirmación de envío** | 201 de `POST /invite` | Email invitado, fecha de expiración, opción "Invitar otro" |

### Pantalla pública (usuario invitado)

| # | Pantalla | Trigger | Datos disponibles |
|---|---|---|---|
| U1 | **Validando invitación** (loading) | Usuario abre link del email | — |
| U2a | **Crear contraseña** (`user_exists=false`) | 200 de `GET /validate` con `user_exists=false` | `email` (precompletado), `tenant_name`, `app_name`, `roles`, `expires_at` |
| U2b | **Confirmar acceso** (`user_exists=true`) | 200 de `GET /validate` con `user_exists=true` | `email`, `app_name`, `roles` |
| U3 | **Invitación aceptada** | 200 de `POST /accept` | `app_name`, CTA "Iniciar sesión" |
| U4 | **Invitación inválida / expirada** | 404 / 410 de `GET /validate` | Mensaje de error, sugerencia de contactar admin |

---

## Pasos de implementación

| # | Acción | Archivo / Módulo | Estado |
|---|---|---|---|
| 1 | Crear RFC en `doc/04-decisions/rfc/` por impacto multi-módulo | `RFC-NNN-tenant-app-invitation-flow.md` | PENDING |
| 2 | Agregar entidades de dominio: `AppMembershipInvitation`, `InvitationToken`, `InvitationStatus` | `keygo-domain/membership/` | PENDING |
| 3 | Crear migración V24: tabla `app_membership_invitations` | `keygo-supabase/.../V24__app_membership_invitations.sql` | PENDING |
| 4 | Crear puertos: `MembershipInvitationRepositoryPort`, `InvitationTokenGeneratorPort` | `keygo-app/membership/port/` | PENDING |
| 5 | Crear use cases: `InviteUserToAppUseCase`, `ValidateInvitationTokenUseCase`, `AcceptInvitationUseCase` | `keygo-app/membership/usecase/` | PENDING |
| 6 | Crear use cases admin: `ResendInvitationUseCase`, `RevokeInvitationUseCase`, `ListInvitationsUseCase` | `keygo-app/membership/usecase/` | PENDING |
| 7 | Crear JPA entity: `AppMembershipInvitationEntity` + adapter | `keygo-supabase/membership/` | PENDING |
| 8 | Crear controller: `TenantAppInvitationController` con todos los endpoints | `keygo-api/membership/controller/` | PENDING |
| 9 | Registrar endpoints públicos en `SecurityConfig` | `keygo-run/.../SecurityConfig.java` | PENDING |
| 10 | Crear email template de invitación en `EmailNotificationPort` | `keygo-infra/notification/` | PENDING |
| 11 | Crear feedback out `BE-009` en `doc/02-functional/frontend/feedback/` | `BE-009-tenant-app-invitation-flow.md` | APPLIED |
| 12 | Actualizar Postman con flujo completo de invitación | Colección Postman | PENDING |
| 13 | Actualizar docs: `data-model.md`, `entity-relationships.md`, `migrations.md` | `doc/08-reference/data/` | PENDING |

## Guía de verificación

```bash
# Compilación sin errores
./mvnw clean package -DskipTests

# Tests de use cases de invitación
./mvnw test -Dtest=InviteUserToAppUseCaseTest -pl keygo-app
./mvnw test -Dtest=AcceptInvitationUseCaseTest -pl keygo-app

# Tests de controller
./mvnw test -Dtest=TenantAppInvitationControllerTest -pl keygo-api

# Verificar migración Flyway
./mvnw spring-boot:run -pl keygo-run -Dspring-boot.run.profiles=local
# Confirmar tabla app_membership_invitations en H2 console
```

---

## Historial de transiciones

- 2026-04-14 → ⬜ Registrada
