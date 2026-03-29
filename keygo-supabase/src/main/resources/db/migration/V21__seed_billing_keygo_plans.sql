-- V21: Seed — Billing plans for keygo-platform app
--
-- Creates the canonical FREE / STARTER / BUSINESS / ENTERPRISE plan stack
-- for the "keygo-platform" ClientApp (ID: 11111111-1111-1111-1111-333333333333).
-- Subscriber type is TENANT (B2B model).
--
-- All plans are public and ACTIVE. Pricing is in MXN, billed MONTHLY.
-- Stable UUIDs allow idempotent re-runs (ON CONFLICT DO UPDATE).
--
-- Plan IDs:
--   FREE:       22222222-2222-2222-2222-100000000001
--   STARTER:    22222222-2222-2222-2222-100000000002
--   BUSINESS:   22222222-2222-2222-2222-100000000003
--   ENTERPRISE: 22222222-2222-2222-2222-100000000004
--
-- Version IDs:
--   FREE v1.0:       33333333-3333-3333-3333-100000000001
--   STARTER v1.0:    33333333-3333-3333-3333-100000000002
--   BUSINESS v1.0:   33333333-3333-3333-3333-100000000003
--   ENTERPRISE v1.0: 33333333-3333-3333-3333-100000000004

-- ────────────────────────────────────────────────────────────────────────────
-- PLANS
-- ────────────────────────────────────────────────────────────────────────────

INSERT INTO app_plans (id, client_app_id, code, name, description, subscriber_type, status, is_public)
VALUES
  (
    '22222222-2222-2222-2222-100000000001',
    '11111111-1111-1111-1111-333333333333',
    'FREE',
    'Free',
    'Ideal para explorar KeyGo sin costo. Sin tarjeta de crédito.',
    'TENANT',
    'ACTIVE',
    TRUE
  ),
  (
    '22222222-2222-2222-2222-100000000002',
    '11111111-1111-1111-1111-333333333333',
    'STARTER',
    'Starter',
    'Para startups y equipos pequeños que necesitan IAM básico.',
    'TENANT',
    'ACTIVE',
    TRUE
  ),
  (
    '22222222-2222-2222-2222-100000000003',
    '11111111-1111-1111-1111-333333333333',
    'BUSINESS',
    'Business',
    'Para empresas medianas con múltiples aplicaciones y equipos.',
    'TENANT',
    'ACTIVE',
    TRUE
  ),
  (
    '22222222-2222-2222-2222-100000000004',
    '11111111-1111-1111-1111-333333333333',
    'ENTERPRISE',
    'Enterprise',
    'Para grandes organizaciones con requerimientos avanzados de seguridad y soporte.',
    'TENANT',
    'ACTIVE',
    TRUE
  )
ON CONFLICT (client_app_id, code) DO UPDATE
  SET name            = EXCLUDED.name,
      description     = EXCLUDED.description,
      subscriber_type = EXCLUDED.subscriber_type,
      status          = EXCLUDED.status,
      is_public       = EXCLUDED.is_public,
      updated_at      = CURRENT_TIMESTAMP;

-- ────────────────────────────────────────────────────────────────────────────
-- PLAN VERSIONS  (v1.0, effective 2026-01-01)
-- ────────────────────────────────────────────────────────────────────────────

INSERT INTO app_plan_versions (id, app_plan_id, version, currency, billing_period, base_price, setup_fee, trial_days, effective_from, status)
VALUES
  (
    '33333333-3333-3333-3333-100000000001',
    '22222222-2222-2222-2222-100000000001',
    '1.0', 'MXN', 'MONTHLY',   0.00, 0.00,  0, '2026-01-01', 'ACTIVE'
  ),
  (
    '33333333-3333-3333-3333-100000000002',
    '22222222-2222-2222-2222-100000000002',
    '1.0', 'MXN', 'MONTHLY', 299.00, 0.00, 14, '2026-01-01', 'ACTIVE'
  ),
  (
    '33333333-3333-3333-3333-100000000003',
    '22222222-2222-2222-2222-100000000003',
    '1.0', 'MXN', 'MONTHLY', 999.00, 0.00, 14, '2026-01-01', 'ACTIVE'
  ),
  (
    '33333333-3333-3333-3333-100000000004',
    '22222222-2222-2222-2222-100000000004',
    '1.0', 'MXN', 'MONTHLY', 3999.00, 0.00, 30, '2026-01-01', 'ACTIVE'
  )
