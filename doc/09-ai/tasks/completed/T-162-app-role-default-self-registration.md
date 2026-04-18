# T-162 — Rol por defecto de app en self-registration

**Estado:** ✅ COMPLETED  
**Correlativo:** T-162  
**Fecha de registro:** 2026-04-18

---

## Requerimiento

Al completar el flujo de self-registration (verificación de email), el usuario queda con una membresía en la app pero **sin ningún rol asignado**. Esto impide que la app pueda autorizar al usuario hasta que un administrador le asigne un rol manualmente.

Dado que cada app define su propio catálogo de roles (tabla `app_roles`, scoped por `client_app_id`), no existe un código de rol universal que se pueda asignar automáticamente. La solución es permitir que cada app marque uno de sus roles como **rol por defecto para nuevos miembros**, y que el orquestador de self-registration lo asigne automáticamente al crear la membresía.

---

## Decisión de diseño

Agregar columna `is_default BOOLEAN NOT NULL DEFAULT false` en `app_roles`, con un constraint que garantice **como máximo un rol por defecto por app** (`UNIQUE (client_app_id) WHERE is_default = true`).

El `SelfRegistrationOrchestrator` consulta el rol por defecto de la app. Si existe, lo incluye en el `CreateMembershipCommand`; si no existe, la membresía se crea sin roles (comportamiento actual).

### Por qué esta opción

- Respeta que los roles son definidos por el dueño de la app, no por la plataforma.
- No agrega complejidad al flujo de invitación ni a la asignación manual de roles.
- El constraint unique parcial garantiza integridad sin lógica en código.
- Es coherente con el patrón de `is_default` usado en `app_plan_billing_options`.

---

## Comportamiento del caso de uso

### `POST /api/v1/tenants/{slug}/apps/{clientId}/verify-email`

Flujo completo tras verificación exitosa:

1. `VerifyEmailUseCase` activa el usuario (`PENDING → ACTIVE`).
2. `SelfRegistrationOrchestrator` obtiene la `RegistrationPolicy` de la app.
3. Si la política es `OPEN_AUTO_ACTIVE` o `OPEN_AUTO_PENDING`:
   a. Consulta el rol por defecto de la app (`AppRoleRepositoryPort.findDefaultByClientApp(clientAppId)`).
   b. Si existe, construye `CreateMembershipCommand` con `roleCodes = Set.of(defaultRole.code())`.
   c. Si no existe, construye `CreateMembershipCommand` con `roleCodes = Set.of()` (sin roles).
   d. Invoca `CreateMembershipUseCase`.
4. Retorna el usuario activado.

### Invariantes

- Solo puede haber un rol por defecto por app (`UNIQUE (client_app_id) WHERE is_default = true`).
- La asignación automática es **best-effort**: si la app no tiene rol por defecto definido, la membresía se crea vacía y un administrador debe asignar el rol manualmente.
- El rol por defecto aplica únicamente al flujo de self-registration, no al flujo de invitación ni a la asignación manual.

---

## Pasos de implementación

| # | Paso | Estado |
|---|---|---|
| 1 | Flyway V30: `ALTER TABLE app_roles ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT false` + unique index parcial `WHERE is_default = true` | ⬜ PENDING |
| 2 | Actualizar `AppRole` (domain): agregar campo `isDefault`; actualizar builder | ⬜ PENDING |
| 3 | Actualizar `AppRoleEntity` (JPA): mapear columna `is_default` | ⬜ PENDING |
| 4 | Agregar `findDefaultByClientApp(UUID clientAppId): Optional<AppRole>` en `AppRoleRepositoryPort` + implementación en adapter | ⬜ PENDING |
| 5 | Actualizar `SelfRegistrationOrchestrator`: consultar rol por defecto e incluirlo en el comando si existe; corregir comentario incorrecto | ⬜ PENDING |
| 6 | Actualizar seeds (V16): marcar `user` como `is_default = true` en `demo-ui` y `acme-ui` | ⬜ PENDING |
| 7 | Tests: `SelfRegistrationOrchestratorTest` — caso con rol por defecto y caso sin él | ⬜ PENDING |

---

## Relaciones

- **Derivada de:** análisis de T-154 (flujo de self-registration abierto) — al revisar la membresía creada se detectó que no se asigna ningún rol.
- **Complementaria:** T-044 (`membership_attributes`) — atributos adicionales de membresía.
- **Complementaria:** T-045 (claim mappers) — los roles de membresía se mapean a claims del JWT de la app.
