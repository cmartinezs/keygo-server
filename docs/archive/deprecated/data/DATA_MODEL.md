# Data Model (Consolidated)

⚠️ **This documentation has been consolidated.**

**See:** [`../design/DATABASE_SCHEMA.md`](../design/DATABASE_SCHEMA.md) for complete database design, entity relationships, and schema.

This file is maintained for backward compatibility only. All updates are made to the canonical `DATABASE_SCHEMA.md`.
   - `audit_event_payloads`
   - `audit_event_tags`
   - `audit_entity_links`

## Núcleo de identidad

### `platform_users`

- PK: `id`
- Email global: `email CITEXT UNIQUE`
- Credencial global: `password_hash`
- Estado: `PENDING | ACTIVE | SUSPENDED | RESET_PASSWORD | DELETED`
- Perfil global:
  - `first_name`
  - `last_name`
  - `display_name`
  - `phone_number`
  - `locale`
  - `zoneinfo`
  - `profile_picture_url`
  - `birthdate`
  - `website`
- Trazabilidad:
  - `email_verified_at`
  - `last_login_at`
  - `created_at`
  - `updated_at`

### `platform_user_notification_preferences`

- 1:1 con `platform_users`
- Flags globales:
  - `security_alerts_email`
  - `security_alerts_in_app`
  - `billing_alerts_email`
  - `product_updates_email`
  - `weekly_digest`

### `platform_activity_events`

- Feed global de actividad self-service
- Puede referenciar tenant, app y sesiones
- `metadata JSONB` para detalle liviano

## Multi-tenant

### `tenants`

- PK: `id`
- `slug UNIQUE`
- `status`: `PENDING | ACTIVE | SUSPENDED | DELETED`
- `contractor_id NULL` para tenants internos o reservados
- `is_internal_reserved` distingue tenants como `keygo`

### `tenant_users`

- PK: `id`
- FK fuerte:
  - `tenant_id -> tenants`
  - `platform_user_id -> platform_users`
- Unicidad:
  - `(tenant_id, platform_user_id)`
  - `(tenant_id, local_username)` parcial
  - `(id, tenant_id)` para FKs compuestas
- Estado:
  - `INVITED | PENDING | ACTIVE | SUSPENDED | DELETED`

## RBAC

### Plataforma

- `platform_roles`
- `platform_role_hierarchy`
- `platform_user_roles`

Scope soportado en `platform_user_roles`:

- `GLOBAL`
- `CONTRACTOR`
- `TENANT`

### Tenant

- `tenant_roles`
- `tenant_role_hierarchy`
- `tenant_user_roles`

Integridad:

- `tenant_user_roles` usa FKs compuestas para forzar que el rol y el tenant user pertenezcan al mismo tenant

### App

- `app_roles`
- `app_role_hierarchy`
- `app_memberships`
- `app_membership_roles`

Integridad:

- `app_memberships` impide cross-tenant entre usuario y app
- `app_membership_roles` impide asignar roles de otra app
- `app_role_hierarchy` impide ciclos y limita profundidad a cinco

## OAuth/OIDC y sesiones

### `client_apps`

- Tenant-scoped
- `client_id UNIQUE`
- `type`: `PUBLIC | CONFIDENTIAL`
- `status`: `ACTIVE | SUSPENDED | PENDING`
- `is_internal` para clientes técnicos como `keygo-ui`

Tablas hijas:

- `client_redirect_uris`
- `client_allowed_grants`
- `client_allowed_scopes`

### `platform_sessions`

- Sesión global de cuenta
- Apta para UI de seguridad:
  - dispositivo
  - navegador
  - SO
  - IP
  - ubicación aproximada
  - última actividad
  - motivo de término

### `oauth_sessions`

- Sesión por app/tenant
- Se enlaza a `platform_sessions`
- Puede admitir `tenant_user_id NULL` solo en apps internas de tenants reservados
- Trigger valida consistencia entre platform user, tenant user y app

### Artefactos OAuth

- `authorization_codes`
  - persiste `code_hash`, no el código plano
- `refresh_tokens`
  - persiste `token_hash`, no el token plano
  - soporta rotación con `replaced_by_id`
- `signing_keys`
  - puede ser global (`tenant_id NULL`) o tenant-scoped

## Flujos globales

- `email_verifications`
- `password_reset_tokens`

Ambos quedan ligados a `platform_users`, no a `tenant_users`.

## Billing

### `contractors`

- Root de billing
- Puede representar persona o empresa
- Se enlaza a `platform_users` vía `primary_contact_platform_user_id`

### `contractor_users`

- N:M entre contractor y platform user
- `role_code`:
  - `OWNER`
  - `BILLING_ADMIN`
  - `VIEWER`

### Catálogo

- `app_plans`
- `app_plan_versions`
- `app_plan_billing_options`
- `app_plan_entitlements`

### Operación

- `app_contracts`
- `app_subscriptions`
- `payment_transactions`
- `invoices`
- `usage_counters`
- `tenant_billing_profiles`
- `payment_methods`

## Auditoría

### `audit_events`

Dimensiones principales:

- actor
- contractor
- tenant
- app
- sesión
- request/correlation/trace
- categoría, tipo, acción y resultado
- metadata analítica

### Tablas auxiliares

- `audit_event_payloads`
- `audit_event_tags`
- `audit_entity_links`

Características:

- append-only lógico
- detalle pesado fuera de la tabla principal
- índices orientados a investigación y reporting

## Reglas estructurales relevantes

- Emails globales usan `CITEXT`
- UUIDs usan `gen_random_uuid()`
- Estados modelados con `CHECK`, no enums nativos de PostgreSQL
- `updated_at` se mantiene con trigger común en tablas mutables
- Dashboard SQL vive en `docs/sql/platform_dashboards/`
