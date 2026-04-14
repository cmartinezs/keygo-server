# T-035 — Detección de replay attack en refresh tokens

**Estado:** 🔲 PENDING
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`, `keygo-supabase`, `keygo-api`

## Requisito

Al recibir un refresh token en estado `USED`, revocar automáticamente toda la cadena
de sesión (todos los tokens de esa sesión). Esto detecta replay attacks donde un
atacante reutiliza un token ya girado.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
