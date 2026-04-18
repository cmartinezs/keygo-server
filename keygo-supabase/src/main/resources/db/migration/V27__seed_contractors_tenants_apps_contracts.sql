-- ============================================================================
-- V27__seed_contractors_tenants_apps_contracts.sql
-- Dev seed: 3 contractors with varied setups.
--
-- Scenario A — Nova Tech SRL
--   Contractor + tenant + 3 users → nova-web app, TEAM plan MONTHLY ACTIVE.
--   Billing history: 2 paid invoices + 1 current issued.
--
-- Scenario B — Global Solutions SA
--   Contractor + tenant + 2 users → global-web (BUSINESS YEARLY ACTIVE)
--                                  + global-api (ENTERPRISE YEARLY ACTIVE).
--   Billing history: 1 paid invoice per app.
--
-- Scenario C — Startup Inc
--   Contractor only (no tenant, no users).
--   nova-web FREE MONTHLY ACTIVE (no payment needed).
--   global-web PERSONAL MONTHLY PENDING_PAYMENT (in checkout, no subscription).
--
-- Dev password for all users: Admin1234!
-- BCrypt: $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
-- Dev client secret for CONFIDENTIAL apps: same BCrypt hash.
-- ============================================================================

-- ─── Platform users ───────────────────────────────────────────────────────────
INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name,
    locale, zoneinfo, status, email_verified_at
)
VALUES
    -- Nova Tech
    ('11000000-0000-0000-0000-000000000007', 'nova-owner@nova.local',
     '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
     'Nova', 'Owner', 'Nova Owner', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000008', 'nova-admin@nova.local',
     '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
     'Nova', 'Admin', 'Nova Admin', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000009', 'nova-user@nova.local',
     '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
     'Nova', 'User', 'Nova User', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    -- Global Solutions
    ('11000000-0000-0000-0000-000000000010', 'global-owner@global.local',
     '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
     'Global', 'Owner', 'Global Owner', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000011', 'global-user@global.local',
     '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
     'Global', 'User', 'Global User', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    -- Startup Inc
    ('11000000-0000-0000-0000-000000000012', 'startup-owner@startup.local',
     '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
     'Startup', 'Owner', 'Startup Owner', 'es-CL', 'America/Santiago', 'ACTIVE', now())
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash, display_name = EXCLUDED.display_name,
    status = EXCLUDED.status, email_verified_at = EXCLUDED.email_verified_at;

INSERT INTO platform_user_notification_preferences (id, platform_user_id)
SELECT gen_random_uuid(), pu.id
FROM platform_users pu
WHERE pu.email IN (
    'nova-owner@nova.local', 'nova-admin@nova.local', 'nova-user@nova.local',
    'global-owner@global.local', 'global-user@global.local',
    'startup-owner@startup.local'
)
ON CONFLICT (platform_user_id) DO NOTHING;

-- ─── Contractors ──────────────────────────────────────────────────────────────
INSERT INTO contractors (id, type, display_name, legal_name, tax_id, billing_email, primary_contact_platform_user_id, status)
VALUES
    ('12000000-0000-0000-0000-000000000002',
     'COMPANY', 'Nova Tech', 'Nova Tech SRL', 'CL-NOVA-002',
     'billing@nova.local',
     (SELECT id FROM platform_users WHERE email = 'nova-owner@nova.local'), 'ACTIVE'),
    ('12000000-0000-0000-0000-000000000003',
     'COMPANY', 'Global Solutions', 'Global Solutions SA', 'CL-GLOBAL-003',
     'billing@global.local',
     (SELECT id FROM platform_users WHERE email = 'global-owner@global.local'), 'ACTIVE'),
    ('12000000-0000-0000-0000-000000000004',
     'COMPANY', 'Startup Inc', 'Startup Inc SpA', 'CL-START-004',
     'billing@startup.local',
     (SELECT id FROM platform_users WHERE email = 'startup-owner@startup.local'), 'ACTIVE')
ON CONFLICT (id) DO UPDATE
SET display_name = EXCLUDED.display_name, legal_name = EXCLUDED.legal_name,
    tax_id = EXCLUDED.tax_id, billing_email = EXCLUDED.billing_email, status = EXCLUDED.status;

INSERT INTO contractor_users (contractor_id, platform_user_id, role_code)
VALUES
    ('12000000-0000-0000-0000-000000000002',
     (SELECT id FROM platform_users WHERE email = 'nova-owner@nova.local'), 'OWNER'),
    ('12000000-0000-0000-0000-000000000003',
     (SELECT id FROM platform_users WHERE email = 'global-owner@global.local'), 'OWNER'),
    ('12000000-0000-0000-0000-000000000004',
     (SELECT id FROM platform_users WHERE email = 'startup-owner@startup.local'), 'OWNER')
ON CONFLICT DO NOTHING;

-- ─── Tenants ──────────────────────────────────────────────────────────────────
INSERT INTO tenants (id, slug, name, status, contractor_id, is_internal_reserved)
VALUES
    ('13000000-0000-0000-0000-000000000004', 'nova', 'Nova Tech', 'ACTIVE',
     '12000000-0000-0000-0000-000000000002', FALSE),
    ('13000000-0000-0000-0000-000000000005', 'global', 'Global Solutions', 'ACTIVE',
     '12000000-0000-0000-0000-000000000003', FALSE)
ON CONFLICT (slug) DO UPDATE
SET name = EXCLUDED.name, status = EXCLUDED.status, contractor_id = EXCLUDED.contractor_id;

-- ─── Platform RBAC for contractor owners ─────────────────────────────────────
INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type, contractor_id)
SELECT '14000000-0000-0000-0000-000000000005', pu.id, pr.id, 'CONTRACTOR', c.id
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_ACCOUNT_ADMIN'
JOIN contractors c ON c.id = '12000000-0000-0000-0000-000000000002'
WHERE pu.email = 'nova-owner@nova.local'
  AND NOT EXISTS (SELECT 1 FROM platform_user_roles WHERE id = '14000000-0000-0000-0000-000000000005');

INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type, contractor_id)
SELECT '14000000-0000-0000-0000-000000000006', pu.id, pr.id, 'CONTRACTOR', c.id
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_ACCOUNT_ADMIN'
JOIN contractors c ON c.id = '12000000-0000-0000-0000-000000000003'
WHERE pu.email = 'global-owner@global.local'
  AND NOT EXISTS (SELECT 1 FROM platform_user_roles WHERE id = '14000000-0000-0000-0000-000000000006');

INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type, contractor_id)
SELECT '14000000-0000-0000-0000-000000000007', pu.id, pr.id, 'CONTRACTOR', c.id
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_ACCOUNT_ADMIN'
JOIN contractors c ON c.id = '12000000-0000-0000-0000-000000000004'
WHERE pu.email = 'startup-owner@startup.local'
  AND NOT EXISTS (SELECT 1 FROM platform_user_roles WHERE id = '14000000-0000-0000-0000-000000000007');

-- ─── Tenant users ─────────────────────────────────────────────────────────────
INSERT INTO tenant_users (id, tenant_id, platform_user_id, local_username, display_name_override, status)
VALUES
    ('15000000-0000-0000-0000-000000000006',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     (SELECT id FROM platform_users WHERE email = 'nova-owner@nova.local'),
     'nova-owner', 'Nova Owner', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000007',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     (SELECT id FROM platform_users WHERE email = 'nova-admin@nova.local'),
     'nova-admin', 'Nova Admin', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000008',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     (SELECT id FROM platform_users WHERE email = 'nova-user@nova.local'),
     'nova-user', 'Nova User', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000009',
     (SELECT id FROM tenants WHERE slug = 'global'),
     (SELECT id FROM platform_users WHERE email = 'global-owner@global.local'),
     'global-owner', 'Global Owner', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000010',
     (SELECT id FROM tenants WHERE slug = 'global'),
     (SELECT id FROM platform_users WHERE email = 'global-user@global.local'),
     'global-user', 'Global User', 'ACTIVE')
ON CONFLICT (tenant_id, platform_user_id) DO UPDATE
SET local_username = EXCLUDED.local_username, display_name_override = EXCLUDED.display_name_override,
    status = EXCLUDED.status;

-- ─── Tenant roles ─────────────────────────────────────────────────────────────
INSERT INTO tenant_roles (id, tenant_id, code, display_name, description)
VALUES
    ('16000000-0000-0000-0000-000000000006', (SELECT id FROM tenants WHERE slug = 'nova'), 'owner', 'Owner', 'Nova tenant owner'),
    ('16000000-0000-0000-0000-000000000007', (SELECT id FROM tenants WHERE slug = 'nova'), 'admin', 'Admin', 'Nova tenant administrators'),
    ('16000000-0000-0000-0000-000000000008', (SELECT id FROM tenants WHERE slug = 'nova'), 'user',  'User',  'Nova tenant regular users'),
    ('16000000-0000-0000-0000-000000000009', (SELECT id FROM tenants WHERE slug = 'global'), 'owner', 'Owner', 'Global tenant owner'),
    ('16000000-0000-0000-0000-000000000010', (SELECT id FROM tenants WHERE slug = 'global'), 'admin', 'Admin', 'Global tenant administrators'),
    ('16000000-0000-0000-0000-000000000011', (SELECT id FROM tenants WHERE slug = 'global'), 'user',  'User',  'Global tenant regular users')
ON CONFLICT (tenant_id, code) DO UPDATE
SET display_name = EXCLUDED.display_name, description = EXCLUDED.description;

INSERT INTO tenant_user_roles (tenant_user_id, tenant_id, role_id)
SELECT tu.id, tu.tenant_id, tr.id
FROM tenant_users tu
JOIN tenant_roles tr ON tr.tenant_id = tu.tenant_id
WHERE (tu.local_username = 'nova-owner'  AND tr.code = 'owner')
   OR (tu.local_username = 'nova-admin'  AND tr.code = 'admin')
   OR (tu.local_username = 'nova-user'   AND tr.code = 'user')
   OR (tu.local_username = 'global-owner' AND tr.code = 'owner')
   OR (tu.local_username = 'global-user'  AND tr.code = 'user')
ON CONFLICT DO NOTHING;

