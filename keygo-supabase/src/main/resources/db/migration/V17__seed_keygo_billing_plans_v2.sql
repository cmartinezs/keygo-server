-- =============================================================================
-- V18: Seed — Escalera de planes corregida para keygo-ui (v2)
--
-- Reemplaza el catálogo de V17 (FREE/STARTER/BUSINESS/ENTERPRISE) con la
-- escalera comercialmente coherente:
--
--   Free (US$0/mes) · Personal (US$5/mes) · Team (US$49/mes)
--   Business (US$149/mes) · Flex (pago por uso) · Enterprise (custom/anual)
--
-- Límites por plan:
--   Plan       | Tenants | Apps | Identidades activas | Admins
--   Free       |    1    |   1  |          3          |    1
--   Personal   |    1    |   3  |          5          |    1
--   Team       |    1    |  10  |         25          |    3
--   Business   |    1    |  30  |        100          |   10
--   Flex       |  ilim.  | ilim.|        ilim.        | 1/tenant + $4 c/u extra
--   Enterprise | custom  |cust. |        custom       | custom
--
-- Acciones:
--   1. Deprecar versiones v1.0 de V17 (referenciadas por code + client_id)
--   2. Desactivar plan STARTER (reemplazado por PERSONAL)
--   3. Actualizar planes FREE, BUSINESS, ENTERPRISE (descripción + currency USD)
--   4. Insertar planes nuevos: PERSONAL, TEAM, FLEX
--   5. Insertar versiones v2.0 (FREE, BUSINESS, ENTERPRISE) y v1.0 (PERSONAL, TEAM, FLEX)
--   6-11. Insertar entitlements para todas las versiones nuevas
--
-- Convención de FK: nunca se hardcodean UUIDs de FK; se resuelven con
-- subqueries SELECT sobre el campo semántico del padre (client_id, code, version).
-- Los únicos UUIDs fijos son los PKs (columna id) para estabilidad entre resets.
--
-- PKs estables:
--   Plans (nuevos):   PERSONAL=44444444-4444-4444-4444-100000000001
--                     TEAM=44444444-4444-4444-4444-100000000002
--                     FLEX=44444444-4444-4444-4444-100000000003
--   Versions (v2.0):  FREE v2.0=55555555-5555-5555-5555-100000000001
--                     BUSINESS v2.0=55555555-5555-5555-5555-100000000002
--                     ENTERPRISE v2.0=55555555-5555-5555-5555-100000000003
--   Versions (v1.0):  PERSONAL v1.0=55555555-5555-5555-5555-100000000004
--                     TEAM v1.0=55555555-5555-5555-5555-100000000005
--                     FLEX v1.0=55555555-5555-5555-5555-100000000006
-- =============================================================================


-- ────────────────────────────────────────────────────────────────────────────
-- 1. Deprecar versiones v1.0 de V17
--    Referencia: app_plan_versions → app_plans.code + client_apps.client_id + version
-- ────────────────────────────────────────────────────────────────────────────
UPDATE app_plan_versions apv
SET
    status       = 'DEPRECATED',
    effective_to = '2026-03-28'
WHERE apv.id IN (
    SELECT apv2.id
    FROM app_plan_versions apv2
    JOIN app_plans ap ON ap.id = apv2.app_plan_id
    JOIN client_apps ca ON ca.id = ap.client_app_id
    WHERE ca.client_id = 'keygo-ui'
      AND ap.code IN ('FREE', 'STARTER', 'BUSINESS', 'ENTERPRISE')
      AND apv2.version = '1.0'
)
AND apv.status != 'DEPRECATED';


-- ────────────────────────────────────────────────────────────────────────────
-- 2. Desactivar plan STARTER (reemplazado por PERSONAL en la nueva escalera)
--    Referencia: app_plans.code + client_apps.client_id
-- ────────────────────────────────────────────────────────────────────────────
UPDATE app_plans ap
SET
    status     = 'INACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE ap.id = (
    SELECT ap2.id FROM app_plans ap2
    JOIN client_apps ca ON ca.id = ap2.client_app_id
    WHERE ca.client_id = 'keygo-ui' AND ap2.code = 'STARTER'
)
AND ap.status != 'INACTIVE';


