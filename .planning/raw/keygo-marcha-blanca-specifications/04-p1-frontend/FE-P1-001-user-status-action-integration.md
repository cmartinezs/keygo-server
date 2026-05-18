# SPEC Frontend P1 — Integrar contrato real de suspend/activate

| Campo | Valor |
|---|---|
| ID | `FE-P1-001` |
| Tipo | Frontend specification |
| Prioridad | P1 / Importante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Tenant users |
| Estado | Propuesta para implementación |

## Problema

Frontend tipa `SuspendUserResult` y `ActivateUserResult` con flags `already_suspended` / `already_active`, pero backend puede devolver `UserData`. Esta divergencia causa errores de integración o mensajes incorrectos.

## Dependencia

Coordinar con `BE-P1-001`.

## Decisión funcional

La UI debe reflejar el contrato backend definitivo. Si backend adopta DTO de acción, usar flags explícitos. Si backend mantiene `UserData`, simplificar frontend para usar `status`.

## Alcance incluido

- Actualizar types de API.
- Actualizar mensajes de éxito/advertencia.
- Actualizar optimistic update si existe.
- Manejar errores 404/403.

## Fuera de alcance

- Implementar backend.
- Revocar sesiones al suspender.

## Instrucciones para AI Agent

1. Revisar cliente API de usuarios.
2. Confirmar response real de suspend/activate.
3. Ajustar types.
4. Ajustar UI:
   - si `already_suspended=true`, mostrar “El usuario ya estaba suspendido”.
   - si `already_active=true`, mostrar “El usuario ya estaba activo”.
   - si se usa `UserData`, derivar mensaje desde `status`.
5. Agregar tests o mocks actualizados.

## Criterios de aceptación

- TypeScript no asume campos inexistentes.
- UI actualiza estado visible del usuario.
- Mensajes son coherentes con response real.
- No queda mock ocultando divergencia de contrato.

## Pruebas sugeridas

- Suspender usuario activo.
- Suspender usuario ya suspendido.
- Activar usuario suspendido.
- Error por usuario inexistente.

## Definition of Done

- UI y backend tienen contrato consistente para acciones de estado.
