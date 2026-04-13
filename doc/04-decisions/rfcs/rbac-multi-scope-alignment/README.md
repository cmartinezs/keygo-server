# RFC: RBAC Multi-Scope Alignment

> **Estado:** DRAFT  
> **Origen:** Relectura del plan historico `doc/99-archive/historical-plans/documentation-initiative-2026/implementacion/T-111/`  
> **Modulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-supabase`, `keygo-run`, `doc/`  
> **Migracion Flyway:** Probable `V20` si se aprueba la normalizacion del catalogo tenant y la persistencia de `tenant_roles.active`

## Objetivo

Cerrar la brecha entre el plan historico T-111 y el estado real del backend. El schema activo ya
materializo la separacion RBAC por plataforma, tenant y app, pero la capa de aplicacion, los
contratos HTTP y parte de la documentacion siguen mezclando naming, alcances y superficies
incompletas.

Este RFC no revive T-111 literalmente. Reusa sus ideas vigentes y las reencuadra sobre:

1. el baseline Flyway activo (`V1`-`V19`);
2. los flujos actuales de autenticacion plataforma/tenant;
3. la separacion ya existente entre `platform_users`, `tenant_users` y `app_memberships`;
4. las integraciones UI/API ya publicadas recientemente para platform RBAC.

## Documentos

1. [01-current-state.md](01-current-state.md) - que absorbio T-111 y donde sigue habiendo drift
2. [02-target-design.md](02-target-design.md) - decisiones objetivo para catalogos, APIs y lectura de autorizacion
3. [03-implementation-plan.md](03-implementation-plan.md) - fases propuestas para cerrar la alineacion
4. [04-impact-and-acceptance.md](04-impact-and-acceptance.md) - impacto por modulo, docs afectadas y criterios de aceptacion

## Referencia cruzada

- Plan historico relacionado: [T-111/INTEGRATION_PLAN.md](../../../99-archive/historical-plans/documentation-initiative-2026/implementacion/T-111/INTEGRATION_PLAN.md)

## Decisiones clave propuestas

| Tema | Decision propuesta | Motivo |
|---|---|---|
| Catalogo platform RBAC | Mantener `KEYGO_ADMIN`, `KEYGO_ACCOUNT_ADMIN`, `KEYGO_USER` como codigos canonicos | Es lo que ya refleja el baseline fisico y modela mejor el scope contractor/tenant que `KEYGO_TENANT_ADMIN` |
| Alias legacy | Tratar `KEYGO_TENANT_ADMIN`, `keygo_tenant_admin`, `keygo_user`, `ADMIN` y `ADMIN_TENANT` como deuda de transicion, no como canon | Hoy hay drift entre seeds, controladores, use cases, tests y docs |
| Escritura de platform roles | Extender la mutacion para soportar `scope_type`, `contractor_id` y `tenant_id` | El schema ya soporta asignaciones scoped; la API publica aun no |
| Tenant RBAC | Exponer CRUD/assign/revoke/list para roles de tenant y sus asignaciones | La documentacion frontend ya declara esta superficie, pero `keygo-api` no la publica |
| Lectura de autorizacion efectiva | Mantener `/account/access` como vista de memberships y agregar read models de autorizacion por contexto | T-111 apuntaba a autorizacion efectiva, pero hoy la plataforma y el tenant tienen flujos distintos |
| Jerarquias | Mantener jerarquia de app como capacidad ya expuesta; incorporar soporte de aplicacion para platform/tenant solo donde agregue valor operativo | Las tablas ya existen, pero la capa app aun no las gobierna de forma uniforme |

## Relacion con T-111

T-111 sigue siendo valioso como antecedente porque identifico correctamente la necesidad de
separar RBAC por ambito. Lo que quedo obsoleto es su supuesto de partida: ya no estamos frente a
un sistema con solo `AppRole`; hoy el problema principal es de alineacion entre modelo, casos de
uso, seguridad y contratos.