-- ────────────────────────────────────────────────────────────────────────────
-- 3. Actualizar planes existentes (descripción y nombre)
--    Referencia: app_plans.code + client_apps.client_id
-- ────────────────────────────────────────────────────────────────────────────
UPDATE app_plans SET
    name        = 'Free',
    description = 'Explora KeyGo sin costo ni tarjeta de crédito. Hasta 1 app y 3 identidades.',
    updated_at  = CURRENT_TIMESTAMP
WHERE id = (
    SELECT ap.id FROM app_plans ap
    JOIN client_apps ca ON ca.id = ap.client_app_id
    WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FREE'
);

UPDATE app_plans SET
    name        = 'Business',
    description = 'Para empresas reales y SaaS en crecimiento. Hasta 30 apps, 100 identidades y 10 admins.',
    updated_at  = CURRENT_TIMESTAMP
WHERE id = (
    SELECT ap.id FROM app_plans ap
    JOIN client_apps ca ON ca.id = ap.client_app_id
    WHERE ca.client_id = 'keygo-ui' AND ap.code = 'BUSINESS'
);

UPDATE app_plans SET
    name        = 'Enterprise',
    description = 'Para grandes organizaciones. Contrato anual, límites personalizados, SLA garantizado y soporte prioritario.',
    updated_at  = CURRENT_TIMESTAMP
WHERE id = (
    SELECT ap.id FROM app_plans ap
    JOIN client_apps ca ON ca.id = ap.client_app_id
    WHERE ca.client_id = 'keygo-ui' AND ap.code = 'ENTERPRISE'
);


