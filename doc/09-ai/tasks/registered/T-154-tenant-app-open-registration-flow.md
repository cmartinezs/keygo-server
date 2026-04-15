# T-154 — Flujo de self-registro abierto a app de tenant

**Estado:** ⬜ Registrada
**Módulos afectados:** `keygo-api`, `keygo-app`, `keygo-supabase`, docs, Postman

---

## Problema / Requisito

Definir el contrato completo del flujo de self-registro abierto de un usuario a una app de
tenant, incluyendo: orden de endpoints, datos de request/response, códigos de respuesta, y
el estado esperado en UI en cada paso.

El objetivo inmediato es que UI pueda construir las pantallas de registro sin ambigüedad.

**Gap detectado:** El flujo de registro + verificación de email ya existe en backend. Sin
embargo, al completarse la verificación, **no se crea automáticamente una `Membership`** del
usuario a la app. Actualmente eso requiere una llamada admin autenticada separada. Esta tarea
también debe resolver esa decisión de diseño.

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| T-155 | complementaria | T-155 cubre el flujo de invitación por admin; este cubre el registro libre |
| BE-008 | derivada de | Feedback out generado a partir de esta tarea para consumo inmediato de UI |

---

## Flujo: Self-Registro Abierto

### Contexto

- Usuario anónimo accede a la app de un tenant (conoce `tenantSlug` y `clientId`).
- La app tiene `registrationPolicy: OPEN` (cualquiera puede registrarse).
- El backend ya expone los tres endpoints públicos de este flujo.

### Diagrama de secuencia

```
Usuario          UI                    Backend
   │── rellena form ──►│                  │
   │                   │── POST /register ──►│ crea User PENDING + envía email
   │                   │◄── 201 {id, username, notification_email, status} ──┤
   │◄── pantalla "revisa tu email" ────────────────────────────────────────│
   │                   │                  │
   │── ingresa código ─►│                  │
   │                   │── POST /verify-email ──►│ activa User → ACTIVE
   │                   │◄── 200 EMAIL_VERIFIED ──┤
   │◄── pantalla "¡listo! ahora inicia sesión" ──────────────────────────│
   │                   │                  │
   │   (código expirado, camino alternativo)
   │                   │── POST /resend-verification ──►│ reenvía código
   │                   │◄── 200 EMAIL_VERIFICATION_RESENT ──┤
```

---

## Especificación de Endpoints

### 1. Registrar usuario

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/register
Authorization: ninguna (endpoint público)
Content-Type: application/json
```

**Path params:**
| Param | Tipo | Descripción |
|---|---|---|
| `tenantSlug` | string | Identificador único del tenant (ej. `acme`) |
| `clientId` | string | OAuth2 `client_id` de la app (ej. `acme-web`) |

**Request body:**
```json
{
  "username": "jdoe",
  "email": "jdoe@example.com",
  "password": "Min8Chars!",
  "first_name": "John",
  "last_name": "Doe"
}
```

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `username` | string | Sí | Único por tenant |
| `email` | string | Sí | Formato email; único por tenant |
| `password` | string | Sí | Mínimo 8 caracteres; no puede ser contraseña temporal |
| `first_name` | string | No | — |
| `last_name` | string | No | — |

**Response exitosa — 201 Created:**
```json
{
  "code": "USER_REGISTERED",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "jdoe",
    "notification_email": "j***@example.com",
    "status": "PENDING"
  }
}
```

**Respuestas de error:**
| HTTP | `code` | Cuándo ocurre |
|---|---|---|
| 400 | `INVALID_INPUT` | Validación de campos fallida (email inválido, contraseña < 8 chars, etc.) |
| 404 | `RESOURCE_NOT_FOUND` | `tenantSlug` o `clientId` no existen |
| 409 | `DUPLICATE_RESOURCE` | `email` o `username` ya existe en el tenant |
| 422 | `TENANT_SUSPENDED` | El tenant está suspendido |

**Estado UI al recibir 201:**
- Guardar `id` y `notification_email` en estado local de la pantalla.
- Navegar a pantalla de verificación de email mostrando el email enmascarado.
- Iniciar countdown de 30 minutos para el código.

---

### 2. Verificar email

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/verify-email
Authorization: ninguna (endpoint público)
Content-Type: application/json
```

**Request body:**
```json
{
  "email": "jdoe@example.com",
  "code": "847291"
}
```

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `email` | string | Sí | El mismo email usado en el registro |
| `code` | string | Sí | Exactamente 6 dígitos recibidos por email |

**Response exitosa — 200 OK:**
```json
{
  "code": "EMAIL_VERIFIED",
  "data": null
}
```

**Respuestas de error:**
| HTTP | `code` | Cuándo ocurre |
|---|---|---|
| 400 | `INVALID_INPUT` | `code` vacío o formato incorrecto |
| 404 | `RESOURCE_NOT_FOUND` | Usuario no encontrado para ese email en la app |
| 409 | `EMAIL_ALREADY_VERIFIED` | El email ya fue verificado previamente |
| 422 | `EMAIL_VERIFICATION_EXPIRED` | El código expiró (30 min transcurridos) |
| 422 | `EMAIL_VERIFICATION_INVALID` | Código incorrecto (pero no expirado) |

