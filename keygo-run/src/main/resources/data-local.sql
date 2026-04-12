-- ============================================================================
-- data-local.sql — Canonical local seed for H2
-- ============================================================================
-- Profile: local,h2
-- DB: H2 file-based in PostgreSQL mode
-- Purpose:
--   - bootstrap a local dev database aligned with the active data model
--   - keep inserts idempotent for repeated startups
--   - clean obvious legacy artifacts from the pre-refactor local schema
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Legacy cleanup from the pre-refactor local model
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS membership_roles;
DROP TABLE IF EXISTS memberships;

ALTER TABLE tenants DROP COLUMN IF EXISTS owner_email;
ALTER TABLE tenant_users DROP COLUMN IF EXISTS username;
ALTER TABLE tenant_users DROP COLUMN IF EXISTS email;
ALTER TABLE tenant_users DROP COLUMN IF EXISTS password_hash;
ALTER TABLE tenant_users DROP COLUMN IF EXISTS first_name;
ALTER TABLE tenant_users DROP COLUMN IF EXISTS last_name;
ALTER TABLE tenant_users DROP COLUMN IF EXISTS email_verified;

ALTER TABLE client_redirect_uris
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE client_allowed_grants
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE client_allowed_scopes
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

DELETE FROM client_allowed_scopes
WHERE client_app_id IN (SELECT id FROM client_apps WHERE client_id = 'key-go-ui');

DELETE FROM client_allowed_grants
WHERE client_app_id IN (SELECT id FROM client_apps WHERE client_id = 'key-go-ui');

DELETE FROM client_redirect_uris
WHERE client_app_id IN (SELECT id FROM client_apps WHERE client_id = 'key-go-ui');

DELETE FROM app_roles
WHERE client_app_id IN (SELECT id FROM client_apps WHERE client_id = 'key-go-ui');

DELETE FROM client_apps
WHERE client_id = 'key-go-ui';

DELETE FROM tenant_users
WHERE platform_user_id IS NULL;

-- ---------------------------------------------------------------------------
-- Stable credentials for local development
-- ---------------------------------------------------------------------------
-- All seeded platform users use the same password unless noted otherwise:
--   Admin1234! -> $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm

-- ---------------------------------------------------------------------------
-- Platform roles and hierarchy
-- ---------------------------------------------------------------------------
UPDATE platform_roles
SET display_name = 'Keygo Admin',
    description = 'Global platform administration',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'KEYGO_ADMIN';

INSERT INTO platform_roles (id, code, display_name, description, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000001', 'KEYGO_ADMIN', 'Keygo Admin', 'Global platform administration',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_roles WHERE code = 'KEYGO_ADMIN');

UPDATE platform_roles
SET display_name = 'Keygo Account Admin',
    description = 'Contractor or tenant scoped account administration',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'KEYGO_ACCOUNT_ADMIN';

INSERT INTO platform_roles (id, code, display_name, description, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000002', 'KEYGO_ACCOUNT_ADMIN', 'Keygo Account Admin',
       'Contractor or tenant scoped account administration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_roles WHERE code = 'KEYGO_ACCOUNT_ADMIN');

UPDATE platform_roles
SET display_name = 'Keygo User',
    description = 'Global self-service access',
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'KEYGO_USER';

INSERT INTO platform_roles (id, code, display_name, description, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000003', 'KEYGO_USER', 'Keygo User', 'Global self-service access',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_roles WHERE code = 'KEYGO_USER');

INSERT INTO platform_role_hierarchy (child_role_id, parent_role_id, created_at)
SELECT child.id, parent.id, CURRENT_TIMESTAMP
FROM platform_roles child
JOIN platform_roles parent ON parent.code = 'KEYGO_ACCOUNT_ADMIN'
WHERE child.code = 'KEYGO_ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM platform_role_hierarchy prh
      WHERE prh.child_role_id = child.id
  );

INSERT INTO platform_role_hierarchy (child_role_id, parent_role_id, created_at)
SELECT child.id, parent.id, CURRENT_TIMESTAMP
FROM platform_roles child
JOIN platform_roles parent ON parent.code = 'KEYGO_USER'
WHERE child.code = 'KEYGO_ACCOUNT_ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM platform_role_hierarchy prh
      WHERE prh.child_role_id = child.id
  );

-- ---------------------------------------------------------------------------
-- Platform users and notification preferences
-- ---------------------------------------------------------------------------
UPDATE platform_users
SET password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    first_name = 'Keygo',
    last_name = 'Admin',
    display_name = 'Keygo Admin',
    locale = 'es-CL',
    zoneinfo = 'America/Santiago',
    status = 'ACTIVE',
    email_verified_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin@keygo.local';

INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name, locale, zoneinfo,
    status, email_verified_at, created_at, updated_at
)
SELECT '11000000-0000-0000-0000-000000000001', 'admin@keygo.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Keygo', 'Admin', 'Keygo Admin', 'es-CL', 'America/Santiago',
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_users WHERE email = 'admin@keygo.local');

UPDATE platform_users
SET password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    first_name = 'Tenant',
    last_name = 'Admin',
    display_name = 'Tenant Admin',
    locale = 'es-CL',
    zoneinfo = 'America/Santiago',
    status = 'ACTIVE',
    email_verified_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'tenant-admin@keygo.local';

INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name, locale, zoneinfo,
    status, email_verified_at, created_at, updated_at
)
SELECT '11000000-0000-0000-0000-000000000002', 'tenant-admin@keygo.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Tenant', 'Admin', 'Tenant Admin', 'es-CL', 'America/Santiago',
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_users WHERE email = 'tenant-admin@keygo.local');

