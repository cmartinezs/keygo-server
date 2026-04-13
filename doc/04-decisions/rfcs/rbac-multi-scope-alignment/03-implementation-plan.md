# 03. Implementation plan

## Fase A - Canonicalizar catalogos y persistencia minima

### Objetivo

Eliminar drift entre baseline, dominio, flows de billing y seguridad.

### Cambios esperados

1. Definir un unico catalogo platform RBAC en dominio, app, API, tests y docs.
2. Ajustar flows que aun usan `KEYGO_TENANT_ADMIN` o lowercase `keygo_*`.
3. Crear `V20` si se aprueba:
   - persistencia real de `tenant_roles.active`;
   - normalizacion de codigos `tenant_roles`;
   - constraints/comentarios que congelen la convension resultante.

## Fase B - Corregir la capa app para tenant RBAC

### Objetivo

Resolver la validacion semantica antes de abrir la superficie HTTP.

### Cambios esperados

1. `AssignTenantRoleUseCase` y `RevokeTenantRoleUseCase` deben validar usando `tenantId` real, no
   `tenantUserId` como si fuera tenant.
2. Introducir una resolucion explicita `tenantUser -> tenant` antes de consultar `tenant_roles`.
3. Decidir si `TenantRole.active` queda soportado por DB o sale del dominio.

## Fase C - Publicar tenant RBAC API

### Objetivo

Hacer operable el RBAC de tenant que hoy ya existe en schema y capa app.

### Cambios esperados

1. Crear controlador y DTOs para roles de tenant.
2. Crear surface de asignacion y revocacion de `tenant_user_roles`.
3. Agregar OpenAPI, respuestas `BaseResponse<T>` y errores canonicos.
4. Actualizar frontend docs porque esa superficie ya esta declarada pero todavia no coincide con la
   implementacion real.

## Fase D - Completar escritura scoped de platform roles

### Objetivo

Alinear la mutacion con la capacidad ya existente del schema.

### Cambios esperados

1. Extender request DTO y command para `scope_type`, `contractor_id`, `tenant_id`.
2. Hacer que `PlatformUserRoleRepositoryAdapter` persista los campos de scope.
3. Validar combinaciones invalidas en app/API antes de llegar a JPA.

## Fase E - Read models de autorizacion efectiva

### Objetivo

Separar claramente:

- acceso funcional a apps;
- autorizacion administrativa de plataforma;
- autorizacion organizacional de tenant.

### Cambios esperados

1. Nuevo `GET /api/v1/platform/account/authorization`.
2. Nuevo `GET /api/v1/tenants/{tenantSlug}/account/authorization`.
3. Mantener `/account/access` como contrato estable para memberships.
4. Definir payloads que expongan catalogo, asignaciones, scopes y herencia efectiva donde aplique.

## Fase F - Seguridad, tests y documentacion

### Objetivo

Cerrar el RFC sin dejar aliases o documentos canonicos desalineados.

### Cambios esperados

1. Actualizar `@PreAuthorize` y cualquier mapeo de autoridades legacy.
2. Corregir tests que aun dependen de `ADMIN`, `ADMIN_TENANT` o `KEYGO_TENANT_ADMIN` cuando ya no
   representen el canon.
3. Actualizar:
   - `doc/02-functional/authentication-flow.md`
   - `doc/02-functional/frontend/06-endpoints-tenant.md`
   - `doc/02-functional/frontend/08-endpoints-admin.md`
   - `doc/03-architecture/authorization-patterns.md`
   - `doc/03-architecture/database-schema.md`
   - `doc/08-reference/data/migrations.md`

## Orden sugerido

```text
Fase A -> Fase B -> Fase C -> Fase D -> Fase E -> Fase F
```

Razon:

- primero se fija el catalogo y el significado;
- luego se corrige la semantica de la capa app;
- despues se publica la API faltante;
- y recien entonces se agrega la lectura efectiva y se limpia la seguridad/documentacion.

## Tareas derivadas anticipadas

Algunas partes del RFC se implementan como tareas independientes antes de que el RFC sea aprobado
formalmente, para desbloquear la UI lo antes posible.

| Tarea | Fase RFC relacionada | Estado |
|---|---|---|
| [T-146 — GET /platform/roles](../../../09-ai/tasks/T-146-platform-roles-catalog-endpoint.md) | Pre-Fase A (habilita UI de asignacion de roles) | ⬜ Registrada |
