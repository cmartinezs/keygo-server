-- V20: Seed — keygo-platform ClientApp for the keygo tenant
--
-- Creates the "keygo-platform" application that KeyGo uses to manage
-- its own billing (dogfood). Plans are NOT seeded here because the
-- plan structure (FREE/STARTER/BUSINESS/ENTERPRISE) needs to be
-- confirmed with the product team before being committed to a migration.
--
-- Existing tenants (keygo, demo) are backward-compatible with the billing
-- model: no active subscription means no limits (CheckAppEntitlementUseCase
-- returns allowed=true when no entitlement is found).
--
-- Stable UUIDs used to allow idempotent re-runs:
--   keygo tenant:           11111111-1111-1111-1111-111111111111  (from V14)
--   keygo-platform app:     11111111-1111-1111-1111-333333333333  (new)

-- ---------------------------------------------------------------------------
-- keygo-platform: the IAM platform ClientApp owned by the keygo tenant
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (
    id,
    tenant_id,
    client_id,
    name,
    description,
    type,
    hashed_secret,
    status
)
VALUES (
    '11111111-1111-1111-1111-333333333333',
    '11111111-1111-1111-1111-111111111111',
    'keygo-platform',
    'KeyGo Platform',
    'Internal app used by KeyGo to manage IAM platform billing and subscriptions',
    'CONFIDENTIAL',
    NULL,           -- no secret needed for internal platform billing
    'ACTIVE'
)
ON CONFLICT (client_id) DO UPDATE
SET
    tenant_id   = EXCLUDED.tenant_id,
    name        = EXCLUDED.name,
    description = EXCLUDED.description,
    type        = EXCLUDED.type,
    status      = EXCLUDED.status,
    updated_at  = CURRENT_TIMESTAMP;

-- Allowed grants for keygo-platform (client_credentials for M2M billing ops)
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-333333333333', 'CLIENT_CREDENTIALS'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = '11111111-1111-1111-1111-333333333333'
      AND grant_type = 'CLIENT_CREDENTIALS'
);

-- App roles for keygo-platform (platform billing admin)
INSERT INTO app_roles (id, client_app_id, code, display_name, description)
VALUES (
    '11111111-1111-1111-1111-500000000001',
    '11111111-1111-1111-1111-333333333333',
    'billing_admin',
    'Billing Admin',
    'Can manage plans and subscriptions for the KeyGo platform'
)
ON CONFLICT (client_app_id, code) DO UPDATE
SET
    display_name = EXCLUDED.display_name,
    description  = EXCLUDED.description,
    updated_at   = CURRENT_TIMESTAMP;

-- NOTE: Plans for keygo-platform (FREE, STARTER, BUSINESS, ENTERPRISE) are
-- intentionally NOT seeded here. They will be added in a subsequent migration
-- (V21__seed_billing_keygo_plans.sql) once the plan structure is confirmed.
--
-- Existing tenants (keygo, demo) do NOT need a subscription record.
-- The billing system is opt-in: no subscription = no limits (backward compatible).