UPDATE platform_users
SET password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    first_name = 'Platform',
    last_name = 'User',
    display_name = 'Platform User',
    locale = 'es-CL',
    zoneinfo = 'America/Santiago',
    status = 'ACTIVE',
    email_verified_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'user@keygo.local';

INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name, locale, zoneinfo,
    status, email_verified_at, created_at, updated_at
)
SELECT '11000000-0000-0000-0000-000000000003', 'user@keygo.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Platform', 'User', 'Platform User', 'es-CL', 'America/Santiago',
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_users WHERE email = 'user@keygo.local');

UPDATE platform_users
SET password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    first_name = 'Acme',
    last_name = 'Owner',
    display_name = 'Acme Owner',
    locale = 'es-CL',
    zoneinfo = 'America/Santiago',
    status = 'ACTIVE',
    email_verified_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'contractor@keygo.local';

INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name, locale, zoneinfo,
    status, email_verified_at, created_at, updated_at
)
SELECT '11000000-0000-0000-0000-000000000004', 'contractor@keygo.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Acme', 'Owner', 'Acme Owner', 'es-CL', 'America/Santiago',
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_users WHERE email = 'contractor@keygo.local');

UPDATE platform_users
SET password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    first_name = 'Demo',
    last_name = 'Admin',
    display_name = 'Demo Admin',
    locale = 'es-CL',
    zoneinfo = 'America/Santiago',
    status = 'ACTIVE',
    email_verified_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'demo-admin@demo.local';

INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name, locale, zoneinfo,
    status, email_verified_at, created_at, updated_at
)
SELECT '11000000-0000-0000-0000-000000000005', 'demo-admin@demo.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Demo', 'Admin', 'Demo Admin', 'es-CL', 'America/Santiago',
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_users WHERE email = 'demo-admin@demo.local');

UPDATE platform_users
SET password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    first_name = 'Demo',
    last_name = 'User',
    display_name = 'Demo User',
    locale = 'es-CL',
    zoneinfo = 'America/Santiago',
    status = 'ACTIVE',
    email_verified_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'demo-user@demo.local';

INSERT INTO platform_users (
    id, email, password_hash, first_name, last_name, display_name, locale, zoneinfo,
    status, email_verified_at, created_at, updated_at
)
SELECT '11000000-0000-0000-0000-000000000006', 'demo-user@demo.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Demo', 'User', 'Demo User', 'es-CL', 'America/Santiago',
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM platform_users WHERE email = 'demo-user@demo.local');

INSERT INTO platform_user_notification_preferences (
    id, platform_user_id, security_alerts_email, security_alerts_in_app, billing_alerts_email,
    product_updates_email, weekly_digest, created_at, updated_at
)
SELECT '11500000-0000-0000-0000-000000000001', pu.id, TRUE, TRUE, TRUE, FALSE, FALSE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
WHERE pu.email = 'admin@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_notification_preferences p
      WHERE p.platform_user_id = pu.id
  );

INSERT INTO platform_user_notification_preferences (
    id, platform_user_id, security_alerts_email, security_alerts_in_app, billing_alerts_email,
    product_updates_email, weekly_digest, created_at, updated_at
)
SELECT '11500000-0000-0000-0000-000000000002', pu.id, TRUE, TRUE, TRUE, FALSE, FALSE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
WHERE pu.email = 'tenant-admin@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_notification_preferences p
      WHERE p.platform_user_id = pu.id
  );

INSERT INTO platform_user_notification_preferences (
    id, platform_user_id, security_alerts_email, security_alerts_in_app, billing_alerts_email,
    product_updates_email, weekly_digest, created_at, updated_at
)
SELECT '11500000-0000-0000-0000-000000000003', pu.id, TRUE, TRUE, TRUE, FALSE, FALSE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
WHERE pu.email = 'user@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_notification_preferences p
      WHERE p.platform_user_id = pu.id
  );

INSERT INTO platform_user_notification_preferences (
    id, platform_user_id, security_alerts_email, security_alerts_in_app, billing_alerts_email,
    product_updates_email, weekly_digest, created_at, updated_at
)
SELECT '11500000-0000-0000-0000-000000000004', pu.id, TRUE, TRUE, TRUE, FALSE, FALSE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
WHERE pu.email = 'contractor@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_notification_preferences p
      WHERE p.platform_user_id = pu.id
  );

INSERT INTO platform_user_notification_preferences (
    id, platform_user_id, security_alerts_email, security_alerts_in_app, billing_alerts_email,
    product_updates_email, weekly_digest, created_at, updated_at
)
SELECT '11500000-0000-0000-0000-000000000005', pu.id, TRUE, TRUE, TRUE, FALSE, FALSE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
WHERE pu.email = 'demo-admin@demo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_notification_preferences p
      WHERE p.platform_user_id = pu.id
  );

INSERT INTO platform_user_notification_preferences (
    id, platform_user_id, security_alerts_email, security_alerts_in_app, billing_alerts_email,
    product_updates_email, weekly_digest, created_at, updated_at
)
SELECT '11500000-0000-0000-0000-000000000006', pu.id, TRUE, TRUE, TRUE, FALSE, FALSE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
WHERE pu.email = 'demo-user@demo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_notification_preferences p
      WHERE p.platform_user_id = pu.id
  );

