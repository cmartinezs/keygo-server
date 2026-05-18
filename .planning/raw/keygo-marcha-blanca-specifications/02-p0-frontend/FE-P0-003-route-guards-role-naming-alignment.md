# SPEC Frontend P0 — Alinear route guards a roles administrativos definitivos

| Campo | Valor |
|---|---|
| ID | `FE-P0-003` |
| Tipo | Frontend specification |
| Prioridad | P0 / Bloqueante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Auth guards, routing, permissions |
| Estado | Propuesta para implementación |

## Problema

La UI permite rutas de tenant console para `keygo_admin` y `keygo_account_admin`, pero el backend podría exigir `KEYGO_TENANT_ADMIN` en algunos controllers. Esto genera riesgo de rutas visibles que fallan con 403.

## Dependencia

Esta specification depende de `BE-P0-001`. El rol definitivo debe ser `KEYGO_ACCOUNT_ADMIN`.

## Decisión funcional

Frontend debe usar la misma nomenclatura definitiva que backend:

- `KEYGO_ADMIN`
- `KEYGO_ACCOUNT_ADMIN`
- `KEYGO_USER`

Si internamente la UI representa roles en lowercase, debe existir normalización centralizada, no comparaciones dispersas.

## Alcance incluido

- Revisar route guards.
- Revisar helpers de roles.
- Revisar claims parseados del token.
- Revisar menús laterales/acciones visibles por rol.
- Agregar tests de acceso a rutas críticas.

## Fuera de alcance

- Cambiar backend.
- Crear sistema ABAC/permisos granulares.

## Instrucciones para AI Agent

1. Buscar en frontend:
   - `keygo_admin`
   - `keygo_account_admin`
   - `keygo_user`
   - `keygo_tenant_admin`
   - `KEYGO_TENANT_ADMIN`
2. Crear o usar helper central:

```ts
export type KeyGoRole = 'KEYGO_ADMIN' | 'KEYGO_ACCOUNT_ADMIN' | 'KEYGO_USER';

export function normalizeRole(role: string): KeyGoRole | null {
  const value = role.trim().toUpperCase();
  if (value === 'KEYGO_ADMIN') return 'KEYGO_ADMIN';
  if (value === 'KEYGO_ACCOUNT_ADMIN') return 'KEYGO_ACCOUNT_ADMIN';
  if (value === 'KEYGO_USER') return 'KEYGO_USER';
  return null;
}
```

3. Reemplazar comparaciones manuales por helper.
4. Asegurar que rutas tenant console permitan `KEYGO_ADMIN` y `KEYGO_ACCOUNT_ADMIN`.
5. Asegurar que acciones administrativas no se muestren a `KEYGO_USER`.

## Criterios de aceptación

- No quedan comparaciones dispersas con nombres antiguos.
- Tenant console se muestra a `KEYGO_ACCOUNT_ADMIN`.
- UI no muestra acciones admin a `KEYGO_USER`.
- La nomenclatura coincide con backend.

## Pruebas sugeridas

- Unit test de `normalizeRole`.
- Route guard test para cada rol.
- Snapshot o render test de menú según rol.

## Definition of Done

- La UI no induce al usuario a rutas que backend rechaza por naming inconsistente.
