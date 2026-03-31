-- =============================================================================
-- V17: Seed — Catalogo de planes de billing para keygo-ui
--
-- Escalera comercial completa:
--   Free ($0/mes) | Personal ($5/mes) | Team ($49/mes)
--   Business ($149/mes) | Flex (pago por uso) | Enterprise (custom/anual)
--
-- Limites por plan:
--   Plan       | Tenants | Apps | Identidades activas | Admins
--   Free       |    1    |   1  |          3          |    1
--   Personal   |    1    |   3  |          5          |    1
--   Team       |    1    |  10  |         25          |    3
--   Business   |    1    |  30  |        100          |   10
--   Flex       |  ilim.  | ilim.|        ilim.        | 1/tenant + $4 extra
--   Enterprise | custom  |cust. |        custom       | custom
--
-- subscriber_type = TENANT (B2B) para todos los planes de keygo-ui
-- Currency: USD
-- sort_order: Free=0 | Personal=1 | Team=2 | Business=3 | Flex=4 | Enterprise=5
--
-- Convenciones de FK: nunca hardcodear UUIDs de FK; usar subqueries semanticas.
-- PKs estables (para estabilidad entre resets de DB):
--   Plans:    FREE=22222222-2222-2222-2222-100000000001
--             PERSONAL=44444444-4444-4444-4444-100000000001
--             TEAM=44444444-4444-4444-4444-100000000002
--             BUSINESS=22222222-2222-2222-2222-100000000003
--             FLEX=44444444-4444-4444-4444-100000000003
--             ENTERPRISE=22222222-2222-2222-2222-100000000004
--   Versions: FREE v1.0=55555555-5555-5555-5555-100000000001
--             PERSONAL v1.0=55555555-5555-5555-5555-100000000004
--             TEAM v1.0=55555555-5555-5555-5555-100000000005
--             BUSINESS v1.0=55555555-5555-5555-5555-100000000002
--             FLEX v1.0=55555555-5555-5555-5555-100000000006
--             ENTERPRISE v1.0=55555555-5555-5555-5555-100000000003
--   Billing options PKs:
--             PERSONAL MONTHLY=77777777-7777-7777-7777-100000000001
--             PERSONAL YEARLY =77777777-7777-7777-7777-100000000002
--             TEAM MONTHLY    =77777777-7777-7777-7777-100000000003
--             TEAM YEARLY     =77777777-7777-7777-7777-100000000004
--             BUSINESS MONTHLY=77777777-7777-7777-7777-100000000005
--             BUSINESS YEARLY =77777777-7777-7777-7777-100000000006
--             ENTERPRISE YEARLY=77777777-7777-7777-7777-100000000007
-- =============================================================================
-- ────────────────────────────────────────────────────────────────────────────
-- 1. PLANES (app_plans)
--    FK client_app_id → client_apps.client_id (subquery)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plans
    (id, client_app_id, code, name, description, subscriber_type, status, is_public, sort_order)
