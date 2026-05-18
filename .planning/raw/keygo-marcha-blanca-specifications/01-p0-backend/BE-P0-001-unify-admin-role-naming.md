# SPEC Backend P0 — Unificar nomenclatura de roles administrativos

| Campo | Valor |
|---|---|
| ID | `BE-P0-001` |
| Tipo | Backend specification |
| Prioridad | P0 / Bloqueante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / módulos backend |
| Módulo sugerido | Security, authorization, controllers |
| Estado | Propuesta para implementación |

## Problema

Existe inconsistencia entre roles administrativos usados por la UI y roles exigidos por algunos controllers backend. La UI maneja `keygo_admin`, `keygo_account_admin`, `keygo_user`, mientras el backend define `KEYGO_ADMIN`, `KEYGO_ACCOUNT_ADMIN`, `KEYGO_USER`, pero algunos controllers protegen con `KEYGO_TENANT_ADMIN`.

Esto puede provocar que un usuario `KEYGO_ACCOUNT_ADMIN` vea rutas en frontend, pero reciba `403 Forbidden` en backend.

## Decisión funcional

Usar `KEYGO_ACCOUNT_ADMIN` como rol administrativo del tenant/account en todo KeyGo.

`KEYGO_TENANT_ADMIN` debe ser eliminado, migrado o tratado como alias temporal solo si ya existen datos persistidos con ese valor.

## Alcance incluido

- Revisar enums, authorities, annotations, security matchers y validaciones manuales.
- Reemplazar usos de `KEYGO_TENANT_ADMIN` por `KEYGO_ACCOUNT_ADMIN`.
- Garantizar compatibilidad con tokens emitidos por KeyGo.
- Agregar tests de autorización para endpoints protegidos.

## Fuera de alcance

- Rediseñar todo el modelo RBAC.
- Agregar permisos granulares nuevos.
- Cambiar roles de app cliente.

## Instrucciones para AI Agent

1. Buscar en el backend todos los usos de:
   - `KEYGO_TENANT_ADMIN`
   - `TENANT_ADMIN`
   - `keygo_tenant_admin`
   - `KEYGO_ACCOUNT_ADMIN`
   - `keygo_account_admin`
2. Identificar si `KEYGO_TENANT_ADMIN` existe en:
   - enum,
   - base de datos,
   - migraciones,
   - seeds,
   - tests,
   - security annotations,
   - JWT claims.
3. Definir una estrategia:
   - si no hay datos persistidos: eliminar `KEYGO_TENANT_ADMIN` y reemplazar por `KEYGO_ACCOUNT_ADMIN`;
   - si hay datos persistidos: crear migración para convertirlo o alias temporal con deprecación explícita.
4. Actualizar controllers para aceptar:
   - `KEYGO_ADMIN` en rutas globales/ops;
   - `KEYGO_ACCOUNT_ADMIN` en tenant console;
   - `KEYGO_USER` solo donde aplique self-service.
5. Actualizar tests.

## Contrato esperado

Un usuario con rol `KEYGO_ACCOUNT_ADMIN` debe poder consumir endpoints administrativos de su tenant, incluyendo:

- gestión de apps,
- gestión de usuarios,
- gestión de memberships,
- gestión de roles de app.

Un usuario con solo `KEYGO_USER` no debe poder consumir esos endpoints.

## Criterios de aceptación

- No quedan referencias activas a `KEYGO_TENANT_ADMIN` salvo alias/migración documentada.
- Los controllers de tenant console aceptan `KEYGO_ACCOUNT_ADMIN`.
- Un token con `KEYGO_ACCOUNT_ADMIN` no recibe 403 por nomenclatura inconsistente.
- Los tests cubren al menos:
  - acceso permitido para `KEYGO_ACCOUNT_ADMIN`,
  - acceso permitido para `KEYGO_ADMIN` cuando corresponda,
  - acceso rechazado para `KEYGO_USER`,
  - acceso rechazado sin autenticación.

## Pruebas sugeridas

- Test unitario de conversión de roles/authorities.
- Test de seguridad web por controller crítico.
- Test de integración con JWT mock que contenga `KEYGO_ACCOUNT_ADMIN`.

## Definition of Done

- Compila backend.
- Pasa suite de seguridad.
- No hay 403 inesperado para tenant admin/account admin.
- README o documentación de roles queda actualizada si existe.
