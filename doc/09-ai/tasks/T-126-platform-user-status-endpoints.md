# T-126 — Endpoints admin para gestión de status de PlatformUser

**Estado:** 🔲 PENDING
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-run`

## Requisito

Implementar endpoints de administración para gestionar el status de `PlatformUser`:
`PUT /platform/users/{userId}/suspend`, `/activate`, `/require-reset-password`.
Cada uno con su use case y `@PreAuthorize("hasAuthority('ADMIN')")`.
Actualmente el status solo se gestiona vía DB directa.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
