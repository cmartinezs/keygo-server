# SPEC Frontend P1 — Pantalla “No tienes acceso a esta aplicación”

| Campo | Valor |
|---|---|
| ID | `FE-P1-009` |
| Tipo | Frontend specification |
| Prioridad | P1 / UX de autorización |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Auth error screens |
| Estado | Propuesta para implementación |

## Problema

Cuando un usuario autenticado del tenant intenta acceder a una app sin membership activa, se requiere una experiencia clara. Para marcha blanca, “Solicitar acceso” debe estar deshabilitado u oculto si el flujo no existe.

## Dependencia

Coordinar con `BE-P1-005`, que debe devolver error funcional `KG-NO-MEMBERSHIP` o equivalente.

## Decisión funcional

Crear pantalla específica para error de membership ausente.

## Mockup funcional

```text
┌───────────────────────────────────────────────┐
│ No tienes acceso a esta aplicación            │
├───────────────────────────────────────────────┤
│ Tu cuenta existe en Acme SpA, pero no tienes  │
│ una asignación activa para Portal Clientes.   │
│                                               │
│ Código: KG-NO-MEMBERSHIP                      │
│                                               │
│ [Volver]  [Solicitar acceso]                  │
└───────────────────────────────────────────────┘
```

## Alcance incluido

- Detectar error `KG-NO-MEMBERSHIP`.
- Mostrar pantalla clara.
- Botón volver.
- Botón solicitar acceso oculto/deshabilitado si no hay flujo.

## Fuera de alcance

- Implementar solicitud de acceso completa.
- Emails o aprobación automática.

## Instrucciones para AI Agent

1. Localizar manejo de errores de login/authorize/token.
2. Detectar código funcional `KG-NO-MEMBERSHIP`.
3. Crear route o componente de error.
4. Mostrar tenant/app si la información viene en error o contexto local.
5. Deshabilitar “Solicitar acceso” con texto “No disponible en marcha blanca” si no está implementado.
6. Agregar test de render.

## Criterios de aceptación

- Usuario sin membership no ve error genérico técnico.
- Código `KG-NO-MEMBERSHIP` se muestra o se registra claramente.
- Solicitar acceso no aparece como funcional si no existe flujo.
- El usuario puede volver a una pantalla segura.

## Pruebas sugeridas

- Error `KG-NO-MEMBERSHIP` renderiza pantalla específica.
- Otro error sigue flujo genérico.
- Botón volver funciona.

## Definition of Done

- Fallo de autorización por membership es entendible y no parece caída del sistema.
