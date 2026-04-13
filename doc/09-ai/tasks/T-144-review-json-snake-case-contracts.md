# T-144 — Revisar contratos JSON para alinear request/response a `snake_case`

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-api`, `keygo-run`, `postman`, docs

## Requisito

Revisar la situación actual de los contratos JSON expuestos y consumidos por el backend para
identificar atributos de **request body** y **response body** que todavía estén en `camelCase`
o en naming mixto, y definir el plan de corrección hacia `snake_case` como convención única.

La revisión debe considerar al menos:

1. DTOs de entrada y salida en `keygo-api`.
2. Serialización efectiva de `BaseResponse`, envelopes paginados y payloads por endpoint.
3. OpenAPI, colección Postman y documentación funcional frontend.
4. Riesgos de compatibilidad para contratos ya consumidos por UI u otros clientes.

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si la corrección será masiva o por lotes priorizados según impacto funcional.
2. Si habrá compatibilidad transitoria para clientes que hoy consuman `camelCase`.
3. Qué estrategia de serialización se usará: convención global Jackson, anotaciones por DTO o una
   combinación controlada.
4. Cómo validar que Postman, OpenAPI y documentación queden alineados con el naming efectivo.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
