-- V23: Add missing subscriber_type column to app_subscriptions
--
-- The AppSubscriptionEntity defines subscriber_type (NOT NULL) as a discriminator
-- that indicates whether the subscriber is a Tenant (B2B) or a TenantUser (B2C).
-- This column was omitted from V18 and caused Hibernate schema validation to fail.
--
-- Migration strategy:
--   1. Add the column as nullable (to handle existing rows).
--   2. Back-fill existing rows deriving the value from the polymorphic FKs:
--        subscriber_tenant_id      IS NOT NULL  → 'TENANT'
--        subscriber_tenant_user_id IS NOT NULL  → 'TENANT_USER'
--   3. Apply NOT NULL + CHECK constraints.

-- Step 1: Add column (nullable for now)
ALTER TABLE app_subscriptions
    ADD COLUMN IF NOT EXISTS subscriber_type VARCHAR(20);

-- Step 2: Back-fill from existing polymorphic FK columns
UPDATE app_subscriptions
SET subscriber_type = CASE
    WHEN subscriber_tenant_id      IS NOT NULL THEN 'TENANT'
    WHEN subscriber_tenant_user_id IS NOT NULL THEN 'TENANT_USER'
    ELSE 'TENANT'  -- safe fallback (should not occur given check constraint)
END
WHERE subscriber_type IS NULL;

-- Step 3: Add NOT NULL + CHECK constraints
ALTER TABLE app_subscriptions
    ALTER COLUMN subscriber_type SET NOT NULL;

ALTER TABLE app_subscriptions
    ADD CONSTRAINT chk_app_subscriptions_subscriber_type
        CHECK (subscriber_type IN ('TENANT', 'TENANT_USER'));