-- ---------------------------------------------------------------------------
-- Contractor and tenant foundation
-- ---------------------------------------------------------------------------
UPDATE contractors
SET type = 'COMPANY',
    display_name = 'Acme Holdings',
    legal_name = 'Acme Holdings SpA',
    tax_id = 'CL-ACME-001',
    primary_contact_platform_user_id = (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'),
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE billing_email = 'billing@acme.local';

INSERT INTO contractors (
    id, type, display_name, legal_name, tax_id, billing_email,
    primary_contact_platform_user_id, status, created_at, updated_at
)
SELECT '12000000-0000-0000-0000-000000000001', 'COMPANY', 'Acme Holdings', 'Acme Holdings SpA',
       'CL-ACME-001', 'billing@acme.local',
       (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'),
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM contractors WHERE billing_email = 'billing@acme.local');

DELETE FROM contractor_users
WHERE contractor_id = (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local')
  AND role_code IN ('OWNER', 'BILLING_ADMIN')
  AND platform_user_id NOT IN (
      SELECT id FROM platform_users
      WHERE email IN ('contractor@keygo.local', 'tenant-admin@keygo.local')
  );

INSERT INTO contractor_users (contractor_id, platform_user_id, role_code, assigned_at)
SELECT c.id, pu.id, 'OWNER', CURRENT_TIMESTAMP
FROM contractors c
JOIN platform_users pu ON pu.email = 'contractor@keygo.local'
WHERE c.billing_email = 'billing@acme.local'
  AND NOT EXISTS (
      SELECT 1 FROM contractor_users cu
      WHERE cu.contractor_id = c.id
        AND cu.platform_user_id = pu.id
        AND cu.role_code = 'OWNER'
  );

INSERT INTO contractor_users (contractor_id, platform_user_id, role_code, assigned_at)
SELECT c.id, pu.id, 'BILLING_ADMIN', CURRENT_TIMESTAMP
FROM contractors c
JOIN platform_users pu ON pu.email = 'tenant-admin@keygo.local'
WHERE c.billing_email = 'billing@acme.local'
  AND NOT EXISTS (
      SELECT 1 FROM contractor_users cu
      WHERE cu.contractor_id = c.id
        AND cu.platform_user_id = pu.id
        AND cu.role_code = 'BILLING_ADMIN'
  );

UPDATE tenants
SET name = 'Keygo Internal',
    status = 'ACTIVE',
    contractor_id = NULL,
    is_internal_reserved = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'keygo';

INSERT INTO tenants (id, slug, name, status, contractor_id, is_internal_reserved, created_at, updated_at)
SELECT '13000000-0000-0000-0000-000000000001', 'keygo', 'Keygo Internal', 'ACTIVE', NULL, TRUE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'keygo');

UPDATE tenants
SET name = 'Demo Tenant',
    status = 'ACTIVE',
    contractor_id = NULL,
    is_internal_reserved = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'demo';

INSERT INTO tenants (id, slug, name, status, contractor_id, is_internal_reserved, created_at, updated_at)
SELECT '13000000-0000-0000-0000-000000000002', 'demo', 'Demo Tenant', 'ACTIVE', NULL, FALSE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'demo');

UPDATE tenants
SET name = 'Acme Tenant',
    status = 'ACTIVE',
    contractor_id = (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
    is_internal_reserved = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE slug = 'acme';

INSERT INTO tenants (id, slug, name, status, contractor_id, is_internal_reserved, created_at, updated_at)
SELECT '13000000-0000-0000-0000-000000000003', 'acme', 'Acme Tenant', 'ACTIVE',
       (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
       FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'acme');

-- ---------------------------------------------------------------------------
-- Platform RBAC assignments
-- ---------------------------------------------------------------------------
INSERT INTO platform_user_roles (
    id, platform_user_id, role_id, scope_type, contractor_id, tenant_id, assigned_at, created_at, updated_at
)
SELECT '14000000-0000-0000-0000-000000000001', pu.id, pr.id, 'GLOBAL', NULL, NULL,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_ADMIN'
WHERE pu.email = 'admin@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_roles pur
      WHERE pur.platform_user_id = pu.id
        AND pur.role_id = pr.id
        AND pur.scope_type = 'GLOBAL'
  );

INSERT INTO platform_user_roles (
    id, platform_user_id, role_id, scope_type, contractor_id, tenant_id, assigned_at, created_at, updated_at
)
SELECT '14000000-0000-0000-0000-000000000002', pu.id, pr.id, 'GLOBAL', NULL, NULL,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_USER'
WHERE pu.email = 'user@keygo.local'
  AND NOT EXISTS (
      SELECT 1 FROM platform_user_roles pur
      WHERE pur.platform_user_id = pu.id
        AND pur.role_id = pr.id
        AND pur.scope_type = 'GLOBAL'
  );

INSERT INTO platform_user_roles (
    id, platform_user_id, role_id, scope_type, contractor_id, tenant_id, assigned_at, created_at, updated_at
)
SELECT '14000000-0000-0000-0000-000000000003', pu.id, pr.id, 'CONTRACTOR', c.id, NULL,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
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

INSERT INTO platform_user_roles (
    id, platform_user_id, role_id, scope_type, contractor_id, tenant_id, assigned_at, created_at, updated_at
)
SELECT '14000000-0000-0000-0000-000000000004', pu.id, pr.id, 'CONTRACTOR', c.id, NULL,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
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
-- Tenant participation and tenant RBAC
-- ---------------------------------------------------------------------------
UPDATE tenant_users
SET local_username = 'demo-admin',
    display_name_override = 'Demo Admin',
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
  AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'demo-admin@demo.local');

INSERT INTO tenant_users (
    id, tenant_id, platform_user_id, local_username, display_name_override, status, created_at, updated_at
)
SELECT '15000000-0000-0000-0000-000000000001',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM platform_users WHERE email = 'demo-admin@demo.local'),
       'demo-admin', 'Demo Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_users
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
      AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'demo-admin@demo.local')
);

UPDATE tenant_users
SET local_username = 'demo-user',
    display_name_override = 'Demo User',
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
  AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'demo-user@demo.local');