VALUES
  (
    '22222222-2222-2222-2222-100000000001',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'FREE', 'Free',
    'Explora KeyGo sin costo ni tarjeta de credito. Hasta 1 app y 3 identidades.',
    'TENANT', 'ACTIVE', TRUE, 0
  ),
  (
    '44444444-4444-4444-4444-100000000001',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'PERSONAL', 'Personal',
    'Para proyectos individuales y side-projects. Hasta 3 apps y 5 identidades.',
    'TENANT', 'ACTIVE', TRUE, 1
  ),
  (
    '44444444-4444-4444-4444-100000000002',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'TEAM', 'Team',
    'Ideal para pymes y SaaS pequenos. Hasta 10 apps, 25 identidades y 3 admins.',
    'TENANT', 'ACTIVE', TRUE, 2
  ),
  (
    '22222222-2222-2222-2222-100000000003',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'BUSINESS', 'Business',
    'Para empresas en crecimiento. Hasta 30 apps, 100 identidades y 10 admins.',
    'TENANT', 'ACTIVE', TRUE, 3
  ),
  (
    '44444444-4444-4444-4444-100000000003',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'FLEX', 'Flex',
    'Para agencias y consultoras multi-cliente. Pago por uso con tarifas escalonadas. Todas las features incluidas.',
    'TENANT', 'ACTIVE', TRUE, 4
  ),
  (
    '22222222-2222-2222-2222-100000000004',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'ENTERPRISE', 'Enterprise',
    'Para grandes organizaciones. Contrato anual, limites personalizados, SLA garantizado y soporte prioritario.',
    'TENANT', 'ACTIVE', TRUE, 5
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
    name           = EXCLUDED.name,
    description    = EXCLUDED.description,
    subscriber_type = EXCLUDED.subscriber_type,
    status         = EXCLUDED.status,
    is_public      = EXCLUDED.is_public,
    sort_order     = EXCLUDED.sort_order,
    updated_at     = CURRENT_TIMESTAMP;
-- ────────────────────────────────────────────────────────────────────────────
-- 2. VERSIONES DE PLAN (app_plan_versions) — todas v1.0
--    FK app_plan_id → app_plans.code + client_apps.client_id (subquery)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_versions
    (id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, status)
VALUES
  -- FREE v1.0
  (
    '55555555-5555-5555-5555-100000000001',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FREE'),
    '1.0', 'USD', 0.00, 0, '2026-01-01', 'ACTIVE'
  ),
  -- PERSONAL v1.0
  (
    '55555555-5555-5555-5555-100000000004',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'PERSONAL'),
    '1.0', 'USD', 0.00, 14, '2026-01-01', 'ACTIVE'
  ),
  -- TEAM v1.0
  (
    '55555555-5555-5555-5555-100000000005',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'TEAM'),
    '1.0', 'USD', 0.00, 14, '2026-01-01', 'ACTIVE'
  ),
  -- BUSINESS v1.0
  (
    '55555555-5555-5555-5555-100000000002',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'BUSINESS'),
    '1.0', 'USD', 0.00, 14, '2026-01-01', 'ACTIVE'
  ),
  -- FLEX v1.0 (pago por uso, sin precio fijo)
  (
    '55555555-5555-5555-5555-100000000006',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FLEX'),
    '1.0', 'USD', 0.00, 0, '2026-01-01', 'ACTIVE'
  ),
  -- ENTERPRISE v1.0 (contrato anual, precio por negociacion)
  (
    '55555555-5555-5555-5555-100000000003',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'ENTERPRISE'),
    '1.0', 'USD', 0.00, 30, '2026-01-01', 'ACTIVE'
  )
ON CONFLICT (app_plan_id, version) DO UPDATE
SET
    currency       = EXCLUDED.currency,
    setup_fee      = EXCLUDED.setup_fee,
    trial_days     = EXCLUDED.trial_days,
    effective_from = EXCLUDED.effective_from,
    status         = EXCLUDED.status;
