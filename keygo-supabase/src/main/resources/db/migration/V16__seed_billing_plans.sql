-- =============================================================================
-- V17: Seed — Planes de billing para keygo-ui (catálogo público de KeyGo)
--
-- Crea el stack canónico FREE / STARTER / BUSINESS / ENTERPRISE para la
-- ClientApp "keygo-ui" — la app pública a través de la cual los suscriptores
-- contratan el servicio KeyGo IAM.
--
-- Todos los planes son públicos y ACTIVE. Precio en MXN.
-- sort_order controla el orden en la UI (menor = más barato).
-- Billing options separadas: FREE no tiene opciones (plan gratuito).
--
-- Convención de FK: nunca se hardcodean UUIDs de FK; se resuelven con
-- subqueries SELECT sobre el campo semántico del padre (client_id, code, version).
-- Los únicos UUIDs fijos son los PKs (columna id) para estabilidad entre resets.
--
-- PKs estables:
--   Plans:    FREE=22222222-2222-2222-2222-100000000001
--             STARTER=22222222-2222-2222-2222-100000000002
--             BUSINESS=22222222-2222-2222-2222-100000000003
--             ENTERPRISE=22222222-2222-2222-2222-100000000004
--   Versions: FREE v1.0=33333333-3333-3333-3333-100000000001
--             STARTER v1.0=33333333-3333-3333-3333-100000000002
--             BUSINESS v1.0=33333333-3333-3333-3333-100000000003
--             ENTERPRISE v1.0=33333333-3333-3333-3333-100000000004
-- =============================================================================

-- ────────────────────────────────────────────────────────────────────────────
-- PLANES (con sort_order: FREE=0, STARTER=1, BUSINESS=2, ENTERPRISE=3)
--    FK client_app_id → client_apps.client_id (subquery)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plans (id, client_app_id, code, name, description, status, is_public, sort_order)
VALUES
  (
    '22222222-2222-2222-2222-100000000001',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'FREE', 'Free',
    'Ideal para explorar KeyGo sin costo. Sin tarjeta de crédito.',
    'ACTIVE', TRUE, 0
  ),
  (
    '22222222-2222-2222-2222-100000000002',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'STARTER', 'Starter',
    'Para startups y equipos pequeños que necesitan IAM básico.',
    'ACTIVE', TRUE, 1
  ),
  (
    '22222222-2222-2222-2222-100000000003',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'BUSINESS', 'Business',
    'Para empresas medianas con múltiples aplicaciones y equipos.',
    'ACTIVE', TRUE, 2
  ),
  (
    '22222222-2222-2222-2222-100000000004',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'ENTERPRISE', 'Enterprise',
    'Para grandes organizaciones con requerimientos avanzados de seguridad y soporte.',
    'ACTIVE', TRUE, 3
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
  name        = EXCLUDED.name,
  description = EXCLUDED.description,
  status      = EXCLUDED.status,
  is_public   = EXCLUDED.is_public,
  sort_order  = EXCLUDED.sort_order,
  updated_at  = CURRENT_TIMESTAMP;

-- ────────────────────────────────────────────────────────────────────────────
-- VERSIONES DE PLAN (v1.0, vigente desde 2026-01-01)
-- NOTA: billing_period y base_price ya no van aquí; están en app_plan_billing_options
--    FK app_plan_id → app_plans.code + client_apps.client_id (subquery)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_versions (id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, status)
VALUES
  (
    '33333333-3333-3333-3333-100000000001',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FREE'),
    '1.0', 'MXN', 0.00,  0, '2026-01-01', 'ACTIVE'
  ),
  (
    '33333333-3333-3333-3333-100000000002',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'STARTER'),
    '1.0', 'MXN', 0.00, 14, '2026-01-01', 'ACTIVE'
  ),
  (
    '33333333-3333-3333-3333-100000000003',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'BUSINESS'),
    '1.0', 'MXN', 0.00, 14, '2026-01-01', 'ACTIVE'
  ),
  (
    '33333333-3333-3333-3333-100000000004',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'ENTERPRISE'),
    '1.0', 'MXN', 0.00, 30, '2026-01-01', 'ACTIVE'
  )
