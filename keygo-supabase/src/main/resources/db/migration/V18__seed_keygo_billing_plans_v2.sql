-- =============================================================================
-- V18: Seed — Escalera de planes corregida para keygo-platform (v2)
--
-- Reemplaza el catálogo de V17 (FREE/STARTER/BUSINESS/ENTERPRISE) con la
-- escalera comercialmente coherente definida en el análisis de billing:
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
--   1. Deprecar versiones v1.0 de V17 y cerrar su effective_to
--   2. Desactivar plan STARTER (renombrado / reemplazado por PERSONAL)
--   3. Actualizar planes FREE, BUSINESS, ENTERPRISE (descripción + currency USD)
--   4. Insertar planes nuevos: PERSONAL, TEAM, FLEX
--   5. Insertar versiones v2.0 (FREE, BUSINESS, ENTERPRISE) y
--      v1.0 (PERSONAL, TEAM, FLEX)
--   6. Insertar entitlements para todas las versiones nuevas
--
-- Tarifas Flex almacenadas en centavos de USD (BIGINT):
--   800 = $8.00 · 600 = $6.00 · 400 = $4.00 · 200 = $2.00 · 150 = $1.50
--   120 = $1.20 · 100 = $1.00 · 90  = $0.90 · 60  = $0.60 · 400 = $4.00
--
-- UUIDs estables:
-- ── Plans (existentes desde V17) ────────────────────────────────────────────
--   FREE       : 22222222-2222-2222-2222-100000000001  → se mantiene ACTIVE
--   STARTER    : 22222222-2222-2222-2222-100000000002  → pasa a INACTIVE
--   BUSINESS   : 22222222-2222-2222-2222-100000000003  → se mantiene ACTIVE
--   ENTERPRISE : 22222222-2222-2222-2222-100000000004  → se mantiene ACTIVE
-- ── Plans (nuevos) ──────────────────────────────────────────────────────────
--   PERSONAL   : 44444444-4444-4444-4444-100000000001
--   TEAM       : 44444444-4444-4444-4444-100000000002
--   FLEX       : 44444444-4444-4444-4444-100000000003
-- ── Versions (existentes desde V17, serán DEPRECATED) ───────────────────────
--   FREE v1.0       : 33333333-3333-3333-3333-100000000001
--   STARTER v1.0    : 33333333-3333-3333-3333-100000000002
--   BUSINESS v1.0   : 33333333-3333-3333-3333-100000000003
--   ENTERPRISE v1.0 : 33333333-3333-3333-3333-100000000004
-- ── Versions (nuevas) ───────────────────────────────────────────────────────
--   FREE v2.0       : 55555555-5555-5555-5555-100000000001
--   BUSINESS v2.0   : 55555555-5555-5555-5555-100000000002
--   ENTERPRISE v2.0 : 55555555-5555-5555-5555-100000000003
--   PERSONAL v1.0   : 55555555-5555-5555-5555-100000000004
--   TEAM v1.0       : 55555555-5555-5555-5555-100000000005
--   FLEX v1.0       : 55555555-5555-5555-5555-100000000006
-- =============================================================================


-- ────────────────────────────────────────────────────────────────────────────
-- 1. Deprecar versiones v1.0 de V17 y cerrar su effective_to
-- ────────────────────────────────────────────────────────────────────────────
UPDATE app_plan_versions
SET
    status       = 'DEPRECATED',
    effective_to = '2026-03-28'          -- último día en vigor (day before v2.0)
WHERE id IN (
    '33333333-3333-3333-3333-100000000001',   -- FREE v1.0
    '33333333-3333-3333-3333-100000000002',   -- STARTER v1.0
    '33333333-3333-3333-3333-100000000003',   -- BUSINESS v1.0
    '33333333-3333-3333-3333-100000000004'    -- ENTERPRISE v1.0
)
AND status != 'DEPRECATED';


-- ────────────────────────────────────────────────────────────────────────────
-- 2. Desactivar plan STARTER (reemplazado por PERSONAL en la nueva escalera)
-- ────────────────────────────────────────────────────────────────────────────
UPDATE app_plans
SET
    status     = 'INACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE id = '22222222-2222-2222-2222-100000000002'
  AND status != 'INACTIVE';


-- ────────────────────────────────────────────────────────────────────────────
-- 3. Actualizar planes existentes (descripción y estado)
-- ────────────────────────────────────────────────────────────────────────────
UPDATE app_plans
SET
    name        = 'Free',
    description = 'Explora KeyGo sin costo ni tarjeta de crédito. Hasta 1 app y 3 identidades.',
    updated_at  = CURRENT_TIMESTAMP
WHERE id = '22222222-2222-2222-2222-100000000001';

