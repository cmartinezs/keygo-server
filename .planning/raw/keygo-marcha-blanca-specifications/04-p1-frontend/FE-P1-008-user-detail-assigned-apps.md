# SPEC Frontend P1 — Usuario detalle con apps asignadas

| Campo | Valor |
|---|---|
| ID | `FE-P1-008` |
| Tipo | Frontend specification |
| Prioridad | P1 / UX operativa |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | User detail, memberships |
| Estado | Propuesta para implementación |

## Problema

Usuarios y memberships pueden sentirse como módulos separados. Para tenant admin, lo natural es entrar al usuario y ver a qué apps tiene acceso.

## Decisión funcional

Agregar en detalle de usuario una pestaña o sección “Apps asignadas”, consumiendo memberships del usuario.

## Alcance incluido

- Vista detalle de usuario.
- Tab `Apps asignadas`.
- Listar apps, estado de membership y roles.
- Acción rápida “Asignar app”.
- Acción “Revocar” si ya existe endpoint y UI segura.

## Fuera de alcance

- Administración masiva.
- Editor avanzado de roles inline si no existe contrato.

## Mockup funcional

```text
┌───────────────────────────────────────────────┐
│ Ana Pérez                         Activa      │
│ ana@empresa.cl                                │
├───────────────────────────────────────────────┤
│ [Perfil] [Apps asignadas] [Sesiones] [Seguridad]│
├───────────────────────────────────────────────┤
│ Apps asignadas                                │
│ - Portal Clientes: USER, VIEWER               │
│ - Backoffice: ADMIN                           │
│                                               │
│ [Asignar app] [Suspender usuario] [Reset pass]│
└───────────────────────────────────────────────┘
```

## Instrucciones para AI Agent

1. Localizar user detail o crear ruta si no existe.
2. Consumir endpoint de memberships por usuario.
3. Renderizar app name, status y roles legibles.
4. Reutilizar componentes de chips de roles de `FE-P0-004`.
5. Agregar botón asignar app que abra flujo existente de membership.
6. Ocultar tab sesiones si `FE-P1-002` no está listo.

## Criterios de aceptación

- Desde un usuario se ven sus apps asignadas.
- Roles son legibles.
- Estado de membership se muestra claramente.
- La pantalla ayuda a operar sin saltar manualmente entre módulos.

## Pruebas sugeridas

- Usuario sin apps asignadas: empty state.
- Usuario con dos apps: render correcto.
- Click asignar app abre modal/flujo.

## Definition of Done

- Tenant admin entiende el acceso del usuario desde una sola vista.
