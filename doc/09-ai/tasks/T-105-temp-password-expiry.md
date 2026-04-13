# T-105 — Política de expiración de contraseñas temporales

**Estado:** 🔲 PENDING
**Horizonte:** mediano plazo
**Módulos afectados:** `keygo-supabase`, `keygo-app`, `keygo-run`

## Requisito

Agregar TTL de 24 h para contraseñas temporales. Campo `temp_password_expires_at`
en `tenant_users`. Job `@Scheduled` que detecta usuarios `RESET_PASSWORD` con TTL
vencido, genera nueva contraseña y la reenvía por email.
Config: `keygo.security.temp-password-ttl-hours`.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
