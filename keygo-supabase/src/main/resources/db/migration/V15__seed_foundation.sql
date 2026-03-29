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
-- Convención de FK: nunca se hardcodean UUIDs de FK; se resuelven con
-- subqueries SELECT sobre el campo semántico del padre (slug, client_id, username).
-- Los únicos UUIDs fijos son los PKs (columna id) para estabilidad entre resets.
--
-- PKs estables (id):
--   Tenants:     keygo=11111111-1111-1111-1111-111111111111  demo=22222222-2222-2222-2222-222222222222
--   ClientApps:  keygo-ui=11111111-1111-1111-1111-222222222222  demo-ui=22222222-2222-2222-2222-333333333333
--   TenantUsers: keygo_admin=...-000000000001  keygo_tenant_admin=...-000000000002  keygo_user=...-000000000003
--                demo_admin=22222222-...-000000000001  demo_user=22222222-...-000000000002
--   AppRoles:    keygo-ui: admin=...-300000000001  admin_tenant=...-300000000002  user_tenant=...-300000000003
--                demo-ui:  demo_admin=22222222-...-300000000001  demo_user=22222222-...-300000000002
--   Memberships: keygo_admin→keygo-ui=...-400000000001  (...)  demo_user→demo-ui=22222222-...-400000000002
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
--    FK tenant_id → tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status)
VALUES
  (
    '11111111-1111-1111-1111-222222222222',
    (SELECT id FROM tenants WHERE slug = 'keygo'),
    'keygo-ui',
    'KeyGo UI',
    'Single UI app for platform and tenant administration',
    'PUBLIC', NULL, 'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-333333333333',
    (SELECT id FROM tenants WHERE slug = 'demo'),
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

-- ── Redirect URIs — FK client_app_id → client_apps.client_id ──────────────
INSERT INTO client_redirect_uris (id, client_app_id, uri)
SELECT gen_random_uuid(), (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'), 'http://localhost:5173/callback'
WHERE NOT EXISTS (
  SELECT 1 FROM client_redirect_uris
  WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
    AND uri = 'http://localhost:5173/callback'
);
INSERT INTO client_redirect_uris (id, client_app_id, uri)
SELECT gen_random_uuid(), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), 'http://localhost:5174/callback'
WHERE NOT EXISTS (
  SELECT 1 FROM client_redirect_uris
  WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui')
    AND uri = 'http://localhost:5174/callback'
);

-- ── Allowed grants — FK client_app_id → client_apps.client_id ─────────────
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'), g
FROM (VALUES ('AUTHORIZATION_CODE'), ('REFRESH_TOKEN')) AS t(g)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui') AND grant_type = g
);
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), g
FROM (VALUES ('AUTHORIZATION_CODE'), ('REFRESH_TOKEN')) AS t(g)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_grants
  WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui') AND grant_type = g
);

-- ── Allowed scopes — FK client_app_id → client_apps.client_id ─────────────
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'), s
FROM (VALUES ('openid'), ('profile'), ('email')) AS t(s)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui') AND scope = s
);
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT gen_random_uuid(), (SELECT id FROM client_apps WHERE client_id = 'demo-ui'), s
FROM (VALUES ('openid'), ('profile'), ('email')) AS t(s)
WHERE NOT EXISTS (
  SELECT 1 FROM client_allowed_scopes
  WHERE client_app_id = (SELECT id FROM client_apps WHERE client_id = 'demo-ui') AND scope = s
);

