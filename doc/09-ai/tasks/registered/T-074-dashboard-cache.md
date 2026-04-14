# T-074 — Caché @Cacheable en GetPlatformDashboardUseCase

**Estado:** 🔲 PENDING
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`, `keygo-run`

## Requisito

Agregar caché `@Cacheable` con TTL de 60 segundos en `GetPlatformDashboardUseCase`
usando Spring Cache + Caffeine. El use case realiza ~25 queries JPA por llamada,
lo que lo hace candidato prioritario para caché.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
