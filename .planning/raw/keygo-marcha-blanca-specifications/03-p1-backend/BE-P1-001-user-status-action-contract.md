# SPEC Backend P1 — Contrato consistente para suspend/activate de usuarios

| Campo | Valor |
|---|---|
| ID | `BE-P1-001` |
| Tipo | Backend specification |
| Prioridad | P1 / Importante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / users module |
| Módulo sugerido | Tenant users |
| Estado | Propuesta para implementación |

## Problema

La UI espera respuestas `already_suspended` / `already_active`, pero backend devuelve `BaseResponse<UserData>` en `suspendUser` y `activateUser`.

## Decisión funcional

Elegir un contrato único para acciones de estado de usuario. Recomendación: backend debe devolver un resultado de acción explícito para que UI pueda mostrar mensajes correctos.

## Contrato recomendado

```json
{
  "user_id": "user_123",
  "previous_status": "ACTIVE",
  "current_status": "SUSPENDED",
  "already_suspended": false
}
```

Para activar:

```json
{
  "user_id": "user_123",
  "previous_status": "SUSPENDED",
  "current_status": "ACTIVE",
  "already_active": false
}
```

## Alcance incluido

- Definir DTOs `SuspendUserResult` y `ActivateUserResult` o uno genérico `UserStatusActionResult`.
- Actualizar endpoints suspend/activate.
- Mantener información suficiente para UI.
- Agregar tests para idempotencia.

## Fuera de alcance

- Crear UI.
- Auditar sesiones activas al suspender, salvo que ya exista requirement.

## Instrucciones para AI Agent

1. Revisar endpoints actuales de suspend/activate.
2. Decidir con mínima ruptura:
   - opción A: devolver DTO explícito;
   - opción B: mantener `UserData` y ajustar UI.
3. Si se adopta DTO explícito, implementar campos de idempotencia.
4. Agregar tests:
   - suspender usuario activo;
   - suspender usuario ya suspendido;
   - activar usuario suspendido;
   - activar usuario ya activo.
5. Documentar response.

## Criterios de aceptación

- Contrato backend coincide con types frontend.
- Acciones son idempotentes o al menos reportan estado previo/actual.
- UI puede mostrar mensaje correcto sin inferencias frágiles.

## Pruebas sugeridas

- Integration test por endpoint.
- Test de usuario inexistente: 404.
- Test de usuario de otro tenant: 404 o 403 según política.

## Definition of Done

- No hay divergencia entre response real y type esperado por UI.
