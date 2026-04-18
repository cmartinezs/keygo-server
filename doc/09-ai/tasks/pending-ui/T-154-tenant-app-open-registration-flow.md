# T-154 — Flujo de self-registro abierto a app de tenant

**Estado:** 🧩 Pendiente integración UI
**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-supabase`, `keygo-run`, docs, Postman

---

## Problema / Requisito

Completar el flujo de self-registro abierto de un usuario a una app de tenant:
- Los tres endpoints HTTP ya existen y están documentados en OpenAPI.
- **Gap pendiente:** al verificar el email, no se crea Membership automáticamente.

El objetivo inmediato es que UI pueda construir las pantallas de registro sin ambigüedad y
que el backend cree la Membership según la política configurada en la app.

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| T-155 | complementaria | T-155 cubre el flujo de invitación por admin; este cubre el registro libre |
| T-156 | habilitadora | T-156 documenta el patrón Orchestration Use Case que esta tarea introduce |
| T-158 | bloqueante de integración UI | Sin los endpoints públicos de descubrimiento, la UI no puede construir la pantalla de selección tenant/app previa al registro |
| BE-008 | derivada de | Feedback out generado a partir de esta tarea para consumo inmediato de UI |

---

## Decisiones de diseño

### Membership automática — Opción D (APROBADA)

`ClientApp` tendrá un campo `registrationPolicy` que controla el comportamiento
post-verificación de email en el flujo de self-registro.

| Valor | Comportamiento |
|---|---|
| `OPEN_AUTO_ACTIVE` | Membership creada con status `ACTIVE` al verificar email |
| `OPEN_AUTO_PENDING` | Membership creada con status `PENDING`; admin la aprueba |
| `OPEN_NO_MEMBERSHIP` | Sin membership automática (default conservador) |
| `INVITE_ONLY` | Registro libre bloqueado; solo se accede por invitación |

### Patrón de implementación — Orchestrator

`VerifyEmailUseCase` se mantiene puro (solo verifica email y activa usuario).
Se introduce `SelfRegistrationOrchestrator` como orquestador específico del flujo
de registro abierto.

**Convención de nombre:** sufijo `Orchestrator` para clases que coordinan múltiples use
cases. Se diferencia de `UseCase` (operación atómica, un solo bounded context) en que
puede cruzar contextos y no debe usarse como building block de otro orquestador.

```
RegistrationController
        │
        ▼
SelfRegistrationOrchestrator        ← orquestador del flujo de registro
    │                   │
    ▼                   ▼