**Estado UI al recibir 200:**
- Mostrar pantalla de éxito: "Tu cuenta está activa. Ya puedes iniciar sesión."
- Ofrecer CTA para navegar al login (flujo OAuth2/OIDC del tenant).

**Estado UI al recibir 422 `EMAIL_VERIFICATION_EXPIRED`:**
- Mostrar aviso de expiración.
- Ofrecer botón "Reenviar código" que llama al endpoint siguiente.

**Estado UI al recibir 422 `EMAIL_VERIFICATION_INVALID`:**
- Mostrar aviso "Código incorrecto. Inténtalo de nuevo."
- Permitir reintentar (no redirigir).

---

### 3. Reenviar código de verificación

```
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/resend-verification
Authorization: ninguna (endpoint público)
Content-Type: application/json
```

**Request body:**
```json
{
  "email": "jdoe@example.com"
}
```

| Campo | Tipo | Requerido |
|---|---|---|
| `email` | string | Sí |

**Response exitosa — 200 OK:**
```json
{
  "code": "EMAIL_VERIFICATION_RESENT",
  "data": null
}
```

**Respuestas de error:**
| HTTP | `code` | Cuándo ocurre |
|---|---|---|
| 400 | `INVALID_INPUT` | Email vacío o formato incorrecto |
| 404 | `RESOURCE_NOT_FOUND` | Usuario no encontrado |
| 409 | `EMAIL_ALREADY_VERIFIED` | Usuario ya activo |
| 422 | `VERIFICATION_CODE_STILL_VALID` | El código anterior aún no ha expirado (debe esperar) |

**Estado UI al recibir 200:**
- Mostrar mensaje: "Código reenviado a [email enmascarado]."
- Reiniciar countdown de 30 minutos.

**Estado UI al recibir 422 `VERIFICATION_CODE_STILL_VALID`:**
- Mostrar: "Tu código anterior sigue vigente. Revisa tu bandeja de entrada."
- Deshabilitar el botón de reenvío hasta que expire.

---

## Decisión de diseño pendiente: Membership automática

Al completar `verify-email` (usuario activo), actualmente **no se crea Membership**.
Opciones:

| Opción | Comportamiento | Pros | Contras |
|---|---|---|---|
| **A — Auto ACTIVE** | Membership se crea automáticamente con status `ACTIVE` al verificar email | Sin fricción | Admin no tiene control de aprobación |
| **B — Auto PENDING** | Membership se crea con status `PENDING`; admin la aprueba manualmente | Control granular | Usuario registrado no puede usar la app hasta aprobación |
| **C — Sin auto-membership** | Mantener separado; admin crea Membership explícitamente | Máxima flexibilidad | Flujo más largo para el usuario |
| **D — Policy por app** | `ClientApp.registrationPolicy` controla si es auto-ACTIVE, auto-PENDING o sin auto | Flexible | Mayor complejidad de implementación |

**Acción requerida antes de activar esta tarea:** decidir opción A/B/C/D.

---

## Pantallas UI requeridas

| # | Pantalla | Trigger | Datos disponibles |
|---|---|---|---|
| 1 | **Formulario de registro** | Usuario navega a `/register` o equivalente | `tenantSlug`, `clientId` (del contexto de la app) |
| 2 | **"Revisa tu email"** | 201 de `/register` | `notification_email` (enmascarado), countdown 30 min |
| 3 | **Ingresar código** | El usuario abre el email y vuelve a la app | `email` (precompletado), campo de 6 dígitos |
| 4 | **Código expirado** | 422 `EMAIL_VERIFICATION_EXPIRED` | Botón "Reenviar código" |
| 5 | **Registro completo / Activación exitosa** | 200 de `/verify-email` | CTA "Iniciar sesión" |

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Decidir política de Membership automática (A/B/C/D) | — | PENDING |
| 2 | Si opción A/B/D: extender `VerifyEmailUseCase` para invocar `CreateMembershipUseCase` | `keygo-app/.../usecase/VerifyEmailUseCase.java` | PENDING |
| 3 | Si opción D: agregar `registrationPolicy` a `ClientApp` domain + migración | `keygo-domain/clientapp/model/ClientApp.java` | PENDING |
| 4 | Crear feedback out `BE-008` en `doc/02-functional/frontend/feedback/` | `BE-008-tenant-app-self-registration.md` | APPLIED |
| 5 | Actualizar Postman con los tres endpoints de este flujo | Colección Postman tenant flows | PENDING |

## Guía de verificación

```bash
# Compilación sin errores
./mvnw clean package -DskipTests -pl keygo-api,keygo-app

# Test del flujo de registro
./mvnw test -Dtest=RegistrationControllerTest -pl keygo-api

# Test del use case de verificación
./mvnw test -Dtest=VerifyEmailUseCaseTest -pl keygo-app
```

---

## Historial de transiciones

- 2026-04-14 → ⬜ Registrada
