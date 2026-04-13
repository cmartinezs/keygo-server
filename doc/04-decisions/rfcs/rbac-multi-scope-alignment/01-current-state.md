# 01. Current state

## 1. Que partes de T-111 ya quedaron absorbidas

| Idea original T-111 | Estado actual |
|---|---|
| `platform_roles` + `platform_user_roles` | Ya existen en `V4__platform_rbac.sql`, con `scope_type`, `contractor_id`, `tenant_id` y jerarquia |
| `tenant_roles` + `tenant_user_roles` | Ya existen en `V6__tenant_users_and_rbac.sql`, junto a `tenant_role_hierarchy` |
| `app_roles` como tercer ambito | Ya existe en `V8__app_rbac_and_memberships.sql`, con `app_role_hierarchy` y asignacion sobre `app_membership_roles` |
| Modelos dominio para platform/tenant roles | Ya existen en `keygo-domain` (`PlatformRole`, `PlatformUserRole`, `TenantRole`, `TenantUserRole`) |
| Repositorios/adaptadores JPA | Ya existen en `keygo-supabase` para platform y tenant RBAC |
| Casos de uso base para platform/tenant RBAC | Ya existen en `keygo-app`, al menos para assign/revoke/list/create |
| Surface API de platform RBAC | Ya existe en `PlatformUserController` para listar, asignar y revocar roles de plataforma |

## 2. Gaps reales del estado vigente

### 2.1 Drift del catalogo platform RBAC

Hoy conviven al menos cuatro variantes para el mismo universo:

- baseline y seeds: `KEYGO_ADMIN`, `KEYGO_ACCOUNT_ADMIN`, `KEYGO_USER`;
- dominio/use cases legacy: `keygo_admin`, `keygo_tenant_admin`, `keygo_user`;
- seguridad/controladores: `KEYGO_ADMIN`, `KEYGO_TENANT_ADMIN`;
- documentacion funcional: mezcla de lowercase y uppercase.

Esto ya no es una diferencia cosmetica. Hay codigo productivo de billing que intenta asignar
`keygo_tenant_admin`, mientras el baseline vigente siembra `KEYGO_ACCOUNT_ADMIN`.

### 2.2 Escritura incompleta para platform role assignments

El schema de `platform_user_roles` ya soporta asignaciones:

- `GLOBAL`
- `CONTRACTOR`
- `TENANT`

Sin embargo, el contrato publico solo recibe `roleCode` y el adapter actual persiste la asignacion
sin exponer `scope_type`, `contractor_id` ni `tenant_id`. La lectura ya muestra esos campos, por lo
que la API quedo asimetrica.

### 2.3 Tenant RBAC sin superficie HTTP canonica

La documentacion frontend ya declara `GET/POST /api/v1/tenants/{tenantSlug}/roles`, pero en
`keygo-api` no aparece un controlador equivalente para roles de tenant ni para asignaciones
`tenant_user_roles`.

Consecuencia: el modelo existe en DDL y en capa app, pero no hay una superficie canonica para que
UI o admin console lo operen.

### 2.4 Casos de uso de tenant RBAC con resolucion incorrecta

`AssignTenantRoleUseCase` y `RevokeTenantRoleUseCase` consultan `TenantRoleRepositoryPort.findByTenantId(...)`
pasando `tenantUserId`. Eso mezcla identidad de usuario con scope de tenant, y deja la validacion
de integridad semantica incompleta aunque la persistencia final use FKs correctas.

### 2.5 Jerarquias desbalanceadas entre DB y aplicacion

El baseline ya modela:

- `platform_role_hierarchy`
- `tenant_role_hierarchy`
- `app_role_hierarchy`

Pero la capa de aplicacion solo tiene una superficie clara para jerarquia de app. Platform y tenant
RBAC todavia no tienen una politica operativa uniforme: o se administran explicitamente, o se
declaran como catalogos internos y seeded.

### 2.6 No existe un read model de autorizacion efectiva por contexto

Hoy existen dos vistas parciales:

- platform JWT que carga roles desde `platform_user_roles`;
- `/api/v1/tenants/{tenantSlug}/account/access`, que solo devuelve apps + roles de app.

Falta una lectura canonica que responda, por contexto, que permisos administrativos tiene una
persona en plataforma y en un tenant, sin mezclar eso con memberships de app.

### 2.7 Drift dominio/JPA en tenant roles

`TenantRole` expone `active`, pero `TenantRoleEntity` lo maneja como `@Transient`; el baseline no
persiste ese atributo. En la practica, la semantica de activacion esta declarada en dominio pero no
existe como invariantes del schema.

## 3. Lectura sintetica

T-111 ya no debe interpretarse como "crear tablas faltantes". El trabajo pendiente es:

1. normalizar el catalogo y los nombres;
2. cerrar la diferencia entre lo que el schema soporta y lo que la API publica;
3. exponer tenant RBAC como superficie real;
4. agregar lectura de autorizacion efectiva;
5. resolver drift entre dominio, seguridad, seeds y documentacion.
