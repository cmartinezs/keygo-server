-- ============================================================================
-- V20__platform_plan_catalog.sql
-- Enable and seed public platform-level billing plans (client_app_id IS NULL).
-- ============================================================================

ALTER TABLE app_plans
    ALTER COLUMN client_app_id DROP NOT NULL;

COMMENT ON COLUMN app_plans.client_app_id IS
    'NULL = platform plan offered by KeyGo. NOT NULL = app plan offered by a client app.';

CREATE UNIQUE INDEX uq_app_plans_platform_code
    ON app_plans(code)
    WHERE client_app_id IS NULL;

INSERT INTO app_plans (id, client_app_id, code, name, description, status, is_public, sort_order)
VALUES
    ('25000000-0000-0000-0000-000000000001', NULL, 'FREE', 'Free', 'Entry plan for individual evaluation on KeyGo platform', 'ACTIVE', TRUE, 10),
    ('25000000-0000-0000-0000-000000000002', NULL, 'PERSONAL', 'Personal', 'Single operator platform plan', 'ACTIVE', TRUE, 20),
    ('25000000-0000-0000-0000-000000000003', NULL, 'TEAM', 'Team', 'Small team platform plan', 'ACTIVE', TRUE, 30),
    ('25000000-0000-0000-0000-000000000004', NULL, 'BUSINESS', 'Business', 'Operational business platform plan', 'ACTIVE', TRUE, 40),
    ('25000000-0000-0000-0000-000000000005', NULL, 'FLEX', 'Flex', 'Hybrid usage-based platform plan', 'ACTIVE', TRUE, 50),
    ('25000000-0000-0000-0000-000000000006', NULL, 'ENTERPRISE', 'Enterprise', 'Negotiated enterprise platform plan', 'ACTIVE', TRUE, 60);

INSERT INTO app_plan_versions (id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, status)
VALUES
    ('26000000-0000-0000-0000-000000000001', '25000000-0000-0000-0000-000000000001', 'v1.0', 'USD', 0.00, 0, CURRENT_DATE, 'ACTIVE'),
    ('26000000-0000-0000-0000-000000000002', '25000000-0000-0000-0000-000000000002', 'v1.0', 'USD', 0.00, 14, CURRENT_DATE, 'ACTIVE'),
    ('26000000-0000-0000-0000-000000000003', '25000000-0000-0000-0000-000000000003', 'v1.0', 'USD', 49.00, 14, CURRENT_DATE, 'ACTIVE'),
    ('26000000-0000-0000-0000-000000000004', '25000000-0000-0000-0000-000000000004', 'v1.0', 'USD', 99.00, 14, CURRENT_DATE, 'ACTIVE'),
    ('26000000-0000-0000-0000-000000000005', '25000000-0000-0000-0000-000000000005', 'v1.0', 'USD', 0.00, 7, CURRENT_DATE, 'ACTIVE'),
    ('26000000-0000-0000-0000-000000000006', '25000000-0000-0000-0000-000000000006', 'v1.0', 'USD', 199.00, 30, CURRENT_DATE, 'ACTIVE');

INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'MONTHLY', 19.00, 0.00, TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'YEARLY', 190.00, 16.67, FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'MONTHLY', 49.00, 0.00, TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'YEARLY', 490.00, 16.67, FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'MONTHLY', 99.00, 0.00, TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'YEARLY', 990.00, 16.67, FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'MONTHLY', 59.00, 0.00, TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'YEARLY', 1990.00, 0.00, TRUE);

INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'MAX_TENANTS', 'QUOTA', 1, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'MAX_USERS', 'QUOTA', 3, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'MAX_TENANTS', 'QUOTA', 1, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'MAX_USERS', 'QUOTA', 5, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'MAX_TENANTS', 'QUOTA', 3, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'MAX_USERS', 'QUOTA', 25, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'MAX_TENANTS', 'QUOTA', 10, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'MAX_USERS', 'QUOTA', 100, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'MAX_TENANTS', 'QUOTA', 20, 'MONTH', 'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'MAX_USERS', 'QUOTA', 250, 'MONTH', 'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'MAX_TENANTS', 'QUOTA', NULL, 'MONTH', 'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'MAX_USERS', 'QUOTA', NULL, 'MONTH', 'SOFT', TRUE);
