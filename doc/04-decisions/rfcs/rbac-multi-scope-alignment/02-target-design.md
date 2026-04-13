# 02. Target design

## 1. Modelo RBAC por ambito

| Ambito | Catalogo | Asignacion | Lectura principal | Surface de administracion |
|---|---|---|---|---|
| Plataforma | `platform_roles` | `platform_user_roles` | token de plataforma + `GET /api/v1/platform/account/authorization` | `PlatformUserController` extendido a scopes |
| Tenant | `tenant_roles` | `tenant_user_roles` | `GET /api/v1/tenants/{tenantSlug}/account/authorization` | nuevo `TenantRoleController` |
| App | `app_roles` | `app_membership_roles` | token tenant/app + `/api/v1/tenants/{tenantSlug}/account/access` | surface ya existente de app roles |

## 2. Catalogo canonico y reglas de naming

### 2.1 Plataforma

Se propone fijar como catalogo canonico:

- `KEYGO_ADMIN`
- `KEYGO_ACCOUNT_ADMIN`
- `KEYGO_USER`

`KEYGO_ACCOUNT_ADMIN` se mantiene porque el modelo actual ya soporta scopes `CONTRACTOR` y `TENANT`,
por lo que "account admin" representa mejor el alcance real que "tenant admin".

### 2.2 Tenant

- Los codigos de `tenant_roles` deben ser `UPPER_SNAKE_CASE`.
- La capa DB debe terminar alineada con la validacion de dominio, evitando seeds o inserts en
  lowercase para este catalogo.
- La semantica `active` debe definirse de una sola forma: o se persiste fisicamente, o se elimina
  del dominio. Este RFC propone persistirla.

### 2.3 App

- `app_roles` se mantienen con su convension actual (`lowercase`, digitos, `_`, `-`), porque ya es
  una capacidad vigente en schema, dominio y flujos OAuth.
- No se fuerza un rename transversal de app roles dentro de este RFC.

## 3. Contratos HTTP objetivo

### 3.1 Platform role assignments con scope explicito

`POST /api/v1/platform/users/{userId}/platform-roles`

```json
{
  "role_code": "KEYGO_ACCOUNT_ADMIN",
  "scope_type": "CONTRACTOR",
  "contractor_id": "uuid",
  "tenant_id": null
}
```

Reglas:

1. `GLOBAL` exige `contractor_id = null` y `tenant_id = null`.
2. `CONTRACTOR` exige `contractor_id`.
3. `TENANT` exige `tenant_id`.
4. El `GET` actual mantiene backward compatibility y sigue devolviendo el contexto resumido.

### 3.2 Tenant RBAC como first-class API

Se propone publicar, al menos:

1. `GET /api/v1/tenants/{tenantSlug}/roles`
2. `POST /api/v1/tenants/{tenantSlug}/roles`
3. `PATCH /api/v1/tenants/{tenantSlug}/roles/{roleId}`
4. `DELETE /api/v1/tenants/{tenantSlug}/roles/{roleId}`
5. `GET /api/v1/tenants/{tenantSlug}/users/{tenantUserId}/roles`
6. `POST /api/v1/tenants/{tenantSlug}/users/{tenantUserId}/roles`
7. `DELETE /api/v1/tenants/{tenantSlug}/users/{tenantUserId}/roles/{roleId}`

La autorizacion debe aceptar:

- `KEYGO_ADMIN` como override global;
- `KEYGO_ACCOUNT_ADMIN` scoped al contractor/tenant correspondiente;
- roles tenant explicitos una vez exista la lectura efectiva consolidada.

### 3.3 Read models de autorizacion

En vez de un unico `/me/authorization` global, se proponen dos lecturas alineadas al diseño
actual de superficies:

1. `GET /api/v1/platform/account/authorization`
2. `GET /api/v1/tenants/{tenantSlug}/account/authorization`

La vista tenant puede convivir con el actual `/account/access`:

- `/account/access` sigue siendo una vista de memberships de app;
- `/account/authorization` pasa a ser la vista de capacidades administrativas y RBAC efectivo en ese
  contexto.

## 4. Jerarquias

### 4.1 Platform hierarchy

Se mantiene inicialmente como catalogo interno/seedeado. No se expone CRUD publico en esta fase.

### 4.2 Tenant hierarchy

Se habilita solo despues de que exista CRUD basico de tenant roles y lectura efectiva. No conviene
publicar jerarquia mientras todavia falte la superficie principal de roles y asignaciones.

### 4.3 App hierarchy

Permanece como la capacidad ya existente y no se regresa.

## 5. Compatibilidad transitoria

Durante la migracion de naming:

1. los controladores y guards pueden aceptar aliases legacy para no romper clientes internos;
2. los JWT nuevos deben emitirse con el catalogo canonico;
3. la documentacion funcional y frontend debe explicitar la equivalencia temporal;
4. la fase final elimina los aliases de seguridad y tests una vez toda la cadena quede normalizada.