UPDATE app_plans
SET
    name        = 'Business',
    description = 'Para empresas reales y SaaS en crecimiento. Hasta 30 apps, 100 identidades y 10 admins.',
    updated_at  = CURRENT_TIMESTAMP
WHERE id = '22222222-2222-2222-2222-100000000003';

UPDATE app_plans
SET
    name        = 'Enterprise',
    description = 'Para grandes organizaciones. Contrato anual, límites personalizados, SLA garantizado y soporte prioritario.',
    updated_at  = CURRENT_TIMESTAMP
WHERE id = '22222222-2222-2222-2222-100000000004';


-- ────────────────────────────────────────────────────────────────────────────
-- 4. Insertar planes nuevos: PERSONAL, TEAM, FLEX
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plans (id, client_app_id, code, name, description, subscriber_type, status, is_public)
VALUES
  (
    '44444444-4444-4444-4444-100000000001',
    '11111111-1111-1111-1111-333333333333',
    'PERSONAL',
    'Personal',
    'Entrada ultra agresiva para proyectos individuales y side-projects. Hasta 3 apps y 5 identidades.',
    'TENANT', 'ACTIVE', TRUE
  ),
  (
    '44444444-4444-4444-4444-100000000002',
    '11111111-1111-1111-1111-333333333333',
    'TEAM',
    'Team',
    'Ideal para pymes y SaaS pequeños. Hasta 10 apps, 25 identidades y 3 admins.',
    'TENANT', 'ACTIVE', TRUE
  ),
  (
    '44444444-4444-4444-4444-100000000003',
    '11111111-1111-1111-1111-333333333333',
    'FLEX',
    'Flex',
    'Para agencias, software factories y consultoras multi-cliente. Pago por uso con tarifas escalonadas. Todas las features incluidas sin límite fijo.',
    'TENANT', 'ACTIVE', TRUE
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
    name            = EXCLUDED.name,
    description     = EXCLUDED.description,
    subscriber_type = EXCLUDED.subscriber_type,
    status          = EXCLUDED.status,
    is_public       = EXCLUDED.is_public,
    updated_at      = CURRENT_TIMESTAMP;


-- ────────────────────────────────────────────────────────────────────────────
-- 5. Insertar versiones de plan
--    v2.0: planes actualizados (FREE, BUSINESS) y ENTERPRISE (ahora YEARLY)
--    v1.0: planes nuevos (PERSONAL, TEAM, FLEX)
--
--    Precios en USD (currency = 'USD').
--    Enterprise: base_price = 0 — el precio real se negocia por contrato.
--    Flex:       base_price = 0 — billing completamente por uso.
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_versions
    (id, app_plan_id, version, currency, billing_period, base_price, setup_fee, trial_days, effective_from, status)
VALUES
  -- FREE v2.0
  (
    '55555555-5555-5555-5555-100000000001',
    '22222222-2222-2222-2222-100000000001',
    '2.0', 'USD', 'MONTHLY', 0.00, 0.00, 0, '2026-03-29', 'ACTIVE'
  ),
  -- BUSINESS v2.0  (límites corregidos: 30 apps · 100 identidades · 10 admins)
  (
    '55555555-5555-5555-5555-100000000002',
    '22222222-2222-2222-2222-100000000003',
    '2.0', 'USD', 'MONTHLY', 149.00, 0.00, 14, '2026-03-29', 'ACTIVE'
  ),
  -- ENTERPRISE v2.0  (YEARLY · base $0 · precio por contrato)
  (
    '55555555-5555-5555-5555-100000000003',
    '22222222-2222-2222-2222-100000000004',
    '2.0', 'USD', 'YEARLY', 0.00, 0.00, 30, '2026-03-29', 'ACTIVE'
  ),
  -- PERSONAL v1.0
  (
    '55555555-5555-5555-5555-100000000004',
    '44444444-4444-4444-4444-100000000001',
    '1.0', 'USD', 'MONTHLY', 5.00, 0.00, 14, '2026-03-29', 'ACTIVE'
  ),
  -- TEAM v1.0
  (
    '55555555-5555-5555-5555-100000000005',
    '44444444-4444-4444-4444-100000000002',
    '1.0', 'USD', 'MONTHLY', 49.00, 0.00, 14, '2026-03-29', 'ACTIVE'
  ),
  -- FLEX v1.0  (pago por uso · base $0)
  (
    '55555555-5555-5555-5555-100000000006',
    '44444444-4444-4444-4444-100000000003',
    '1.0', 'USD', 'MONTHLY', 0.00, 0.00, 0, '2026-03-29', 'ACTIVE'
  )
ON CONFLICT (id) DO UPDATE
SET
    version        = EXCLUDED.version,
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
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'MAX_TENANTS',       'QUOTA',   1,    'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'MAX_CLIENT_APPS',   'QUOTA',   1,    'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'MAX_TENANT_USERS',  'QUOTA',   3,    'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'MAX_ADMINS',        'QUOTA',   1,    'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'MAX_MONTHLY_TOKENS','QUOTA',   1000, 'MONTH', 'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'SOCIAL_LOGIN',      'BOOLEAN', NULL, 'NONE',  'HARD', FALSE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'CUSTOM_DOMAIN',     'BOOLEAN', NULL, 'NONE',  'HARD', FALSE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'SLA_UPTIME_PCT',    'QUOTA',   NULL, 'NONE',  'SOFT', FALSE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000001', 'AUDIT_LOG_DAYS',    'QUOTA',   7,    'NONE',  'SOFT', TRUE)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;


