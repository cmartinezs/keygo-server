# T-050 — Refactorizar lookup de roles en CreateAppRoleUseCase

**Estado:** 🔲 PENDING
**Horizonte:** mediano plazo
**Módulos afectados:** `keygo-app`, `keygo-supabase`

## Requisito

Reemplazar la validación en `CreateAppRoleUseCase` basada en
`findAllByTenantId(...).stream().anyMatch(...)` por un lookup directo app+tenant
(ej: `findByIdAndTenantId`) para evitar cargar todos los roles en memoria.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