-- ─── Client apps ──────────────────────────────────────────────────────────────
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, is_internal, registration_policy)
VALUES
    ('17000000-0000-0000-0000-000000000012',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     'nova-web', 'Nova Web Portal', 'Self-service web portal for Nova Tech users',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'OPEN_AUTO_ACTIVE'),
    ('17000000-0000-0000-0000-000000000013',
     (SELECT id FROM tenants WHERE slug = 'global'),
     'global-web', 'Global Web App', 'Customer-facing web application for Global Solutions',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'OPEN_AUTO_PENDING'),
    ('17000000-0000-0000-0000-000000000014',
     (SELECT id FROM tenants WHERE slug = 'global'),
     'global-api', 'Global API Client', 'Machine-to-machine API access for Global Solutions integrations',
     'CONFIDENTIAL', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
     'ACTIVE', FALSE, 'INVITE_ONLY')
ON CONFLICT (client_id) DO UPDATE
SET name = EXCLUDED.name, description = EXCLUDED.description,
    type = EXCLUDED.type, hashed_secret = EXCLUDED.hashed_secret,
    status = EXCLUDED.status, is_internal = EXCLUDED.is_internal,
    registration_policy = EXCLUDED.registration_policy;

INSERT INTO client_redirect_uris (id, client_app_id, uri)
VALUES
    ('17100000-0000-0000-0000-000000000012', (SELECT id FROM client_apps WHERE client_id = 'nova-web'),   'http://localhost:4300/callback'),
    ('17100000-0000-0000-0000-000000000013', (SELECT id FROM client_apps WHERE client_id = 'global-web'), 'http://localhost:4400/callback'),
    ('17100000-0000-0000-0000-000000000014', (SELECT id FROM client_apps WHERE client_id = 'global-api'), 'http://localhost:4410/callback')
ON CONFLICT (client_app_id, uri) DO NOTHING;

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), ca.id, g.grant_type
FROM client_apps ca
CROSS JOIN (VALUES ('AUTHORIZATION_CODE'), ('REFRESH_TOKEN')) AS g(grant_type)
WHERE ca.client_id IN ('nova-web', 'global-web', 'global-api')
ON CONFLICT (client_app_id, grant_type) DO NOTHING;

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), ca.id, s.scope
FROM client_apps ca
CROSS JOIN (VALUES ('openid'), ('profile'), ('email')) AS s(scope)
WHERE ca.client_id IN ('nova-web', 'global-web', 'global-api')
ON CONFLICT (client_app_id, scope) DO NOTHING;

-- ─── App roles ────────────────────────────────────────────────────────────────
INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description)
VALUES
    ('18000000-0000-0000-0000-000000000021',
     (SELECT tenant_id FROM client_apps WHERE client_id = 'nova-web'),
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     'admin', 'Admin', 'Nova Web administrators'),
    ('18000000-0000-0000-0000-000000000022',
     (SELECT tenant_id FROM client_apps WHERE client_id = 'nova-web'),
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     'user', 'User', 'Nova Web regular users'),
    ('18000000-0000-0000-0000-000000000023',
     (SELECT tenant_id FROM client_apps WHERE client_id = 'global-web'),
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     'admin', 'Admin', 'Global Web administrators'),
    ('18000000-0000-0000-0000-000000000024',
     (SELECT tenant_id FROM client_apps WHERE client_id = 'global-web'),
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     'user', 'User', 'Global Web regular users'),
    ('18000000-0000-0000-0000-000000000025',
     (SELECT tenant_id FROM client_apps WHERE client_id = 'global-api'),
     (SELECT id FROM client_apps WHERE client_id = 'global-api'),
     'admin', 'Admin', 'Global API administrators')
ON CONFLICT (client_app_id, code) DO UPDATE
SET display_name = EXCLUDED.display_name, description = EXCLUDED.description;

