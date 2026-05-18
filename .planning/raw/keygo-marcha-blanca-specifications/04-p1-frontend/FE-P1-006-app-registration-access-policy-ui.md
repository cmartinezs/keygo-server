# SPEC Frontend P1 — Configurar política de acceso/registro por app

| Campo | Valor |
|---|---|
| ID | `FE-P1-006` |
| Tipo | Frontend specification |
| Prioridad | P1 / Importante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | App detail / Access tab |
| Estado | Propuesta para implementación |

## Problema

La documentación define Closed app, Open join y Self-signup configurable, pero la UI de apps no permite configurar esa política.

## Dependencia

Coordinar con `BE-P1-005`.

## Decisión funcional

Agregar tab `Acceso` en detalle de app con política segura por defecto. Para marcha blanca, dejar default `CLOSED`/`INVITE_ONLY`.

## Alcance incluido

- Mostrar política actual de la app.
- Permitir modificar solo opciones soportadas por backend.
- Mostrar advertencias al elegir opciones más abiertas.
- Deshabilitar Self-signup si no está implementado.

## Fuera de alcance

- Registro público completo.
- Diseño de flujo de autoaprobación.

## Opciones UI sugeridas

```text
Política de acceso
(o) Cerrada / Solo usuarios asignados
( ) Open join / Usuarios del tenant pueden unirse automáticamente
( ) Self-signup público  [No disponible en marcha blanca]
```

## Instrucciones para AI Agent

1. Crear tab `Acceso` en detalle app si no existe.
2. Leer campo `access_policy` o `registration_policy` desde backend.
3. Renderizar opciones según enum real.
4. Bloquear opciones no soportadas.
5. Al guardar, enviar update app.
6. Mostrar advertencia si se selecciona Open join.

## Criterios de aceptación

- App nueva muestra política cerrada por defecto.
- Tenant admin puede ver política de acceso.
- UI no permite activar Self-signup si backend no está listo.
- Cambio de política se persiste en backend.

## Pruebas sugeridas

- Render policy default.
- Guardar cambio a Open join si soportado.
- Self-signup disabled en marcha blanca.
- Error backend visible.

## Definition of Done

- La política de acceso no queda implícita ni fuera del control del tenant admin.
