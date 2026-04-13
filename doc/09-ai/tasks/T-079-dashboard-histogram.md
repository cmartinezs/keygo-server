# T-079 — Histograma temporal para dashboard

**Estado:** 🔲 PENDING
**Horizonte:** largo plazo
**Módulos afectados:** `keygo-api`, `keygo-supabase`

## Requisito

Implementar `GET /api/v1/admin/platform/dashboard/histogram?days=30` con series
de tiempo diarias de registros, sesiones y logins para gráficas de tendencia en UI.
Primera aproximación con `GROUP BY DATE(created_at)`. Mejora con T-076 (`audit_events`).

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