-- ─── App memberships ──────────────────────────────────────────────────────────
INSERT INTO app_memberships (id, tenant_id, tenant_user_id, client_app_id, status)
VALUES
    ('19000000-0000-0000-0000-000000000006',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     (SELECT id FROM tenant_users WHERE local_username = 'nova-owner'),
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000007',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     (SELECT id FROM tenant_users WHERE local_username = 'nova-admin'),
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000008',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     (SELECT id FROM tenant_users WHERE local_username = 'nova-user'),
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000009',
     (SELECT id FROM tenants WHERE slug = 'global'),
     (SELECT id FROM tenant_users WHERE local_username = 'global-owner'),
     (SELECT id FROM client_apps WHERE client_id = 'global-web'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000010',
     (SELECT id FROM tenants WHERE slug = 'global'),
     (SELECT id FROM tenant_users WHERE local_username = 'global-user'),
     (SELECT id FROM client_apps WHERE client_id = 'global-web'), 'ACTIVE')
ON CONFLICT (tenant_user_id, client_app_id) DO UPDATE SET status = EXCLUDED.status;

INSERT INTO app_membership_roles (membership_id, client_app_id, role_id)
SELECT am.id, am.client_app_id, ar.id
FROM app_memberships am
JOIN app_roles ar ON ar.client_app_id = am.client_app_id
WHERE (am.id = '19000000-0000-0000-0000-000000000006' AND ar.code = 'admin')
   OR (am.id = '19000000-0000-0000-0000-000000000007' AND ar.code = 'admin')
   OR (am.id = '19000000-0000-0000-0000-000000000008' AND ar.code = 'user')
   OR (am.id = '19000000-0000-0000-0000-000000000009' AND ar.code = 'admin')
   OR (am.id = '19000000-0000-0000-0000-000000000010' AND ar.code = 'user')
ON CONFLICT DO NOTHING;

-- ─── Billing catalogs ─────────────────────────────────────────────────────────
-- nova-web: FREE, PERSONAL, TEAM, BUSINESS
INSERT INTO app_plans (id, client_app_id, code, name, description, status, is_public, sort_order)
VALUES
    ('21000000-0000-0000-0000-000000000007', (SELECT id FROM client_apps WHERE client_id = 'nova-web'), 'FREE',     'Free',     'Evaluation tier',                'ACTIVE', TRUE, 10),
    ('21000000-0000-0000-0000-000000000008', (SELECT id FROM client_apps WHERE client_id = 'nova-web'), 'PERSONAL', 'Personal', 'Single-operator plan',           'ACTIVE', TRUE, 20),
    ('21000000-0000-0000-0000-000000000009', (SELECT id FROM client_apps WHERE client_id = 'nova-web'), 'TEAM',     'Team',     'Small team collaboration plan',   'ACTIVE', TRUE, 30),
    ('21000000-0000-0000-0000-000000000010', (SELECT id FROM client_apps WHERE client_id = 'nova-web'), 'BUSINESS', 'Business', 'Full business operations plan',   'ACTIVE', TRUE, 40),
    -- global-web: FREE, PERSONAL, TEAM, BUSINESS
    ('21000000-0000-0000-0000-000000000011', (SELECT id FROM client_apps WHERE client_id = 'global-web'), 'FREE',     'Free',     'Evaluation tier',                'ACTIVE', TRUE, 10),
    ('21000000-0000-0000-0000-000000000012', (SELECT id FROM client_apps WHERE client_id = 'global-web'), 'PERSONAL', 'Personal', 'Single-operator plan',           'ACTIVE', TRUE, 20),
    ('21000000-0000-0000-0000-000000000013', (SELECT id FROM client_apps WHERE client_id = 'global-web'), 'TEAM',     'Team',     'Small team collaboration plan',   'ACTIVE', TRUE, 30),
    ('21000000-0000-0000-0000-000000000014', (SELECT id FROM client_apps WHERE client_id = 'global-web'), 'BUSINESS', 'Business', 'Full business operations plan',   'ACTIVE', TRUE, 40),
    -- global-api: TEAM, ENTERPRISE
    ('21000000-0000-0000-0000-000000000015', (SELECT id FROM client_apps WHERE client_id = 'global-api'), 'TEAM',       'Team',       'API access for small teams',           'ACTIVE', TRUE, 10),
    ('21000000-0000-0000-0000-000000000016', (SELECT id FROM client_apps WHERE client_id = 'global-api'), 'ENTERPRISE', 'Enterprise', 'Unlimited API access — negotiated SLA', 'ACTIVE', TRUE, 20)
ON CONFLICT (client_app_id, code) DO UPDATE
SET name = EXCLUDED.name, description = EXCLUDED.description,
    status = EXCLUDED.status, is_public = EXCLUDED.is_public, sort_order = EXCLUDED.sort_order;

-- App plan versions (v1.0, all active)
INSERT INTO app_plan_versions (id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, status)
SELECT ids.version_id, ap.id, 'v1.0', 'USD', ids.setup_fee, ids.trial_days, CURRENT_DATE, 'ACTIVE'
FROM app_plans ap
JOIN (VALUES
    ('21000000-0000-0000-0000-000000000007'::uuid, '22000000-0000-0000-0000-000000000007'::uuid,  0.00::numeric, 0),
    ('21000000-0000-0000-0000-000000000008'::uuid, '22000000-0000-0000-0000-000000000008'::uuid,  0.00::numeric, 14),
    ('21000000-0000-0000-0000-000000000009'::uuid, '22000000-0000-0000-0000-000000000009'::uuid, 49.00::numeric, 14),
    ('21000000-0000-0000-0000-000000000010'::uuid, '22000000-0000-0000-0000-000000000010'::uuid, 99.00::numeric, 14),
    ('21000000-0000-0000-0000-000000000011'::uuid, '22000000-0000-0000-0000-000000000011'::uuid,  0.00::numeric, 0),
    ('21000000-0000-0000-0000-000000000012'::uuid, '22000000-0000-0000-0000-000000000012'::uuid,  0.00::numeric, 14),
    ('21000000-0000-0000-0000-000000000013'::uuid, '22000000-0000-0000-0000-000000000013'::uuid, 49.00::numeric, 14),
    ('21000000-0000-0000-0000-000000000014'::uuid, '22000000-0000-0000-0000-000000000014'::uuid, 99.00::numeric, 14),
    ('21000000-0000-0000-0000-000000000015'::uuid, '22000000-0000-0000-0000-000000000015'::uuid, 49.00::numeric, 14),
    ('21000000-0000-0000-0000-000000000016'::uuid, '22000000-0000-0000-0000-000000000016'::uuid,199.00::numeric, 30)
) AS ids(plan_id, version_id, setup_fee, trial_days) ON ids.plan_id = ap.id
ON CONFLICT (app_plan_id, version) DO UPDATE
SET currency = EXCLUDED.currency, setup_fee = EXCLUDED.setup_fee,
    trial_days = EXCLUDED.trial_days, status = EXCLUDED.status;

-- Billing options (paid tiers: MONTHLY + YEARLY with 16.67% discount)
INSERT INTO app_plan_billing_options (id, app_plan_version_id, billing_period, base_price, discount_pct, is_default)
SELECT gen_random_uuid(), apv.id, opt.billing_period, opt.base_price, opt.discount_pct, opt.is_default
FROM app_plan_versions apv
JOIN app_plans ap ON ap.id = apv.app_plan_id
JOIN (VALUES
    -- nova-web & global-web PERSONAL
    ('nova-web',   'PERSONAL', 'MONTHLY',  19.00::numeric,  0.00::numeric, TRUE),
    ('nova-web',   'PERSONAL', 'YEARLY',  190.00::numeric, 16.67::numeric, FALSE),
    ('nova-web',   'TEAM',     'MONTHLY',  49.00::numeric,  0.00::numeric, TRUE),
    ('nova-web',   'TEAM',     'YEARLY',  490.00::numeric, 16.67::numeric, FALSE),
    ('nova-web',   'BUSINESS', 'MONTHLY',  99.00::numeric,  0.00::numeric, TRUE),
    ('nova-web',   'BUSINESS', 'YEARLY',  990.00::numeric, 16.67::numeric, FALSE),
    ('global-web', 'PERSONAL', 'MONTHLY',  19.00::numeric,  0.00::numeric, TRUE),
    ('global-web', 'PERSONAL', 'YEARLY',  190.00::numeric, 16.67::numeric, FALSE),
    ('global-web', 'TEAM',     'MONTHLY',  49.00::numeric,  0.00::numeric, TRUE),
    ('global-web', 'TEAM',     'YEARLY',  490.00::numeric, 16.67::numeric, FALSE),
    ('global-web', 'BUSINESS', 'MONTHLY',  99.00::numeric,  0.00::numeric, TRUE),
    ('global-web', 'BUSINESS', 'YEARLY',  990.00::numeric, 16.67::numeric, FALSE),
    ('global-api', 'TEAM',       'MONTHLY',   49.00::numeric,  0.00::numeric, TRUE),
    ('global-api', 'ENTERPRISE', 'YEARLY',  1990.00::numeric,  0.00::numeric, TRUE)
) AS opt(client_id, plan_code, billing_period, base_price, discount_pct, is_default)
  ON opt.client_id = (SELECT client_id FROM client_apps WHERE id = ap.client_app_id)
 AND opt.plan_code = ap.code
WHERE apv.version = 'v1.0'
ON CONFLICT (app_plan_version_id, billing_period) DO UPDATE
SET base_price = EXCLUDED.base_price, discount_pct = EXCLUDED.discount_pct,
    is_default = EXCLUDED.is_default;

-- Entitlements
INSERT INTO app_plan_entitlements (id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode, is_enabled)
SELECT gen_random_uuid(), apv.id, ent.metric_code, ent.metric_type, ent.limit_value, ent.period_type, ent.enforcement_mode, TRUE
FROM app_plan_versions apv
JOIN app_plans ap ON ap.id = apv.app_plan_id
JOIN (VALUES
    -- nova-web
    ('nova-web', 'FREE',     'MAX_USERS', 'QUOTA',   3::numeric, 'MONTH', 'HARD'),
    ('nova-web', 'PERSONAL', 'MAX_USERS', 'QUOTA',   5::numeric, 'MONTH', 'HARD'),
    ('nova-web', 'TEAM',     'MAX_USERS', 'QUOTA',  25::numeric, 'MONTH', 'HARD'),
    ('nova-web', 'BUSINESS', 'MAX_USERS', 'QUOTA', 100::numeric, 'MONTH', 'HARD'),
    -- global-web
    ('global-web', 'FREE',     'MAX_USERS', 'QUOTA',   3::numeric, 'MONTH', 'HARD'),
    ('global-web', 'PERSONAL', 'MAX_USERS', 'QUOTA',   5::numeric, 'MONTH', 'HARD'),
    ('global-web', 'TEAM',     'MAX_USERS', 'QUOTA',  25::numeric, 'MONTH', 'HARD'),
    ('global-web', 'BUSINESS', 'MAX_USERS', 'QUOTA', 100::numeric, 'MONTH', 'HARD'),
    -- global-api
    ('global-api', 'TEAM',       'MAX_USERS', 'QUOTA',   10::numeric, 'MONTH', 'HARD'),
    ('global-api', 'ENTERPRISE', 'MAX_USERS', 'QUOTA', NULL::numeric, 'MONTH', 'SOFT')
) AS ent(client_id, plan_code, metric_code, metric_type, limit_value, period_type, enforcement_mode)
  ON ent.client_id = (SELECT client_id FROM client_apps WHERE id = ap.client_app_id)
 AND ent.plan_code = ap.code
WHERE apv.version = 'v1.0'
ON CONFLICT (app_plan_version_id, metric_code) DO UPDATE
SET metric_type = EXCLUDED.metric_type, limit_value = EXCLUDED.limit_value,
    period_type = EXCLUDED.period_type, enforcement_mode = EXCLUDED.enforcement_mode,
    is_enabled = EXCLUDED.is_enabled;

-- ─── Billing profiles & payment methods ──────────────────────────────────────
INSERT INTO tenant_billing_profiles (id, tenant_id, billing_type, display_name, legal_name, tax_id, billing_email, billing_address, is_default)
VALUES
    ('23000000-0000-0000-0000-000000000003',
     (SELECT id FROM tenants WHERE slug = 'nova'),
     'COMPANY', 'Nova Tech Billing', 'Nova Tech SRL', 'CL-NOVA-002',
     'billing@nova.local', 'Av. Innovación 456, Santiago, Chile', TRUE),
    ('23000000-0000-0000-0000-000000000005',
     (SELECT id FROM tenants WHERE slug = 'global'),
     'COMPANY', 'Global Solutions Billing', 'Global Solutions SA', 'CL-GLOBAL-003',
     'billing@global.local', 'Calle Global 789, Santiago, Chile', TRUE)
ON CONFLICT (id) DO UPDATE
SET display_name = EXCLUDED.display_name, legal_name = EXCLUDED.legal_name,
    billing_email = EXCLUDED.billing_email, billing_address = EXCLUDED.billing_address;

INSERT INTO payment_methods (id, contractor_id, provider, method_type, external_reference, display_label, brand, last4, exp_month, exp_year, status, is_default)
VALUES
    ('23000000-0000-0000-0000-000000000004',
     '12000000-0000-0000-0000-000000000002',
     'MOCK', 'CARD', 'mock-pm-nova-default',
     'Mock Mastercard ending 5555', 'MASTERCARD', '5555', 11, 2029, 'ACTIVE', TRUE),
    ('23000000-0000-0000-0000-000000000006',
     '12000000-0000-0000-0000-000000000003',
     'MOCK', 'CARD', 'mock-pm-global-default',
     'Mock Amex ending 3714', 'AMEX', '3714', 6, 2031, 'ACTIVE', TRUE)
ON CONFLICT (provider, external_reference) DO UPDATE
SET display_label = EXCLUDED.display_label, status = EXCLUDED.status, is_default = EXCLUDED.is_default;

-- ─── Contracts ────────────────────────────────────────────────────────────────
-- A: Nova Tech → nova-web TEAM MONTHLY ACTIVE (started 3 months ago)
-- B: Global Solutions → global-web BUSINESS YEARLY ACTIVE (started 6 months ago)
-- C: Global Solutions → global-api ENTERPRISE YEARLY ACTIVE (started 1 month ago)
-- D: Startup Inc → nova-web FREE MONTHLY ACTIVE (no payment needed)
-- E: Startup Inc → global-web PERSONAL MONTHLY PENDING_PAYMENT (email verified, awaiting payment)
INSERT INTO app_contracts (
    id, contractor_id, client_app_id, app_plan_version_id,
    created_by_platform_user_id, billing_period, status,
    billing_contact_email, billing_contact_name,
    contractor_email_verified_at, payment_verified_at, expires_at
)
VALUES
    -- A: Nova Tech TEAM MONTHLY
    ('24000000-0000-0000-0000-000000000007',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'TEAM' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'nova-web') AND apv.version = 'v1.0'),
     (SELECT id FROM platform_users WHERE email = 'nova-owner@nova.local'),
     'MONTHLY', 'ACTIVE', 'billing@nova.local', 'Nova Billing Team',
     now() - interval '3 months', now() - interval '3 months',
     now() + interval '9 months'),
    -- B: Global Solutions BUSINESS YEARLY
    ('24000000-0000-0000-0000-000000000008',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'BUSINESS' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'global-web') AND apv.version = 'v1.0'),
     (SELECT id FROM platform_users WHERE email = 'global-owner@global.local'),
     'YEARLY', 'ACTIVE', 'billing@global.local', 'Global Billing Team',
     now() - interval '6 months', now() - interval '6 months',
     now() + interval '6 months'),
    -- C: Global Solutions ENTERPRISE YEARLY (global-api)
    ('24000000-0000-0000-0000-000000000009',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-api'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'ENTERPRISE' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'global-api') AND apv.version = 'v1.0'),
     (SELECT id FROM platform_users WHERE email = 'global-owner@global.local'),
     'YEARLY', 'ACTIVE', 'billing@global.local', 'Global Billing Team',
     now() - interval '1 month', now() - interval '1 month',
     now() + interval '11 months'),
    -- D: Startup Inc FREE (nova-web) — no payment required
    ('24000000-0000-0000-0000-000000000010',
     '12000000-0000-0000-0000-000000000004',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'FREE' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'nova-web') AND apv.version = 'v1.0'),
     (SELECT id FROM platform_users WHERE email = 'startup-owner@startup.local'),
     'MONTHLY', 'ACTIVE', 'billing@startup.local', 'Startup Billing',
     now(), now(),
     now() + interval '1 year'),
    -- E: Startup Inc PERSONAL (global-web) — email verified, awaiting payment
    ('24000000-0000-0000-0000-000000000011',
     '12000000-0000-0000-0000-000000000004',
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'PERSONAL' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'global-web') AND apv.version = 'v1.0'),
     (SELECT id FROM platform_users WHERE email = 'startup-owner@startup.local'),
     'MONTHLY', 'PENDING_PAYMENT', 'billing@startup.local', 'Startup Billing',
     now(), NULL,
     now() + interval '48 hours')
