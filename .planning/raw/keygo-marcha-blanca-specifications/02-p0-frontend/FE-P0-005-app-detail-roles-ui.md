# SPEC Frontend P0 — Pantalla Detalle de App con gestión básica de roles

| Campo | Valor |
|---|---|
| ID | `FE-P0-005` |
| Tipo | Frontend specification |
| Prioridad | P0/P1 / Necesario antes de vender RBAC por app |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Apps detail, App Roles |
| Estado | Propuesta para implementación |

## Problema

Backend ya expone roles por app, incluso jerarquía, y frontend tiene API functions como `listAppRoles` y `createAppRole`, pero la página de apps no ofrece una gestión completa y clara de roles por aplicación.

## Decisión funcional

Crear o completar pantalla **Detalle de App → Roles** para listar y crear roles básicos de app antes de gestionar memberships.

## Alcance incluido

- Crear vista detalle de app con tabs.
- Agregar tab `Roles`.
- Listar roles existentes.
- Crear rol con `code`, `name/display_name` y estado/default si existe.
- Mostrar jerarquía de forma básica si endpoint ya existe.

## Fuera de alcance

- Editor visual avanzado de jerarquía.
- Matriz completa de permisos.
- ABAC o policies complejas.

## Mockup base

```text
┌───────────────────────────────────────────────┐
│ Roles de Portal Clientes                      │
├───────────────────────────────────────────────┤
│ [Crear rol]                                   │
│                                               │
│ Código       Nombre          Default Acciones │
│ ADMIN        Administrador   No      Editar   │
│ USER         Usuario         Sí      Editar   │
│ VIEWER       Lector          No      Editar   │
│                                               │
│ Jerarquía                                     │
│ ADMIN > USER > VIEWER                         │
└───────────────────────────────────────────────┘
```

## Instrucciones para AI Agent

1. Localizar routing de apps.
2. Si no existe detalle de app, crear ruta:

```text
/tenant/:tenantSlug/apps/:clientAppId
```

3. Crear layout con tabs:
   - General,
   - OAuth,
   - Roles,
   - Acceso,
   - Secret,
   - Danger.
4. Implementar inicialmente tab Roles.
5. Usar API existente `listAppRoles` y `createAppRole` si está disponible.
6. Validar `code`:
   - uppercase recomendado,
   - sin espacios,
   - único por app.
7. Mostrar errores de backend.
8. Agregar loading, empty state y error state.

## Criterios de aceptación

- Tenant admin puede entrar al detalle de una app.
- Tenant admin puede listar roles de esa app.
- Tenant admin puede crear un rol básico.
- La UI permite usar roles creados luego en memberships.
- No se presenta RBAC como funcional si no puede configurarse desde UI.

## Pruebas sugeridas

- Render roles list.
- Empty state sin roles.
- Crear rol exitoso actualiza listado.
- Error de rol duplicado se muestra al usuario.

## Definition of Done

- Existe superficie mínima para configurar roles por app desde UI.
