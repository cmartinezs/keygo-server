# SPEC Frontend P1 — Autocomplete remoto para usuarios y apps en memberships

| Campo | Valor |
|---|---|
| ID | `FE-P1-004` |
| Tipo | Frontend specification |
| Prioridad | P1 / Importante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Memberships form |
| Estado | Propuesta para implementación |

## Problema

La UI carga solo primera página de usuarios y apps para selects (`listUsers(..., 0, 20)` y `listClientApps(..., 0, 20)`). Si hay más de 20, no se pueden seleccionar todos.

## Dependencia

Coordinar con `BE-P1-003` para soporte de búsqueda `q`.

## Decisión funcional

Reemplazar selects fijos por autocomplete remoto con búsqueda paginada.

## Alcance incluido

- Selector remoto de usuarios.
- Selector remoto de apps.
- Debounce de búsqueda.
- Loading y empty state.
- No cargar todos los usuarios/apps en memoria.

## Fuera de alcance

- Búsqueda avanzada multifiltro.
- Selección masiva.

## Instrucciones para AI Agent

1. Localizar form de creación/asignación de membership.
2. Identificar selects actuales.
3. Crear componente reutilizable `RemoteEntitySelect` o usar componente existente.
4. Agregar búsqueda:
   - users por email/nombre,
   - apps por name/client_id.
5. Agregar debounce 250–400 ms.
6. Mantener selección actual visible aunque el usuario cambie búsqueda.
7. Manejar errores.

## Criterios de aceptación

- Se puede seleccionar usuario que no está en primera página.
- Se puede seleccionar app que no está en primera página.
- La UI no hace request por cada tecla sin debounce.
- Empty state informa “Sin resultados”.
- Error state permite reintentar.

## Pruebas sugeridas

- Mock API con más de 20 usuarios.
- Buscar por email parcial.
- Buscar por app name parcial.
- Seleccionar resultado y enviar form.

## Definition of Done

- Memberships escalan más allá de 20 usuarios/apps sin workaround manual.