ON CONFLICT (id) DO UPDATE
SET status = EXCLUDED.status, billing_contact_email = EXCLUDED.billing_contact_email,
    contractor_email_verified_at = EXCLUDED.contractor_email_verified_at,
    payment_verified_at = EXCLUDED.payment_verified_at, expires_at = EXCLUDED.expires_at;

-- ─── Subscriptions ────────────────────────────────────────────────────────────
-- No subscription for E (PENDING_PAYMENT).
INSERT INTO app_subscriptions (
    id, contractor_id, client_app_id, app_plan_version_id, contract_id,
    status, current_period_start, current_period_end,
    cancel_at_period_end, auto_renew, next_billing_at
)
VALUES
    -- A: Nova Tech TEAM MONTHLY
    ('24000000-0000-0000-0000-000000000012',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'TEAM' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'nova-web') AND apv.version = 'v1.0'),
     '24000000-0000-0000-0000-000000000007',
     'ACTIVE',
     date_trunc('month', now()),
     date_trunc('month', now()) + interval '1 month',
     FALSE, TRUE,
     date_trunc('month', now()) + interval '1 month'),
    -- B: Global Solutions BUSINESS YEARLY
    ('24000000-0000-0000-0000-000000000013',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'BUSINESS' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'global-web') AND apv.version = 'v1.0'),
     '24000000-0000-0000-0000-000000000008',
     'ACTIVE',
     date_trunc('month', now()) - interval '6 months',
     date_trunc('month', now()) - interval '6 months' + interval '1 year',
     FALSE, TRUE,
     date_trunc('month', now()) - interval '6 months' + interval '1 year'),
    -- C: Global Solutions ENTERPRISE YEARLY (global-api)
    ('24000000-0000-0000-0000-000000000014',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-api'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'ENTERPRISE' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'global-api') AND apv.version = 'v1.0'),
     '24000000-0000-0000-0000-000000000009',
     'ACTIVE',
     date_trunc('month', now()) - interval '1 month',
     date_trunc('month', now()) - interval '1 month' + interval '1 year',
     FALSE, TRUE,
     date_trunc('month', now()) - interval '1 month' + interval '1 year'),
    -- D: Startup FREE
    ('24000000-0000-0000-0000-000000000015',
     '12000000-0000-0000-0000-000000000004',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     (SELECT apv.id FROM app_plan_versions apv JOIN app_plans ap ON ap.id = apv.app_plan_id
      WHERE ap.code = 'FREE' AND ap.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'nova-web') AND apv.version = 'v1.0'),
     '24000000-0000-0000-0000-000000000010',
     'ACTIVE',
     date_trunc('month', now()),
     date_trunc('month', now()) + interval '1 month',
     FALSE, TRUE,
     date_trunc('month', now()) + interval '1 month')
