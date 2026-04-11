# Entity Relationships (Consolidated)

⚠️ **This documentation has been consolidated.**

**See:** [`../design/DATABASE_SCHEMA.md`](../design/DATABASE_SCHEMA.md#entity-relationship-diagram-erd) for complete Entity-Relationship Diagram and table relationships.

This file is maintained for backward compatibility only. All updates are made to the canonical `DATABASE_SCHEMA.md`.
    app_roles ||--o{ app_membership_roles : assigned
```

Invariantes:

- `app_memberships` usa `(tenant_user_id, tenant_id)` y `(client_app_id, tenant_id)`
- `app_membership_roles` usa `(membership_id, client_app_id)` y `(role_id, client_app_id)`
- no se permiten relaciones cross-tenant ni cross-app

### RBAC separado por ámbito

```mermaid
erDiagram
    platform_users ||--o{ platform_user_roles : has
    platform_roles ||--o{ platform_user_roles : assigned
    platform_roles ||--o| platform_role_hierarchy : child

    tenant_users ||--o{ tenant_user_roles : has
    tenant_roles ||--o{ tenant_user_roles : assigned
    tenant_roles ||--o| tenant_role_hierarchy : child

    app_roles ||--o| app_role_hierarchy : child
```

Notas:

- plataforma, tenant y app no comparten tablas de roles
- las jerarquías son árboles simples con profundidad máxima cinco

### Sesiones y OAuth

```mermaid
erDiagram
    platform_users ||--o{ platform_sessions : owns
    platform_sessions ||--o{ oauth_sessions : expands_to
    tenants ||--o{ oauth_sessions : scopes
    tenant_users ||--o{ oauth_sessions : contextualizes
    client_apps ||--o{ oauth_sessions : serves
    signing_keys ||--o{ oauth_sessions : signs
    platform_sessions ||--o{ authorization_codes : starts
    oauth_sessions ||--o{ refresh_tokens : rotates
```

Invariantes:

- `oauth_sessions.platform_user_id` debe coincidir con `platform_sessions.platform_user_id`
- `tenant_user_id` solo puede ser null en apps internas de tenants reservados
- `authorization_codes.redirect_uri` debe existir para la app
- `refresh_tokens` hereda el contexto exacto de `oauth_sessions`

### Billing

```mermaid
erDiagram
    platform_users ||--o{ contractor_users : administers
    contractors ||--o{ contractor_users : has
    contractors ||--o{ tenants : owns
    client_apps ||--o{ app_plans : catalogs
    app_plans ||--o{ app_plan_versions : versions
    app_plan_versions ||--o{ app_plan_billing_options : prices
    app_plan_versions ||--o{ app_plan_entitlements : entitles
    contractors ||--o{ app_contracts : signs
    contractors ||--o{ app_subscriptions : subscribes
    app_contracts ||--o{ payment_transactions : originates
    app_subscriptions ||--o{ invoices : bills
    app_subscriptions ||--o{ payment_transactions : pays
```

Claves:

- `contractors` depende de identidad global
- `tenant_billing_profiles` es por tenant
- `payment_methods` es por contractor

### Auditoría

```mermaid
erDiagram
    audit_events ||--o| audit_event_payloads : stores_heavy_payload
    audit_events ||--o{ audit_event_tags : tags
    audit_events ||--o{ audit_entity_links : links
    platform_users ||--o{ audit_events : acts
    tenant_users ||--o{ audit_events : acts
    platform_sessions ||--o{ audit_events : traces
    oauth_sessions ||--o{ audit_events : traces
```

Características:

- `audit_events` es append-only
- payloads grandes salen de la tabla principal
- el modelo soporta agregación y drill-down

## Eventos auditables modelados

Categorías esperadas en reporting:

- `AUTH`
- `PLATFORM`
- `TENANT_ADMIN`
- `APP_ADMIN`
- `BILLING`
- `SECURITY`
- `UI`

Tipos esperados:

- `LOGIN_SUCCESS`
- `LOGIN_FAILURE`
- `PASSWORD_RESET_REQUESTED`
- `PASSWORD_RESET_COMPLETED`
- `SESSION_TERMINATED`
- `ACCESS_DENIED`
- `CONTRACT_ACTIVATED`
- `PAYMENT_APPROVED`
- `INVOICE_OVERDUE`
- `SCREEN_VIEWED`

## Relación con dashboards

La explotación analítica del modelo no vive en Flyway.

Ubicación:

- `docs/sql/platform_dashboards/common/`
- `docs/sql/platform_dashboards/keygo_admin/`
- `docs/sql/platform_dashboards/keygo_account_admin/`
- `docs/sql/platform_dashboards/keygo_user/`

Regla:

- cada agregado relevante tiene una query detalle asociada
