# SPEC Frontend P1 — Integrar logout/revocación OAuth real

| Campo | Valor |
|---|---|
| ID | `FE-P1-007` |
| Tipo | Frontend specification |
| Prioridad | P1 / Importante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Auth/logout |
| Estado | Propuesta para implementación |

## Problema

Frontend marca revocación como pendiente. Para marcha blanca, logout no debe ser solo limpieza local si existen refresh tokens o sesiones persistentes.

## Dependencia

Coordinar con `BE-P1-006`.

## Decisión funcional

Frontend debe llamar endpoint real de logout/revoke y luego limpiar estado local.

## Alcance incluido

- Identificar flujo actual de logout.
- Llamar endpoint backend real.
- Manejar idempotencia y errores seguros.
- Limpiar tokens locales solo después de intento de revocación o en finally controlado.

## Fuera de alcance

- Single Logout federado.
- Revocar todas las sesiones.

## Instrucciones para AI Agent

1. Revisar auth store/context.
2. Localizar función logout.
3. Confirmar endpoint backend definitivo.
4. Implementar llamada:
   - revoke refresh token si disponible,
   - logout sesión si aplica.
5. Limpiar storage local.
6. Redirigir a login o landing.
7. Eliminar comentarios de “pendiente” si queda resuelto.

## Criterios de aceptación

- Logout llama backend real.
- Si backend responde OK, tokens locales se limpian.
- Si backend falla por token ya inválido, UI igual queda deslogueada de forma segura.
- No se depende de mock.

## Pruebas sugeridas

- Logout exitoso.
- Logout con backend 401/invalid token.
- Logout sin refresh token local.
- Verificar redirect final.

## Definition of Done

- Cierre de sesión es funcional, no solo visual.
