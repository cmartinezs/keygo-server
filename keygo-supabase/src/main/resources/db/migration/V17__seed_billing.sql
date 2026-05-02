-- ============================================================================
-- V17__seed_billing.sql
-- Billing catalog and active commercial example for local development.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Catalog for acme-ui
-- ---------------------------------------------------------------------------
INSERT INTO app_plans (id, client_app_id, code, name, description, status, is_public, sort_order)
VALUES
    ('21000000-0000-0000-0000-000000000001', (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'FREE', 'Free', 'Entry plan for evaluation', 'ACTIVE', TRUE, 10),
    ('21000000-0000-0000-0000-000000000002', (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'PERSONAL', 'Personal', 'Single operator plan', 'ACTIVE', TRUE, 20),
    ('21000000-0000-0000-0000-000000000003', (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'TEAM', 'Team', 'Small team plan', 'ACTIVE', TRUE, 30),
    ('21000000-0000-0000-0000-000000000004', (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'BUSINESS', 'Business', 'Operational business plan', 'ACTIVE', TRUE, 40),
    ('21000000-0000-0000-0000-000000000005', (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'FLEX', 'Flex', 'Usage based hybrid plan', 'ACTIVE', TRUE, 50),
    ('21000000-0000-0000-0000-000000000006', (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'ENTERPRISE', 'Enterprise', 'Negotiated enterprise plan', 'ACTIVE', TRUE, 60)
ON CONFLICT (client_app_id, code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    is_public = EXCLUDED.is_public,
    sort_order = EXCLUDED.sort_order;

INSERT INTO app_plan_versions (id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, status)
SELECT
    ids.version_id,
    ap.id,
    'v1.0',
    'USD',
    ids.setup_fee,
    ids.trial_days,
    CURRENT_DATE,
    'ACTIVE'
FROM app_plans ap
JOIN (
    VALUES
        ('FREE', '22000000-0000-0000-0000-000000000001'::uuid, 0.00::numeric, 0),
        ('PERSONAL', '22000000-0000-0000-0000-000000000002'::uuid, 0.00::numeric, 14),
        ('TEAM', '22000000-0000-0000-0000-000000000003'::uuid, 49.00::numeric, 14),
        ('BUSINESS', '22000000-0000-0000-0000-000000000004'::uuid, 99.00::numeric, 14),
        ('FLEX', '22000000-0000-0000-0000-000000000005'::uuid, 0.00::numeric, 7),
        ('ENTERPRISE', '22000000-0000-0000-0000-000000000006'::uuid, 199.00::numeric, 30)
) AS ids(code, version_id, setup_fee, trial_days)
  ON ids.code = ap.code
ON CONFLICT (app_plan_id, version) DO UPDATE
SET currency = EXCLUDED.currency,
    setup_fee = EXCLUDED.setup_fee,
    trial_days = EXCLUDED.trial_days,
    effective_from = EXCLUDED.effective_from,
    status = EXCLUDED.status;

INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
SELECT gen_random_uuid(), apv.id, opt.billing_period, opt.base_price, opt.discount_pct, opt.is_default
FROM app_plan_versions apv
JOIN app_plans ap ON ap.id = apv.app_plan_id
JOIN (
    VALUES
        ('PERSONAL', 'MONTHLY', 19.00::numeric, 0.00::numeric, TRUE),
        ('PERSONAL', 'YEARLY', 190.00::numeric, 16.67::numeric, FALSE),
        ('TEAM', 'MONTHLY', 49.00::numeric, 0.00::numeric, TRUE),
        ('TEAM', 'YEARLY', 490.00::numeric, 16.67::numeric, FALSE),
        ('BUSINESS', 'MONTHLY', 99.00::numeric, 0.00::numeric, TRUE),
        ('BUSINESS', 'YEARLY', 990.00::numeric, 16.67::numeric, FALSE),
        ('FLEX', 'MONTHLY', 59.00::numeric, 0.00::numeric, TRUE),
        ('ENTERPRISE', 'YEARLY', 1990.00::numeric, 0.00::numeric, TRUE)
) AS opt(plan_code, billing_period, base_price, discount_pct, is_default)
  ON opt.plan_code = ap.code
WHERE apv.version = 'v1.0'
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price = EXCLUDED.base_price,
    discount_pct = EXCLUDED.discount_pct,
    is_default = EXCLUDED.is_default;

INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), apv.id, ent.metric_code, ent.metric_type, ent.limit_value, ent.period_type, ent.enforcement_mode, TRUE
FROM app_plan_versions apv
JOIN app_plans ap ON ap.id = apv.app_plan_id
JOIN (
    VALUES
        ('FREE', 'MAX_TENANTS', 'QUOTA', 1::numeric, 'MONTH', 'HARD'),
        ('FREE', 'MAX_USERS', 'QUOTA', 3::numeric, 'MONTH', 'HARD'),
        ('PERSONAL', 'MAX_TENANTS', 'QUOTA', 1::numeric, 'MONTH', 'HARD'),
        ('PERSONAL', 'MAX_USERS', 'QUOTA', 5::numeric, 'MONTH', 'HARD'),
        ('TEAM', 'MAX_TENANTS', 'QUOTA', 3::numeric, 'MONTH', 'HARD'),
        ('TEAM', 'MAX_USERS', 'QUOTA', 25::numeric, 'MONTH', 'HARD'),
        ('BUSINESS', 'MAX_TENANTS', 'QUOTA', 10::numeric, 'MONTH', 'HARD'),
        ('BUSINESS', 'MAX_USERS', 'QUOTA', 100::numeric, 'MONTH', 'HARD'),
        ('FLEX', 'MAX_TENANTS', 'QUOTA', 20::numeric, 'MONTH', 'SOFT'),
        ('FLEX', 'MAX_USERS', 'QUOTA', 250::numeric, 'MONTH', 'SOFT'),
        ('ENTERPRISE', 'MAX_TENANTS', 'QUOTA', NULL::numeric, 'MONTH', 'SOFT'),
        ('ENTERPRISE', 'MAX_USERS', 'QUOTA', NULL::numeric, 'MONTH', 'SOFT')
) AS ent(plan_code, metric_code, metric_type, limit_value, period_type, enforcement_mode)
  ON ent.plan_code = ap.code
WHERE apv.version = 'v1.0'
ON CONFLICT (app_plan_version_id, metric_code) DO UPDATE
SET metric_type = EXCLUDED.metric_type,
    limit_value = EXCLUDED.limit_value,
    period_type = EXCLUDED.period_type,
    enforcement_mode = EXCLUDED.enforcement_mode,
    is_enabled = EXCLUDED.is_enabled;

-- ---------------------------------------------------------------------------
-- Active billing example for acme contractor
-- ---------------------------------------------------------------------------
INSERT INTO tenant_billing_profiles (
    id, tenant_id, billing_type, display_name, legal_name, tax_id, billing_email, billing_address, is_default
)
VALUES (
    '23000000-0000-0000-0000-000000000001',
    (SELECT id FROM tenants WHERE slug = 'acme'),
    'COMPANY',
    'Acme Billing',
    'Acme Holdings SpA',
    'CL-ACME-001',
    'billing@acme.local',
    'Avenida Demo 123, Santiago, Chile',
    TRUE
)
ON CONFLICT (id) DO UPDATE
SET billing_type = EXCLUDED.billing_type,
    display_name = EXCLUDED.display_name,
    legal_name = EXCLUDED.legal_name,
    tax_id = EXCLUDED.tax_id,
    billing_email = EXCLUDED.billing_email,
    billing_address = EXCLUDED.billing_address,
    is_default = EXCLUDED.is_default;

INSERT INTO payment_methods (
    id, contractor_id, provider, method_type, external_reference, display_label, brand, last4, exp_month, exp_year, status, is_default
)
VALUES (
    '23000000-0000-0000-0000-000000000002',
    (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
    'MOCK',
    'CARD',
    'mock-pm-acme-default',
    'Mock Visa ending 4242',
    'VISA',
    '4242',
    12,
    2030,
    'ACTIVE',
    TRUE
)
ON CONFLICT (provider, external_reference) DO UPDATE
SET display_label = EXCLUDED.display_label,
    brand = EXCLUDED.brand,
    last4 = EXCLUDED.last4,
    exp_month = EXCLUDED.exp_month,
    exp_year = EXCLUDED.exp_year,
    status = EXCLUDED.status,
    is_default = EXCLUDED.is_default;

INSERT INTO app_contracts (
    id, contractor_id, client_app_id, app_plan_version_id, created_by_platform_user_id,
    billing_period, status, billing_contact_email, billing_contact_name,
    contractor_email_verified_at, payment_verified_at, expires_at
)
VALUES (
    '24000000-0000-0000-0000-000000000001',
    (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
    (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
    (SELECT apv.id
       FROM app_plan_versions apv
       JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'BUSINESS'
        AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
        AND apv.version = 'v1.0'),
    (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'),
    'MONTHLY',
    'ACTIVE',
    'billing@acme.local',
    'Acme Billing Team',
    now(),
    now(),
    now() + interval '1 year'
)
ON CONFLICT (id) DO UPDATE
SET status = EXCLUDED.status,
    billing_contact_email = EXCLUDED.billing_contact_email,
    billing_contact_name = EXCLUDED.billing_contact_name,
    contractor_email_verified_at = EXCLUDED.contractor_email_verified_at,
    payment_verified_at = EXCLUDED.payment_verified_at,
    expires_at = EXCLUDED.expires_at;

INSERT INTO app_subscriptions (
    id, contractor_id, client_app_id, app_plan_version_id, contract_id, status,
    current_period_start, current_period_end, cancel_at_period_end, auto_renew, next_billing_at
)
VALUES (
    '24000000-0000-0000-0000-000000000002',
    (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
    (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
    (SELECT apv.id
       FROM app_plan_versions apv
       JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'BUSINESS'
        AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
        AND apv.version = 'v1.0'),
    '24000000-0000-0000-0000-000000000001',
    'ACTIVE',
    date_trunc('month', now()),
    date_trunc('month', now()) + interval '1 month',
    FALSE,
    TRUE,
    date_trunc('month', now()) + interval '1 month'
)
ON CONFLICT (id) DO UPDATE
SET status = EXCLUDED.status,
    current_period_start = EXCLUDED.current_period_start,
    current_period_end = EXCLUDED.current_period_end,
    cancel_at_period_end = EXCLUDED.cancel_at_period_end,
    auto_renew = EXCLUDED.auto_renew,
    next_billing_at = EXCLUDED.next_billing_at;

INSERT INTO payment_transactions (
    id, contractor_id, client_app_id, contract_id, subscription_id, provider, provider_reference,
    amount, currency, status, paid_at, provider_payload
)
VALUES (
    '24000000-0000-0000-0000-000000000003',
    (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
    (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
    '24000000-0000-0000-0000-000000000001',
    '24000000-0000-0000-0000-000000000002',
    'MOCK',
    'mock-tx-acme-0001',
    99.00,
    'USD',
    'APPROVED',
    now(),
    jsonb_build_object('mode', 'seed', 'note', 'development baseline payment')
)
ON CONFLICT (id) DO UPDATE
SET status = EXCLUDED.status,
    paid_at = EXCLUDED.paid_at,
    provider_payload = EXCLUDED.provider_payload;

INSERT INTO invoices (
    id, contractor_id, client_app_id, subscription_id, invoice_number, status, issue_date, due_date,
    period_start, period_end, currency, subtotal, tax_amount, total, paid_at,
    billing_name_snapshot, billing_tax_id_snapshot, billing_address_snapshot,
    plan_name_snapshot, plan_version_snapshot
)
VALUES (
    '24000000-0000-0000-0000-000000000004',
    (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
    (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
    '24000000-0000-0000-0000-000000000002',
    'INV-ACME-0001',
    'PAID',
    CURRENT_DATE,
    CURRENT_DATE + 15,
    CURRENT_DATE,
    CURRENT_DATE + 30,
    'USD',
    99.00,
    0.00,
    99.00,
    now(),
    'Acme Holdings SpA',
    'CL-ACME-001',
    'Avenida Demo 123, Santiago, Chile',
    'Business',
    'v1.0'
)
ON CONFLICT (invoice_number) DO UPDATE
SET status = EXCLUDED.status,
    paid_at = EXCLUDED.paid_at,
    subtotal = EXCLUDED.subtotal,
    tax_amount = EXCLUDED.tax_amount,
    total = EXCLUDED.total;

INSERT INTO usage_counters (
    id, contractor_id, client_app_id, metric_code, period_start, period_end, used_value
)
VALUES
    (
        '24000000-0000-0000-0000-000000000005',
        (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
        (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
        'MAX_TENANTS',
        date_trunc('month', now()),
        date_trunc('month', now()) + interval '1 month',
        1
    ),
    (
        '24000000-0000-0000-0000-000000000006',
        (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
        (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
        'MAX_USERS',
        date_trunc('month', now()),
        date_trunc('month', now()) + interval '1 month',
        2
    )
ON CONFLICT (client_app_id, contractor_id, metric_code, period_start, period_end) DO UPDATE
SET used_value = EXCLUDED.used_value;
