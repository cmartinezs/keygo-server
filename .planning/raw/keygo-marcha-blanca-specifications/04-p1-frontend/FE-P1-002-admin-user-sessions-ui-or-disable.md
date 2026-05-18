# SPEC Frontend P1 — Sesiones admin de usuario o retiro temporal de UI

| Campo | Valor |
|---|---|
| ID | `FE-P1-002` |
| Tipo | Frontend specification |
| Prioridad | P1 / Condicional |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | User detail / sessions |
| Estado | Propuesta para implementación |

## Problema

La UI llama `GET /users/{userId}/sessions`, pero backend podría no tener endpoint. Mantener botón o tab funcional con mock puede inducir a error en marcha blanca.

## Decisión funcional

Elegir una alternativa:

A. Si `BE-P1-002` se implementa, integrar UI real.
B. Si no se implementa, ocultar o deshabilitar tab/botón de sesiones en marcha blanca.

## Alcance incluido

- Revisar componente de sesiones.
- Eliminar dependencia de mock en flujo principal.
- Mostrar empty state real si endpoint existe.
- Ocultar feature si endpoint no existe.

## Fuera de alcance

- Implementar backend.
- Revocación de sesiones individuales.

## Instrucciones para AI Agent

1. Buscar `getAdminUserSessions`.
2. Verificar si se invoca desde UI visible.
3. Si backend existe:
   - actualizar endpoint real,
   - renderizar sesiones,
   - manejar loading/error/empty.
4. Si backend no existe:
   - ocultar tab `Sesiones`, o
   - mostrar “No disponible en marcha blanca” sin llamar endpoint.
5. Remover mock de sesiones para build de marcha blanca.

## Criterios de aceptación

- No hay llamada a endpoint inexistente en UI real.
- No se muestra feature como operativa si backend no existe.
- Si existe backend, la UI consume datos reales.

## Pruebas sugeridas

- Render user detail sin sesiones cuando feature disabled.
- Render lista de sesiones cuando feature enabled.
- Error state si endpoint falla.

## Definition of Done

- La sección de sesiones no genera falsa sensación de funcionalidad.
