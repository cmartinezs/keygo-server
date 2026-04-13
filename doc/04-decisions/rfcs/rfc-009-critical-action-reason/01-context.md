# 01. Contexto y estado actual

## Situación actual

La capa de aplicación expone acciones administrativas sobre usuarios, roles y tenants a través de
use cases en `keygo-app`. Estas acciones se ejecutan hoy sin ningún campo de justificación:

| Use case | Firma actual |
|---|---|
| `SuspendPlatformUserUseCase` | `execute(UserId)` |
| `ActivatePlatformUserUseCase` | `execute(UserId)` |
| `SuspendTenantUseCase` | `execute(String slug)` |
| `ActivateTenantUseCase` | `execute(String slug)` |
| `AssignPlatformRoleUseCase` | `execute(AssignPlatformRoleCommand)` — command sin `reason` |
| `RevokePlatformRoleUseCase` | `execute(UUID, String)` |
| `RevokeMembershipUseCase` | `execute(…)` |
| `RevokeTenantRoleUseCase` | `execute(…)` |
| `ResetPlatformPasswordUseCase` | `execute(…)` |
| `RevokeUserSessionUseCase` | `execute(…)` |

La tabla `audit_events` existe en `keygo-supabase` con entidades JPA completas, incluyendo
campos como `summary`, `metadata` (jsonb), `event_type`, `event_action`, `severity`,
`actor_type` y `target_*`. Sin embargo:

- No existe ningún puerto `AuditEventPort` en `keygo-app`.
- Los use cases no tienen acceso a la infraestructura de auditoría sin romper la separación
  de capas.
- La tabla `audit_events` no tiene columna `reason`.
- El campo `actor_type` (String) no tiene un catálogo formal de valores válidos.
- No existe mecanismo para encadenar eventos causalmente (evento padre → eventos consecuentes).

## Gaps detectados

### 2.1 Ausencia de justificación operativa

Cuando un operador ejecuta una acción crítica hoy, el sistema no captura la intención ni la
justificación. Ante una revisión de cumplimiento o un incidente, no es posible responder
"¿por qué se suspendió este usuario?" o "¿por qué se le asignó KEYGO_ADMIN?".

### 2.2 Puerto de auditoría inexistente en app layer

`AuditEventEntity` y `AuditEventPayloadEntity` viven en `keygo-supabase` sin exposición hacia
`keygo-app`. Para que los use cases emitan eventos de auditoría respetando la arquitectura
hexagonal, se requiere un puerto `AuditEventPort` en `keygo-app` y un adaptador en
`keygo-supabase`.

### 2.3 Columna `reason` inexistente en el schema

`audit_events` no tiene columna `reason`. Almacenarlo en `metadata` (jsonb) lo haría opaco
para consultas directas e indicaría falta de intención explícita en el schema.

### 2.4 Catálogo de acciones críticas sin definición formal

No existe una definición de qué constituye una "acción crítica". Cada acción hoy es tratada
igual, sin distinción entre operaciones de bajo riesgo y operaciones de alto impacto que
modifican autorización, estado o acceso.

### 2.5 Sin distinción entre actor humano y actor automatizado

La UI puede generar dos clases de eventos contra el backend:

- **Eventos directos:** el operador toma una decisión explícita (clic en "Suspender usuario").
  Estos requieren `reason`.
- **Eventos consecuentes:** la UI ejecuta pasos automáticos posteriores a la acción del usuario,
  como parte de un flujo (p. ej., después de suspender, la UI revoca automáticamente las
  sesiones activas del usuario). Estos no requieren `reason` explícito del operador, sino
  que heredan el contexto de la acción que los originó.

El campo `actor_type` ya existe en `audit_events` como `VARCHAR(30)`, pero no tiene valores
definidos ni hay forma de encadenar un evento consecuente con su evento origen.

### 2.6 Sin cadena de causalidad entre eventos

No existe un mecanismo para vincular un evento consecuente con el evento que lo originó. Dos
eventos emitidos por la misma interacción quedan sin relación formal en la tabla.

## Lectura sintética

- Los use cases de acciones críticas no reciben ni propagan un motivo.
- La infraestructura de auditoría existe a nivel JPA pero está desconectada de la app layer.
- El schema no tiene columna `reason` ni columna de causalidad; ambas requieren migración `V34`.
- No hay catálogo formal de acciones críticas ni de tipos de actor: este RFC debe definir ambos.
- La distinción humano/automatizado es necesaria para saber cuándo exigir `reason` y cómo
  leer el historial de auditoría de forma coherente.
