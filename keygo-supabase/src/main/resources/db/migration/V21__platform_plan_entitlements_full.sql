-- ============================================================================
-- V35__platform_plan_entitlements_full.sql
-- Complete entitlement matrix for platform plans (client_app_id IS NULL).
--
-- V20 seeded only MAX_TENANTS and MAX_USERS with period_type='MONTH'.
-- This migration:
--   1. Corrects period_type to 'NONE' for the 12 existing rows.
--   2. Inserts the remaining 61 entitlements to reach the full 73-row matrix.
--
-- Plan version IDs (from V20):
--   FREE:       26000000-0000-0000-0000-000000000001
--   PERSONAL:   26000000-0000-0000-0000-000000000002
--   TEAM:       26000000-0000-0000-0000-000000000003
--   BUSINESS:   26000000-0000-0000-0000-000000000004
--   FLEX:       26000000-0000-0000-0000-000000000005
--   ENTERPRISE: 26000000-0000-0000-0000-000000000006
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Step 1: Fix period_type for existing MAX_TENANTS and MAX_USERS rows
-- ----------------------------------------------------------------------------
UPDATE app_plan_entitlements
SET period_type = 'NONE'
WHERE app_plan_version_id IN (
    '26000000-0000-0000-0000-000000000001',
    '26000000-0000-0000-0000-000000000002',
    '26000000-0000-0000-0000-000000000003',
    '26000000-0000-0000-0000-000000000004',
    '26000000-0000-0000-0000-000000000005',
    '26000000-0000-0000-0000-000000000006'
  )
  AND metric_code IN ('MAX_TENANTS', 'MAX_USERS');

-- ----------------------------------------------------------------------------
-- Step 2: Insert missing entitlements
-- Uses ON CONFLICT DO NOTHING as safety guard (idempotent re-run).
-- ----------------------------------------------------------------------------

-- FREE (7 new rows; MAX_TENANTS and MAX_USERS already exist from V20)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'MAX_CLIENT_APPS',    'QUOTA',   1,    'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'MAX_ADMINS',         'QUOTA',   1,    'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'MAX_MONTHLY_TOKENS', 'QUOTA',   1000, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'SOCIAL_LOGIN',       'BOOLEAN', NULL, 'NONE',  'HARD', FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'CUSTOM_DOMAIN',      'BOOLEAN', NULL, 'NONE',  'HARD', FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'SLA_UPTIME_PCT',     'QUOTA',   NULL, 'NONE',  'SOFT', FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000001', 'AUDIT_LOG_DAYS',     'QUOTA',   7,    'NONE',  'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- PERSONAL (7 new rows)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'MAX_CLIENT_APPS',    'QUOTA',   3,     'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'MAX_ADMINS',         'QUOTA',   1,     'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'MAX_MONTHLY_TOKENS', 'QUOTA',   10000, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'SOCIAL_LOGIN',       'BOOLEAN', NULL,  'NONE',  'HARD', FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'CUSTOM_DOMAIN',      'BOOLEAN', NULL,  'NONE',  'HARD', FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'SLA_UPTIME_PCT',     'QUOTA',   NULL,  'NONE',  'SOFT', FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000002', 'AUDIT_LOG_DAYS',     'QUOTA',   14,    'NONE',  'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- TEAM (7 new rows; MAX_TENANTS=3 confirmed for contractor context)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'MAX_CLIENT_APPS',    'QUOTA',   10,     'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'MAX_ADMINS',         'QUOTA',   3,      'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'MAX_MONTHLY_TOKENS', 'QUOTA',   100000, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'SOCIAL_LOGIN',       'BOOLEAN', NULL,   'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'CUSTOM_DOMAIN',      'BOOLEAN', NULL,   'NONE',  'HARD', FALSE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'SLA_UPTIME_PCT',     'QUOTA',   99,     'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000003', 'AUDIT_LOG_DAYS',     'QUOTA',   30,     'NONE',  'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- BUSINESS (7 new rows; MAX_TENANTS=10 confirmed for contractor context)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'MAX_CLIENT_APPS',    'QUOTA',   30,     'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'MAX_ADMINS',         'QUOTA',   10,     'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'MAX_MONTHLY_TOKENS', 'QUOTA',   500000, 'MONTH', 'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'SOCIAL_LOGIN',       'BOOLEAN', NULL,   'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'CUSTOM_DOMAIN',      'BOOLEAN', NULL,   'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'SLA_UPTIME_PCT',     'QUOTA',   999,    'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000004', 'AUDIT_LOG_DAYS',     'QUOTA',   90,     'NONE',  'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- FLEX (23 new rows; MAX_TENANTS and MAX_USERS already exist from V20)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'MAX_CLIENT_APPS',              'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'MAX_MONTHLY_TOKENS',            'QUOTA',   NULL, 'MONTH', 'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'SOCIAL_LOGIN',                  'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'CUSTOM_DOMAIN',                 'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'SLA_UPTIME_PCT',                'QUOTA',   999,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'AUDIT_LOG_DAYS',                'QUOTA',   90,   'NONE',  'SOFT', TRUE),
    -- Tenant tiers
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_TENANT_T1_MAX',            'QUOTA',   10,   'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_TENANT_RATE_T1',           'RATE',    800,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_TENANT_T2_MAX',            'QUOTA',   50,   'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_TENANT_RATE_T2',           'RATE',    600,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_TENANT_RATE_T3',           'RATE',    400,  'NONE',  'SOFT', TRUE),
    -- App tiers
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_APP_T1_MAX',               'QUOTA',   20,   'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_APP_RATE_T1',              'RATE',    200,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_APP_T2_MAX',               'QUOTA',   100,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_APP_RATE_T2',              'RATE',    150,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_APP_RATE_T3',              'RATE',    100,  'NONE',  'SOFT', TRUE),
    -- Identity tiers
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_IDENTITY_T1_MAX',          'QUOTA',   100,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_IDENTITY_RATE_T1',         'RATE',    120,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_IDENTITY_T2_MAX',          'QUOTA',   500,  'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_IDENTITY_RATE_T2',         'RATE',    90,   'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_IDENTITY_RATE_T3',         'RATE',    60,   'NONE',  'SOFT', TRUE),
    -- Admin tiers (replaces fixed MAX_ADMINS in FLEX)
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_ADMIN_INCLUDED_PER_TENANT','QUOTA',   1,    'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000005', 'FLEX_ADMIN_RATE',               'RATE',    400,  'NONE',  'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- ENTERPRISE (10 new rows; MAX_TENANTS and MAX_USERS already exist from V20)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'MAX_CLIENT_APPS',        'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'MAX_ADMINS',             'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'MAX_MONTHLY_TOKENS',     'QUOTA',   NULL, 'MONTH', 'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'SOCIAL_LOGIN',           'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'CUSTOM_DOMAIN',          'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'SLA_UPTIME_PCT',         'QUOTA',   999,  'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'AUDIT_LOG_DAYS',         'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'PRIORITY_SUPPORT',       'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'CUSTOM_SLA',             'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
    (gen_random_uuid(), '26000000-0000-0000-0000-000000000006', 'DEDICATED_SUCCESS_MGR',  'BOOLEAN', NULL, 'NONE',  'HARD', TRUE)
ON CONFLICT DO NOTHING;