-- ────────────────────────────────────────────────────────────────────────────
-- 7. Entitlements — PERSONAL v1.0
--    1 tenant · 3 apps · 5 identidades · 1 admin · sin SLA · 14 días de logs
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'MAX_TENANTS',       'QUOTA',   1,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'MAX_CLIENT_APPS',   'QUOTA',   3,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'MAX_TENANT_USERS',  'QUOTA',   5,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'MAX_ADMINS',        'QUOTA',   1,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'MAX_MONTHLY_TOKENS','QUOTA',   10000, 'MONTH', 'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'SOCIAL_LOGIN',      'BOOLEAN', NULL,  'NONE',  'HARD', FALSE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'CUSTOM_DOMAIN',     'BOOLEAN', NULL,  'NONE',  'HARD', FALSE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'SLA_UPTIME_PCT',    'QUOTA',   NULL,  'NONE',  'SOFT', FALSE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000004', 'AUDIT_LOG_DAYS',    'QUOTA',   14,    'NONE',  'SOFT', TRUE)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;


-- ────────────────────────────────────────────────────────────────────────────
-- 8. Entitlements — TEAM v1.0
--    1 tenant · 10 apps · 25 identidades · 3 admins · SLA 99 % · 30 días logs
--    Social login incluido · sin dominio propio
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'MAX_TENANTS',       'QUOTA',   1,      'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'MAX_CLIENT_APPS',   'QUOTA',   10,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'MAX_TENANT_USERS',  'QUOTA',   25,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'MAX_ADMINS',        'QUOTA',   3,      'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'MAX_MONTHLY_TOKENS','QUOTA',   100000, 'MONTH', 'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'SOCIAL_LOGIN',      'BOOLEAN', NULL,   'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'CUSTOM_DOMAIN',     'BOOLEAN', NULL,   'NONE',  'HARD', FALSE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'SLA_UPTIME_PCT',    'QUOTA',   99,     'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000005', 'AUDIT_LOG_DAYS',    'QUOTA',   30,     'NONE',  'SOFT', TRUE)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;


-- ────────────────────────────────────────────────────────────────────────────
-- 9. Entitlements — BUSINESS v2.0
--    1 tenant · 30 apps · 100 identidades · 10 admins · SLA 99.9 % · 90 días
--    Social login + dominio propio incluidos
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'MAX_TENANTS',       'QUOTA',   1,      'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'MAX_CLIENT_APPS',   'QUOTA',   30,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'MAX_TENANT_USERS',  'QUOTA',   100,    'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'MAX_ADMINS',        'QUOTA',   10,     'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'MAX_MONTHLY_TOKENS','QUOTA',   500000, 'MONTH', 'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'SOCIAL_LOGIN',      'BOOLEAN', NULL,   'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'CUSTOM_DOMAIN',     'BOOLEAN', NULL,   'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'SLA_UPTIME_PCT',    'QUOTA',   999,    'NONE',  'SOFT', TRUE),  -- 99.9 %
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000002', 'AUDIT_LOG_DAYS',    'QUOTA',   90,     'NONE',  'SOFT', TRUE)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;


