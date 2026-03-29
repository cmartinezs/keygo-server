-- V24: Add missing subscriber_type column to usage_counters
--
-- V19 created invoices and usage_counters. All invoices columns match
-- InvoiceEntity. usage_counters is missing subscriber_type, which is
-- defined as NOT NULL in UsageCounterEntity but was omitted from V19.
--
-- Same pattern as V23 (subscriber_type in app_subscriptions):
--   1. Add column as nullable.
--   2. Back-fill from existing polymorphic FKs.
--   3. Apply NOT NULL + CHECK constraint.

-- Step 1: Add column (nullable for now)
ALTER TABLE usage_counters
    ADD COLUMN IF NOT EXISTS subscriber_type VARCHAR(20);

-- Step 2: Back-fill from existing polymorphic FK columns
UPDATE usage_counters
SET subscriber_type = CASE
    WHEN subscriber_tenant_id      IS NOT NULL THEN 'TENANT'
    WHEN subscriber_tenant_user_id IS NOT NULL THEN 'TENANT_USER'
    ELSE 'TENANT'  -- safe fallback (should not occur given existing check constraint)
END
WHERE subscriber_type IS NULL;

-- Step 3: Add NOT NULL + CHECK constraints
ALTER TABLE usage_counters
    ALTER COLUMN subscriber_type SET NOT NULL;

ALTER TABLE usage_counters
    ADD CONSTRAINT chk_usage_counters_subscriber_type
        CHECK (subscriber_type IN ('TENANT', 'TENANT_USER'));
