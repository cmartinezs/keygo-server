# Migraciones Flyway — KeyGo Server

> Última actualización: 2026-04-18  
> Baseline vigente: **V1–V24**  
> Baseline anterior archivado en: `keygo-supabase/src/main/resources/db/backup_20260409_v33/`  
> Próxima migración disponible: **V31**

## Estrategia

- Ubicación activa: `keygo-supabase/src/main/resources/db/migration/`
- Motor: PostgreSQL
- Flyway gestiona el schema completo
- `hibernate.ddl-auto=validate` sigue siendo la política esperada del proyecto
- Las consultas analíticas viven fuera de Flyway en `sql/platform_dashboards/`
- La vista arquitectónica sincronizada del baseline vive en `doc/03-architecture/database-schema.md`

## Convención

```text
V{n}__{descripcion}.sql
```

- Numeración secuencial y limpia
- Migraciones agrupadas por dominio
- Sin parches correctivos mezclados en el baseline
- Constraints, índices y comentarios explícitos

## Baseline actual

| Versión | Archivo | Dominio | Propósito |
|---|---|---|---|
| V1 | `V1__drop_all.sql` | Bootstrap | Limpieza total del schema operativo, excluyendo `flyway_schema_history` |
| V2 | `V2__foundation.sql` | Bootstrap | Extensiones `pgcrypto` y `citext`, trigger `update_updated_at_column()`, helper append-only |
| V3 | `V3__platform_users.sql` | Identity | Cuenta global `platform_users` |
| V4 | `V4__platform_rbac.sql` | RBAC | `platform_roles`, `platform_role_hierarchy`, `platform_user_roles` |
| V5 | `V5__tenants.sql` | Core | `tenants` y FK de scope TENANT en RBAC de plataforma |
| V6 | `V6__tenant_users_and_rbac.sql` | Core | `tenant_users`, `tenant_roles`, `tenant_role_hierarchy`, `tenant_user_roles` |
| V7 | `V7__client_apps.sql` | OAuth/OIDC | `client_apps`, redirect URIs, grants, scopes |
| V8 | `V8__app_rbac_and_memberships.sql` | Access | `app_roles`, `app_role_hierarchy`, `app_memberships`, `app_membership_roles` |
| V9 | `V9__sessions_oauth_and_keys.sql` | Auth | `platform_sessions`, `oauth_sessions`, `authorization_codes`, `refresh_tokens`, `signing_keys` |
| V10 | `V10__identity_flows_and_activity.sql` | Identity | `email_verifications`, `password_reset_tokens`, preferencias y actividad global |
| V11 | `V11__audit.sql` | Audit | `audit_events`, payloads, tags y links append-only |
| V12 | `V12__contractors.sql` | Billing | `contractors`, `contractor_users`, FK de `tenants.contractor_id` |
| V13 | `V13__billing_catalog.sql` | Billing | catálogo `app_plans*` y entitlements |
| V14 | `V14__billing_contracts_and_subscriptions.sql` | Billing | `app_contracts`, `app_subscriptions` |
| V15 | `V15__billing_operations.sql` | Billing | `payment_transactions`, `invoices`, `usage_counters`, `tenant_billing_profiles`, `payment_methods` |
| V16 | `V16__seed_foundation.sql` | Seed | roles, usuarios, contractor base, tenants, tenant users, apps, memberships |
| V17 | `V17__seed_billing.sql` | Seed | planes, versiones, billing options, entitlements y caso activo de billing |
| V18 | `V18__contract_email_verifications.sql` | Billing | `contract_email_verifications` y flexibilización de `app_contracts.contractor_id` para onboarding previo a provisión |
| V19 | `V19__app_contract_onboarding_snapshot.sql` | Billing | snapshot persistido de contacto y empresa del onboarding dentro de `app_contracts` |
| V20 | `V20__platform_plan_catalog.sql` | Billing | habilita `app_plans.client_app_id = NULL` para catálogo de plataforma y siembra planes públicos de KeyGo |
| V21 | `V21__platform_plan_entitlements_full.sql` | Billing | completa la matriz de entitlements de los 6 planes de plataforma: corrige `period_type` de las 12 filas de V20 e inserta las 61 métricas restantes hasta alcanzar las 73 filas totales (FREE=9, PERSONAL=9, TEAM=9, BUSINESS=9, FLEX=25, ENTERPRISE=12) |
| V22 | `V22__app_contracts_client_app_nullable.sql` | Billing | hace nullable `app_contracts.client_app_id` para soportar contratos de plataforma sin app específica (NULL = plataforma, NOT NULL = app) |
| V23 | `V23__app_subscriptions_client_app_nullable.sql` | Billing | hace nullable `app_subscriptions.client_app_id` para alinear con V22: subscripciones de plataforma sin app específica |
| V24 | `V24__billing_operations_client_app_nullable.sql` | Billing | hace nullable `client_app_id` en `invoices`, `payment_transactions` y `usage_counters` para alinear con V22/V23: operaciones de plataforma sin app específica |
| V25 | `V25__seed_platform_plan_catalog.sql` | Seed | seed del catálogo de planes de plataforma |
| V26 | `V26__seed_platform_contracts_and_subscriptions.sql` | Seed | seed de contratos y suscripciones de plataforma |
| V27 | `V27__seed_contractors_tenants_apps_contracts.sql` | Seed | seed de contractors, tenants adicionales, apps y contratos |
| V28 | `V28__seed_restricted_tenant_no_self_registration.sql` | Seed | seed de tenant restringido sin auto-registro |
| V29 | `V29__backfill_platform_user_roles_keygo_user.sql` | Fix | asigna `KEYGO_USER` (scope GLOBAL) a todos los `platform_users` sin ningún rol de plataforma asignado |
| V30 | `V30__app_roles_is_default.sql` | RBAC | agrega `is_default BOOLEAN` a `app_roles` con unique parcial por app; promueve el primer rol existente como default por retrocompatibilidad |