-- ---------------------------------------------------------------------------
-- 3) Tenant Users
--    FK tenant_id → tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status)
VALUES
  (
    '11111111-1111-1111-1111-000000000001',
    (SELECT id FROM tenants WHERE slug = 'keygo'),
    'keygo_admin', 'admin@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'KeyGo', 'Admin', 'ACTIVE'
  ),
  (
    '11111111-1111-1111-1111-000000000002',
    (SELECT id FROM tenants WHERE slug = 'keygo'),
    'keygo_tenant_admin', 'tenant-admin@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'Tenant', 'Admin', 'ACTIVE'
  ),
  (
    '11111111-1111-1111-1111-000000000003',
    (SELECT id FROM tenants WHERE slug = 'keygo'),
    'keygo_user', 'user@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'KeyGo', 'User', 'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-000000000001',
    (SELECT id FROM tenants WHERE slug = 'demo'),
    'demo_admin', 'admin@demo.local',
    '$2a$10$VmQ.AQnJb11Ld9nqD9hCfurlSAO6wDIYPv12HXN/f2O6RXWANXr6G',  -- DevAdmin1!
    'Demo', 'Admin', 'ACTIVE'
  ),
  (
    '22222222-2222-2222-2222-000000000002',
    (SELECT id FROM tenants WHERE slug = 'demo'),
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
--    FK client_app_id → client_apps.client_id (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO app_roles (id, client_app_id, code, display_name, description)
VALUES
  (
    '11111111-1111-1111-1111-300000000001',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'admin', 'Platform Admin', 'Global KeyGo administrator role'
  ),
  (
    '11111111-1111-1111-1111-300000000002',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'admin_tenant', 'Tenant Admin', 'Tenant-scoped administrator role'
  ),
  (
    '11111111-1111-1111-1111-300000000003',
    (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
    'user_tenant', 'Tenant User', 'Standard tenant user role'
  ),
  (
    '22222222-2222-2222-2222-300000000001',
    (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
    'demo_admin', 'Demo Admin', 'Administrator role for demo app'
  ),
  (
    '22222222-2222-2222-2222-300000000002',
    (SELECT id FROM client_apps WHERE client_id = 'demo-ui'),
    'demo_user', 'Demo User', 'Standard user role for demo app'
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
  display_name = EXCLUDED.display_name,
  description  = EXCLUDED.description,
  updated_at   = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 5) Memberships (acceso de usuario a app)
--    FKs: user_id → tenant_users.username + tenants.slug
--         client_app_id → client_apps.client_id
--    Se usa CTE con datos semánticos para resolver los UUIDs vía JOIN.
-- ---------------------------------------------------------------------------
WITH
  assignments (stable_id, tenant_slug, username, app_client_id) AS (
    VALUES
      ('11111111-1111-1111-1111-400000000001'::UUID, 'keygo', 'keygo_admin',        'keygo-ui'),
      ('11111111-1111-1111-1111-400000000002'::UUID, 'keygo', 'keygo_tenant_admin', 'keygo-ui'),
      ('11111111-1111-1111-1111-400000000003'::UUID, 'keygo', 'keygo_user',         'keygo-ui'),
      ('22222222-2222-2222-2222-400000000001'::UUID, 'demo',  'demo_admin',         'demo-ui'),
      ('22222222-2222-2222-2222-400000000002'::UUID, 'demo',  'demo_user',          'demo-ui')
  )
INSERT INTO memberships (id, user_id, client_app_id, status)
SELECT a.stable_id, tu.id, ca.id, 'ACTIVE'
FROM assignments      a
JOIN tenants      t  ON t.slug        = a.tenant_slug
JOIN tenant_users tu ON tu.tenant_id  = t.id  AND tu.username  = a.username
JOIN client_apps  ca ON ca.client_id  = a.app_client_id
ON CONFLICT (user_id, client_app_id) DO UPDATE
SET
  status     = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 6) Membership Role Assignments
--    FKs: membership_id → resuelto por (tenant_slug, username, app_client_id)
--         role_id        → app_roles.code + client_apps.client_id
-- ---------------------------------------------------------------------------
INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT DISTINCT m.id, ar.id, NOW()
FROM (VALUES
  ('keygo', 'keygo_admin',        'keygo-ui', 'admin'),
  ('keygo', 'keygo_tenant_admin', 'keygo-ui', 'admin_tenant'),
  ('keygo', 'keygo_user',         'keygo-ui', 'user_tenant'),
  ('demo',  'demo_admin',         'demo-ui',  'demo_admin'),
  ('demo',  'demo_user',          'demo-ui',  'demo_user')
) AS ra(tenant_slug, username, app_client_id, role_code)
JOIN tenants      t  ON t.slug        = ra.tenant_slug
JOIN tenant_users tu ON tu.tenant_id  = t.id  AND tu.username   = ra.username
JOIN client_apps  ca ON ca.client_id  = ra.app_client_id
JOIN memberships  m  ON m.user_id     = tu.id AND m.client_app_id = ca.id
JOIN app_roles    ar ON ar.client_app_id = ca.id AND ar.code      = ra.role_code
WHERE NOT EXISTS (
  SELECT 1 FROM membership_roles mr
  WHERE mr.membership_id = m.id AND mr.role_id = ar.id
);