INSERT INTO tenant_users (
    id, tenant_id, platform_user_id, local_username, display_name_override, status, created_at, updated_at
)
SELECT '15000000-0000-0000-0000-000000000002',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM platform_users WHERE email = 'demo-user@demo.local'),
       'demo-user', 'Demo User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_users
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
      AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'demo-user@demo.local')
);

UPDATE tenant_users
SET local_username = 'tenant-admin-demo',
    display_name_override = 'Tenant Admin Demo',
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
  AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local');

INSERT INTO tenant_users (
    id, tenant_id, platform_user_id, local_username, display_name_override, status, created_at, updated_at
)
SELECT '15000000-0000-0000-0000-000000000003',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local'),
       'tenant-admin-demo', 'Tenant Admin Demo', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_users
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
      AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local')
);

UPDATE tenant_users
SET local_username = 'tenant-admin-acme',
    display_name_override = 'Tenant Admin Acme',
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
  AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local');

INSERT INTO tenant_users (
    id, tenant_id, platform_user_id, local_username, display_name_override, status, created_at, updated_at
)
SELECT '15000000-0000-0000-0000-000000000004',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local'),
       'tenant-admin-acme', 'Tenant Admin Acme', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_users
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
      AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'tenant-admin@keygo.local')
);

UPDATE tenant_users
SET local_username = 'acme-owner',
    display_name_override = 'Acme Owner',
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
  AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local');

INSERT INTO tenant_users (
    id, tenant_id, platform_user_id, local_username, display_name_override, status, created_at, updated_at
)
SELECT '15000000-0000-0000-0000-000000000005',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'),
       'acme-owner', 'Acme Owner', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_users
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
      AND platform_user_id = (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local')
);

UPDATE tenant_roles
SET display_name = 'Admin',
    description = 'Tenant administrators',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo') AND code = 'admin';

INSERT INTO tenant_roles (id, tenant_id, code, display_name, description, created_at, updated_at)
SELECT '16000000-0000-0000-0000-000000000001',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       'admin', 'Admin', 'Tenant administrators', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_roles
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
      AND code = 'admin'
);

UPDATE tenant_roles
SET display_name = 'User',
    description = 'Regular tenant users',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo') AND code = 'user';

INSERT INTO tenant_roles (id, tenant_id, code, display_name, description, created_at, updated_at)
SELECT '16000000-0000-0000-0000-000000000002',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       'user', 'User', 'Regular tenant users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_roles
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'demo')
      AND code = 'user'
);

UPDATE tenant_roles
SET display_name = 'Owner',
    description = 'Tenant owner',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme') AND code = 'owner';

INSERT INTO tenant_roles (id, tenant_id, code, display_name, description, created_at, updated_at)
SELECT '16000000-0000-0000-0000-000000000003',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       'owner', 'Owner', 'Tenant owner', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_roles
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
      AND code = 'owner'
);

UPDATE tenant_roles
SET display_name = 'Admin',
    description = 'Tenant administrators',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme') AND code = 'admin';

INSERT INTO tenant_roles (id, tenant_id, code, display_name, description, created_at, updated_at)
SELECT '16000000-0000-0000-0000-000000000004',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       'admin', 'Admin', 'Tenant administrators', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_roles
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
      AND code = 'admin'
);

UPDATE tenant_roles
SET display_name = 'User',
    description = 'Regular tenant users',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme') AND code = 'user';

INSERT INTO tenant_roles (id, tenant_id, code, display_name, description, created_at, updated_at)
SELECT '16000000-0000-0000-0000-000000000005',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       'user', 'User', 'Regular tenant users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_roles
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
      AND code = 'user'
);

INSERT INTO tenant_user_roles (tenant_user_id, tenant_id, role_id, assigned_at)
SELECT tu.id, tu.tenant_id, tr.id, CURRENT_TIMESTAMP
FROM tenant_users tu
JOIN tenant_roles tr ON tr.tenant_id = tu.tenant_id
WHERE tu.local_username = 'demo-admin'
  AND tr.code = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM tenant_user_roles tur
      WHERE tur.tenant_user_id = tu.id
        AND tur.role_id = tr.id
  );

INSERT INTO tenant_user_roles (tenant_user_id, tenant_id, role_id, assigned_at)
SELECT tu.id, tu.tenant_id, tr.id, CURRENT_TIMESTAMP
FROM tenant_users tu
JOIN tenant_roles tr ON tr.tenant_id = tu.tenant_id
WHERE tu.local_username = 'demo-user'
  AND tr.code = 'user'
  AND NOT EXISTS (
      SELECT 1 FROM tenant_user_roles tur
      WHERE tur.tenant_user_id = tu.id
        AND tur.role_id = tr.id
  );

INSERT INTO tenant_user_roles (tenant_user_id, tenant_id, role_id, assigned_at)
SELECT tu.id, tu.tenant_id, tr.id, CURRENT_TIMESTAMP
FROM tenant_users tu
JOIN tenant_roles tr ON tr.tenant_id = tu.tenant_id
WHERE tu.local_username = 'tenant-admin-demo'
  AND tr.code = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM tenant_user_roles tur
      WHERE tur.tenant_user_id = tu.id
        AND tur.role_id = tr.id
  );

INSERT INTO tenant_user_roles (tenant_user_id, tenant_id, role_id, assigned_at)
SELECT tu.id, tu.tenant_id, tr.id, CURRENT_TIMESTAMP
FROM tenant_users tu
JOIN tenant_roles tr ON tr.tenant_id = tu.tenant_id
WHERE tu.local_username = 'tenant-admin-acme'
  AND tr.code = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM tenant_user_roles tur
      WHERE tur.tenant_user_id = tu.id
        AND tur.role_id = tr.id
  );

