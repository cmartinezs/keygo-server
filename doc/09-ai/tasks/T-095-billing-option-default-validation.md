# T-095 — Validar isDefault en CreateAppPlanCommand

**Estado:** 🔲 PENDING
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`

## Requisito

Validar en `CreateAppPlanCommand` que si `billingOptions` no está vacía, al menos
una opción tenga `isDefault=true`. Lanzar `IllegalArgumentException` si ninguna
opción es default.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
