# 02. Diseño objetivo

## Tipos de actor

Toda acción auditada tiene un `actorType` que determina el origen de la intención. Este valor
se formaliza como enum en `keygo-app`:

| `AuditActorType` | Descripción |
|---|---|
| `PLATFORM_USER` | Operador de plataforma actuando de forma directa y consciente |
| `TENANT_USER` | Administrador de tenant actuando de forma directa |
| `UI_AUTOMATION` | La UI ejecuta un paso automático posterior a una acción del operador |
| `SYSTEM` | Proceso interno del backend (job, scheduled task, trigger interno) |

### Eventos directos vs. eventos consecuentes

| Clase | Actor | `reason` | Ejemplo |
|---|---|---|---|
| **Directo** | `PLATFORM_USER` / `TENANT_USER` | Obligatorio | Operador suspende un usuario |
| **Consecuente** | `UI_AUTOMATION` | No aplica | UI revoca sesiones activas luego de la suspensión |
| **Sistema** | `SYSTEM` | No aplica | Job limpia sesiones expiradas |

Un **evento consecuente** es una acción que la UI dispara automáticamente como paso siguiente
dentro de un flujo, sin que el operador haya tomado una decisión explícita sobre ese paso en
particular. Aunque llegue como llamada HTTP al backend, su origen es automatizado.

## Cadena de causalidad

Para mantener trazabilidad completa, los eventos consecuentes deben poder vincularse con el
evento que los originó. Se agrega columna `parent_event_id UUID` nullable en `audit_events`,
con FK a sí misma:

```
audit_events
  id               PK
  parent_event_id  FK → audit_events.id  (nullable)
  correlation_id   agrupación de sesión/flujo  (ya existe)
```

- `parent_event_id`: referencia directa al evento inmediatamente anterior en la cadena causal.
- `correlation_id`: agrupa todos los eventos de una misma interacción o flujo (ya existe en el
  schema; se usa para consultas de auditoría por sesión/request).

Ejemplo de cadena:

```
E1  actor=PLATFORM_USER   action=PLATFORM_USER_SUSPENDED  reason="Actividad sospechosa"
 └─ E2  actor=UI_AUTOMATION  action=SESSION_FORCE_REVOKED     reason=null  parent=E1
 └─ E3  actor=UI_AUTOMATION  action=PASSWORD_FORCE_RESET       reason=null  parent=E1
```

## Criterios para clasificar una acción como crítica

Una acción es **crítica** si cumple al menos uno de los siguientes criterios:

1. **Modifica la autorización** de un sujeto (asignar o revocar roles, revocar membresía).
2. **Altera el estado de acceso** de un sujeto (suspender, activar, requerir reset de contraseña).
3. **Invalida sesiones activas** de forma forzada por un tercero.
4. **Afecta a un recurso organizacional completo** (suspender o activar un tenant).

Las acciones que el propio usuario realiza sobre sí mismo (cambio de contraseña voluntario,
logout propio) **no** son críticas bajo esta definición.

## Catálogo inicial de acciones críticas

### Acciones directas — requieren `reason`

| Acción | Use case | Scope |
|---|---|---|
| Suspender usuario de plataforma | `SuspendPlatformUserUseCase` | Plataforma |
| Activar usuario de plataforma | `ActivatePlatformUserUseCase` | Plataforma |
| Requerir reset de contraseña | *(T-126, pendiente)* | Plataforma |
| Asignar rol de plataforma | `AssignPlatformRoleUseCase` | Plataforma |
| Revocar rol de plataforma | `RevokePlatformRoleUseCase` | Plataforma |
| Asignar rol de tenant | `AssignTenantRoleUseCase` | Tenant |
| Revocar rol de tenant | `RevokeTenantRoleUseCase` | Tenant |
| Revocar membresía | `RevokeMembershipUseCase` | Tenant / App |
| Suspender tenant | `SuspendTenantUseCase` | Plataforma |
| Activar tenant | `ActivateTenantUseCase` | Plataforma |
| Reset de contraseña forzado por admin | `ResetPlatformPasswordUseCase` | Plataforma |

### Acciones consecuentes — `reason` no requerido; heredan contexto del evento padre

| Acción | Use case | Disparador típico |
|---|---|---|
| Revocar sesión de usuario (forzado) | `RevokeUserSessionUseCase` | UI post-suspensión |

> El catálogo es cerrado al aprobarse el RFC. Futuras adiciones requieren actualizar este
> documento. Los eventos consecuentes adicionales que la UI agregue en el futuro aplican el
> mismo modelo: `actorType=UI_AUTOMATION` + `parentEventId` del evento origen.