ON CONFLICT (id) DO UPDATE
SET status = EXCLUDED.status, current_period_start = EXCLUDED.current_period_start,
    current_period_end = EXCLUDED.current_period_end, auto_renew = EXCLUDED.auto_renew,
    next_billing_at = EXCLUDED.next_billing_at;

-- ─── Payment transactions ─────────────────────────────────────────────────────
-- Nova TEAM: 2 past months paid. No transaction for FREE. No transaction for PENDING.
INSERT INTO payment_transactions (
    id, contractor_id, client_app_id, contract_id, subscription_id,
    provider, provider_reference, amount, currency, status, paid_at, provider_payload
)
VALUES
    ('24000000-0000-0000-0000-000000000016',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     '24000000-0000-0000-0000-000000000007', '24000000-0000-0000-0000-000000000012',
     'MOCK', 'mock-tx-nova-0001', 49.00, 'USD', 'APPROVED',
     date_trunc('month', now()) - interval '2 months',
     jsonb_build_object('mode', 'seed', 'period', 'month-2')),
    ('24000000-0000-0000-0000-000000000017',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     '24000000-0000-0000-0000-000000000007', '24000000-0000-0000-0000-000000000012',
     'MOCK', 'mock-tx-nova-0002', 49.00, 'USD', 'APPROVED',
     date_trunc('month', now()) - interval '1 month',
     jsonb_build_object('mode', 'seed', 'period', 'month-1')),
    -- Global web: 1 yearly payment up front
    ('24000000-0000-0000-0000-000000000018',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     '24000000-0000-0000-0000-000000000008', '24000000-0000-0000-0000-000000000013',
     'MOCK', 'mock-tx-global-web-0001', 990.00, 'USD', 'APPROVED',
     date_trunc('month', now()) - interval '6 months',
     jsonb_build_object('mode', 'seed', 'period', 'year-1')),
    -- Global api: 1 yearly payment up front
    ('24000000-0000-0000-0000-000000000019',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-api'),
     '24000000-0000-0000-0000-000000000009', '24000000-0000-0000-0000-000000000014',
     'MOCK', 'mock-tx-global-api-0001', 1990.00, 'USD', 'APPROVED',
     date_trunc('month', now()) - interval '1 month',
     jsonb_build_object('mode', 'seed', 'period', 'year-1'))
