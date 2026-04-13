# RFC-009 — Motivo obligatorio en acciones críticas y persistencia en auditoría

> **Estado:** DRAFT
> **Origen:** Iniciativa directa — necesidad de trazabilidad operativa para acciones sensibles de administración
> **Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-supabase`, `keygo-run`, `postman`, docs
> **Migración Flyway:** Probable `V34` — columna `reason` en `audit_events`

## Objetivo

Exigir un motivo explícito (`reason`) cuando un operador de plataforma ejecuta acciones críticas
sobre usuarios, roles o tenants, y persistir ese motivo junto con el evento de auditoría
correspondiente.

Hoy las acciones críticas (suspender usuario, asignar `KEYGO_ADMIN`, revocar rol, etc.) se
ejecutan sin ningún campo de justificación. Esto deja un gap operativo: ante un incidente o
revisión de cumplimiento, no es posible saber por qué ocurrió la acción, solo quién y cuándo.

## Documentos

1. [01-context.md](01-context.md) — estado actual, gap de trazabilidad y acciones críticas identificadas
2. [02-design.md](02-design.md) — criterios de criticidad, contrato de reason y decisiones de diseño
3. [03-implementation-plan.md](03-implementation-plan.md) — fases y subtareas
4. [04-impact-and-acceptance.md](04-impact-and-acceptance.md) — impacto por módulo y criterios de aceptación

## Decisiones clave propuestas

| Tema | Decisión propuesta | Motivo |
|---|---|---|
| Almacenamiento | Columnas `reason TEXT` + `parent_event_id UUID FK` en `audit_events` (V34) | `reason` es consultable directamente; `parent_event_id` permite reconstruir cadenas causales |
| Tipos de actor | `AuditActorType` enum: `PLATFORM_USER`, `TENANT_USER`, `UI_AUTOMATION`, `SYSTEM` | Formaliza el campo `actor_type` libre actual; determina si `reason` es obligatorio |
| `reason` obligatorio | Solo cuando `actorType` es `PLATFORM_USER` o `TENANT_USER` | Los eventos consecuentes (`UI_AUTOMATION`) no exigen reason; lo heredan del evento padre vía `parent_event_id` |
| Eventos consecuentes | La UI puede disparar pasos automáticos post-acción con `X-Audit-Actor-Type: UI_AUTOMATION` + `X-Audit-Parent-Event-Id` | Permite auditar flujos multi-paso sin exigir reason al operador en cada paso |
| Puerto de auditoría | `AuditEventPort` en `keygo-app`; retorna `UUID` del evento para propagación como `parentEventId` | Hoy no existe puerto app-layer; la infraestructura JPA está desconectada de los use cases |
| Scope del catálogo | Cerrado al aprobarse el RFC; extensible documentando en este RFC | Evita creep; las acciones fuera del catálogo no exigen reason |

## Relaciones

| Artefacto | Tipo de relación | Descripción |
|---|---|---|
| T-076 (`audit_events` table) | complementaria | T-076 propone la tabla de auditoría; este RFC la conecta a acciones críticas con reason |
| T-127 (status audit events) | habilitadora | Este RFC establece el puerto y el modelo de auditoría que T-127 necesitará para event sourcing |
| RFC-008 (`rbac-multi-scope-alignment`) | complementaria | Las acciones de asignación/revocación de roles son parte del catálogo de acciones críticas de este RFC |
| T-126 (suspend/activate/require-reset) | habilitadora | T-126 implementa los endpoints de cambio de estado; este RFC los extiende con reason obligatorio |

## Historial de transiciones

### 2026-04-13 — 📄 En RFC (DRAFT)

- RFC creado como iniciativa directa: ninguna tarea previa.
- Se identificaron las acciones críticas del catálogo inicial revisando use cases de
  `SuspendPlatformUser`, `SuspendTenant`, `AssignPlatformRole`, `RevokePlatformRole`,
  `ActivatePlatformUser`, `ActivateTenant`, `RevokeMembership`, `RevokeTenantRole`,
  `ResetPlatformPassword` y `RevokeUserSession`.
- Se confirmó que `AuditEventEntity` existe en JPA (`keygo-supabase`) pero no hay puerto
  en `keygo-app`, por lo que los use cases no pueden registrar eventos sin acoplamiento directo.
- Se propuso columna `reason TEXT` en `audit_events` sobre jsonb por legibilidad y consultabilidad.

### 2026-04-13 — 📄 Revisión DRAFT — tipos de actor y eventos consecuentes

- Se incorporó la distinción entre eventos generados directamente por el operador
  (`PLATFORM_USER` / `TENANT_USER`) y eventos generados por la UI como pasos automáticos
  posteriores a una acción (`UI_AUTOMATION`).
- Se agregó columna `parent_event_id UUID FK` a `V34` para encadenar causalmente los eventos
  consecuentes con su evento origen.
- `AuditActorType` enum formaliza el campo `actor_type` libre actual.
- `reason` es obligatorio solo para actores humanos; `UI_AUTOMATION` no propaga reason.
- El transporte del contexto de automatización usa headers HTTP `X-Audit-Actor-Type` y
  `X-Audit-Parent-Event-Id` para no contaminar los request bodies de endpoints humanos.
- `AuditEventPort.record(...)` retorna `UUID` del evento para que el use case pueda
  propagarlo como `parentEventId` a los eventos consecuentes si corresponde.
