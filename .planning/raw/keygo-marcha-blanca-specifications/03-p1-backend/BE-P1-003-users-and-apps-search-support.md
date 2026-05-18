# SPEC Backend P1 — Soporte de búsqueda remota para usuarios y apps

| Campo | Valor |
|---|---|
| ID | `BE-P1-003` |
| Tipo | Backend specification |
| Prioridad | P1 / Importante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / users/apps modules |
| Módulo sugerido | List/search endpoints |
| Estado | Propuesta para implementación |

## Problema

La UI de memberships carga solo primera página de usuarios y apps con tamaño 20. Si existen más de 20, no todos se pueden seleccionar.

## Decisión funcional

Backend debe soportar búsqueda paginada por texto para usuarios y apps, de modo que frontend use autocomplete remoto.

## Endpoints esperados

Puede reutilizar endpoints existentes de listado agregando query param `q`.

```http
GET /api/v1/tenants/{tenantSlug}/users?q=ana&page=0&size=10
GET /api/v1/tenants/{tenantSlug}/apps?q=portal&page=0&size=10
```

## Alcance incluido

- Búsqueda por email/nombre para usuarios.
- Búsqueda por name/client_id para apps.
- Paginación estable.
- Orden consistente.
- Aislamiento por tenant.

## Fuera de alcance

- Búsqueda full-text compleja.
- Indexación externa.
- Autocomplete fuzzy avanzado.

## Instrucciones para AI Agent

1. Revisar endpoints actuales listUsers/listClientApps.
2. Agregar filtro opcional `q` si no existe.
3. Implementar búsqueda case-insensitive.
4. Mantener paginación existente.
5. Agregar índices DB si la tabla ya lo amerita; para marcha blanca puede quedar documentado.
6. Agregar tests.

## Criterios de aceptación

- Búsqueda usuarios por email parcial funciona.
- Búsqueda usuarios por nombre parcial funciona si existe nombre.
- Búsqueda apps por nombre/client_id funciona.
- Resultado respeta tenant.
- Frontend puede obtener más de los primeros 20 registros.

## Pruebas sugeridas

- `q=ana` encuentra `ana@empresa.cl`.
- `q=Portal` encuentra `Portal Clientes`.
- `q` sin resultados devuelve página vacía.
- Usuario/app de otro tenant no aparece.

## Definition of Done

- Membership form puede usar autocomplete remoto sin limitarse a primera página.
