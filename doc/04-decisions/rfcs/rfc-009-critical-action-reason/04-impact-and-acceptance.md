# 04. Impacto y criterios de aceptación

## Impacto por módulo

| Módulo | Impacto | Detalle |
|---|---|---|
| `keygo-domain` | Ninguno | `reason` y `actorType` son datos operativos, no invariantes de dominio |
| `keygo-app` | Alto | `AuditActorType` enum, `AuditEvent` record, `AuditEventPort`, modificación de 10+ use cases y commands |
| `keygo-api` | Medio | Request DTOs: `reason` obligatorio en endpoints directos; controllers: lectura de headers `X-Audit-*` |
| `keygo-supabase` | Medio | Migración `V34` (dos columnas), `AuditEventAdapter`, actualización de `AuditEventEntity` |
| `keygo-run` | Bajo | Solo registro de bean `AuditEventAdapter` en `ApplicationConfig` |
| `postman` | Bajo | Campo `reason` y headers `X-Audit-*` en ejemplos de colecciones afectadas |
| `docs` | Medio | `migrations.md`, `data-model.md`, `entity-relationships.md`, `frontend-developer-guide.md`, feedback out |

## Migración Flyway

**`V34__add_reason_and_parent_to_audit_events.sql`**

```sql
-- Motivo explícito del operador para acciones críticas directas
ALTER TABLE audit_events
  ADD COLUMN reason TEXT;

COMMENT ON COLUMN audit_events.reason IS
  'Motivo provisto por el operador al ejecutar una acción crítica directa. '
  'Null para eventos consecuentes (UI_AUTOMATION) y de sistema (SYSTEM). '
  'Obligatoriedad se aplica en capa de aplicación, no como constraint DB. '
  'Catálogo de acciones que lo exigen definido en RFC-009.';

-- Encadenamiento causal: evento consecuente apunta a su evento origen
ALTER TABLE audit_events
  ADD COLUMN parent_event_id UUID REFERENCES audit_events(id);

COMMENT ON COLUMN audit_events.parent_event_id IS
  'ID del evento que originó este evento consecuente. '
  'Null para eventos directos (raíz de cadena). '
  'Permite reconstruir el árbol de causalidad de una interacción.';
```

Ambas columnas son nullable en DDL para compatibilidad con eventos históricos y con eventos
que no pertenecen al catálogo de acciones críticas.

## Documentación afectada

- [ ] `doc/08-reference/data/migrations.md` — registrar `V34`
- [ ] `doc/08-reference/data/data-model.md` — columnas `reason` y `parent_event_id` en `audit_events`
- [ ] `doc/08-reference/data/entity-relationships.md` — auto-FK `parent_event_id → audit_events.id`
- [ ] `doc/02-functional/frontend/frontend-developer-guide.md` — campo `reason`, headers `X-Audit-*`, distinción directo vs. consecuente
- [ ] `doc/02-functional/frontend/feedback/BE-NNN-critical-action-reason.md` — notificar a UI

## Feedback de salida a UI

Al completar la Fase E, crear:

- [ ] `BE-NNN-critical-action-reason.md` en `doc/02-functional/frontend/feedback/` con:
  - Lista de endpoints que requieren `reason` en el body.
  - Descripción de los headers `X-Audit-Actor-Type` y `X-Audit-Parent-Event-Id`.
  - Ejemplo de flujo directo + consecuente con `parentEventId` propagado.
- [ ] Actualizar índice `doc/02-functional/frontend/feedback/README.md`.

## Criterios de aceptación

### Schema
- [ ] `V34` aplicada: columna `reason TEXT` y `parent_event_id UUID FK` existen en `audit_events`.

### Puerto y adaptador
- [ ] `AuditActorType` enum existe en `keygo-app` con los cuatro valores definidos.
- [ ] `AuditEventPort.record(AuditEvent)` existe en `keygo-app` y retorna el `UUID` del evento.
- [ ] `AuditEventAdapter` implementa el puerto y persiste ambas columnas nuevas correctamente.

### Acciones directas
- [ ] Enviar request a un endpoint crítico directo sin `reason` devuelve `400`.
- [ ] Enviar request con `reason` válido persiste el evento con `actorType=PLATFORM_USER` y `reason` correcto.
- [ ] `parent_event_id` es `null` en eventos directos.

### Acciones consecuentes
- [ ] Enviar request con `X-Audit-Actor-Type: UI_AUTOMATION` sin `reason` devuelve `200` (no error).
- [ ] El evento persiste con `actorType=UI_AUTOMATION`, `reason=null` y `parent_event_id` correcto.
- [ ] La cadena causal es reconstruible: `SELECT * FROM audit_events WHERE parent_event_id = <id_evento_directo>`.

### Tests
- [ ] Test de use case: `reason` en blanco con `PLATFORM_USER` lanza `InvalidCommandFieldException`.
- [ ] Test de use case: `actorType=UI_AUTOMATION` con `reason=null` es válido.
- [ ] Test de controller: validación `@NotBlank` activa en endpoints directos.
- [ ] Test de controller: headers `X-Audit-*` se propagan correctamente al command.

### Documentación
- [ ] `migrations.md`, `data-model.md` y `entity-relationships.md` actualizados.
- [ ] `frontend-developer-guide.md` cubre campo `reason`, headers y flujo directo/consecuente.
- [ ] Feedback out `BE-NNN` creado y registrado en el índice.
