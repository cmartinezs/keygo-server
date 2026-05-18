# SPEC Backend P1 — Política de acceso/registro por app

| Campo | Valor |
|---|---|
| ID | `BE-P1-005` |
| Tipo | Backend specification |
| Prioridad | P1 / Importante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / apps/auth modules |
| Módulo sugerido | ClientApp access policy |
| Estado | Propuesta para implementación |

## Problema

La documentación define políticas como Closed app, Open join y Self-signup configurable, pero la UI de apps no permite configurarlas y el contrato backend debe soportarlas de forma explícita si se habilitan.

## Decisión funcional

Para marcha blanca, default debe ser `CLOSED` o `INVITE_ONLY`. Cualquier auto-join o self-signup debe estar deshabilitado por defecto.

## Modelo sugerido

Agregar a `ClientApp` un campo:

```text
access_policy: CLOSED | OPEN_JOIN | SELF_SIGNUP
```

o bien:

```text
registration_policy: INVITE_ONLY | TENANT_USERS_CAN_JOIN | PUBLIC_SIGNUP
```

Usar un solo nombre de contrato y mantenerlo consistente en backend/frontend.

## Comportamiento esperado

| Política | Usuario existe en tenant | Membership ausente | Resultado |
|---|---:|---:|---|
| `CLOSED` / `INVITE_ONLY` | Sí | Sí | Denegar con `KG-NO-MEMBERSHIP` |
| `OPEN_JOIN` / `TENANT_USERS_CAN_JOIN` | Sí | Sí | Crear membership y permitir |
| `SELF_SIGNUP` / `PUBLIC_SIGNUP` | No | Sí | Permitir registro + membership si está habilitado |

## Alcance incluido

- Campo de política en app.
- Default seguro.
- Validación en flujo de authorize/login/token según arquitectura actual.
- Error funcional cuando no hay membership.

## Fuera de alcance

- UI pública completa de self-signup.
- Flujos de aprobación si no se decide incluirlos.

## Instrucciones para AI Agent

1. Revisar entidad/DTO de ClientApp.
2. Definir nombre único: preferido `access_policy` si el foco es autorización de acceso.
3. Agregar default `CLOSED`/`INVITE_ONLY` en creación.
4. Ajustar validación de login/authorize para membership ausente.
5. Emitir error funcional estable:

```json
{
  "code": "KG-NO-MEMBERSHIP",
  "message": "User has no active membership for this application."
}
```

6. Agregar tests por política.

## Criterios de aceptación

- Apps nuevas quedan cerradas por defecto.
- Usuario sin membership en app cerrada no recibe token.
- Error `KG-NO-MEMBERSHIP` es detectable por UI.
- Auto-join no ocurre salvo política explícita.

## Pruebas sugeridas

- Closed app + user sin membership: denegado.
- Closed app + user con membership active: permitido.
- Open join + user del tenant sin membership: crea membership y permite.
- Self signup queda deshabilitado si no está implementado.

## Definition of Done

- Política de acceso no queda implícita ni repartida en condiciones frágiles.
