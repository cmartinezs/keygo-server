-- V14: Seed base data for UI development (keygo + demo tenants)
--
-- Scope:
--   - Tenant keygo + app key-go-ui
--   - Tenant demo + app demo-ui
--   - Tenant users (admin/global-like, admin_tenant, user_tenant) for keygo
--   - Tenant users (app admin + app user) for demo
--   - App roles + memberships + membership_roles
--
-- Important:
--   Legacy tables (users, user_roles) are intentionally NOT seeded here because
--   they are planned for removal. This migration only uses tenant-scoped model.
--
-- Idempotency strategy:
--   - INSERT ... ON CONFLICT for natural unique keys
--   - join-table inserts guarded with WHERE NOT EXISTS

-- ---------------------------------------------------------------------------
-- Constants (stable UUIDs for deterministic references across environments)
-- ---------------------------------------------------------------------------
-- Tenants
-- keygo: 11111111-1111-1111-1111-111111111111
-- demo : 22222222-2222-2222-2222-222222222222
--
-- Apps
-- key-go-ui: 11111111-1111-1111-1111-222222222222
-- demo-ui  : 22222222-2222-2222-2222-333333333333

-- ---------------------------------------------------------------------------
-- 1) Tenants
-- ---------------------------------------------------------------------------
INSERT INTO tenants (id, slug, name, owner_email, status)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'keygo', 'KeyGo', 'owner@keygo.local', 'ACTIVE'),
  ('22222222-2222-2222-2222-222222222222', 'demo',  'Demo',  'owner@demo.local',  'ACTIVE')
ON CONFLICT (slug) DO UPDATE
SET
  name = EXCLUDED.name,
  owner_email = EXCLUDED.owner_email,
  status = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 2) Client apps
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status)
VALUES
  (
    '11111111-1111-1111-1111-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'key-go-ui',
    'KeyGo UI',
    'Single UI app for platform and tenant administration',
    'PUBLIC',
    NULL,
    'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-333333333333',
    '22222222-2222-2222-2222-222222222222',
    'demo-ui',
    'Demo UI',
    'Demo tenant UI application',
    'PUBLIC',
    NULL,
    'ACTIVE'
  )
ON CONFLICT (client_id) DO UPDATE
SET
  tenant_id = EXCLUDED.tenant_id,
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  type = EXCLUDED.type,
  hashed_secret = EXCLUDED.hashed_secret,
  status = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- Redirect URIs
INSERT INTO client_redirect_uris (id, client_app_id, uri)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', 'http://localhost:5173/callback'
WHERE NOT EXISTS (
  SELECT 1 FROM client_redirect_uris
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
    AND uri = 'http://localhost:5173/callback'
);

INSERT INTO client_redirect_uris (id, client_app_id, uri)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', 'http://localhost:5174/callback'
WHERE NOT EXISTS (
  SELECT 1 FROM client_redirect_uris
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
    AND uri = 'http://localhost:5174/callback'
);

-- Allowed grants
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
    AND grant_type = 'AUTHORIZATION_CODE'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', 'REFRESH_TOKEN'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
    AND grant_type = 'REFRESH_TOKEN'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
    AND grant_type = 'AUTHORIZATION_CODE'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', 'REFRESH_TOKEN'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
    AND grant_type = 'REFRESH_TOKEN'
);

-- Allowed scopes
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', 'openid'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
    AND scope = 'openid'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', 'profile'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
    AND scope = 'profile'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', 'email'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
    AND scope = 'email'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', 'openid'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
    AND scope = 'openid'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', 'profile'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
    AND scope = 'profile'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', 'email'
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
    AND scope = 'email'
);

-- ---------------------------------------------------------------------------
-- 3) Tenant users (password hash uses same dev hash seeded in V2)
-- ---------------------------------------------------------------------------
-- bcrypt hash for local/dev seed users (same as V2 admin)
-- value intentionally reused for bootstrap environments only.
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status)
VALUES
  (
    '11111111-1111-1111-1111-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'keygo_admin',
    'admin@keygo.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'KeyGo',
    'Admin',
    'ACTIVE'
  ),
  (
    '11111111-1111-1111-1111-000000000002',
    '11111111-1111-1111-1111-111111111111',
    'keygo_tenant_admin',
    'tenant-admin@keygo.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Tenant',
    'Admin',
    'ACTIVE'
  ),
  (
    '11111111-1111-1111-1111-000000000003',
    '11111111-1111-1111-1111-111111111111',
    'keygo_user',
    'user@keygo.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'KeyGo',
    'User',
    'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-000000000001',
    '22222222-2222-2222-2222-222222222222',
    'demo_admin',
    'admin@demo.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Demo',
    'Admin',
    'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-000000000002',
    '22222222-2222-2222-2222-222222222222',
    'demo_user',
    'user@demo.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Demo',
    'User',
    'ACTIVE'
  )
