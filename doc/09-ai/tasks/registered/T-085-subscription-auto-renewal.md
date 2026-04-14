# T-085 — Renovación automática de suscripciones

**Estado:** 🔲 PENDING
**Horizonte:** mediano plazo
**Módulos afectados:** `keygo-app`, `keygo-supabase`

## Requisito

Implementar un `@Scheduled` job que detecte suscripciones con
`currentPeriodEnd < now()` y `autoRenew=true`, genere nueva factura y actualice
el período. Depende de T-084 (gateway de pago real).

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
