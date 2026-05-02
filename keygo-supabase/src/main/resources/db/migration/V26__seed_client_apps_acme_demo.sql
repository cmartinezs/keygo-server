-- ============================================================================
-- V26__seed_client_apps_acme_demo.sql
-- Dev seed: 3 additional client apps for Acme and 5 for Demo.
-- Stable IDs intentional for local resets.
-- Dev client secret for CONFIDENTIAL apps: Admin1234!
-- BCrypt hash: $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Client apps
-- Acme (3): acme-web (PUBLIC), acme-mobile (PUBLIC), acme-backoffice (CONFIDENTIAL)
-- Demo (5): demo-web (PUBLIC), demo-mobile (PUBLIC), demo-partner (PUBLIC),
--           demo-analytics (PUBLIC), demo-backoffice (CONFIDENTIAL)
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, is_internal, registration_policy)
VALUES
    -- Acme
    ('17000000-0000-0000-0000-000000000004',
     (SELECT id FROM tenants WHERE slug = 'acme'),
     'acme-web', 'Acme Web Portal', 'Main web portal for Acme end users',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'OPEN_AUTO_ACTIVE'),

    ('17000000-0000-0000-0000-000000000005',
     (SELECT id FROM tenants WHERE slug = 'acme'),
     'acme-mobile', 'Acme Mobile App', 'Native mobile application for Acme users',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'OPEN_AUTO_PENDING'),

    ('17000000-0000-0000-0000-000000000006',
     (SELECT id FROM tenants WHERE slug = 'acme'),
     'acme-backoffice', 'Acme Backoffice', 'Internal backoffice for Acme administrators',
     'CONFIDENTIAL', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'ACTIVE', FALSE, 'INVITE_ONLY'),

    -- Demo
    ('17000000-0000-0000-0000-000000000007',
     (SELECT id FROM tenants WHERE slug = 'demo'),
     'demo-web', 'Demo Web App', 'Main web application for Demo tenant users',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'OPEN_AUTO_ACTIVE'),

    ('17000000-0000-0000-0000-000000000008',
     (SELECT id FROM tenants WHERE slug = 'demo'),
     'demo-mobile', 'Demo Mobile App', 'Native mobile application for Demo users',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'OPEN_AUTO_PENDING'),

    ('17000000-0000-0000-0000-000000000009',
     (SELECT id FROM tenants WHERE slug = 'demo'),
     'demo-partner', 'Demo Partner Portal', 'Self-service portal for Demo external partners',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'OPEN_AUTO_ACTIVE'),

    ('17000000-0000-0000-0000-000000000010',
     (SELECT id FROM tenants WHERE slug = 'demo'),
     'demo-analytics', 'Demo Analytics Dashboard', 'Analytics and reporting dashboard for Demo',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'INVITE_ONLY'),

    ('17000000-0000-0000-0000-000000000011',
     (SELECT id FROM tenants WHERE slug = 'demo'),
     'demo-backoffice', 'Demo Backoffice', 'Internal backoffice for Demo administrators',
     'CONFIDENTIAL', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'ACTIVE', FALSE, 'INVITE_ONLY')

ON CONFLICT (client_id) DO UPDATE
SET name                = EXCLUDED.name,
    description         = EXCLUDED.description,
    type                = EXCLUDED.type,
    hashed_secret       = EXCLUDED.hashed_secret,
    status              = EXCLUDED.status,
    is_internal         = EXCLUDED.is_internal,
    registration_policy = EXCLUDED.registration_policy;

-- ---------------------------------------------------------------------------
-- Redirect URIs
-- ---------------------------------------------------------------------------
INSERT INTO client_redirect_uris (id, client_app_id, uri)
VALUES
    ('17100000-0000-0000-0000-000000000004', (SELECT id FROM client_apps WHERE client_id = 'acme-web'),        'http://localhost:4100/callback'),
    ('17100000-0000-0000-0000-000000000005', (SELECT id FROM client_apps WHERE client_id = 'acme-mobile'),     'http://localhost:4110/callback'),
    ('17100000-0000-0000-0000-000000000006', (SELECT id FROM client_apps WHERE client_id = 'acme-backoffice'), 'http://localhost:4120/callback'),
    ('17100000-0000-0000-0000-000000000007', (SELECT id FROM client_apps WHERE client_id = 'demo-web'),        'http://localhost:4200/callback'),
    ('17100000-0000-0000-0000-000000000008', (SELECT id FROM client_apps WHERE client_id = 'demo-mobile'),     'http://localhost:4210/callback'),
    ('17100000-0000-0000-0000-000000000009', (SELECT id FROM client_apps WHERE client_id = 'demo-partner'),    'http://localhost:4220/callback'),
    ('17100000-0000-0000-0000-000000000010', (SELECT id FROM client_apps WHERE client_id = 'demo-analytics'),  'http://localhost:4230/callback'),
    ('17100000-0000-0000-0000-000000000011', (SELECT id FROM client_apps WHERE client_id = 'demo-backoffice'), 'http://localhost:4240/callback')
