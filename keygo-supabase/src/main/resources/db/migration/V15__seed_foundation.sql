-- =============================================================================
-- V15: Seed Foundation — datos base para desarrollo UI (keygo + demo)
--
-- Consolida el seed inicial de tenants + usuarios + apps + roles + memberships
-- con contraseñas correctas desde el inicio (sin migración de reset separada).
--
-- Credenciales seed (SOLO dev/local — NUNCA usar en producción):
-- ───────────────────────────────────────────────────────────────
--  username              email                     contraseña     tenant   rol
-- ───────────────────────────────────────────────────────────────
--  keygo_admin           admin@keygo.local         Admin1234!     keygo    admin
--  keygo_tenant_admin    tenant-admin@keygo.local  Admin1234!     keygo    admin_tenant
--  keygo_user            user@keygo.local          Admin1234!     keygo    user_tenant
--  demo_admin            admin@demo.local          DevAdmin1!     demo     demo_admin
--  demo_user             user@demo.local           DevUser1!      demo     demo_user
-- ───────────────────────────────────────────────────────────────
--
-- Hashes BCrypt (cost=10, generados con Spring BCryptPasswordEncoder):
--   Admin1234! → $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
--   DevAdmin1! → $2a$10$VmQ.AQnJb11Ld9nqD9hCfurlSAO6wDIYPv12HXN/f2O6RXWANXr6G
--   DevUser1!  → $2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq
--
-- UUIDs estables (deterministas entre ambientes):
-- ───────────────────────────────────────────────
-- Tenants
--   keygo  : 11111111-1111-1111-1111-111111111111
--   demo   : 22222222-2222-2222-2222-222222222222
-- Apps
--   key-go-ui : 11111111-1111-1111-1111-222222222222
--   demo-ui   : 22222222-2222-2222-2222-333333333333
-- TenantUsers (keygo)
--   keygo_admin        : 11111111-1111-1111-1111-000000000001
--   keygo_tenant_admin : 11111111-1111-1111-1111-000000000002
--   keygo_user         : 11111111-1111-1111-1111-000000000003
-- TenantUsers (demo)
--   demo_admin : 22222222-2222-2222-2222-000000000001
--   demo_user  : 22222222-2222-2222-2222-000000000002
-- AppRoles (key-go-ui)
--   admin        : 11111111-1111-1111-1111-300000000001
--   admin_tenant : 11111111-1111-1111-1111-300000000002
--   user_tenant  : 11111111-1111-1111-1111-300000000003
-- AppRoles (demo-ui)
--   demo_admin : 22222222-2222-2222-2222-300000000001
--   demo_user  : 22222222-2222-2222-2222-300000000002
-- Memberships
--   keygo_admin → key-go-ui        : 11111111-1111-1111-1111-400000000001
--   keygo_tenant_admin → key-go-ui : 11111111-1111-1111-1111-400000000002
--   keygo_user → key-go-ui         : 11111111-1111-1111-1111-400000000003
--   demo_admin → demo-ui           : 22222222-2222-2222-2222-400000000001
--   demo_user → demo-ui            : 22222222-2222-2222-2222-400000000002
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1) Tenants
-- ---------------------------------------------------------------------------
INSERT INTO tenants (id, slug, name, owner_email, status)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'keygo', 'KeyGo', 'owner@keygo.local', 'ACTIVE'),
  ('22222222-2222-2222-2222-222222222222', 'demo',  'Demo',  'owner@demo.local',  'ACTIVE')
ON CONFLICT (slug) DO UPDATE
SET
  name        = EXCLUDED.name,
  owner_email = EXCLUDED.owner_email,
  status      = EXCLUDED.status,
  updated_at  = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 2) Client Apps
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status)
VALUES
  (
    '11111111-1111-1111-1111-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'keygo-ui',
    'KeyGo UI',
    'Single UI app for platform and tenant administration',
    'PUBLIC', NULL, 'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-333333333333',
    '22222222-2222-2222-2222-222222222222',
    'demo-ui',
    'Demo UI',
    'Demo tenant UI application',
    'PUBLIC', NULL, 'ACTIVE'
  )
ON CONFLICT (client_id) DO UPDATE
SET
  tenant_id     = EXCLUDED.tenant_id,
  name          = EXCLUDED.name,
  description   = EXCLUDED.description,
  type          = EXCLUDED.type,
  hashed_secret = EXCLUDED.hashed_secret,
  status        = EXCLUDED.status,
  updated_at    = CURRENT_TIMESTAMP;

-- ── Redirect URIs ──────────────────────────────────────────────────────────
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