-- ────────────────────────────────────────────────────────────────────────────
-- 3. BILLING OPTIONS (app_plan_billing_options)
--    FREE v1.0:       sin opciones (plan gratuito)
--    FLEX v1.0:       sin opciones (pago por uso)
--    ENTERPRISE v1.0: solo YEARLY (precio por negociacion, base_price=0)
--    Otros:           MONTHLY (is_default=TRUE) + YEARLY (descuento 16.67%)
-- ────────────────────────────────────────────────────────────────────────────
-- PERSONAL v1.0: $5/mes o $50/anio (2 meses gratis)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
  ('77777777-7777-7777-7777-100000000001', '55555555-5555-5555-5555-100000000004', 'MONTHLY',  5.00,  0.00, TRUE),
  ('77777777-7777-7777-7777-100000000002', '55555555-5555-5555-5555-100000000004', 'YEARLY',  50.00, 16.67, FALSE)
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price=EXCLUDED.base_price, discount_pct=EXCLUDED.discount_pct, is_default=EXCLUDED.is_default;
-- TEAM v1.0: $49/mes o $490/anio (2 meses gratis)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
  ('77777777-7777-7777-7777-100000000003', '55555555-5555-5555-5555-100000000005', 'MONTHLY',  49.00,  0.00, TRUE),
  ('77777777-7777-7777-7777-100000000004', '55555555-5555-5555-5555-100000000005', 'YEARLY',  490.00, 16.67, FALSE)
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price=EXCLUDED.base_price, discount_pct=EXCLUDED.discount_pct, is_default=EXCLUDED.is_default;
-- BUSINESS v1.0: $149/mes o $1490/anio (2 meses gratis)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
  ('77777777-7777-7777-7777-100000000005', '55555555-5555-5555-5555-100000000002', 'MONTHLY',  149.00,  0.00, TRUE),
  ('77777777-7777-7777-7777-100000000006', '55555555-5555-5555-5555-100000000002', 'YEARLY',  1490.00, 16.67, FALSE)
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price=EXCLUDED.base_price, discount_pct=EXCLUDED.discount_pct, is_default=EXCLUDED.is_default;
-- ENTERPRISE v1.0: solo YEARLY (precio por negociacion)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
VALUES
  ('77777777-7777-7777-7777-100000000007', '55555555-5555-5555-5555-100000000003', 'YEARLY', 0.00, 0.00, TRUE)
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price=EXCLUDED.base_price, discount_pct=EXCLUDED.discount_pct, is_default=EXCLUDED.is_default;
-- ────────────────────────────────────────────────────────────────────────────
-- 4. ENTITLEMENTS — FREE v1.0
--    1 tenant | 1 app | 3 identidades | 1 admin | sin SLA | 7 dias logs
-- ────────────────────────────────────────────────────────────────────────────
WITH pv AS (SELECT '55555555-5555-5555-5555-100000000001'::UUID AS id)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM pv,
(VALUES
  ('MAX_TENANTS',        'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_TENANT_USERS',   'QUOTA',      3::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_ADMINS',         'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA',   1000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', FALSE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', FALSE),
  ('SLA_UPTIME_PCT',     'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', FALSE),
  ('AUDIT_LOG_DAYS',     'QUOTA',      7::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;
-- ────────────────────────────────────────────────────────────────────────────
-- 5. ENTITLEMENTS — PERSONAL v1.0
--    1 tenant | 3 apps | 5 identidades | 1 admin | sin SLA | 14 dias logs
-- ────────────────────────────────────────────────────────────────────────────
WITH pv AS (SELECT '55555555-5555-5555-5555-100000000004'::UUID AS id)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM pv,
(VALUES
  ('MAX_TENANTS',        'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',      3::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_TENANT_USERS',   'QUOTA',      5::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_ADMINS',         'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA',  10000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', FALSE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', FALSE),
  ('SLA_UPTIME_PCT',     'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', FALSE),
  ('AUDIT_LOG_DAYS',     'QUOTA',     14::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;
-- ────────────────────────────────────────────────────────────────────────────
-- 6. ENTITLEMENTS — TEAM v1.0
--    1 tenant | 10 apps | 25 identidades | 3 admins | SLA 99% | 30 dias
-- ────────────────────────────────────────────────────────────────────────────
WITH pv AS (SELECT '55555555-5555-5555-5555-100000000005'::UUID AS id)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM pv,
(VALUES
  ('MAX_TENANTS',        'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',     10::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_TENANT_USERS',   'QUOTA',     25::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_ADMINS',         'QUOTA',      3::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA', 100000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', FALSE),
  ('SLA_UPTIME_PCT',     'QUOTA',     99::BIGINT, 'NONE',  'SOFT', TRUE),
  ('AUDIT_LOG_DAYS',     'QUOTA',     30::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;
-- ────────────────────────────────────────────────────────────────────────────
-- 7. ENTITLEMENTS — BUSINESS v1.0
--    1 tenant | 30 apps | 100 identidades | 10 admins | SLA 99.9% | 90 dias
-- ────────────────────────────────────────────────────────────────────────────
WITH pv AS (SELECT '55555555-5555-5555-5555-100000000002'::UUID AS id)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM pv,
(VALUES
  ('MAX_TENANTS',        'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',     30::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_TENANT_USERS',   'QUOTA',    100::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_ADMINS',         'QUOTA',     10::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA', 500000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',     'QUOTA',    999::BIGINT, 'NONE',  'SOFT', TRUE),
  ('AUDIT_LOG_DAYS',     'QUOTA',     90::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;
-- ────────────────────────────────────────────────────────────────────────────
-- 8. ENTITLEMENTS — FLEX v1.0 (pago por uso)
--    Limites SOFT (sin bloqueo; se registra para facturacion).
--    Tarifas de volumen en centavos de USD (BIGINT):
--      Tenants:   $8/u (1-10) | $6/u (11-50) | $4/u (51+)
--      Apps:      $2/u (1-20) | $1.50/u (21-100) | $1/u (101+)
--      Identes:   $1.20/u (1-100) | $0.90/u (101-500) | $0.60/u (501+)
--      Admins:    1 incluido/tenant; extras $4/u
-- ────────────────────────────────────────────────────────────────────────────
WITH pv AS (SELECT '55555555-5555-5555-5555-100000000006'::UUID AS id)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM pv,
(VALUES
  ('MAX_TENANTS',                    'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_CLIENT_APPS',                'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_TENANT_USERS',               'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_MONTHLY_TOKENS',             'QUOTA',   NULL::BIGINT, 'MONTH', 'SOFT', TRUE),
  ('SOCIAL_LOGIN',                   'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',                  'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',                 'QUOTA',    999::BIGINT, 'NONE',  'SOFT', TRUE),
  ('AUDIT_LOG_DAYS',                 'QUOTA',     90::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_TENANT_T1_MAX',             'QUOTA',     10::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_TENANT_RATE_T1',            'RATE',      800::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_TENANT_T2_MAX',             'QUOTA',     50::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_TENANT_RATE_T2',            'RATE',      600::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_TENANT_RATE_T3',            'RATE',      400::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_APP_T1_MAX',                'QUOTA',     20::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_APP_RATE_T1',               'RATE',      200::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_APP_T2_MAX',                'QUOTA',    100::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_APP_RATE_T2',               'RATE',      150::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_APP_RATE_T3',               'RATE',      100::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_IDENTITY_T1_MAX',           'QUOTA',    100::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_IDENTITY_RATE_T1',          'RATE',      120::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_IDENTITY_T2_MAX',           'QUOTA',    500::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_IDENTITY_RATE_T2',          'RATE',       90::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_IDENTITY_RATE_T3',          'RATE',       60::BIGINT,'NONE',  'SOFT', TRUE),
  ('FLEX_ADMIN_INCLUDED_PER_TENANT', 'QUOTA',      1::BIGINT, 'NONE',  'SOFT', TRUE),
  ('FLEX_ADMIN_RATE',                'RATE',       400::BIGINT,'NONE', 'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;
-- ────────────────────────────────────────────────────────────────────────────
-- 9. ENTITLEMENTS — ENTERPRISE v1.0
--    Sin limites (NULL = ilimitado). SLA 99.9% garantizado (HARD).
--    Soporte prioritario + Customer Success Manager incluidos.
-- ────────────────────────────────────────────────────────────────────────────
WITH pv AS (SELECT '55555555-5555-5555-5555-100000000003'::UUID AS id)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM pv,
(VALUES
  ('MAX_TENANTS',             'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_CLIENT_APPS',         'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_TENANT_USERS',        'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_ADMINS',              'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_MONTHLY_TOKENS',      'QUOTA',   NULL::BIGINT, 'MONTH', 'SOFT', TRUE),
  ('SOCIAL_LOGIN',            'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',           'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',          'QUOTA',    999::BIGINT, 'NONE',  'HARD', TRUE),
  ('AUDIT_LOG_DAYS',          'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('PRIORITY_SUPPORT',        'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_SLA',              'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('DEDICATED_SUCCESS_MGR',   'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;