ON CONFLICT (tenant_id, username) DO UPDATE
SET
  email = EXCLUDED.email,
  password_hash = EXCLUDED.password_hash,
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  status = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- Ensure idempotency also when a row already exists by email but not username key path.
UPDATE tenant_users
SET
  username = 'keygo_admin',
  first_name = 'KeyGo',
  last_name = 'Admin',
  status = 'ACTIVE',
  updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = '11111111-1111-1111-1111-111111111111'
  AND email = 'admin@keygo.local';

-- ---------------------------------------------------------------------------
-- 4) App roles
-- ---------------------------------------------------------------------------
-- key-go-ui roles (fixed set): admin, admin_tenant, user_tenant
INSERT INTO app_roles (id, client_app_id, code, display_name, description)
VALUES
  (
    '11111111-1111-1111-1111-300000000001',
    '11111111-1111-1111-1111-222222222222',
    'admin',
    'Platform Admin',
    'Global KeyGo administrator role'
  ),
  (
    '11111111-1111-1111-1111-300000000002',
    '11111111-1111-1111-1111-222222222222',
    'admin_tenant',
    'Tenant Admin',
    'Tenant-scoped administrator role'
  ),
  (
    '11111111-1111-1111-1111-300000000003',
    '11111111-1111-1111-1111-222222222222',
    'user_tenant',
    'Tenant User',
    'Standard tenant user role'
  ),
  (
    '22222222-2222-2222-2222-300000000001',
    '22222222-2222-2222-2222-333333333333',
    'demo_admin',
    'Demo Admin',
    'Administrator role for demo app'
  ),
  (
    '22222222-2222-2222-2222-300000000002',
    '22222222-2222-2222-2222-333333333333',
    'demo_user',
    'Demo User',
    'Standard user role for demo app'
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
  display_name = EXCLUDED.display_name,
  description = EXCLUDED.description,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 5) Memberships (user access to app)
-- ---------------------------------------------------------------------------
INSERT INTO memberships (id, user_id, client_app_id, status)
VALUES
  ('11111111-1111-1111-1111-400000000001', '11111111-1111-1111-1111-000000000001', '11111111-1111-1111-1111-222222222222', 'ACTIVE'),
  ('11111111-1111-1111-1111-400000000002', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-222222222222', 'ACTIVE'),
  ('11111111-1111-1111-1111-400000000003', '11111111-1111-1111-1111-000000000003', '11111111-1111-1111-1111-222222222222', 'ACTIVE'),
  ('22222222-2222-2222-2222-400000000001', '22222222-2222-2222-2222-000000000001', '22222222-2222-2222-2222-333333333333', 'ACTIVE'),
  ('22222222-2222-2222-2222-400000000002', '22222222-2222-2222-2222-000000000002', '22222222-2222-2222-2222-333333333333', 'ACTIVE')
ON CONFLICT (user_id, client_app_id) DO UPDATE
SET
  status = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 6) Membership role assignments
-- ---------------------------------------------------------------------------
INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT '11111111-1111-1111-1111-400000000001', '11111111-1111-1111-1111-300000000001', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM membership_roles
  WHERE membership_id = '11111111-1111-1111-1111-400000000001'
    AND role_id = '11111111-1111-1111-1111-300000000001'
);

INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT '11111111-1111-1111-1111-400000000002', '11111111-1111-1111-1111-300000000002', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM membership_roles
  WHERE membership_id = '11111111-1111-1111-1111-400000000002'
    AND role_id = '11111111-1111-1111-1111-300000000002'
);

INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT '11111111-1111-1111-1111-400000000003', '11111111-1111-1111-1111-300000000003', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM membership_roles
  WHERE membership_id = '11111111-1111-1111-1111-400000000003'
    AND role_id = '11111111-1111-1111-1111-300000000003'
);

INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT '22222222-2222-2222-2222-400000000001', '22222222-2222-2222-2222-300000000001', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM membership_roles
  WHERE membership_id = '22222222-2222-2222-2222-400000000001'
    AND role_id = '22222222-2222-2222-2222-300000000001'
);

INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT '22222222-2222-2222-2222-400000000002', '22222222-2222-2222-2222-300000000002', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM membership_roles
  WHERE membership_id = '22222222-2222-2222-2222-400000000002'
    AND role_id = '22222222-2222-2222-2222-300000000002'
);