## Decisiones clave del remake

- La identidad raíz es `platform_users`; `tenant_users` solo representa pertenencia a tenant.
- El acceso a apps se modela con `app_memberships`, no con `tenant_users` directos.
- El RBAC quedó separado en tres ámbitos:
  - plataforma
  - tenant
  - app
- `contractors` quedó ligado a `platform_users`, no a usuarios locales del tenant `keygo`.
- `keygo-ui` existe como client app interna, pero el portal global no depende de ser miembro del tenant `keygo`.
- La integridad multitenant fuerte se aplica con FKs compuestas y triggers de validación.
- Las jerarquías de roles usan árbol simple con un padre por hijo y profundidad máxima cinco.

## Política de identificadores

- La regla general del baseline es:
  - PK técnica UUID para relaciones internas
  - identificador funcional solo cuando el dominio o el protocolo lo requiere
- `platform_users.id`, `tenant_users.id`, `client_apps.id`, `app_memberships.id` y tablas relacionadas se usan para joins internos y FKs.
- `client_apps.client_id` se mantiene como identificador público OAuth/OIDC.
- La unicidad global de `client_apps.client_id` es deliberada:
  - Keygo opera como authorization server compartido
  - `/authorize`, `/token` y validaciones relacionadas deben resolver un cliente sin ambigüedad cross-tenant
  - las relaciones internas siguen usando `client_apps.id`, por lo que no se mezcla el identificador protocolario con las FKs
- Otros identificadores funcionales que coexisten con PK técnica y quedan documentados explícitamente:
  - `platform_users.email`
  - `tenants.slug`
  - `signing_keys.kid`

## Incompatibilidades intencionales respecto del baseline anterior

- `sessions` se reemplaza por `platform_sessions` + `oauth_sessions`
- `memberships` se reemplaza por `app_memberships`
- `membership_roles` se reemplaza por `app_membership_roles`
- `user_notification_preferences` se reemplaza por `platform_user_notification_preferences`
- `verification_codes` se reemplaza por `email_verifications` + `password_reset_tokens`
- `tenant_users` ya no persiste credenciales globales
- `platform_roles` ya no se mezclan con `app_roles` de `keygo-ui`

## Seeds de desarrollo

- Usuarios de plataforma:
  - `admin@keygo.local`
  - `tenant-admin@keygo.local`
  - `user@keygo.local`
  - `contractor@keygo.local`
  - `demo-admin@demo.local`
  - `demo-user@demo.local`
- Password dev documentado en la migración `V16__seed_foundation.sql`
- Tenants seeded:
  - `keygo`
  - `demo`
  - `acme`
- Contractor seeded:
  - `Acme Holdings`

## Validación esperada

- Flyway debe aplicar desde cero sobre schema limpio
- `flyway validate` debe pasar sobre V1–V20
- `doc/03-architecture/database-schema.md` y `doc/08-reference/data/*` deben permanecer alineados con este baseline
- La capa Java todavía debe revalidarse frente al naming físico antes de considerar cerrada la compatibilidad JPA