## Decisiones de diseño

### D1 — Columnas `reason` y `parent_event_id` en `audit_events` (V34)

Se agregan ambas columnas en la misma migración para no partir el schema en estados
intermedios inconsistentes:

```sql
ALTER TABLE audit_events
  ADD COLUMN reason          TEXT,
  ADD COLUMN parent_event_id UUID REFERENCES audit_events(id);
```

Ambas son nullable en DDL. La obligatoriedad de `reason` se aplica en capa de aplicación.

**`reason` nullable:** eventos consecuentes y de sistema no propagan motivo del operador.  
**`parent_event_id` nullable:** eventos directos (raíz de cadena) no tienen padre.

### D2 — `AuditActorType` enum en `keygo-app`

```java
public enum AuditActorType {
  PLATFORM_USER,
  TENANT_USER,
  UI_AUTOMATION,
  SYSTEM
}
```

Reemplaza el String libre de `actor_type` actual. El adaptador convierte `AuditActorType`
al String que persiste en la columna.

### D3 — `reason` y `actorType` en el Command

Cada acción del catálogo recibe un Command con `actorType`. El guard de `reason` se activa
solo cuando `actorType` es humano:

```java
public record SuspendPlatformUserCommand(UUID userId, AuditActorType actorType, String reason) {
  public SuspendPlatformUserCommand {
    if (userId == null) throw new InvalidCommandFieldException("userId");
    if (actorType == null) throw new InvalidCommandFieldException("actorType");
    boolean isHuman = actorType == PLATFORM_USER || actorType == TENANT_USER;
    if (isHuman && (reason == null || reason.isBlank()))
      throw new InvalidCommandFieldException("reason");
    if (reason != null && reason.length() > 500)
      throw new InvalidCommandFieldException("reason: max 500 chars");
  }
}
```

Los eventos consecuentes usan el mismo command pero con `actorType=UI_AUTOMATION` y
`reason=null`, más un `parentEventId` opcional.

### D4 — Puerto `AuditEventPort` en `keygo-app`

```java
public interface AuditEventPort {
  UUID record(AuditEvent event);  // retorna el ID del evento persistido
}
```

`AuditEvent` es un record en `keygo-app` con: `actorType`, `actorId`, `action`,
`targetType`, `targetId`, `reason`, `parentEventId`, `correlationId`, `summary`.

El `record(...)` retorna el `UUID` del evento creado para que el use case pueda propagarlo
como `parentEventId` a los eventos consecuentes si corresponde.

### D5 — Transporte del `actorType` desde la API

El `actorType` se determina en el controlador, no en el DTO:

- Cuando el operador envía el request manualmente: el controller asume `PLATFORM_USER` o
  `TENANT_USER` según el contexto del JWT.
- Cuando la UI envía un paso automatizado: incluye el header `X-Audit-Actor-Type: UI_AUTOMATION`
  y opcionalmente `X-Audit-Parent-Event-Id: <uuid>`. El controller lee estos headers y los
  propaga al command.

Esto evita exponer `actorType` como campo del body en endpoints humanos (donde siempre es
el mismo valor y confundiría al operador).

### D6 — Validación uniforme

| Capa | Validación |
|---|---|
| Controller | Lee `X-Audit-Actor-Type` del header; default = tipo inferido del JWT |
| DTO (keygo-api) | `@NotBlank @Size(max=500)` en `reason` solo para endpoints de acción directa |
| Command (keygo-app) | Guard condicional según `actorType` |
| Use case | Confianza en el Command; no re-valida |

## Componentes afectados

| Módulo | Cambio | Detalle |
|---|---|---|
| `keygo-app` | Crear | `AuditActorType` enum, `AuditEvent` record, `AuditEventPort` |
| `keygo-app` | Modificar | Commands de acciones directas: agregar `actorType` + `reason`; commands consecuentes: agregar `actorType` + `parentEventId`; use cases: invocar `AuditEventPort.record(...)` |
| `keygo-api` | Modificar | Request DTOs de endpoints de acción directa: agregar `@NotBlank String reason`; controllers: leer headers `X-Audit-*` y construir command con `actorType` |
| `keygo-supabase` | Crear | `AuditEventAdapter` implementando `AuditEventPort`; migración `V34` |
| `keygo-run` | Modificar | Registrar bean `AuditEventAdapter` en `ApplicationConfig` |
| `docs` | Actualizar | `frontend-developer-guide.md`, feedback out `BE-NNN`, `migrations.md`, `data-model.md` |
