# BE-008 — Flujo de self-registro abierto a app de tenant

**Fecha:** 2026-04-14  
**Iniciado por:** Backend  
**Estado:** 🔴 Abierto  
**Contexto / Plan:** Registro público de usuarios en apps de tenant / T-154

---

## Apertura _(→ Backend)_

### Descripción

Backend expone un flujo completo de registro público que permite a cualquier usuario crear
cuenta en la app de un tenant, verificar su email y activarse. Los tres endpoints son
**públicos** (sin autenticación).

**Ruta base:** `/api/v1/tenants/{tenantSlug}/apps/{clientId}`

#### 1. Registrar usuario

```
POST /register
```

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
| 400 | `INVALID_INPUT` | Errores inline en el formulario |
| 409 | `DUPLICATE_RESOURCE` | "Ya existe una cuenta con ese email o nombre de usuario" |
| 404 | `RESOURCE_NOT_FOUND` | Error de configuración — app o tenant inválido |

#### 2. Verificar email

```
POST /verify-email
```

**Request:** `{ "email": "jdoe@example.com", "code": "847291" }`

**Response 200:** `{ "code": "EMAIL_VERIFIED", "data": null }`

| HTTP | `code` | Acción UI |
|---|---|---|
| 422 | `EMAIL_VERIFICATION_EXPIRED` | Mensaje de expiración + botón "Reenviar código" |
| 422 | `EMAIL_VERIFICATION_INVALID` | "Código incorrecto. Inténtalo de nuevo." |
| 409 | `EMAIL_ALREADY_VERIFIED` | Redirigir a login |

#### 3. Reenviar código de verificación

```
POST /resend-verification
```

**Request:** `{ "email": "jdoe@example.com" }`

**Response 200:** `{ "code": "EMAIL_VERIFICATION_RESENT", "data": null }`

| HTTP | `code` | Acción UI |
|---|---|---|
| 422 | `VERIFICATION_CODE_STILL_VALID` | "Tu código anterior sigue vigente. Revisa tu correo." |
| 409 | `EMAIL_ALREADY_VERIFIED` | Redirigir a login |

### Expectativa del receptor

**Flujo completo para UI:**

```
Pantalla 1: Formulario de registro
  └─► POST /register
        ├─ 201 → Pantalla 2
        └─ 400/409 → error inline

Pantalla 2: "Revisa tu email"
  - Mostrar notification_email (enmascarado)
  - Countdown 30 minutos (TTL fijo — el backend no lo retorna)
  - Botón "Ya tengo el código" → Pantalla 3
  - Botón "Reenviar" (activo solo si countdown = 0)
      └─► POST /resend-verification → reiniciar countdown

Pantalla 3: "Ingresar código"
  - Campo 6 dígitos / email precompletado (no editable)
  └─► POST /verify-email
        ├─ 200 → Pantalla 4
        ├─ 422 INVALID → error inline
        └─ 422 EXPIRED → Pantalla 2 con aviso de expiración

Pantalla 4: Registro completado
  - CTA: "Iniciar sesión" → flujo OAuth2/OIDC del tenant
```

> **Nota sobre Membership:** al completar `EMAIL_VERIFIED`, el usuario queda `ACTIVE` pero
> sin Membership asignada automáticamente a la app. Este comportamiento está pendiente de
> definición en T-154. La UI debe considerar que el usuario puede autenticarse pero podría
> no tener acceso a recursos de la app hasta que un admin cree su Membership.

---

## Respuesta _(→ UI)_

_Pendiente confirmación de integración por parte de UI._

**Referencia:** [T-154](../../../09-ai/tasks/registered/T-154-tenant-app-open-registration-flow.md)