ON CONFLICT (id) DO UPDATE
SET status = EXCLUDED.status, paid_at = EXCLUDED.paid_at, provider_payload = EXCLUDED.provider_payload;

-- ─── Invoices ─────────────────────────────────────────────────────────────────
INSERT INTO invoices (
    id, contractor_id, client_app_id, subscription_id, invoice_number,
    status, issue_date, due_date, period_start, period_end,
    currency, subtotal, tax_amount, total, paid_at,
    billing_name_snapshot, billing_tax_id_snapshot, billing_address_snapshot,
    plan_name_snapshot, plan_version_snapshot
)
VALUES
    -- Nova month -2 (PAID)
    ('24000000-0000-0000-0000-000000000020',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     '24000000-0000-0000-0000-000000000012',
     'INV-NOVA-0001', 'PAID',
     (date_trunc('month', now()) - interval '2 months')::date,
     (date_trunc('month', now()) - interval '2 months' + interval '15 days')::date,
     (date_trunc('month', now()) - interval '2 months')::date,
     (date_trunc('month', now()) - interval '1 month')::date,
     'USD', 49.00, 0.00, 49.00,
     date_trunc('month', now()) - interval '2 months' + interval '1 day',
     'Nova Tech SRL', 'CL-NOVA-002', 'Av. Innovación 456, Santiago, Chile', 'Team', 'v1.0'),
    -- Nova month -1 (PAID)
    ('24000000-0000-0000-0000-000000000021',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     '24000000-0000-0000-0000-000000000012',
     'INV-NOVA-0002', 'PAID',
     (date_trunc('month', now()) - interval '1 month')::date,
     (date_trunc('month', now()) - interval '1 month' + interval '15 days')::date,
     (date_trunc('month', now()) - interval '1 month')::date,
     date_trunc('month', now())::date,
     'USD', 49.00, 0.00, 49.00,
     date_trunc('month', now()) - interval '1 month' + interval '1 day',
     'Nova Tech SRL', 'CL-NOVA-002', 'Av. Innovación 456, Santiago, Chile', 'Team', 'v1.0'),
    -- Nova current month (ISSUED — awaiting payment)
    ('24000000-0000-0000-0000-000000000022',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     '24000000-0000-0000-0000-000000000012',
     'INV-NOVA-0003', 'ISSUED',
     date_trunc('month', now())::date,
     (date_trunc('month', now()) + interval '15 days')::date,
     date_trunc('month', now())::date,
     (date_trunc('month', now()) + interval '1 month')::date,
     'USD', 49.00, 0.00, 49.00, NULL,
     'Nova Tech SRL', 'CL-NOVA-002', 'Av. Innovación 456, Santiago, Chile', 'Team', 'v1.0'),
    -- Global web yearly (PAID)
    ('24000000-0000-0000-0000-000000000023',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     '24000000-0000-0000-0000-000000000013',
     'INV-GLOBAL-WEB-0001', 'PAID',
     (date_trunc('month', now()) - interval '6 months')::date,
     (date_trunc('month', now()) - interval '6 months' + interval '15 days')::date,
     (date_trunc('month', now()) - interval '6 months')::date,
     (date_trunc('month', now()) - interval '6 months' + interval '1 year')::date,
     'USD', 990.00, 0.00, 990.00,
     date_trunc('month', now()) - interval '6 months' + interval '1 day',
     'Global Solutions SA', 'CL-GLOBAL-003', 'Calle Global 789, Santiago, Chile', 'Business', 'v1.0'),
    -- Global api yearly (PAID)
    ('24000000-0000-0000-0000-000000000024',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-api'),
     '24000000-0000-0000-0000-000000000014',
     'INV-GLOBAL-API-0001', 'PAID',
     (date_trunc('month', now()) - interval '1 month')::date,
     (date_trunc('month', now()) - interval '1 month' + interval '15 days')::date,
     (date_trunc('month', now()) - interval '1 month')::date,
     (date_trunc('month', now()) - interval '1 month' + interval '1 year')::date,
     'USD', 1990.00, 0.00, 1990.00,
     date_trunc('month', now()) - interval '1 month' + interval '1 day',
     'Global Solutions SA', 'CL-GLOBAL-003', 'Calle Global 789, Santiago, Chile', 'Enterprise', 'v1.0')