-- ────────────────────────────────────────────────────────────────────────────
-- 4. Insertar planes nuevos: PERSONAL, TEAM, FLEX
--    FK client_app_id → client_apps.client_id (subquery)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plans (id, client_app_id, code, name, description, status, is_public)
VALUES
  (
    '44444444-4444-4444-4444-100000000001',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'PERSONAL', 'Personal',
    'Entrada ultra agresiva para proyectos individuales y side-projects. Hasta 3 apps y 5 identidades.',
    'ACTIVE', TRUE
  ),
  (
    '44444444-4444-4444-4444-100000000002',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'TEAM', 'Team',
    'Ideal para pymes y SaaS pequeños. Hasta 10 apps, 25 identidades y 3 admins.',
    'ACTIVE', TRUE
  ),
  (
    '44444444-4444-4444-4444-100000000003',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'FLEX', 'Flex',
    'Para agencias, software factories y consultoras multi-cliente. Pago por uso con tarifas escalonadas. Todas las features incluidas sin límite fijo.',
    'ACTIVE', TRUE
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
    name        = EXCLUDED.name,
    description = EXCLUDED.description,
    status      = EXCLUDED.status,
    is_public   = EXCLUDED.is_public,
    updated_at  = CURRENT_TIMESTAMP;


-- ────────────────────────────────────────────────────────────────────────────
-- 5. Insertar versiones de plan
--    FK app_plan_id → app_plans.code + client_apps.client_id (subquery)
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_versions
    (id, app_plan_id, version, currency, billing_period, base_price, setup_fee, trial_days, effective_from, status)
VALUES
  -- FREE v2.0
  (
    '55555555-5555-5555-5555-100000000001',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FREE'),
    '2.0', 'USD', 'MONTHLY', 0.00, 0.00, 0, '2026-03-29', 'ACTIVE'
  ),
  -- BUSINESS v2.0 (límites corregidos: 30 apps · 100 identidades · 10 admins)
  (
    '55555555-5555-5555-5555-100000000002',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'BUSINESS'),
    '2.0', 'USD', 'MONTHLY', 149.00, 0.00, 14, '2026-03-29', 'ACTIVE'
  ),
  -- ENTERPRISE v2.0 (YEARLY · base $0 · precio por contrato)
  (
    '55555555-5555-5555-5555-100000000003',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'ENTERPRISE'),
    '2.0', 'USD', 'YEARLY', 0.00, 0.00, 30, '2026-03-29', 'ACTIVE'
  ),
  -- PERSONAL v1.0
  (
    '55555555-5555-5555-5555-100000000004',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'PERSONAL'),
    '1.0', 'USD', 'MONTHLY', 5.00, 0.00, 14, '2026-03-29', 'ACTIVE'
  ),
  -- TEAM v1.0
  (
    '55555555-5555-5555-5555-100000000005',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'TEAM'),
    '1.0', 'USD', 'MONTHLY', 49.00, 0.00, 14, '2026-03-29', 'ACTIVE'
  ),
  -- FLEX v1.0 (pago por uso · base $0)
  (
    '55555555-5555-5555-5555-100000000006',
    (SELECT ap.id FROM app_plans ap JOIN client_apps ca ON ca.id = ap.client_app_id WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FLEX'),
    '1.0', 'USD', 'MONTHLY', 0.00, 0.00, 0, '2026-03-29', 'ACTIVE'
  )
ON CONFLICT (app_plan_id, version) DO UPDATE
SET
    currency       = EXCLUDED.currency,
    billing_period = EXCLUDED.billing_period,
    base_price     = EXCLUDED.base_price,
    setup_fee      = EXCLUDED.setup_fee,
    trial_days     = EXCLUDED.trial_days,
    effective_from = EXCLUDED.effective_from,
    status         = EXCLUDED.status;


-- ────────────────────────────────────────────────────────────────────────────
-- 6. Entitlements — FREE v2.0
--    1 tenant · 1 app · 3 identidades · 1 admin · sin SLA · 7 días de logs
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FREE' AND apv.version = '2.0'
)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
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
-- 7. Entitlements — PERSONAL v1.0
--    1 tenant · 3 apps · 5 identidades · 1 admin · sin SLA · 14 días de logs
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'PERSONAL' AND apv.version = '1.0'
)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
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
-- 8. Entitlements — TEAM v1.0
--    1 tenant · 10 apps · 25 identidades · 3 admins · SLA 99% · 30 días logs
--    Social login incluido · sin dominio propio
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'TEAM' AND apv.version = '1.0'
)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
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
-- 9. Entitlements — BUSINESS v2.0
--    1 tenant · 30 apps · 100 identidades · 10 admins · SLA 99.9% · 90 días
--    Social login + dominio propio incluidos
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'BUSINESS' AND apv.version = '2.0'
)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
(VALUES
  ('MAX_TENANTS',        'QUOTA',      1::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_CLIENT_APPS',    'QUOTA',     30::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_TENANT_USERS',   'QUOTA',    100::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_ADMINS',         'QUOTA',     10::BIGINT, 'NONE',  'HARD', TRUE),
  ('MAX_MONTHLY_TOKENS', 'QUOTA', 500000::BIGINT, 'MONTH', 'HARD', TRUE),
  ('SOCIAL_LOGIN',       'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',      'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',     'QUOTA',    999::BIGINT, 'NONE',  'SOFT', TRUE),  -- 99.9%
  ('AUDIT_LOG_DAYS',     'QUOTA',     90::BIGINT, 'NONE',  'SOFT', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;


-- ────────────────────────────────────────────────────────────────────────────
-- 10. Entitlements — FLEX v1.0 (pago por uso)
--
--    Límites estándar: SOFT (no bloquea; se registra para facturación).
--    Tarifas de volumen en centavos de USD (BIGINT):
--      800¢=$8.00 · 600¢=$6.00 · 400¢=$4.00 · 200¢=$2.00 · 150¢=$1.50
--      120¢=$1.20 · 100¢=$1.00 ·  90¢=$0.90 ·  60¢=$0.60
--    FLEX_*_T{n}_MAX : límite superior del tramo n (inclusive).
--    FLEX_*_RATE_T{n}: tarifa por unidad dentro del tramo.
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'FLEX' AND apv.version = '1.0'
)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
(VALUES
  -- Límites globales (SOFT: sin bloqueo, para monitoreo y facturación)
  ('MAX_TENANTS',                    'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_CLIENT_APPS',                'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_TENANT_USERS',               'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_MONTHLY_TOKENS',             'QUOTA',   NULL::BIGINT, 'MONTH', 'SOFT', TRUE),
  -- Features incluidas en Flex
  ('SOCIAL_LOGIN',                   'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',                  'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',                 'QUOTA',    999::BIGINT, 'NONE',  'SOFT', TRUE),  -- 99.9%
  ('AUDIT_LOG_DAYS',                 'QUOTA',     90::BIGINT, 'NONE',  'SOFT', TRUE),
  -- Tarifas por tenant (centavos USD) — tramos escalonados
  ('FLEX_TENANT_T1_MAX',             'QUOTA',     10::BIGINT, 'NONE',  'SOFT', TRUE),  -- 1-10 tenants
  ('FLEX_TENANT_RATE_T1',            'RATE',      800::BIGINT,'NONE',  'SOFT', TRUE),  -- $8.00/u
  ('FLEX_TENANT_T2_MAX',             'QUOTA',     50::BIGINT, 'NONE',  'SOFT', TRUE),  -- 11-50 tenants
  ('FLEX_TENANT_RATE_T2',            'RATE',      600::BIGINT,'NONE',  'SOFT', TRUE),  -- $6.00/u
  ('FLEX_TENANT_RATE_T3',            'RATE',      400::BIGINT,'NONE',  'SOFT', TRUE),  -- 51+ → $4.00/u
  -- Tarifas por app (centavos USD)
  ('FLEX_APP_T1_MAX',                'QUOTA',     20::BIGINT, 'NONE',  'SOFT', TRUE),  -- 1-20 apps
  ('FLEX_APP_RATE_T1',               'RATE',      200::BIGINT,'NONE',  'SOFT', TRUE),  -- $2.00/u
  ('FLEX_APP_T2_MAX',                'QUOTA',    100::BIGINT, 'NONE',  'SOFT', TRUE),  -- 21-100 apps
  ('FLEX_APP_RATE_T2',               'RATE',      150::BIGINT,'NONE',  'SOFT', TRUE),  -- $1.50/u
  ('FLEX_APP_RATE_T3',               'RATE',      100::BIGINT,'NONE',  'SOFT', TRUE),  -- 101+ → $1.00/u
  -- Tarifas por identidad activa (centavos USD)
  ('FLEX_IDENTITY_T1_MAX',           'QUOTA',    100::BIGINT, 'NONE',  'SOFT', TRUE),  -- 1-100 idents
  ('FLEX_IDENTITY_RATE_T1',          'RATE',      120::BIGINT,'NONE',  'SOFT', TRUE),  -- $1.20/u
  ('FLEX_IDENTITY_T2_MAX',           'QUOTA',    500::BIGINT, 'NONE',  'SOFT', TRUE),  -- 101-500 idents
  ('FLEX_IDENTITY_RATE_T2',          'RATE',       90::BIGINT,'NONE',  'SOFT', TRUE),  -- $0.90/u
  ('FLEX_IDENTITY_RATE_T3',          'RATE',       60::BIGINT,'NONE',  'SOFT', TRUE),  -- 501+ → $0.60/u
  -- Tarifas por admin (centavos USD)
  ('FLEX_ADMIN_INCLUDED_PER_TENANT', 'QUOTA',      1::BIGINT, 'NONE',  'SOFT', TRUE),  -- 1 admin incluido/tenant
  ('FLEX_ADMIN_RATE',                'RATE',       400::BIGINT,'NONE', 'SOFT', TRUE)   -- extra → $4.00/u
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;


-- ────────────────────────────────────────────────────────────────────────────
-- 11. Entitlements — ENTERPRISE v2.0
--     Sin límites (NULL = ilimitado). SLA 99.9% garantizado (HARD).
--     Soporte prioritario + Customer Success Manager incluidos.
-- ────────────────────────────────────────────────────────────────────────────
WITH plan_version AS (
  SELECT apv.id FROM app_plan_versions apv
  JOIN app_plans ap ON ap.id = apv.app_plan_id
  JOIN client_apps ca ON ca.id = ap.client_app_id
  WHERE ca.client_id = 'keygo-ui' AND ap.code = 'ENTERPRISE' AND apv.version = '2.0'
)
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), pv.id, e.metric_code, e.metric_type, e.limit_value, e.period_type, e.enforcement_mode, e.is_enabled
FROM plan_version pv,
(VALUES
  ('MAX_TENANTS',             'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_CLIENT_APPS',         'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_TENANT_USERS',        'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_ADMINS',              'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),
  ('MAX_MONTHLY_TOKENS',      'QUOTA',   NULL::BIGINT, 'MONTH', 'SOFT', TRUE),
  ('SOCIAL_LOGIN',            'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_DOMAIN',           'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('SLA_UPTIME_PCT',          'QUOTA',    999::BIGINT, 'NONE',  'HARD', TRUE),  -- 99.9% GARANTIZADO
  ('AUDIT_LOG_DAYS',          'QUOTA',   NULL::BIGINT, 'NONE',  'SOFT', TRUE),  -- retención indefinida
  ('PRIORITY_SUPPORT',        'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('CUSTOM_SLA',              'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE),
  ('DEDICATED_SUCCESS_MGR',   'BOOLEAN', NULL::BIGINT, 'NONE',  'HARD', TRUE)
) AS e(metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;

