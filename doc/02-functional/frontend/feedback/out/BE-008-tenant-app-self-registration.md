# BE-008 — Flujo de self-registro abierto a app de tenant

**Fecha:** 2026-04-14
**Estado:** 🔴 Abierto
**Tarea:** [T-154](../../../09-ai/tasks/registered/T-154-tenant-app-open-registration-flow.md)

---

## Cambio

Backend expone un flujo completo de registro público que permite a cualquier usuario crear
cuenta en la app de un tenant, verificar su email y activarse. Los tres endpoints son
**públicos** (sin autenticación).

**Ruta base:** `/api/v1/tenants/{tenantSlug}/apps/{clientId}`

---

## Endpoints disponibles

### 1. Registrar usuario

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/register
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

| Campo | Requerido | Notas |
|---|---|---|
| `username` | Sí | Único por tenant |
| `email` | Sí | Único por tenant |
| `password` | Sí | Mínimo 8 caracteres |
| `first_name` | No | — |
| `last_name` | No | — |

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

**Errores relevantes para UI:**
| HTTP | `code` | Acción sugerida en UI |
|---|---|---|
| 400 | `INVALID_INPUT` | Mostrar errores inline en los campos del formulario |
| 409 | `DUPLICATE_RESOURCE` | "Ya existe una cuenta con ese email o nombre de usuario" |
| 404 | `RESOURCE_NOT_FOUND` | Error de configuración — app o tenant inválido |

---

### 2. Verificar email

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/verify-email
```

**Request:**
```json
{
  "email": "jdoe@example.com",
  "code": "847291"
}
```

**Response 200:**
```json
{
  "code": "EMAIL_VERIFIED",
  "data": null
}
```

**Errores relevantes para UI:**
| HTTP | `code` | Acción sugerida en UI |
|---|---|---|
| 422 | `EMAIL_VERIFICATION_EXPIRED` | Mostrar mensaje de expiración + botón "Reenviar código" |
| 422 | `EMAIL_VERIFICATION_INVALID` | "Código incorrecto. Inténtalo de nuevo." (sin redirigir) |
| 409 | `EMAIL_ALREADY_VERIFIED` | Redirigir a login — usuario ya activo |

---

### 3. Reenviar código de verificación

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/resend-verification
```

**Request:**
```json
{
  "email": "jdoe@example.com"
}
```

**Response 200:**
```json
{
  "code": "EMAIL_VERIFICATION_RESENT",
  "data": null
}
```

**Errores relevantes para UI:**
| HTTP | `code` | Acción sugerida en UI |
|---|---|---|
| 422 | `VERIFICATION_CODE_STILL_VALID` | "Tu código anterior sigue vigente. Revisa tu correo." |
| 409 | `EMAIL_ALREADY_VERIFIED` | Redirigir a login |

---

## Flujo completo para UI

```
Pantalla 1: Formulario de registro
  └─► POST /register
        ├─ 201 → Pantalla 2
        └─ 400/409 → mostrar error inline

Pantalla 2: "Revisa tu email"
  - Mostrar: notification_email (enmascarado)
  - Iniciar countdown 30 minutos
  - Botón: "Ya tengo el código" → Pantalla 3
  - Botón: "Reenviar" (activo solo si countdown = 0)
      └─► POST /resend-verification → reiniciar countdown

Pantalla 3: "Ingresar código"
  - Campo: 6 dígitos (input tipo OTP o texto numérico)
  - Email: precompletado (no editable)
  └─► POST /verify-email
        ├─ 200 → Pantalla 4
        ├─ 422 INVALID → error inline "Código incorrecto"
        └─ 422 EXPIRED → navegar a Pantalla 2 con aviso de expiración

Pantalla 4: Registro completado
  - Mensaje: "Tu cuenta está activa."
  - CTA: "Iniciar sesión" → flujo OAuth2/OIDC del tenant
```

---

## Impacto en UI

1. **Pantallas a crear:** 4 (formulario, check-email, ingresar-código, éxito)
2. **Estado local necesario:** `notification_email`, `email`, countdown de 30 min
3. **Sin autenticación:** los tres endpoints son públicos — no se requiere token
4. **`tenantSlug` y `clientId`:** deben estar disponibles en el contexto de la app (config de la app cliente o URL)
5. **`notification_email`:** es el email enmascarado (ej. `j***@example.com`) — solo para mostrar, no para el request de verificación que usa el email completo
6. **Countdown:** el backend no retorna el tiempo restante; la UI debe iniciarlo al recibir el 201 o el 200 de reenvío (TTL fijo de 30 minutos)

## Nota sobre Membership

> Al completar la verificación (`EMAIL_VERIFIED`), el usuario queda en estado `ACTIVE` pero
> **no tiene una Membership asignada automáticamente** a la app. Este comportamiento está
> pendiente de definición en T-154. Mientras no se resuelva, la UI debe considerar que el
> usuario podrá autenticarse pero podría no tener acceso a recursos de la app hasta que un
> admin cree su Membership.

## Verificación

Backend ha verificado que estos endpoints existen y son funcionales:

- ✅ `POST /register` — `RegistrationController`
- ✅ `POST /verify-email` — `RegistrationController`
- ✅ `POST /resend-verification` — `RegistrationController`
- ✅ Endpoints documentados en OpenAPI/Swagger
- ✅ Endpoints públicos (sin autenticación)

## Confirmación

_Pendiente confirmación por parte de UI._