ON CONFLICT (invoice_number) DO UPDATE
SET status = EXCLUDED.status, paid_at = EXCLUDED.paid_at,
    subtotal = EXCLUDED.subtotal, total = EXCLUDED.total;

-- ─── Usage counters ───────────────────────────────────────────────────────────
INSERT INTO usage_counters (id, contractor_id, client_app_id, metric_code, period_start, period_end, used_value)
VALUES
    ('24000000-0000-0000-0000-000000000025',
     '12000000-0000-0000-0000-000000000002',
     (SELECT id FROM client_apps WHERE client_id = 'nova-web'),
     'MAX_USERS', date_trunc('month', now()), date_trunc('month', now()) + interval '1 month', 3),
    ('24000000-0000-0000-0000-000000000026',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-web'),
     'MAX_USERS', date_trunc('month', now()), date_trunc('month', now()) + interval '1 month', 2),
    ('24000000-0000-0000-0000-000000000027',
     '12000000-0000-0000-0000-000000000003',
     (SELECT id FROM client_apps WHERE client_id = 'global-api'),
     'MAX_USERS', date_trunc('month', now()), date_trunc('month', now()) + interval '1 month', 2)
ON CONFLICT (client_app_id, contractor_id, metric_code, period_start, period_end)
DO UPDATE SET used_value = EXCLUDED.used_value;
