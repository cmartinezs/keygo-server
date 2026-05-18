# SPEC Frontend P1 — Desactivar mocks MSW en flujos centrales y crear matriz mock vs real

| Campo | Valor |
|---|---|
| ID | `FE-P1-003` |
| Tipo | Frontend specification |
| Prioridad | P1 / Importante antes de piloto |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Bootstrap, MSW, environment config |
| Estado | Propuesta para implementación |

## Problema

El bootstrap indica mocks para `connections`, suspend/activate, sesiones y features pendientes. Antes de marcha blanca, los flujos principales no deben depender de MSW ni mocks ocultos.

## Decisión funcional

MSW debe estar desactivado por defecto en ambientes de marcha blanca. Además, debe existir matriz documentada de funcionalidades reales vs mock.

## Alcance incluido

- Revisar `main.tsx` y bootstrap de MSW.
- Controlar mocks por variable de entorno explícita.
- Desactivar mocks por defecto.
- Crear documentación `docs/mock-real-matrix.md` o equivalente.
- Revisar que login/apps/users/memberships/roles/logout no usen mocks.

## Fuera de alcance

- Eliminar MSW para desarrollo local.
- Reescribir tests que dependen de MSW salvo necesarios.

## Variable sugerida

```env
VITE_ENABLE_MSW=false
```

## Matriz mínima esperada

| Funcionalidad | Estado | Fuente |
|---|---|---|
| Login | Real | Backend |
| Apps | Real | Backend |
| Users | Real | Backend |
| Memberships | Real | Backend |
| App roles | Real | Backend |
| Sessions | Disabled o Real | Según BE-P1-002 |
| Connections | Mock/Disabled | No central |

## Instrucciones para AI Agent

1. Localizar inicialización MSW.
2. Asegurar que MSW solo inicia si variable explícita está en `true`.
3. Auditar handlers MSW.
4. Crear matriz de estado.
5. Ajustar README/env example.
6. Verificar build de marcha blanca con mocks off.

## Criterios de aceptación

- En build default, MSW no intercepta flujos principales.
- Existe forma explícita de activar mocks en desarrollo local.
- Matriz mock vs real queda documentada.
- No hay comentarios ambiguos sobre endpoints pendientes en flujos centrales.

## Pruebas sugeridas

- Ejecutar app con `VITE_ENABLE_MSW=false` y validar llamadas reales.
- Ejecutar app con `VITE_ENABLE_MSW=true` en local y validar handlers.
- Revisar Network tab en flujo de creación de app y membership.

## Definition of Done

- La marcha blanca prueba el sistema real, no una simulación parcial.
