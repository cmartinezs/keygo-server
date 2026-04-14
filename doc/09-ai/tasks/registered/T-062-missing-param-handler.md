# T-062 — Handler para MissingServletRequestParameterException

**Estado:** 🔲 PENDING
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-api`

## Requisito

Agregar un handler específico en `GlobalExceptionHandler` para
`MissingServletRequestParameterException` que responda `400 INVALID_INPUT`
en lugar del `500 OPERATION_FAILED` que se produce actualmente cuando falta
un parámetro de request.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
