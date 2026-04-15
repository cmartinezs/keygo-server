# 03. Plan de implementación

## Fase A — Fundamentos: migración, enum de actor y puerto de auditoría

### Objetivo

Establecer la infraestructura mínima sobre la cual descansa todo el RFC: columnas en schema,
enum de actor, record de evento y puerto de salida.

### Subtareas

| # | Subtarea | Archivo clave | Estado |
|---|---|---|---|
| A1 | Crear `V34__add_reason_and_parent_to_audit_events.sql` con `reason TEXT` y `parent_event_id UUID FK` en `audit_events` | `keygo-supabase/src/main/resources/db/migration/V34__add_reason_and_parent_to_audit_events.sql` | PENDING |
| A2 | Crear `AuditActorType` enum en `keygo-app` con valores `PLATFORM_USER`, `TENANT_USER`, `UI_AUTOMATION`, `SYSTEM` | `keygo-app/.../audit/model/AuditActorType.java` | PENDING |
| A3 | Crear `AuditEvent` record en `keygo-app` con campos: `actorType`, `actorId`, `action`, `targetType`, `targetId`, `reason`, `parentEventId`, `correlationId`, `summary` | `keygo-app/.../audit/model/AuditEvent.java` | PENDING |
| A4 | Crear `AuditEventPort` en `keygo-app` con operación `UUID record(AuditEvent event)` | `keygo-app/.../audit/port/AuditEventPort.java` | PENDING |
| A5 | Agregar campos `reason` y `parentEventId` en `AuditEventEntity`; ajustar `@ManyToOne` self-referencial para `parent_event_id` | `keygo-supabase/.../audit/entity/AuditEventEntity.java` | PENDING |
| A6 | Crear `AuditEventAdapter` en `keygo-supabase` implementando `AuditEventPort` | `keygo-supabase/.../audit/adapter/AuditEventAdapter.java` | PENDING |
| A7 | Registrar bean `AuditEventAdapter` en `ApplicationConfig` | `keygo-run/.../config/ApplicationConfig.java` | PENDING |
| A8 | Actualizar `migrations.md` y `data-model.md` con `V34` | `doc/08-reference/data/migrations.md`, `doc/08-reference/data/data-model.md` | PENDING |

## Fase B — Commands de acciones directas (actor humano)

### Objetivo

Agregar `actorType` y `reason` obligatorios a los commands de acciones que el operador
ejecuta de forma directa. Las firmas que hoy reciben parámetros sueltos se migran a command.

### Subtareas

| # | Subtarea | Archivo clave | Estado |
|---|---|---|---|
| B1 | Crear `SuspendPlatformUserCommand(userId, actorType, reason)`; adaptar `SuspendPlatformUserUseCase` | `keygo-app/.../user/command/SuspendPlatformUserCommand.java` | PENDING |
| B2 | Crear `ActivatePlatformUserCommand(userId, actorType, reason)`; adaptar `ActivatePlatformUserUseCase` | `keygo-app/.../user/command/ActivatePlatformUserCommand.java` | PENDING |
| B3 | Agregar `actorType` + `reason` a `AssignPlatformRoleCommand`; adaptar `AssignPlatformRoleUseCase` | `keygo-app/.../membership/command/AssignPlatformRoleCommand.java` | PENDING |
| B4 | Crear `RevokePlatformRoleCommand(userId, roleCode, actorType, reason)`; adaptar `RevokePlatformRoleUseCase` | `keygo-app/.../membership/command/RevokePlatformRoleCommand.java` | PENDING |
| B5 | Agregar `actorType` + `reason` a `AssignTenantRoleCommand`; adaptar `AssignTenantRoleUseCase` | `keygo-app/.../membership/command/AssignTenantRoleCommand.java` | PENDING |
| B6 | Crear `RevokeTenantRoleCommand(userId, roleCode, actorType, reason)`; adaptar `RevokeTenantRoleUseCase` | `keygo-app/.../membership/command/RevokeTenantRoleCommand.java` | PENDING |
| B7 | Crear `SuspendTenantCommand(slug, actorType, reason)`; adaptar `SuspendTenantUseCase` | `keygo-app/.../tenant/command/SuspendTenantCommand.java` | PENDING |
| B8 | Crear `ActivateTenantCommand(slug, actorType, reason)`; adaptar `ActivateTenantUseCase` | `keygo-app/.../tenant/command/ActivateTenantCommand.java` | PENDING |
| B9 | Agregar `actorType` + `reason` a `ResetUserPasswordCommand` (reset forzado por admin) | `keygo-app/.../user/command/ResetUserPasswordCommand.java` | PENDING |

## Fase C — Commands de acciones consecuentes (actor UI_AUTOMATION)

### Objetivo

Adaptar los commands de acciones que la UI puede disparar automáticamente como pasos
posteriores a una acción del operador. Estos reciben `actorType=UI_AUTOMATION`,
`reason=null` y opcionalmente `parentEventId`.

### Subtareas

| # | Subtarea | Archivo clave | Estado |
|---|---|---|---|
| C1 | Agregar `actorType`, `parentEventId` a `RevokeUserSessionCommand`; el guard de `reason` no aplica para `UI_AUTOMATION`; adaptar `RevokeUserSessionUseCase` | `keygo-app/.../user/command/RevokeUserSessionCommand.java` | PENDING |

