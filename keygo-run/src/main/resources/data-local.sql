    WHERE membership_id = 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
      AND role_id = 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
-- ─── 9. Rol asignado a la membresía ─────────────────────────
INSERT INTO membership_roles (membership_id, role_id)
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');
    'Administrador',
    'Rol de administrador local',
-- ============================================================
-- data-local.sql — Seed mínimo para perfil LOCAL (H2 file-based)
-- Compatible con H2 en MODE=PostgreSQL.
-- Idempotente: usa INSERT ... SELECT ... WHERE NOT EXISTS.
-- Corre en cada arranque; no duplica datos.
-- Compatible con H2 (perfil local) y PostgreSQL.
-- Corre en cada arranque; no duplica datos si el archivo ya existe.
-- ─────────────────────────────────────────────────────────────
--  username             | email                    | password     | tenant
-- ─────────────────────────────────────────────────────────────
--   Tenant:  keygo
--   App:     keygo-ui  (client_id)  — PUBLIC/PKCE, sin secret
--   Usuario: keygo_admin / admin@keygo.local / Admin1234!
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
-- ─── 1. Tenant ───────────────────────────────────────────────
    'key-go-ui',
    'KeyGo UI',
    'Single UI app for platform and tenant administration',
    'PUBLIC',
    NULL,
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    CURRENT_TIMESTAMP,
    'admin@keygo.local',
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'key-go-ui');

-- demo → demo-ui (PUBLIC/PKCE, sin secret)
-- ─── 2. Client App (PUBLIC → PKCE, sin hashed_secret) ────────

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

    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'keygo-ui',
    '22222222-2222-2222-2222-333333333333',
    'Frontend local de desarrollo',
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
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'keygo-ui');
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
-- ─── 3. Redirect URI ─────────────────────────────────────────
      AND grant_type = 'AUTHORIZATION_CODE'
);

    'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');
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
SELECT 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');
    SELECT 1 FROM client_allowed_scopes
    WHERE client_app_id = '22222222-2222-2222-2222-333333333333' AND scope = 'profile'
);

SELECT 'f3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = 'f3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');
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
SELECT 'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = 'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');

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
    'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '11111111-1111-1111-1111-300000000001',
SELECT 'f6eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = 'f6eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');
SELECT
    '11111111-1111-1111-1111-300000000002',
    '11111111-1111-1111-1111-222222222222',
    'admin_tenant',
SELECT 'f7eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = 'f7eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');

-- ─── 8. Membresía: keygo_admin → keygo-ui ───────────────────
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
-- ─── 6. Usuario admin ────────────────────────────────────────
-- Contraseña: Admin1234!  (BCrypt cost=10)
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = '22222222-2222-2222-2222-300000000002');

-- ─── 8. Memberships ──────────────────────────────────────────
-- keygo_admin → key-go-ui
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT
    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '11111111-1111-1111-1111-400000000001',
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = '11111111-1111-1111-1111-400000000001');

-- keygo_tenant_admin → key-go-ui
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');

-- demo_user → demo-ui
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT
    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
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
-- ─── 7. Rol admin en keygo-ui ────────────────────────────────
      AND role_id = '22222222-2222-2222-2222-300000000002'
);