ON CONFLICT (app_plan_id, version) DO UPDATE
SET
  currency       = EXCLUDED.currency,
  setup_fee      = EXCLUDED.setup_fee,
  trial_days     = EXCLUDED.trial_days,
  effective_from = EXCLUDED.effective_from,
  status         = EXCLUDED.status;

-- ────────────────────────────────────────────────────────────────────────────
-- BILLING OPTIONS — opciones de período por versión
-- FREE: sin opciones (plan gratuito, no se requiere pago)
-- STARTER / BUSINESS / ENTERPRISE: MONTHLY + YEARLY (con descuento)
-- ────────────────────────────────────────────────────────────────────────────
-- STARTER v1.0 — MONTHLY $299 / YEARLY $2,990 (descuento ~17%)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'MONTHLY',   299.00,  0.00, TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000002', 'YEARLY',   2990.00, 16.62, FALSE)
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price=EXCLUDED.base_price, discount_pct=EXCLUDED.discount_pct, is_default=EXCLUDED.is_default;

-- BUSINESS v1.0 — MONTHLY $999 / YEARLY $9,990 (descuento ~17%)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'MONTHLY',   999.00,  0.00, TRUE),
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000003', 'YEARLY',   9990.00, 16.62, FALSE)
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price=EXCLUDED.base_price, discount_pct=EXCLUDED.discount_pct, is_default=EXCLUDED.is_default;

-- ENTERPRISE v1.0 — solo YEARLY (precio por negociación)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-100000000004', 'YEARLY', 3999.00, 0.00, TRUE)
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price=EXCLUDED.base_price, discount_pct=EXCLUDED.discount_pct, is_default=EXCLUDED.is_default;

-- ────────────────────────────────────────────────────────────────────────────
-- ENTITLEMENTS — FREE v1.0
--    FK app_plan_version_id resuelto por CTE (client_id + plan code + version)
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FREE' AND apv.version = '1.0'
)
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
(VALUES
  ('MAX_TENANT_USERS',   'QUOTA',      3::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA',   1000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', FALSE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', FALSE),
  ('SLA_UPTIME_PCT',     'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', FALSE),
  ('AUDIT_LOG_DAYS',     'QUOTA',      7::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;

-- ────────────────────────────────────────────────────────────────────────────
-- ENTITLEMENTS — STARTER v1.0
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'STARTER' AND apv.version = '1.0'
)
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
(VALUES
  ('MAX_TENANT_USERS',   'QUOTA',      25::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',       5::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA',   50000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT,  'NONE',  'HARD', FALSE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT,  'NONE',  'HARD', FALSE),
  ('SLA_UPTIME_PCT',     'QUOTA',      99::BIGINT, 'NONE',  'SOFT', TRUE),
  ('AUDIT_LOG_DAYS',     'QUOTA',      30::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;

-- ────────────────────────────────────────────────────────────────────────────
-- ENTITLEMENTS — BUSINESS v1.0
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'BUSINESS' AND apv.version = '1.0'
)
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
(VALUES
  ('MAX_TENANT_USERS',   'QUOTA',     200::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',      20::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA',  500000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT,  'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT,  'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',     'QUOTA',     999::BIGINT, 'NONE',  'SOFT', TRUE),
  ('AUDIT_LOG_DAYS',     'QUOTA',      90::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;

-- ────────────────────────────────────────────────────────────────────────────
-- ENTITLEMENTS — ENTERPRISE v1.0
--    Sin límites en usuarios/apps/tokens, SLA máximo, retención indefinida
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'ENTERPRISE' AND apv.version = '1.0'
)
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
(VALUES
  ('MAX_TENANT_USERS',   'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA',   NULL::BIGINT, 'MONTH', 'SOFT', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',     'QUOTA',    999::BIGINT, 'NONE',  'HARD', TRUE),
  ('AUDIT_LOG_DAYS',     'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;
