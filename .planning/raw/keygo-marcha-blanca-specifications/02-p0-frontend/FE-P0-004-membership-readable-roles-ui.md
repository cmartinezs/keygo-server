# SPEC Frontend P0 — Mostrar memberships con roles legibles y fecha válida

| Campo | Valor |
|---|---|
| ID | `FE-P0-004` |
| Tipo | Frontend specification |
| Prioridad | P0 / Bloqueante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Memberships UI |
| Estado | Propuesta para implementación |

## Problema

La UI lista memberships mostrando `role_ids` y `created_at`. Si backend devuelve solo UUIDs de roles o fecha nula, la administración se vuelve poco operable.

## Dependencia

Depende de `BE-P0-002`, que debe entregar `roles: [{ id, code, display_name }]` y `created_at` real.

## Decisión funcional

La UI debe mostrar roles como chips legibles usando `code` y/o `display_name`, nunca UUIDs como dato principal para usuario final.

## Alcance incluido

- Actualizar types de membership.
- Renderizar roles legibles.
- Renderizar fecha válida.
- Manejar compatibilidad temporal si aún llega `roleIds`.
- Mostrar fallback controlado si no hay roles.

## Fuera de alcance

- Crear roles de app.
- Rediseñar todo el módulo memberships.

## Contrato esperado en frontend

```ts
export interface MembershipRoleView {
  id: string;
  code: string;
  display_name?: string;
  displayName?: string;
}

export interface MembershipView {
  membership_id: string;
  status: 'ACTIVE' | 'PENDING' | 'INVITED' | 'SUSPENDED' | 'REVOKED';
  roles: MembershipRoleView[];
  created_at: string;
}
```

## Instrucciones para AI Agent

1. Localizar página/listado de memberships.
2. Actualizar types según response backend.
3. Crear mapper tolerant si backend usa camelCase:

```ts
const roleLabel = role.display_name ?? role.displayName ?? role.code;
```

4. Mostrar roles como chips:
   - etiqueta principal: `display_name` si existe;
   - fallback: `code`;
   - no mostrar UUID salvo tooltip técnico opcional.
5. Formatear fecha con utilidad existente.
6. Si `created_at` es nulo, mostrar “Fecha no disponible” y reportar warning no bloqueante.

## Criterios de aceptación

- Memberships muestran app, estado, roles legibles y fecha válida.
- No se muestran UUIDs como roles visibles.
- UI soporta memberships sin roles sin romper render.
- UI soporta transición temporal entre `display_name` y `displayName`.

## Pruebas sugeridas

- Render membership con roles USER/VIEWER.
- Render membership sin roles.
- Render con fecha válida.
- No renderizar `role_id` como texto principal.

## Definition of Done

- Tenant admin puede interpretar qué acceso tiene cada usuario sin revisar IDs técnicos.
