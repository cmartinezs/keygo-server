# SPEC Backend P0 — DTO de memberships con roles legibles y fecha válida

| Campo | Valor |
|---|---|
| ID | `BE-P0-002` |
| Tipo | Backend specification |
| Prioridad | P0 / Bloqueante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / módulos backend |
| Módulo sugerido | Memberships, DTOs, mappers |
| Estado | Propuesta para implementación |

## Problema

El backend devuelve memberships con `roleIds` y en algunos flujos `createdAt(null)` o sin fecha seteada. Esto obliga a la UI a mostrar UUIDs de roles y fechas inválidas, generando una experiencia no operable para tenant admins.

## Decisión funcional

El backend debe devolver memberships con roles legibles y `created_at` real.

## Contrato esperado

### Respuesta sugerida

```json
{
  "membership_id": "mem_123",
  "tenant_id": "tenant_123",
  "client_app": {
    "id": "app_123",
    "client_id": "kg_app_abc123",
    "name": "Portal Clientes"
  },
  "user": {
    "id": "user_123",
    "email": "ana@empresa.cl",
    "display_name": "Ana Pérez"
  },
  "status": "ACTIVE",
  "roles": [
    {
      "id": "role_1",
      "code": "USER",
      "display_name": "Usuario"
    },
    {
      "id": "role_2",
      "code": "VIEWER",
      "display_name": "Lector"
    }
  ],
  "created_at": "2026-05-17T12:30:00Z"
}
```

## Alcance incluido

- Actualizar DTO de membership para exponer `roles` como objetos.
- Mantener `roleIds` solo si hay compatibilidad necesaria, pero marcarlo como legacy/deprecated.
- Poblar `created_at` desde entidad persistida.
- Revisar respuestas de:
  - crear membership,
  - listar memberships por usuario,
  - listar memberships por app,
  - aprobar membership,
  - revocar membership.

## Fuera de alcance

- Rediseñar tablas de roles.
- Implementar permisos granulares.
- Crear UI.

## Instrucciones para AI Agent

1. Buscar DTOs relacionados con memberships.
2. Identificar mapper actual que llena `roleIds` y `createdAt`.
3. Agregar DTO de rol resumido:

```java
public record MembershipRoleData(
    UUID id,
    String code,
    String displayName
) {}
```

4. Agregar `List<MembershipRoleData> roles` al DTO de membership.
5. Asegurar que `createdAt` se pobla desde entidad o auditable base.
6. Corregir el caso donde creación de membership devuelve `createdAt(null)`.
7. Actualizar tests de serialización/mapping.
8. Actualizar documentación API si existe.

## Criterios de aceptación

- Las memberships ya no obligan a la UI a mostrar UUIDs como rol visible.
- `created_at` nunca es `null` en memberships persistidas.
- El listado por usuario y por app devuelve roles con `code` y `display_name`.
- El contrato es consistente en create/list/approve/revoke.

## Pruebas sugeridas

- Mapper test: entidad con dos roles devuelve DTO con dos roles legibles.
- Integration test: crear membership y validar `created_at` no nulo.
- Contract test: response JSON contiene `roles[].code`, `roles[].display_name` y `created_at`.

## Definition of Done

- Backend compila.
- Tests pasan.
- La UI puede renderizar roles sin lookup adicional obligatorio.