ON CONFLICT (id) DO UPDATE
  SET version        = EXCLUDED.version,
      currency       = EXCLUDED.currency,
      billing_period = EXCLUDED.billing_period,
      base_price     = EXCLUDED.base_price,
      setup_fee      = EXCLUDED.setup_fee,
      trial_days     = EXCLUDED.trial_days,
      effective_from = EXCLUDED.effective_from,
      status         = EXCLUDED.status;

-- ────────────────────────────────────────────────────────────────────────────
-- ENTITLEMENTS — FREE v1.0 (límites muy bajos, sin SLA)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000001', 'MAX_TENANT_USERS',    'QUOTA',   3,    'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000001', 'MAX_CLIENT_APPS',     'QUOTA',   1,    'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000001', 'MAX_MONTHLY_TOKENS',  'QUOTA',   1000, 'MONTH','HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000001', 'SOCIAL_LOGIN',        'BOOLEAN', NULL, 'NONE', 'HARD', FALSE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000001', 'CUSTOM_DOMAIN',       'BOOLEAN', NULL, 'NONE', 'HARD', FALSE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000001', 'SLA_UPTIME_PCT',      'QUOTA',   NULL, 'NONE', 'SOFT', FALSE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000001', 'AUDIT_LOG_DAYS',      'QUOTA',   7,    'NONE', 'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- ENTITLEMENTS — STARTER v1.0
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'MAX_TENANT_USERS',    'QUOTA',   25,    'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'MAX_CLIENT_APPS',     'QUOTA',   5,     'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'MAX_MONTHLY_TOKENS',  'QUOTA',   50000, 'MONTH','HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'SOCIAL_LOGIN',        'BOOLEAN', NULL,  'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'CUSTOM_DOMAIN',       'BOOLEAN', NULL,  'NONE', 'HARD', FALSE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'SLA_UPTIME_PCT',      'QUOTA',   99,    'NONE', 'SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'AUDIT_LOG_DAYS',      'QUOTA',   30,    'NONE', 'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- ENTITLEMENTS — BUSINESS v1.0
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'MAX_TENANT_USERS',    'QUOTA',   200,    'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'MAX_CLIENT_APPS',     'QUOTA',   20,     'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'MAX_MONTHLY_TOKENS',  'QUOTA',   500000, 'MONTH','SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'SOCIAL_LOGIN',        'BOOLEAN', NULL,   'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'CUSTOM_DOMAIN',       'BOOLEAN', NULL,   'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'SLA_UPTIME_PCT',      'QUOTA',   999,    'NONE', 'SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'AUDIT_LOG_DAYS',      'QUOTA',   90,     'NONE', 'SOFT', TRUE)
ON CONFLICT DO NOTHING;

-- ENTITLEMENTS — ENTERPRISE v1.0 (sin límites duros para cuotas mayores)
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'MAX_TENANT_USERS',    'QUOTA',   NULL, 'NONE', 'SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'MAX_CLIENT_APPS',     'QUOTA',   NULL, 'NONE', 'SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'MAX_MONTHLY_TOKENS',  'QUOTA',   NULL, 'MONTH','SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'SOCIAL_LOGIN',        'BOOLEAN', NULL, 'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'CUSTOM_DOMAIN',       'BOOLEAN', NULL, 'NONE', 'HARD', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'SLA_UPTIME_PCT',      'QUOTA',   9999, 'NONE', 'SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'AUDIT_LOG_DAYS',      'QUOTA',   365,  'NONE', 'SOFT', TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'DEDICATED_SUPPORT',   'BOOLEAN', NULL, 'NONE', 'HARD', TRUE)
ON CONFLICT DO NOTHING;

