-- ============================================================
-- data-local.sql — Seed para perfil LOCAL (H2 file-based)
-- ============================================================
-- Compatible con H2 en MODE=PostgreSQL.
-- Idempotente: usa INSERT ... SELECT ... WHERE NOT EXISTS.
-- Corre en cada arranque; no duplica datos.
-- Flyway está DESHABILITADO en local — Hibernate (ddl-auto:update)
-- genera el esquema desde las entidades JPA.
--
-- UUIDs y datos IDÉNTICOS a los de las migraciones V14 + V15
-- para facilitar depuración cruzada entre perfiles.
--
-- Credenciales dev (¡NUNCA usar en producción!):
-- ─────────────────────────────────────────────────────────────
--  username             | email                    | password     | tenant
-- ─────────────────────────────────────────────────────────────
--  keygo_admin          | admin@keygo.local        | Admin1234!   | keygo
--  keygo_tenant_admin   | tenant-admin@keygo.local | Admin1234!   | keygo
--  keygo_user           | user@keygo.local         | Admin1234!   | keygo
--  demo_admin           | admin@demo.local         | DevAdmin1!   | demo
--  demo_user            | user@demo.local          | DevUser1!    | demo
-- ─────────────────────────────────────────────────────────────
--
-- UUIDs estables (mismos que V14):
--  Tenant keygo : 11111111-1111-1111-1111-111111111111
--  Tenant demo  : 22222222-2222-2222-2222-222222222222
--  App key-go-ui: 11111111-1111-1111-1111-222222222222
--  App demo-ui  : 22222222-2222-2222-2222-333333333333
-- ============================================================

-- ─── 1. Tenants ──────────────────────────────────────────────
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-111111111111',
    'keygo',
    'KeyGo',
    'owner@keygo.local',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'keygo');

INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-222222222222',
    'demo',
    'Demo',
    'owner@demo.local',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'demo');

-- ─── 2. Client Apps ──────────────────────────────────────────
-- keygo → key-go-ui (PUBLIC/PKCE, sin secret)
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'key-go-ui',
    'KeyGo UI',
    'Single UI app for platform and tenant administration',
    'PUBLIC',
    NULL,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'key-go-ui');

-- demo → demo-ui (PUBLIC/PKCE, sin secret)
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-333333333333',
    '22222222-2222-2222-2222-222222222222',
    'demo-ui',
    'Demo UI',
    'Demo tenant UI application',
    'PUBLIC',
    NULL,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'demo-ui');

-- ─── 3. Redirect URIs ────────────────────────────────────────
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT
    '11111111-1111-1111-1111-500000000001',
    '11111111-1111-1111-1111-222222222222',
    'http://localhost:5173/callback',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_redirect_uris
    WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
      AND uri = 'http://localhost:5173/callback'
);

INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT
    '22222222-2222-2222-2222-500000000001',
    '22222222-2222-2222-2222-333333333333',
    'http://localhost:5174/callback',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM client_redirect_uris
    WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
      AND uri = 'http://localhost:5174/callback'
);

-- ─── 4. Grants permitidos ────────────────────────────────────
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '11111111-1111-1111-1111-600000000001', '11111111-1111-1111-1111-222222222222', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
      AND grant_type = 'AUTHORIZATION_CODE'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '11111111-1111-1111-1111-600000000002', '11111111-1111-1111-1111-222222222222', 'REFRESH_TOKEN'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = '11111111-1111-1111-1111-222222222222'
      AND grant_type = 'REFRESH_TOKEN'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '22222222-2222-2222-2222-600000000001', '22222222-2222-2222-2222-333333333333', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
      AND grant_type = 'AUTHORIZATION_CODE'
);

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '22222222-2222-2222-2222-600000000002', '22222222-2222-2222-2222-333333333333', 'REFRESH_TOKEN'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = '22222222-2222-2222-2222-333333333333'
      AND grant_type = 'REFRESH_TOKEN'
);