ON CONFLICT (client_app_id, uri) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Allowed grants: AUTHORIZATION_CODE + REFRESH_TOKEN for all new apps
-- ---------------------------------------------------------------------------
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), ca.id, grants.grant_type
FROM client_apps ca
CROSS JOIN (VALUES ('AUTHORIZATION_CODE'), ('REFRESH_TOKEN')) AS grants(grant_type)
WHERE ca.client_id IN (
    'acme-web', 'acme-mobile', 'acme-backoffice',
    'demo-web', 'demo-mobile', 'demo-partner', 'demo-analytics', 'demo-backoffice'
)
ON CONFLICT (client_app_id, grant_type) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Allowed scopes: openid, profile, email for all new apps
-- ---------------------------------------------------------------------------
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), ca.id, scopes.scope
FROM client_apps ca
CROSS JOIN (VALUES ('openid'), ('profile'), ('email')) AS scopes(scope)
WHERE ca.client_id IN (
    'acme-web', 'acme-mobile', 'acme-backoffice',
    'demo-web', 'demo-mobile', 'demo-partner', 'demo-analytics', 'demo-backoffice'
)
ON CONFLICT (client_app_id, scope) DO NOTHING;

-- ---------------------------------------------------------------------------
-- App roles: admin + user for each new app
-- ---------------------------------------------------------------------------
INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description)
VALUES
    -- Acme Web Portal
    ('18000000-0000-0000-0000-000000000005', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-web'), (SELECT id FROM client_apps WHERE client_id = 'acme-web'), 'admin', 'Admin', 'Acme Web administrators'),
    ('18000000-0000-0000-0000-000000000006', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-web'), (SELECT id FROM client_apps WHERE client_id = 'acme-web'), 'user',  'User',  'Acme Web regular users'),
    -- Acme Mobile App
    ('18000000-0000-0000-0000-000000000007', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-mobile'), (SELECT id FROM client_apps WHERE client_id = 'acme-mobile'), 'admin', 'Admin', 'Acme Mobile administrators'),
    ('18000000-0000-0000-0000-000000000008', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-mobile'), (SELECT id FROM client_apps WHERE client_id = 'acme-mobile'), 'user',  'User',  'Acme Mobile regular users'),
    -- Acme Backoffice
    ('18000000-0000-0000-0000-000000000009', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-backoffice'), (SELECT id FROM client_apps WHERE client_id = 'acme-backoffice'), 'admin', 'Admin', 'Acme Backoffice administrators'),
    ('18000000-0000-0000-0000-000000000010', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-backoffice'), (SELECT id FROM client_apps WHERE client_id = 'acme-backoffice'), 'user',  'User',  'Acme Backoffice operators'),
    -- Demo Web App
    ('18000000-0000-0000-0000-000000000011', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-web'), (SELECT id FROM client_apps WHERE client_id = 'demo-web'), 'admin', 'Admin', 'Demo Web administrators'),
    ('18000000-0000-0000-0000-000000000012', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-web'), (SELECT id FROM client_apps WHERE client_id = 'demo-web'), 'user',  'User',  'Demo Web regular users'),
    -- Demo Mobile App
    ('18000000-0000-0000-0000-000000000013', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-mobile'), (SELECT id FROM client_apps WHERE client_id = 'demo-mobile'), 'admin', 'Admin', 'Demo Mobile administrators'),
    ('18000000-0000-0000-0000-000000000014', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-mobile'), (SELECT id FROM client_apps WHERE client_id = 'demo-mobile'), 'user',  'User',  'Demo Mobile regular users'),
    -- Demo Partner Portal
    ('18000000-0000-0000-0000-000000000015', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-partner'), (SELECT id FROM client_apps WHERE client_id = 'demo-partner'), 'admin',   'Admin',   'Demo Partner Portal administrators'),
    ('18000000-0000-0000-0000-000000000016', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-partner'), (SELECT id FROM client_apps WHERE client_id = 'demo-partner'), 'partner', 'Partner', 'Demo external partners'),
    -- Demo Analytics Dashboard
    ('18000000-0000-0000-0000-000000000017', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-analytics'), (SELECT id FROM client_apps WHERE client_id = 'demo-analytics'), 'admin',  'Admin',  'Demo Analytics administrators'),
    ('18000000-0000-0000-0000-000000000018', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-analytics'), (SELECT id FROM client_apps WHERE client_id = 'demo-analytics'), 'viewer', 'Viewer', 'Demo Analytics read-only viewers'),
    -- Demo Backoffice
    ('18000000-0000-0000-0000-000000000019', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-backoffice'), (SELECT id FROM client_apps WHERE client_id = 'demo-backoffice'), 'admin',    'Admin',    'Demo Backoffice administrators'),
    ('18000000-0000-0000-0000-000000000020', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-backoffice'), (SELECT id FROM client_apps WHERE client_id = 'demo-backoffice'), 'operator', 'Operator', 'Demo Backoffice operators')
ON CONFLICT (client_app_id, code) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description  = EXCLUDED.description;