-- ────────────────────────────────────────────────────────────────────────────
-- 10. Entitlements — FLEX v1.0 (pago por uso)
--
--    Límites estándar: SOFT (no bloquea; se registra para facturación).
--    Tarifas de volumen en centavos de USD (BIGINT):
--      800 ¢ = $8.00  |  600 ¢ = $6.00  |  400 ¢ = $4.00
--      200 ¢ = $2.00  |  150 ¢ = $1.50  |  100 ¢ = $1.00
--      120 ¢ = $1.20  |   90 ¢ = $0.90  |   60 ¢ = $0.60
--    FLEX_*_T{n}_MAX : límite superior del tramo n (inclusive).
--    FLEX_*_RATE_T{n}: tarifa por unidad dentro del tramo.
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  -- ── Límites globales (SOFT: sin bloqueo, para monitoreo y facturación) ───
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'MAX_TENANTS',                   'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'MAX_CLIENT_APPS',               'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'MAX_TENANT_USERS',              'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'MAX_MONTHLY_TOKENS',            'QUOTA',   NULL, 'MONTH', 'SOFT', TRUE),
  -- ── Features incluidas en Flex ───────────────────────────────────────────
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'SOCIAL_LOGIN',                  'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'CUSTOM_DOMAIN',                 'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'SLA_UPTIME_PCT',                'QUOTA',   999,  'NONE',  'SOFT', TRUE),   -- 99.9 %
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'AUDIT_LOG_DAYS',                'QUOTA',   90,   'NONE',  'SOFT', TRUE),
  -- ── Tarifas por tenant (centavos USD) ────────────────────────────────────
  --    Tramo 1: 1-10 tenants → $8.00 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_TENANT_T1_MAX',            'QUOTA',   10,   'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_TENANT_RATE_T1',           'RATE',    800,  'NONE',  'SOFT', TRUE),
  --    Tramo 2: 11-50 tenants → $6.00 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_TENANT_T2_MAX',            'QUOTA',   50,   'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_TENANT_RATE_T2',           'RATE',    600,  'NONE',  'SOFT', TRUE),
  --    Tramo 3: 51+ tenants → $4.00 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_TENANT_RATE_T3',           'RATE',    400,  'NONE',  'SOFT', TRUE),
  -- ── Tarifas por app (centavos USD) ───────────────────────────────────────
  --    Tramo 1: 1-20 apps → $2.00 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_APP_T1_MAX',               'QUOTA',   20,   'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_APP_RATE_T1',              'RATE',    200,  'NONE',  'SOFT', TRUE),
  --    Tramo 2: 21-100 apps → $1.50 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_APP_T2_MAX',               'QUOTA',   100,  'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_APP_RATE_T2',              'RATE',    150,  'NONE',  'SOFT', TRUE),
  --    Tramo 3: 101+ apps → $1.00 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_APP_RATE_T3',              'RATE',    100,  'NONE',  'SOFT', TRUE),
  -- ── Tarifas por identidad activa (centavos USD) ──────────────────────────
  --    Tramo 1: 1-100 identidades → $1.20 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_IDENTITY_T1_MAX',          'QUOTA',   100,  'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_IDENTITY_RATE_T1',         'RATE',    120,  'NONE',  'SOFT', TRUE),
  --    Tramo 2: 101-500 identidades → $0.90 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_IDENTITY_T2_MAX',          'QUOTA',   500,  'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_IDENTITY_RATE_T2',         'RATE',    90,   'NONE',  'SOFT', TRUE),
  --    Tramo 3: 501+ identidades → $0.60 c/u
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_IDENTITY_RATE_T3',         'RATE',    60,   'NONE',  'SOFT', TRUE),
  -- ── Tarifas por admin (centavos USD) ─────────────────────────────────────
  --    1 admin incluido por tenant; cada admin adicional → $4.00
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_ADMIN_INCLUDED_PER_TENANT','QUOTA',   1,    'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000006', 'FLEX_ADMIN_RATE',               'RATE',    400,  'NONE',  'SOFT', TRUE)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;


-- ────────────────────────────────────────────────────────────────────────────
-- 11. Entitlements — ENTERPRISE v2.0
--     Sin límites (NULL = ilimitado). SLA 99.9 % garantizado (HARD).
--     Soporte prioritario + Customer Success Manager incluidos.
--     Precio por contrato anual; base_price = 0 en la versión de plan.
-- ────────────────────────────────────────────────────────────────────────────
INSERT INTO app_plan_entitlements
    (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
VALUES
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'MAX_TENANTS',             'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'MAX_CLIENT_APPS',         'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'MAX_TENANT_USERS',        'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'MAX_ADMINS',              'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'MAX_MONTHLY_TOKENS',      'QUOTA',   NULL, 'MONTH', 'SOFT', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'SOCIAL_LOGIN',            'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'CUSTOM_DOMAIN',           'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'SLA_UPTIME_PCT',          'QUOTA',   999,  'NONE',  'HARD', TRUE),   -- 99.9 % GARANTIZADO
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'AUDIT_LOG_DAYS',          'QUOTA',   NULL, 'NONE',  'SOFT', TRUE),   -- retención indefinida
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'PRIORITY_SUPPORT',        'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'CUSTOM_SLA',              'BOOLEAN', NULL, 'NONE',  'HARD', TRUE),
  (gen_random_uuid(), '55555555-5555-5555-5555-100000000003', 'DEDICATED_SUCCESS_MGR',   'BOOLEAN', NULL, 'NONE',  'HARD', TRUE)
ON CONFLICT (app_plan_version_id, metric_code) DO NOTHING;