-- ─── 5. Scopes permitidos ────────────────────────────────────
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '11111111-1111-1111-1111-700000000001', '11111111-1111-1111-1111-222222222222', 'openid'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = '11111111-1111-1111-1111-222222222222' AND scope = 'openid'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '11111111-1111-1111-1111-700000000002', '11111111-1111-1111-1111-222222222222', 'profile'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = '11111111-1111-1111-1111-222222222222' AND scope = 'profile'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '11111111-1111-1111-1111-700000000003', '11111111-1111-1111-1111-222222222222', 'email'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = '11111111-1111-1111-1111-222222222222' AND scope = 'email'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '22222222-2222-2222-2222-700000000001', '22222222-2222-2222-2222-333333333333', 'openid'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = '22222222-2222-2222-2222-333333333333' AND scope = 'openid'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '22222222-2222-2222-2222-700000000002', '22222222-2222-2222-2222-333333333333', 'profile'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = '22222222-2222-2222-2222-333333333333' AND scope = 'profile'
);

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '22222222-2222-2222-2222-700000000003', '22222222-2222-2222-2222-333333333333', 'email'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = '22222222-2222-2222-2222-333333333333' AND scope = 'email'
);

-- ─── 6. Tenant Users ─────────────────────────────────────────
-- Hashes BCrypt verificados (V15):
--   Admin1234! → $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
--   DevAdmin1! → $2a$10$VmQ.AQnJb11Ld9nqD9hCfurlSAO6wDIYPv12HXN/f2O6RXWANXr6G
--   DevUser1!  → $2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq

-- keygo_admin (Admin1234!)
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'keygo_admin',
    'admin@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    'KeyGo',
    'Admin',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '11111111-1111-1111-1111-000000000001');

-- keygo_tenant_admin (Admin1234!)
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-000000000002',
    '11111111-1111-1111-1111-111111111111',
    'keygo_tenant_admin',
    'tenant-admin@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    'Tenant',
    'Admin',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '11111111-1111-1111-1111-000000000002');

-- keygo_user (Admin1234!)
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-000000000003',
    '11111111-1111-1111-1111-111111111111',
    'keygo_user',
    'user@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    'KeyGo',
    'User',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '11111111-1111-1111-1111-000000000003');

-- demo_admin (DevAdmin1!)
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-000000000001',
    '22222222-2222-2222-2222-222222222222',
    'demo_admin',
    'admin@demo.local',
    '$2a$10$VmQ.AQnJb11Ld9nqD9hCfurlSAO6wDIYPv12HXN/f2O6RXWANXr6G',
    'Demo',
    'Admin',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '22222222-2222-2222-2222-000000000001');

-- demo_user (DevUser1!)
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-000000000002',
    '22222222-2222-2222-2222-222222222222',
    'demo_user',
    'user@demo.local',
    '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
    'Demo',
    'User',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '22222222-2222-2222-2222-000000000002');

-- ─── 7. App Roles ────────────────────────────────────────────
-- key-go-ui roles: admin, admin_tenant, user_tenant
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-300000000001',
    '11111111-1111-1111-1111-222222222222',
    'admin',
    'Platform Admin',
    'Global KeyGo administrator role',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = '11111111-1111-1111-1111-300000000001');

INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-300000000002',
    '11111111-1111-1111-1111-222222222222',
    'admin_tenant',
    'Tenant Admin',
    'Tenant-scoped administrator role',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = '11111111-1111-1111-1111-300000000002');

INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-300000000003',
    '11111111-1111-1111-1111-222222222222',
    'user_tenant',
    'Tenant User',
    'Standard tenant user role',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = '11111111-1111-1111-1111-300000000003');

-- demo-ui roles: demo_admin, demo_user
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-300000000001',
    '22222222-2222-2222-2222-333333333333',
    'demo_admin',
    'Demo Admin',
    'Administrator role for demo app',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = '22222222-2222-2222-2222-300000000001');

INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-300000000002',
    '22222222-2222-2222-2222-333333333333',
    'demo_user',
    'Demo User',
    'Standard user role for demo app',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = '22222222-2222-2222-2222-300000000002');

-- ─── 8. Memberships ──────────────────────────────────────────
-- keygo_admin → key-go-ui
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-400000000001',
    '11111111-1111-1111-1111-000000000001',
    '11111111-1111-1111-1111-222222222222',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = '11111111-1111-1111-1111-400000000001');

-- keygo_tenant_admin → key-go-ui
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-400000000002',
    '11111111-1111-1111-1111-000000000002',
    '11111111-1111-1111-1111-222222222222',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = '11111111-1111-1111-1111-400000000002');

-- keygo_user → key-go-ui
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT
    '11111111-1111-1111-1111-400000000003',
    '11111111-1111-1111-1111-000000000003',
    '11111111-1111-1111-1111-222222222222',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = '11111111-1111-1111-1111-400000000003');

-- demo_admin → demo-ui
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-400000000001',
    '22222222-2222-2222-2222-000000000001',
    '22222222-2222-2222-2222-333333333333',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = '22222222-2222-2222-2222-400000000001');

-- demo_user → demo-ui
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT
    '22222222-2222-2222-2222-400000000002',
    '22222222-2222-2222-2222-000000000002',
    '22222222-2222-2222-2222-333333333333',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = '22222222-2222-2222-2222-400000000002');

-- ─── 9. Membership Roles ─────────────────────────────────────
-- Nota: en H2 la tabla membership_roles solo tiene (membership_id, role_id)
-- sin columna assigned_at (esa columna existe únicamente en PostgreSQL vía V14).

-- keygo_admin → admin
INSERT INTO membership_roles (membership_id, role_id)
SELECT '11111111-1111-1111-1111-400000000001', '11111111-1111-1111-1111-300000000001'
WHERE NOT EXISTS (
    SELECT 1 FROM membership_roles
    WHERE membership_id = '11111111-1111-1111-1111-400000000001'
      AND role_id = '11111111-1111-1111-1111-300000000001'
);

-- keygo_tenant_admin → admin_tenant
INSERT INTO membership_roles (membership_id, role_id)
SELECT '11111111-1111-1111-1111-400000000002', '11111111-1111-1111-1111-300000000002'
WHERE NOT EXISTS (
    SELECT 1 FROM membership_roles
    WHERE membership_id = '11111111-1111-1111-1111-400000000002'
      AND role_id = '11111111-1111-1111-1111-300000000002'
);

-- keygo_user → user_tenant
INSERT INTO membership_roles (membership_id, role_id)
SELECT '11111111-1111-1111-1111-400000000003', '11111111-1111-1111-1111-300000000003'
WHERE NOT EXISTS (
    SELECT 1 FROM membership_roles
    WHERE membership_id = '11111111-1111-1111-1111-400000000003'
      AND role_id = '11111111-1111-1111-1111-300000000003'
);

-- demo_admin → demo_admin role
INSERT INTO membership_roles (membership_id, role_id)
SELECT '22222222-2222-2222-2222-400000000001', '22222222-2222-2222-2222-300000000001'
WHERE NOT EXISTS (
    SELECT 1 FROM membership_roles
    WHERE membership_id = '22222222-2222-2222-2222-400000000001'
      AND role_id = '22222222-2222-2222-2222-300000000001'
);

-- demo_user → demo_user role
INSERT INTO membership_roles (membership_id, role_id)
SELECT '22222222-2222-2222-2222-400000000002', '22222222-2222-2222-2222-300000000002'
WHERE NOT EXISTS (
    SELECT 1 FROM membership_roles
    WHERE membership_id = '22222222-2222-2222-2222-400000000002'
      AND role_id = '22222222-2222-2222-2222-300000000002'
);