> Si en el futuro la UI agrega otros pasos consecuentes al catálogo, cada uno sigue el mismo
> patrón: `actorType=UI_AUTOMATION`, `parentEventId` del evento raíz, sin `reason`.

## Fase D — Integración de AuditEventPort en use cases

### Objetivo

Hacer que cada use case del catálogo invoque `AuditEventPort.record(...)` y propague el `UUID`
del evento creado cuando el use case sea el origen de una cadena consecuente.

### Subtareas

| # | Subtarea | Archivo clave | Estado |
|---|---|---|---|
| D1 | Inyectar `AuditEventPort` en `SuspendPlatformUserUseCase`; emitir `PLATFORM_USER_SUSPENDED`; retornar `UUID` del evento | — | PENDING |
| D2 | Inyectar `AuditEventPort` en `ActivatePlatformUserUseCase`; emitir `PLATFORM_USER_ACTIVATED` | — | PENDING |
| D3 | Inyectar `AuditEventPort` en `AssignPlatformRoleUseCase`; emitir `PLATFORM_ROLE_ASSIGNED` | — | PENDING |
| D4 | Inyectar `AuditEventPort` en `RevokePlatformRoleUseCase`; emitir `PLATFORM_ROLE_REVOKED` | — | PENDING |
| D5 | Inyectar `AuditEventPort` en `SuspendTenantUseCase`; emitir `TENANT_SUSPENDED` | — | PENDING |
| D6 | Inyectar `AuditEventPort` en `ActivateTenantUseCase`; emitir `TENANT_ACTIVATED` | — | PENDING |
| D7 | Inyectar `AuditEventPort` en `AssignTenantRoleUseCase`; emitir `TENANT_ROLE_ASSIGNED` | — | PENDING |
| D8 | Inyectar `AuditEventPort` en `RevokeTenantRoleUseCase`; emitir `TENANT_ROLE_REVOKED` | — | PENDING |
| D9 | Inyectar `AuditEventPort` en `RevokeUserSessionUseCase`; emitir `SESSION_FORCE_REVOKED` con `parentEventId` si presente | — | PENDING |
| D10 | Inyectar `AuditEventPort` en `ResetPlatformPasswordUseCase`; emitir `PASSWORD_FORCE_RESET` | — | PENDING |

## Fase E — Request DTOs, controladores y headers

### Objetivo

Propagar `reason`, `actorType` y `parentEventId` desde el contrato HTTP hasta los commands,
sin lógica de negocio en los controladores.

### Subtareas

| # | Subtarea | Archivo clave | Estado |
|---|---|---|---|
| E1 | Agregar `@NotBlank @Size(max=500) String reason` a los request bodies de endpoints de acción directa | `keygo-api/.../request/` | PENDING |
| E2 | Para endpoints sin body (`DELETE`, `PATCH` vacío), decidir entre body mínimo con `reason` o header `X-Audit-Reason`; documentar decisión | `keygo-api/.../controller/` | PENDING |
| E3 | Leer `X-Audit-Actor-Type` y `X-Audit-Parent-Event-Id` en controllers; inferir `actorType` del JWT cuando el header no está presente | — | PENDING |
| E4 | Construir command con `actorType` + `reason` + `parentEventId` en cada controller afectado | — | PENDING |
| E5 | Actualizar OpenAPI: documentar `reason` en body y headers `X-Audit-*` en endpoints críticos | — | PENDING |
| E6 | Actualizar colecciones Postman: agregar `reason` en body y headers de ejemplo | `postman/` | PENDING |

## Fase F — Tests y documentación

### Objetivo

Cerrar el RFC con cobertura y documentación alineada.

### Subtareas

| # | Subtarea | Archivo clave | Estado |
|---|---|---|---|
| F1 | Tests de use cases: `reason` en blanco con `PLATFORM_USER` lanza error; `UI_AUTOMATION` sin `reason` es válido; evento se persiste con `parentEventId` correcto | `keygo-app/.../test/` | PENDING |
| F2 | Tests de controller: sin `reason` → `400`; con `reason` válido → `200`; `UI_AUTOMATION` sin `reason` → `200` | `keygo-api/.../test/` | PENDING |
| F3 | Actualizar `frontend-developer-guide.md`: nuevo campo `reason`, headers `X-Audit-*` y distinción de flujo directo vs. consecuente | `doc/02-functional/frontend/frontend-developer-guide.md` | PENDING |
| F4 | Crear feedback out `BE-NNN-critical-action-reason.md`; actualizar `feedback/README.md` | `doc/02-functional/frontend/feedback/` | PENDING |
| F5 | Actualizar `entity-relationships.md` por la auto-FK `parent_event_id` en `audit_events` | `doc/08-reference/data/entity-relationships.md` | PENDING |

## Orden sugerido

```text
Fase A → Fase B → Fase C → Fase D → Fase E → Fase F
```

Razón:
- El schema y el puerto deben existir antes que cualquier comando o use case los use.
- Los commands de acción directa (B) preceden a los consecuentes (C) porque modelan la raíz
  de la cadena; un evento consecuente necesita el `UUID` producido por un evento directo.
- La integración del puerto en use cases (D) requiere que los commands estén terminados.
- Los controllers (E) solo conectan HTTP → command; dependen de D.
- Tests y docs (F) cierran cuando todo el stack está implementado.
