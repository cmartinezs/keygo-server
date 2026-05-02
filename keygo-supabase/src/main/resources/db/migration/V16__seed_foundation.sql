-- ============================================================================
-- V16__seed_foundation.sql
-- Development seeds. Stable IDs are intentional for local resets.
-- Dev password for all seeded platform users: Admin1234!
-- BCrypt hash: $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Platform roles and hierarchy
-- ---------------------------------------------------------------------------
INSERT INTO platform_roles (id, code, display_name, description)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'KEYGO_ADMIN', 'Keygo Admin', 'Global platform administration'),
    ('10000000-0000-0000-0000-000000000002', 'KEYGO_ACCOUNT_ADMIN', 'Keygo Account Admin', 'Contractor or tenant scoped account administration'),
    ('10000000-0000-0000-0000-000000000003', 'KEYGO_USER', 'Keygo User', 'Global self-service access')
ON CONFLICT (code) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description;

INSERT INTO platform_role_hierarchy (child_role_id, parent_role_id)
SELECT '10000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002'
WHERE NOT EXISTS (
    SELECT 1 FROM platform_role_hierarchy
    WHERE child_role_id = '10000000-0000-0000-0000-000000000001'
);

INSERT INTO platform_role_hierarchy (child_role_id, parent_role_id)
SELECT '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003'
WHERE NOT EXISTS (
    SELECT 1 FROM platform_role_hierarchy
    WHERE child_role_id = '10000000-0000-0000-0000-000000000002'
);

-- ---------------------------------------------------------------------------
-- Platform users
-- ---------------------------------------------------------------------------
INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name, locale, zoneinfo, status, email_verified_at
)
VALUES
    ('11000000-0000-0000-0000-000000000001', 'admin@keygo.local', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'Keygo', 'Admin', 'Keygo Admin', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000002', 'tenant-admin@keygo.local', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'Tenant', 'Admin', 'Tenant Admin', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000003', 'user@keygo.local', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'Platform', 'User', 'Platform User', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000004', 'contractor@keygo.local', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'Acme', 'Owner', 'Acme Owner', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000005', 'demo-admin@demo.local', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'Demo', 'Admin', 'Demo Admin', 'es-CL', 'America/Santiago', 'ACTIVE', now()),
    ('11000000-0000-0000-0000-000000000006', 'demo-user@demo.local', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'Demo', 'User', 'Demo User', 'es-CL', 'America/Santiago', 'ACTIVE', now())
ON CONFLICT (email) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    display_name = EXCLUDED.display_name,
    locale = EXCLUDED.locale,
    zoneinfo = EXCLUDED.zoneinfo,
    status = EXCLUDED.status,
    email_verified_at = EXCLUDED.email_verified_at;

INSERT INTO platform_user_notification_preferences (id, platform_user_id)
SELECT gen_random_uuid(), pu.id
FROM platform_users pu
WHERE pu.email IN (
    'admin@keygo.local',
    'tenant-admin@keygo.local',
    'user@keygo.local',
    'contractor@keygo.local',
    'demo-admin@demo.local',
    'demo-user@demo.local'
)
ON CONFLICT (platform_user_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Contractor and tenant foundation
-- ---------------------------------------------------------------------------
INSERT INTO contractors (
    id, type, display_name, legal_name, tax_id, billing_email, primary_contact_platform_user_id, status
)
VALUES (
    '12000000-0000-0000-0000-000000000001',
    'COMPANY',
    'Acme Holdings',
    'Acme Holdings SpA',
    'CL-ACME-001',
    'billing@acme.local',
    (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'),
    'ACTIVE'
)
ON CONFLICT (id) DO UPDATE
SET display_name = EXCLUDED.display_name,
    legal_name = EXCLUDED.legal_name,
    tax_id = EXCLUDED.tax_id,
    billing_email = EXCLUDED.billing_email,
    primary_contact_platform_user_id = EXCLUDED.primary_contact_platform_user_id,
    status = EXCLUDED.status;

INSERT INTO contractor_users (contractor_id, platform_user_id, role_code)
VALUES
    ('12000000-0000-0000-0000-000000000001', (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'), 'OWNER'),
    ('12000000-0000-0000-0000-000000000001', (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local'), 'BILLING_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO tenants (id, slug, name, status, contractor_id, is_internal_reserved)
VALUES
    ('13000000-0000-0000-0000-000000000001', 'keygo', 'Keygo Internal', 'ACTIVE', NULL, TRUE),
    ('13000000-0000-0000-0000-000000000002', 'demo', 'Demo Tenant', 'ACTIVE', NULL, FALSE),
    ('13000000-0000-0000-0000-000000000003', 'acme', 'Acme Tenant', 'ACTIVE', (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'), FALSE)
ON CONFLICT (slug) DO UPDATE
SET name = EXCLUDED.name,
    status = EXCLUDED.status,
    contractor_id = EXCLUDED.contractor_id,
    is_internal_reserved = EXCLUDED.is_internal_reserved;

-- ---------------------------------------------------------------------------
-- Platform RBAC assignments
-- ---------------------------------------------------------------------------
INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type)
SELECT
    '14000000-0000-0000-0000-000000000001',
    pu.id,
    pr.id,
    'GLOBAL'
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_ADMIN'
WHERE pu.email = 'admin@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_roles pur
      WHERE pur.platform_user_id = pu.id
        AND pur.role_id = pr.id
        AND pur.scope_type = 'GLOBAL'
  );

INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type)
SELECT
    '14000000-0000-0000-0000-000000000002',
    pu.id,
    pr.id,
    'GLOBAL'
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_USER'
WHERE pu.email = 'user@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_roles pur
      WHERE pur.platform_user_id = pu.id
        AND pur.role_id = pr.id
        AND pur.scope_type = 'GLOBAL'
  );

INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type, contractor_id)
SELECT
    '14000000-0000-0000-0000-000000000003',
    pu.id,
    pr.id,
    'CONTRACTOR',
    c.id
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_ACCOUNT_ADMIN'
JOIN contractors c ON c.billing_email = 'billing@acme.local'
WHERE pu.email = 'tenant-admin@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_roles pur
      WHERE pur.platform_user_id = pu.id
        AND pur.role_id = pr.id
        AND pur.scope_type = 'CONTRACTOR'
        AND pur.contractor_id = c.id
  );

INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type, contractor_id)
SELECT
    '14000000-0000-0000-0000-000000000004',
    pu.id,
    pr.id,
    'CONTRACTOR',
    c.id
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_ACCOUNT_ADMIN'
JOIN contractors c ON c.billing_email = 'billing@acme.local'
WHERE pu.email = 'contractor@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_roles pur
      WHERE pur.platform_user_id = pu.id
        AND pur.role_id = pr.id
        AND pur.scope_type = 'CONTRACTOR'
        AND pur.contractor_id = c.id
  );

