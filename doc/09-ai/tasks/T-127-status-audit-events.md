# T-127 — Event sourcing para auditoría de cambios de status

**Estado:** 🔲 PENDING
**Horizonte:** largo plazo
**Módulos afectados:** `keygo-supabase`, `keygo-app`, `keygo-api`

## Requisito

Tabla `status_audit_events` (`entity_type`, `entity_id`, `old_status`, `new_status`,
`changed_by`, `changed_at`, `reason`). `StatusAuditPort` emitido desde
`suspend()`/`activate()`/`requirePasswordReset()`.
Query endpoint: `GET /admin/audit/status-changes`.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