VerifyEmailUseCase    CreateMembershipUseCase  (condicionado por registrationPolicy)
```

- SRP: cada use case hace exactamente una cosa.
- OCP: futuros flujos post-verificación no modifican `VerifyEmailUseCase`.
- El patrón queda documentado en T-156 como guía para flujos similares.

---

## Flujo: Self-Registro Abierto

### Contexto

- Usuario anónimo accede a la app de un tenant (conoce `tenantSlug` y `clientId`).
- La app tiene `registrationPolicy` distinto de `INVITE_ONLY`.

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
   │                   │                        │  (crea Membership según policy)
   │                   │◄── 200 EMAIL_VERIFIED ──┤
   │◄── pantalla "¡listo! ahora inicia sesión" ──────────────────────────│
   │                   │                  │
   │   (código expirado — camino alternativo)
   │                   │── POST /resend-verification ──►│ reenvía código vigente
   │                   │                               │ o genera uno nuevo si expiró
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
| `password` | string | Sí | Mínimo 8 caracteres |
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
| 400 | `INVALID_INPUT` | Validación de campos fallida |
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

**Política de reenvío (alineada con implementación actual):**
- Si el código anterior **sigue vigente** → se reenvía el mismo código (el usuario quizás
  no recibió el email anterior).
- Si el código **expiró o no existe** → se genera uno nuevo y se envía.
- No se bloquea el reenvío mientras el código esté activo; se reenvía siempre.

**Response exitosa — 200 OK:**
```json
{
  "code": "EMAIL_VERIFICATION_RESENT",
  "data": {
    "notification_email": "j***@example.com"
  }
}
```

**Respuestas de error:**
| HTTP | `code` | Cuándo ocurre |
|---|---|---|
| 400 | `INVALID_INPUT` | Email vacío o formato incorrecto |
| 404 | `RESOURCE_NOT_FOUND` | Usuario no encontrado |
| 409 | `EMAIL_ALREADY_VERIFIED` | Usuario ya activo |

**Estado UI al recibir 200:**
- Mostrar mensaje: "Código reenviado a [notification_email]."
- Reiniciar countdown de 30 minutos.

---

## Pantallas UI requeridas

| # | Pantalla | Trigger | Datos disponibles |
|---|---|---|---|
| 1 | **Formulario de registro** | Usuario navega a `/register` | `tenantSlug`, `clientId` |
| 2 | **"Revisa tu email"** | 201 de `/register` | `notification_email` (enmascarado), countdown 30 min |
| 3 | **Ingresar código** | El usuario abre el email y vuelve a la app | `email` (precompletado), campo de 6 dígitos |
| 4 | **Código expirado** | 422 `EMAIL_VERIFICATION_EXPIRED` | Botón "Reenviar código" |
| 5 | **Registro completo / Activación exitosa** | 200 de `/verify-email` | CTA "Iniciar sesión" |

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Decidir política de Membership automática (A/B/C/D) | — | ✅ APPLIED (Opción D) |
| 2 | Agregar enum `RegistrationPolicy` al dominio | `keygo-domain/.../clientapp/model/RegistrationPolicy.java` | ✅ APPLIED |
| 3 | Agregar campo `registrationPolicy` a `ClientApp` | `keygo-domain/.../clientapp/model/ClientApp.java` | ✅ APPLIED |
| 4 | Flyway V25: columna `registration_policy` en `client_apps` | `keygo-supabase/.../V25__add_registration_policy_to_client_apps.sql` | ✅ APPLIED |
| 5 | Actualizar `ClientAppEntity` + `ClientAppRepositoryAdapter` | `keygo-supabase/.../ClientAppEntity.java` + Mapper | ✅ APPLIED |
| 6 | Crear `SelfRegistrationOrchestrator` | `keygo-app/.../user/orchestrator/SelfRegistrationOrchestrator.java` | ✅ APPLIED |
| 7 | Actualizar `RegistrationController`: inyectar orquestador en lugar de `VerifyEmailUseCase` | `keygo-api/.../registration/controller/RegistrationController.java` | ✅ APPLIED |
| 8 | Actualizar `ApplicationConfig` / wiring del nuevo use case | `keygo-run/.../config/ApplicationConfig.java` | ✅ APPLIED |
| 9 | Crear feedback out `BE-008` en `doc/02-functional/frontend/feedback/` | `BE-008-tenant-app-self-registration.md` | ✅ APPLIED |
| 10 | Actualizar Postman con los tres endpoints | Colección Postman tenant flows | ✅ VERIFIED (ya existen) |

> **Nota de migración:** T-155 reclamaba V24 para `app_membership_invitations`.
> Al ejecutarse T-154 primero, **T-155 debe usar V25**. Actualizar T-155 al planificarlo.

---

## Guía de verificación

```bash
# Compilación sin errores
./mvnw clean package -DskipTests -pl keygo-domain,keygo-app,keygo-api,keygo-supabase

# Test del flujo de registro
./mvnw test -Dtest=RegistrationControllerTest -pl keygo-api

# Test del orquestador
./mvnw test -Dtest=SelfRegistrationOrchestratorTest -pl keygo-app

# Test del use case base (no debe cambiar)
./mvnw test -Dtest=VerifyEmailUseCaseTest -pl keygo-app

# Verificar migración Flyway en perfil local
./mvnw spring-boot:run -pl keygo-run -Dspring-boot.run.profiles=local
# Confirmar columna registration_policy en H2 console
```

---

## Historial de transiciones

- 2026-04-14 → ⬜ Registrada
- 2026-04-15 → 📋 Planificada (Opción D aprobada; patrón Orchestration Use Case definido)
- 2026-04-15 → ✅ Backend completado (Todos los pasos implementados; compilación exitosa)
- 2026-04-15 → 🧩 Pendiente integración UI (T-158 bloqueante: endpoints públicos de descubrimiento requeridos para UX completa)