-- ---------------------------------------------------------------------------
-- Tenant users: explicit participation only. Nobody is auto-enrolled in keygo.
-- tenant-admin@keygo.local participates in demo and acme to validate multi-tenant identity.
-- ---------------------------------------------------------------------------
INSERT INTO tenant_users (id, tenant_id, platform_user_id, local_username, display_name_override, status)
VALUES
    ('15000000-0000-0000-0000-000000000001', (SELECT id FROM tenants WHERE slug = 'demo'), (SELECT id FROM platform_users WHERE email = 'demo-admin@demo.local'), 'demo-admin', 'Demo Admin', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000002', (SELECT id FROM tenants WHERE slug = 'demo'), (SELECT id FROM platform_users WHERE email = 'demo-user@demo.local'), 'demo-user', 'Demo User', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000003', (SELECT id FROM tenants WHERE slug = 'demo'), (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local'), 'tenant-admin-demo', 'Tenant Admin Demo', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000004', (SELECT id FROM tenants WHERE slug = 'acme'), (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local'), 'tenant-admin-acme', 'Tenant Admin Acme', 'ACTIVE'),
    ('15000000-0000-0000-0000-000000000005', (SELECT id FROM tenants WHERE slug = 'acme'), (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'), 'acme-owner', 'Acme Owner', 'ACTIVE')
ON CONFLICT (tenant_id, platform_user_id) DO UPDATE
SET local_username = EXCLUDED.local_username,
    display_name_override = EXCLUDED.display_name_override,
    status = EXCLUDED.status;

-- ---------------------------------------------------------------------------
-- Tenant RBAC
-- ---------------------------------------------------------------------------
INSERT INTO tenant_roles (id, tenant_id, code, display_name, description)
VALUES
    ('16000000-0000-0000-0000-000000000001', (SELECT id FROM tenants WHERE slug = 'demo'), 'admin', 'Admin', 'Tenant administrators'),
    ('16000000-0000-0000-0000-000000000002', (SELECT id FROM tenants WHERE slug = 'demo'), 'user', 'User', 'Regular tenant users'),
    ('16000000-0000-0000-0000-000000000003', (SELECT id FROM tenants WHERE slug = 'acme'), 'owner', 'Owner', 'Tenant owner'),
    ('16000000-0000-0000-0000-000000000004', (SELECT id FROM tenants WHERE slug = 'acme'), 'admin', 'Admin', 'Tenant administrators'),
    ('16000000-0000-0000-0000-000000000005', (SELECT id FROM tenants WHERE slug = 'acme'), 'user', 'User', 'Regular tenant users')
ON CONFLICT (tenant_id, code) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description;

INSERT INTO tenant_user_roles (tenant_user_id, tenant_id, role_id)
SELECT tu.id, tu.tenant_id, tr.id
FROM tenant_users tu
JOIN tenant_roles tr ON tr.tenant_id = tu.tenant_id
WHERE (
        tu.local_username = 'demo-admin' AND tr.code = 'admin'
    ) OR (
        tu.local_username = 'demo-user' AND tr.code = 'user'
    ) OR (
        tu.local_username = 'tenant-admin-demo' AND tr.code = 'admin'
    ) OR (
        tu.local_username = 'tenant-admin-acme' AND tr.code = 'admin'
    ) OR (
        tu.local_username = 'acme-owner' AND tr.code = 'owner'
    )
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------
-- Client apps and their OAuth configuration
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, is_internal)
VALUES
    ('17000000-0000-0000-0000-000000000001', (SELECT id FROM tenants WHERE slug = 'keygo'), 'keygo-ui', 'Keygo UI', 'Internal platform account UI and OAuth technical client', 'PUBLIC', NULL, 'ACTIVE', TRUE),
    ('17000000-0000-0000-0000-000000000002', (SELECT id FROM tenants WHERE slug = 'demo'), 'demo-ui', 'Demo UI', 'Demo tenant frontend', 'PUBLIC', NULL, 'ACTIVE', FALSE),
    ('17000000-0000-0000-0000-000000000003', (SELECT id FROM tenants WHERE slug = 'acme'), 'acme-ui', 'Acme UI', 'Commercial tenant frontend', 'PUBLIC', NULL, 'ACTIVE', FALSE)
ON CONFLICT (client_id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    type = EXCLUDED.type,
    hashed_secret = EXCLUDED.hashed_secret,
    status = EXCLUDED.status,
    is_internal = EXCLUDED.is_internal;

INSERT INTO client_redirect_uris (id, client_app_id, uri)
VALUES
    ('17100000-0000-0000-0000-000000000001', (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'), 'http://localhost:3000/callback'),
    ('17100000-0000-0000-0000-000000000002', (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), 'http://localhost:3001/auth/callback'),
    ('17100000-0000-0000-0000-000000000003', (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'http://localhost:3002/auth/callback')
ON CONFLICT (client_app_id, uri) DO NOTHING;

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), ca.id, grants.grant_type
FROM client_apps ca
CROSS JOIN (VALUES ('AUTHORIZATION_CODE'), ('REFRESH_TOKEN')) AS grants(grant_type)
WHERE ca.client_id IN ('keygo-ui', 'demo-ui', 'acme-ui')
ON CONFLICT (client_app_id, grant_type) DO NOTHING;

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), ca.id, scopes.scope
FROM client_apps ca
CROSS JOIN (VALUES ('openid'), ('profile'), ('email')) AS scopes(scope)
WHERE ca.client_id IN ('keygo-ui', 'demo-ui', 'acme-ui')
ON CONFLICT (client_app_id, scope) DO NOTHING;

-- ---------------------------------------------------------------------------
-- App RBAC and memberships. keygo-ui intentionally has no app_roles for platform RBAC.
-- ---------------------------------------------------------------------------
INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description)
VALUES
    ('18000000-0000-0000-0000-000000000001', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-ui'), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), 'admin', 'Admin', 'Demo UI administrators'),
    ('18000000-0000-0000-0000-000000000002', (SELECT tenant_id FROM client_apps WHERE client_id = 'demo-ui'), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), 'user', 'User', 'Demo UI users'),
    ('18000000-0000-0000-0000-000000000003', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-ui'), (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'admin', 'Admin', 'Acme UI administrators'),
    ('18000000-0000-0000-0000-000000000004', (SELECT tenant_id FROM client_apps WHERE client_id = 'acme-ui'), (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'user', 'User', 'Acme UI users')
ON CONFLICT (client_app_id, code) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description;

INSERT INTO app_memberships (id, tenant_id, tenant_user_id, client_app_id, status)
VALUES
    ('19000000-0000-0000-0000-000000000001', (SELECT id FROM tenants WHERE slug = 'demo'), (SELECT id FROM tenant_users WHERE local_username = 'demo-admin'), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000002', (SELECT id FROM tenants WHERE slug = 'demo'), (SELECT id FROM tenant_users WHERE local_username = 'demo-user'), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000003', (SELECT id FROM tenants WHERE slug = 'demo'), (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-demo'), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000004', (SELECT id FROM tenants WHERE slug = 'acme'), (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-acme'), (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'ACTIVE'),
    ('19000000-0000-0000-0000-000000000005', (SELECT id FROM tenants WHERE slug = 'acme'), (SELECT id FROM tenant_users WHERE local_username = 'acme-owner'), (SELECT id FROM client_apps WHERE client_id = 'acme-ui'), 'ACTIVE')
ON CONFLICT (tenant_user_id, client_app_id) DO UPDATE
SET status = EXCLUDED.status;

INSERT INTO app_membership_roles (membership_id, client_app_id, role_id)
SELECT am.id, am.client_app_id, ar.id
FROM app_memberships am
JOIN app_roles ar ON ar.client_app_id = am.client_app_id
WHERE (
        am.id = '19000000-0000-0000-0000-000000000001' AND ar.code = 'admin'
    ) OR (
        am.id = '19000000-0000-0000-0000-000000000002' AND ar.code = 'user'
    ) OR (
        am.id = '19000000-0000-0000-0000-000000000003' AND ar.code = 'admin'
    ) OR (
        am.id = '19000000-0000-0000-0000-000000000004' AND ar.code = 'admin'
    ) OR (
        am.id = '19000000-0000-0000-0000-000000000005' AND ar.code = 'admin'
    )
ON CONFLICT DO NOTHING;

-- No audit seed rows are inserted here. Audit should represent real activity, not synthetic baseline noise.
