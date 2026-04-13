# 04. Impact and acceptance

## 1. Impacto por modulo

| Modulo | Impacto esperado |
|---|---|
| `keygo-domain` | normalizacion de codigos canonicos, decision final sobre `TenantRole.active`, nuevos read models/VOs de autorizacion |
| `keygo-app` | correccion de use cases tenant RBAC, commands scope-aware para platform RBAC, queries de autorizacion efectiva |
| `keygo-api` | nuevo `TenantRoleController`, ampliacion de `PlatformUserController`, endpoints `/account/authorization`, OpenAPI |
| `keygo-supabase` | ajuste de adapters/repositories, posible `V20`, alineacion entity/domain para tenant roles |
| `keygo-run` | limpieza de guards/roles legacy y pruebas del filtro/seguridad |
| `doc/` | actualizacion de contratos frontend, auth flow, arquitectura y referencia de migraciones |

## 2. Artefactos de documentacion que deben quedar alineados

1. `doc/02-functional/authentication-flow.md`
2. `doc/02-functional/frontend/06-endpoints-tenant.md`
3. `doc/02-functional/frontend/08-endpoints-admin.md`
4. `doc/03-architecture/authorization-patterns.md`
5. `doc/03-architecture/database-schema.md`
6. `doc/08-reference/data/migrations.md`

## 3. Criterios de aceptacion

El RFC se considerara materializado cuando:

1. exista un catalogo platform RBAC unico y trazable desde DB, dominio, seguridad y docs;
2. los flows de billing/plataforma no intenten asignar codigos que no existan en `platform_roles`;
3. tenant RBAC tenga surface HTTP canonica para CRUD y asignaciones;
4. los use cases tenant RBAC validen por tenant real, no por `tenantUserId` reutilizado;
5. la escritura de platform roles permita expresar `GLOBAL`, `CONTRACTOR` y `TENANT`;
6. exista al menos una lectura de autorizacion efectiva para plataforma y una para tenant;
7. la documentacion frontend deje de declarar endpoints inexistentes o naming legacy sin aclaracion;
8. las pruebas de seguridad usen el catalogo canonico acordado.

## 4. Riesgos a vigilar

| Riesgo | Mitigacion |
|---|---|
| Romper integraciones internas que aun usan aliases legacy | Mantener ventana de compatibilidad explicita y documentada antes de remover aliases |
| Mezclar roles tenant con roles app | Mantener `/account/access` separado de `/account/authorization` |
| Introducir otra ronda de rename incompleta | Fijar el catalogo canonico antes de tocar guards, docs o flows de negocio |
| Abrir CRUD de jerarquias antes de tener base estable | Postergar jerarquia tenant/platform hasta cerrar CRUD y lectura efectiva |