-- ── Allowed grants ─────────────────────────────────────────────────────────
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', g
FROM (VALUES ('AUTHORIZATION_CODE'), ('REFRESH_TOKEN')) AS t(g)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222' AND grant_type = g
);
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', g
FROM (VALUES ('AUTHORIZATION_CODE'), ('REFRESH_TOKEN')) AS t(g)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333' AND grant_type = g
);

-- ── Allowed scopes ─────────────────────────────────────────────────────────
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-222222222222', s
FROM (VALUES ('openid'), ('profile'), ('email')) AS t(s)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '11111111-1111-1111-1111-222222222222' AND scope = s
);
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), '22222222-2222-2222-2222-333333333333', s
FROM (VALUES ('openid'), ('profile'), ('email')) AS t(s)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = '22222222-2222-2222-2222-333333333333' AND scope = s
);

-- ---------------------------------------------------------------------------
-- 3) Tenant Users — contraseñas correctas desde el inicio
-- ---------------------------------------------------------------------------
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status)
VALUES
  (
    '11111111-1111-1111-1111-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'keygo_admin', 'admin@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'KeyGo', 'Admin', 'ACTIVE'
  ),
  (
    '11111111-1111-1111-1111-000000000002',
    '11111111-1111-1111-1111-111111111111',
    'keygo_tenant_admin', 'tenant-admin@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'Tenant', 'Admin', 'ACTIVE'
  ),
  (
    '11111111-1111-1111-1111-000000000003',
    '11111111-1111-1111-1111-111111111111',
    'keygo_user', 'user@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'KeyGo', 'User', 'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-000000000001',
    '22222222-2222-2222-2222-222222222222',
    'demo_admin', 'admin@demo.local',
    '$2a$10$VmQ.AQnJb11Ld9nqD9hCfurlSAO6wDIYPv12HXN/f2O6RXWANXr6G',  -- DevAdmin1!
    'Demo', 'Admin', 'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-000000000002',
    '22222222-2222-2222-2222-222222222222',
    'demo_user', 'user@demo.local',
    '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',  -- DevUser1!
    'Demo', 'User', 'ACTIVE'
  )
ON CONFLICT (tenant_id, username) DO UPDATE
SET
  email         = EXCLUDED.email,
  password_hash = EXCLUDED.password_hash,
  first_name    = EXCLUDED.first_name,
  last_name     = EXCLUDED.last_name,
  status        = EXCLUDED.status,
  updated_at    = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 4) App Roles
-- ---------------------------------------------------------------------------
INSERT INTO app_roles (id, client_app_id, code, display_name, description)
VALUES
  (
    '11111111-1111-1111-1111-300000000001',
    '11111111-1111-1111-1111-222222222222',
    'admin', 'Platform Admin', 'Global KeyGo administrator role'
  ),
  (
    '11111111-1111-1111-1111-300000000002',
    '11111111-1111-1111-1111-222222222222',
    'admin_tenant', 'Tenant Admin', 'Tenant-scoped administrator role'
  ),
  (
    '11111111-1111-1111-1111-300000000003',
    '11111111-1111-1111-1111-222222222222',
    'user_tenant', 'Tenant User', 'Standard tenant user role'
  ),
  (
    '22222222-2222-2222-2222-300000000001',
    '22222222-2222-2222-2222-333333333333',
    'demo_admin', 'Demo Admin', 'Administrator role for demo app'
  ),
  (
    '22222222-2222-2222-2222-300000000002',
    '22222222-2222-2222-2222-333333333333',
    'demo_user', 'Demo User', 'Standard user role for demo app'
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
  display_name = EXCLUDED.display_name,
  description  = EXCLUDED.description,
  updated_at   = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 5) Memberships (acceso de usuario a app)
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
  status     = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 6) Membership Role Assignments
-- ---------------------------------------------------------------------------
INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT m, r, NOW() FROM (VALUES
  ('11111111-1111-1111-1111-400000000001'::UUID, '11111111-1111-1111-1111-300000000001'::UUID),
  ('11111111-1111-1111-1111-400000000002'::UUID, '11111111-1111-1111-1111-300000000002'::UUID),
  ('11111111-1111-1111-1111-400000000003'::UUID, '11111111-1111-1111-1111-300000000003'::UUID),
  ('22222222-2222-2222-2222-400000000001'::UUID, '22222222-2222-2222-2222-300000000001'::UUID),
  ('22222222-2222-2222-2222-400000000002'::UUID, '22222222-2222-2222-2222-300000000002'::UUID)
) AS t(m, r)
WHERE NOT EXISTS (
  SELECT 1 FROM membership_roles
  WHERE membership_id = m AND role_id = r
);