INSERT INTO tenant_user_roles (tenant_user_id, tenant_id, role_id, assigned_at)
SELECT tu.id, tu.tenant_id, tr.id, CURRENT_TIMESTAMP
FROM tenant_users tu
JOIN tenant_roles tr ON tr.tenant_id = tu.tenant_id
WHERE tu.local_username = 'acme-owner'
  AND tr.code = 'owner'
  AND NOT EXISTS (
      SELECT 1 FROM tenant_user_roles tur
      WHERE tur.tenant_user_id = tu.id
        AND tur.role_id = tr.id
  );

-- ---------------------------------------------------------------------------
-- Client apps, OAuth config and app memberships
-- ---------------------------------------------------------------------------
UPDATE client_apps
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'keygo'),
    name = 'Keygo UI',
    description = 'Internal platform account UI and OAuth technical client',
    type = 'PUBLIC',
    hashed_secret = NULL,
    status = 'ACTIVE',
    is_internal = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE client_id = 'keygo-ui';

INSERT INTO client_apps (
    id, tenant_id, client_id, name, description, type, hashed_secret, status, is_internal, created_at, updated_at
)
SELECT '17000000-0000-0000-0000-000000000001',
       (SELECT id FROM tenants WHERE slug = 'keygo'),
       'keygo-ui', 'Keygo UI', 'Internal platform account UI and OAuth technical client',
       'PUBLIC', NULL, 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'keygo-ui');

UPDATE client_apps
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'demo'),
    name = 'Demo UI',
    description = 'Demo tenant frontend',
    type = 'PUBLIC',
    hashed_secret = NULL,
    status = 'ACTIVE',
    is_internal = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE client_id = 'demo-ui';

INSERT INTO client_apps (
    id, tenant_id, client_id, name, description, type, hashed_secret, status, is_internal, created_at, updated_at
)
SELECT '17000000-0000-0000-0000-000000000002',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       'demo-ui', 'Demo UI', 'Demo tenant frontend',
       'PUBLIC', NULL, 'ACTIVE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'demo-ui');

UPDATE client_apps
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'acme'),
    name = 'Acme UI',
    description = 'Commercial tenant frontend',
    type = 'PUBLIC',
    hashed_secret = NULL,
    status = 'ACTIVE',
    is_internal = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE client_id = 'acme-ui';

INSERT INTO client_apps (
    id, tenant_id, client_id, name, description, type, hashed_secret, status, is_internal, created_at, updated_at
)
SELECT '17000000-0000-0000-0000-000000000003',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       'acme-ui', 'Acme UI', 'Commercial tenant frontend',
       'PUBLIC', NULL, 'ACTIVE', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'acme-ui');

INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '17100000-0000-0000-0000-000000000001',
       (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
       'http://localhost:5173/callback',
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_redirect_uris
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
      AND uri = 'http://localhost:5173/callback'
);

INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '17100000-0000-0000-0000-000000000002',
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'http://localhost:5173/auth/callback',
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_redirect_uris
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND uri = 'http://localhost:5173/auth/callback'
);

INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '17100000-0000-0000-0000-000000000003',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'http://localhost:5173/acme/callback',
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_redirect_uris
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND uri = 'http://localhost:5173/acme/callback'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type, created_at)
SELECT '17200000-0000-0000-0000-000000000001',
       (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
       'AUTHORIZATION_CODE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
      AND grant_type = 'AUTHORIZATION_CODE'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type, created_at)
SELECT '17200000-0000-0000-0000-000000000002',
       (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
       'REFRESH_TOKEN', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
      AND grant_type = 'REFRESH_TOKEN'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type, created_at)
SELECT '17200000-0000-0000-0000-000000000003',
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'AUTHORIZATION_CODE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND grant_type = 'AUTHORIZATION_CODE'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type, created_at)
SELECT '17200000-0000-0000-0000-000000000004',
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'REFRESH_TOKEN', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND grant_type = 'REFRESH_TOKEN'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type, created_at)
SELECT '17200000-0000-0000-0000-000000000005',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'AUTHORIZATION_CODE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND grant_type = 'AUTHORIZATION_CODE'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type, created_at)
SELECT '17200000-0000-0000-0000-000000000006',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'REFRESH_TOKEN', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND grant_type = 'REFRESH_TOKEN'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000001',
       (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
       'openid', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
      AND scope = 'openid'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000002',
       (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
       'profile', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
      AND scope = 'profile'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000003',
       (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
       'email', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
      AND scope = 'email'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000004',
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'openid', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND scope = 'openid'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000005',
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'profile', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND scope = 'profile'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000006',
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'email', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND scope = 'email'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000007',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'openid', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND scope = 'openid'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000008',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'profile', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND scope = 'profile'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope, created_at)
SELECT '17300000-0000-0000-0000-000000000009',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'email', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND scope = 'email'
);

UPDATE app_roles
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'demo'),
    display_name = 'Admin',
    description = 'Demo UI administrators',
    updated_at = CURRENT_TIMESTAMP
WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
  AND code = 'admin';

INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT '18000000-0000-0000-0000-000000000001',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'admin', 'Admin', 'Demo UI administrators', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_roles
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND code = 'admin'
);

UPDATE app_roles
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'demo'),
    display_name = 'User',
    description = 'Demo UI users',
    updated_at = CURRENT_TIMESTAMP
WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
  AND code = 'user';

INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT '18000000-0000-0000-0000-000000000002',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'user', 'User', 'Demo UI users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_roles
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
      AND code = 'user'
);

UPDATE app_roles
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'acme'),
    display_name = 'Admin',
    description = 'Acme UI administrators',
    updated_at = CURRENT_TIMESTAMP
WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
  AND code = 'admin';

INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT '18000000-0000-0000-0000-000000000003',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'admin', 'Admin', 'Acme UI administrators', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_roles
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND code = 'admin'
);

UPDATE app_roles
SET tenant_id = (SELECT id FROM tenants WHERE slug = 'acme'),
    display_name = 'User',
    description = 'Acme UI users',
    updated_at = CURRENT_TIMESTAMP
WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
  AND code = 'user';

INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT '18000000-0000-0000-0000-000000000004',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'user', 'User', 'Acme UI users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_roles
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND code = 'user'
);

UPDATE app_memberships
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'demo-admin')
  AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui');

INSERT INTO app_memberships (id, tenant_id, tenant_user_id, client_app_id, status, created_at, updated_at)
SELECT '19000000-0000-0000-0000-000000000001',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM tenant_users WHERE local_username = 'demo-admin'),
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_memberships
    WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'demo-admin')
      AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
);

UPDATE app_memberships
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'demo-user')
  AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui');

INSERT INTO app_memberships (id, tenant_id, tenant_user_id, client_app_id, status, created_at, updated_at)
SELECT '19000000-0000-0000-0000-000000000002',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM tenant_users WHERE local_username = 'demo-user'),
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_memberships
    WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'demo-user')
      AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
);

UPDATE app_memberships
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-demo')
  AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui');

INSERT INTO app_memberships (id, tenant_id, tenant_user_id, client_app_id, status, created_at, updated_at)
SELECT '19000000-0000-0000-0000-000000000003',
       (SELECT id FROM tenants WHERE slug = 'demo'),
       (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-demo'),
       (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_memberships
    WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-demo')
      AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
);

UPDATE app_memberships
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-acme')
  AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui');

INSERT INTO app_memberships (id, tenant_id, tenant_user_id, client_app_id, status, created_at, updated_at)
SELECT '19000000-0000-0000-0000-000000000004',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-acme'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_memberships
    WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'tenant-admin-acme')
      AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
);

UPDATE app_memberships
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'acme-owner')
  AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui');

INSERT INTO app_memberships (id, tenant_id, tenant_user_id, client_app_id, status, created_at, updated_at)
SELECT '19000000-0000-0000-0000-000000000005',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       (SELECT id FROM tenant_users WHERE local_username = 'acme-owner'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_memberships
    WHERE tenant_user_id = (SELECT id FROM tenant_users WHERE local_username = 'acme-owner')
      AND client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
);

INSERT INTO app_membership_roles (membership_id, client_app_id, role_id, assigned_at)
SELECT am.id, am.client_app_id, ar.id, CURRENT_TIMESTAMP
FROM app_memberships am
JOIN tenant_users tu ON tu.id = am.tenant_user_id
JOIN app_roles ar ON ar.client_app_id = am.client_app_id
WHERE tu.local_username = 'demo-admin'
  AND am.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
  AND ar.code = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM app_membership_roles amr
      WHERE amr.membership_id = am.id
        AND amr.role_id = ar.id
  );

INSERT INTO app_membership_roles (membership_id, client_app_id, role_id, assigned_at)
SELECT am.id, am.client_app_id, ar.id, CURRENT_TIMESTAMP
FROM app_memberships am
JOIN tenant_users tu ON tu.id = am.tenant_user_id
JOIN app_roles ar ON ar.client_app_id = am.client_app_id
WHERE tu.local_username = 'demo-user'
  AND am.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
  AND ar.code = 'user'
  AND NOT EXISTS (
      SELECT 1 FROM app_membership_roles amr
      WHERE amr.membership_id = am.id
        AND amr.role_id = ar.id
  );

INSERT INTO app_membership_roles (membership_id, client_app_id, role_id, assigned_at)
SELECT am.id, am.client_app_id, ar.id, CURRENT_TIMESTAMP
FROM app_memberships am
JOIN tenant_users tu ON tu.id = am.tenant_user_id
JOIN app_roles ar ON ar.client_app_id = am.client_app_id
WHERE tu.local_username = 'tenant-admin-demo'
  AND am.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
  AND ar.code = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM app_membership_roles amr
      WHERE amr.membership_id = am.id
        AND amr.role_id = ar.id
  );

INSERT INTO app_membership_roles (membership_id, client_app_id, role_id, assigned_at)
SELECT am.id, am.client_app_id, ar.id, CURRENT_TIMESTAMP
FROM app_memberships am
JOIN tenant_users tu ON tu.id = am.tenant_user_id
JOIN app_roles ar ON ar.client_app_id = am.client_app_id
WHERE tu.local_username = 'tenant-admin-acme'
  AND am.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
  AND ar.code = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM app_membership_roles amr
      WHERE amr.membership_id = am.id
        AND amr.role_id = ar.id
  );

INSERT INTO app_membership_roles (membership_id, client_app_id, role_id, assigned_at)
SELECT am.id, am.client_app_id, ar.id, CURRENT_TIMESTAMP
FROM app_memberships am
JOIN tenant_users tu ON tu.id = am.tenant_user_id
JOIN app_roles ar ON ar.client_app_id = am.client_app_id
WHERE tu.local_username = 'acme-owner'
  AND am.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
  AND ar.code = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM app_membership_roles amr
      WHERE amr.membership_id = am.id
        AND amr.role_id = ar.id
  );

-- ---------------------------------------------------------------------------
-- Billing sample for acme-ui
-- ---------------------------------------------------------------------------
UPDATE app_plans
SET name = 'Free',
    description = 'Entry plan for evaluation',
    status = 'ACTIVE',
    is_public = TRUE,
    sort_order = 10,
    updated_at = CURRENT_TIMESTAMP
WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
  AND code = 'FREE';

INSERT INTO app_plans (
    id, client_app_id, code, name, description, status, is_public, sort_order, created_at, updated_at
)
SELECT '21000000-0000-0000-0000-000000000001',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'FREE', 'Free', 'Entry plan for evaluation', 'ACTIVE', TRUE, 10,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plans
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND code = 'FREE'
);

UPDATE app_plans
SET name = 'Personal',
    description = 'Single operator plan',
    status = 'ACTIVE',
    is_public = TRUE,
    sort_order = 20,
    updated_at = CURRENT_TIMESTAMP
WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
  AND code = 'PERSONAL';

INSERT INTO app_plans (
    id, client_app_id, code, name, description, status, is_public, sort_order, created_at, updated_at
)
SELECT '21000000-0000-0000-0000-000000000002',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'PERSONAL', 'Personal', 'Single operator plan', 'ACTIVE', TRUE, 20,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plans
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND code = 'PERSONAL'
);

UPDATE app_plans
SET name = 'Business',
    description = 'Operational business plan',
    status = 'ACTIVE',
    is_public = TRUE,
    sort_order = 40,
    updated_at = CURRENT_TIMESTAMP
WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
  AND code = 'BUSINESS';

INSERT INTO app_plans (
    id, client_app_id, code, name, description, status, is_public, sort_order, created_at, updated_at
)
SELECT '21000000-0000-0000-0000-000000000004',
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'BUSINESS', 'Business', 'Operational business plan', 'ACTIVE', TRUE, 40,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plans
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND code = 'BUSINESS'
);

UPDATE app_plan_versions
SET currency = 'USD',
    setup_fee = 0.00,
    trial_days = 0,
    effective_from = DATE '2026-01-01',
    effective_to = NULL,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'FREE')
  AND version = 'v1.0';

INSERT INTO app_plan_versions (
    id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, effective_to, status, created_at, updated_at
)
SELECT '22000000-0000-0000-0000-000000000001',
       (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'FREE'),
       'v1.0', 'USD', 0.00, 0, DATE '2026-01-01', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_versions
    WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'FREE')
      AND version = 'v1.0'
);

UPDATE app_plan_versions
SET currency = 'USD',
    setup_fee = 0.00,
    trial_days = 14,
    effective_from = DATE '2026-01-01',
    effective_to = NULL,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'PERSONAL')
  AND version = 'v1.0';

INSERT INTO app_plan_versions (
    id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, effective_to, status, created_at, updated_at
)
SELECT '22000000-0000-0000-0000-000000000002',
       (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'PERSONAL'),
       'v1.0', 'USD', 0.00, 14, DATE '2026-01-01', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_versions
    WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'PERSONAL')
      AND version = 'v1.0'
);

UPDATE app_plan_versions
SET currency = 'USD',
    setup_fee = 99.00,
    trial_days = 14,
    effective_from = DATE '2026-01-01',
    effective_to = NULL,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS')
  AND version = 'v1.0';

INSERT INTO app_plan_versions (
    id, app_plan_id, version, currency, setup_fee, trial_days, effective_from, effective_to, status, created_at, updated_at
)
SELECT '22000000-0000-0000-0000-000000000004',
       (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS'),
       'v1.0', 'USD', 99.00, 14, DATE '2026-01-01', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_versions
    WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS')
      AND version = 'v1.0'
);

INSERT INTO app_plan_billing_options (
    id, app_plan_version_id, billing_period, base_price, discount_pct, is_default, created_at, updated_at
)
SELECT '22100000-0000-0000-0000-000000000001',
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'FREE') AND version = 'v1.0'),
       'MONTHLY', 0.00, 0.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_billing_options
    WHERE app_plan_version_id = (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'FREE') AND version = 'v1.0')
      AND billing_period = 'MONTHLY'
);

INSERT INTO app_plan_billing_options (
    id, app_plan_version_id, billing_period, base_price, discount_pct, is_default, created_at, updated_at
)
SELECT '22100000-0000-0000-0000-000000000002',
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'PERSONAL') AND version = 'v1.0'),
       'MONTHLY', 19.00, 0.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_billing_options
    WHERE app_plan_version_id = (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'PERSONAL') AND version = 'v1.0')
      AND billing_period = 'MONTHLY'
);

INSERT INTO app_plan_billing_options (
    id, app_plan_version_id, billing_period, base_price, discount_pct, is_default, created_at, updated_at
)
SELECT '22100000-0000-0000-0000-000000000003',
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS') AND version = 'v1.0'),
       'MONTHLY', 99.00, 0.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_billing_options
    WHERE app_plan_version_id = (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS') AND version = 'v1.0')
      AND billing_period = 'MONTHLY'
);

INSERT INTO app_plan_entitlements (
    id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode,
    is_enabled, created_at, updated_at
)
SELECT '22200000-0000-0000-0000-000000000001',
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'FREE') AND version = 'v1.0'),
       'MAX_USERS', 'QUOTA', 3, 'MONTH', 'HARD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_entitlements
    WHERE app_plan_version_id = (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'FREE') AND version = 'v1.0')
      AND metric_code = 'MAX_USERS'
);

INSERT INTO app_plan_entitlements (
    id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode,
    is_enabled, created_at, updated_at
)
SELECT '22200000-0000-0000-0000-000000000002',
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'PERSONAL') AND version = 'v1.0'),
       'MAX_USERS', 'QUOTA', 5, 'MONTH', 'HARD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_entitlements
    WHERE app_plan_version_id = (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'PERSONAL') AND version = 'v1.0')
      AND metric_code = 'MAX_USERS'
);

