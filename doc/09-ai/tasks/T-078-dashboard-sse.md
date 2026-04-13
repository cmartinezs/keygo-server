# T-078 — SSE para push de snapshots del dashboard

**Estado:** 🔲 PENDING
**Horizonte:** largo plazo
**Módulos afectados:** `keygo-api`, `keygo-run`

## Requisito

Implementar `GET /api/v1/admin/platform/dashboard/stream` con SSE que haga push
de snapshots del dashboard cada 30 s, eliminando el polling. Requiere Spring WebFlux
o `SseEmitter` + scheduler. Depende de T-074 (caché).

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