INSERT INTO app_plan_entitlements (
    id, app_plan_version_id, metric_code, metric_type, limit_value, period_type, enforcement_mode,
    is_enabled, created_at, updated_at
)
SELECT '22200000-0000-0000-0000-000000000003',
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS') AND version = 'v1.0'),
       'MAX_USERS', 'QUOTA', 100, 'MONTH', 'HARD', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_plan_entitlements
    WHERE app_plan_version_id = (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS') AND version = 'v1.0')
      AND metric_code = 'MAX_USERS'
);

INSERT INTO tenant_billing_profiles (
    id, tenant_id, billing_type, display_name, legal_name, tax_id, billing_email, billing_address,
    is_default, created_at, updated_at
)
SELECT '23000000-0000-0000-0000-000000000001',
       (SELECT id FROM tenants WHERE slug = 'acme'),
       'COMPANY', 'Acme Billing', 'Acme Holdings SpA', 'CL-ACME-001', 'billing@acme.local',
       'Avenida Demo 123, Santiago, Chile', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_billing_profiles
    WHERE tenant_id = (SELECT id FROM tenants WHERE slug = 'acme')
      AND is_default = TRUE
);

INSERT INTO payment_methods (
    id, contractor_id, provider, method_type, external_reference, display_label, brand, last4,
    exp_month, exp_year, status, is_default, created_at, updated_at
)
SELECT '23000000-0000-0000-0000-000000000002',
       (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
       'MOCK', 'CARD', 'mock-pm-acme-default', 'Mock Visa ending 4242', 'VISA', '4242',
       12, 2030, 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM payment_methods
    WHERE provider = 'MOCK'
      AND external_reference = 'mock-pm-acme-default'
);

INSERT INTO app_contracts (
    id, contractor_id, client_app_id, app_plan_version_id, created_by_platform_user_id, billing_period,
    status, billing_contact_email, billing_contact_name, contractor_email_verified_at, payment_verified_at,
    expires_at, created_at, updated_at
)
SELECT '24000000-0000-0000-0000-000000000001',
       (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS') AND version = 'v1.0'),
       (SELECT id FROM platform_users WHERE email = 'contractor@keygo.local'),
       'MONTHLY', 'ACTIVE', 'billing@acme.local', 'Acme Billing Team',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TIMESTAMP '2030-12-31 23:59:59',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_contracts
    WHERE id = '24000000-0000-0000-0000-000000000001'
);

INSERT INTO app_subscriptions (
    id, contractor_id, client_app_id, app_plan_version_id, contract_id, status, current_period_start,
    current_period_end, cancel_at_period_end, cancelled_at, next_billing_at, auto_renew, created_at, updated_at
)
SELECT '24000000-0000-0000-0000-000000000002',
       (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       (SELECT id FROM app_plan_versions WHERE app_plan_id = (SELECT id FROM app_plans WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui') AND code = 'BUSINESS') AND version = 'v1.0'),
       '24000000-0000-0000-0000-000000000001',
       'ACTIVE',
       TIMESTAMP '2026-04-01 00:00:00',
       TIMESTAMP '2026-04-30 23:59:59',
       FALSE, NULL, TIMESTAMP '2026-05-01 00:00:00', TRUE,
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM app_subscriptions
    WHERE id = '24000000-0000-0000-0000-000000000002'
);

INSERT INTO invoices (
    id, contractor_id, client_app_id, subscription_id, invoice_number, status, issue_date, due_date,
    period_start, period_end, currency, subtotal, tax_amount, total, paid_at, billing_name_snapshot,
    billing_tax_id_snapshot, billing_address_snapshot, plan_name_snapshot, plan_version_snapshot,
    created_at, updated_at
)
SELECT '24000000-0000-0000-0000-000000000004',
       (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       '24000000-0000-0000-0000-000000000002',
       'INV-ACME-0001', 'PAID', DATE '2026-04-01', DATE '2026-04-15',
       DATE '2026-04-01', DATE '2026-04-30', 'USD', 99.00, 0.00, 99.00, CURRENT_TIMESTAMP,
       'Acme Holdings SpA', 'CL-ACME-001', 'Avenida Demo 123, Santiago, Chile',
       'Business', 'v1.0', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM invoices
    WHERE invoice_number = 'INV-ACME-0001'
);

INSERT INTO usage_counters (
    id, contractor_id, client_app_id, metric_code, period_start, period_end, used_value, created_at, updated_at
)
SELECT '24000000-0000-0000-0000-000000000005',
       (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'MAX_TENANTS', TIMESTAMP '2026-04-01 00:00:00', TIMESTAMP '2026-04-30 23:59:59',
       1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM usage_counters
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND contractor_id = (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local')
      AND metric_code = 'MAX_TENANTS'
      AND period_start = TIMESTAMP '2026-04-01 00:00:00'
      AND period_end = TIMESTAMP '2026-04-30 23:59:59'
);

INSERT INTO usage_counters (
    id, contractor_id, client_app_id, metric_code, period_start, period_end, used_value, created_at, updated_at
)
SELECT '24000000-0000-0000-0000-000000000006',
       (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local'),
       (SELECT id FROM client_apps WHERE client_id = 'acme-ui'),
       'MAX_USERS', TIMESTAMP '2026-04-01 00:00:00', TIMESTAMP '2026-04-30 23:59:59',
       2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM usage_counters
    WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'acme-ui')
      AND contractor_id = (SELECT id FROM contractors WHERE billing_email = 'billing@acme.local')
      AND metric_code = 'MAX_USERS'
      AND period_start = TIMESTAMP '2026-04-01 00:00:00'
      AND period_end = TIMESTAMP '2026-04-30 23:59:59'
);
