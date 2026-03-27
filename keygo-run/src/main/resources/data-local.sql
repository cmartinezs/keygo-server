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
-- ─── Tenants base (secciones 1–9) ─────────────────────────────
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
--  UUIDs estables (mismos que V14):
--   Tenant keygo : 11111111-1111-1111-1111-111111111111
--   Tenant demo  : 22222222-2222-2222-2222-222222222222
--   App key-go-ui: 11111111-1111-1111-1111-222222222222
--   App demo-ui  : 22222222-2222-2222-2222-333333333333
--
-- ─── Tenants extra para UI testing (secciones 10–18) ──────────
-- 50 tenants adicionales: 40 ACTIVE · 5 SUSPENDED · 5 PENDING
-- Cada uno con: 1 client app, 1 admin (Admin1234!) y 1 user (DevUser1!)
-- Esquema UUID:  {prefix}-0000-0000-0000-{n:012x}  (n = 1..50 hex)
--   Prefijo 10* = tenants   | 20* = client apps    | 30* = redirect URIs
--   Prefijo 40* = grants AC | 50* = grants RT      | 60/70/80* = scopes
--   Prefijo 90* = admins    | a0* = users          | b0/c0* = roles
--   Prefijo d0* = memberships admin | e0* = memberships user
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

-- ════════════════════════════════════════════════════════════════════════════
-- SEED EXTRA: 50 tenants de prueba para UI testing
-- Contraseñas: admin → Admin1234!  |  user → DevUser1!
-- Estado: 40 ACTIVE · 5 SUSPENDED · 5 PENDING
--
-- Esquema de UUIDs (prefijo hex-fijo, sufijo = número de tenant en hex):
--   Tenant:        10000000-0000-0000-0000-{n:012x}
--   Client App:    20000000-0000-0000-0000-{n:012x}
--   Redirect URI:  30000000-0000-0000-0000-{n:012x}
--   Grant AC:      40000000-0000-0000-0000-{n:012x}
--   Grant RT:      50000000-0000-0000-0000-{n:012x}
--   Scope openid:  60000000-0000-0000-0000-{n:012x}
--   Scope profile: 70000000-0000-0000-0000-{n:012x}
--   Scope email:   80000000-0000-0000-0000-{n:012x}
--   Admin user:    90000000-0000-0000-0000-{n:012x}
--   Regular user:  a0000000-0000-0000-0000-{n:012x}
--   Role admin:    b0000000-0000-0000-0000-{n:012x}
--   Role user:     c0000000-0000-0000-0000-{n:012x}
--   Membership A:  d0000000-0000-0000-0000-{n:012x}
--   Membership U:  e0000000-0000-0000-0000-{n:012x}
-- ════════════════════════════════════════════════════════════════════════════

-- ─── 10. Tenants (extra) ──────────────────────────────────────────────────
-- #01: acme (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000001', 'acme', 'Acme Corp', 'owner@acme.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'acme');
-- #02: techcorp (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000002', 'techcorp', 'Tech Corp', 'owner@techcorp.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'techcorp');
-- #03: innovatek (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000003', 'innovatek', 'InnovaTek', 'owner@innovatek.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'innovatek');
-- #04: nexuslab (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000004', 'nexuslab', 'Nexus Lab', 'owner@nexuslab.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'nexuslab');
-- #05: velocity (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000005', 'velocity', 'Velocity Systems', 'owner@velocity.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'velocity');
-- #06: apextech (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000006', 'apextech', 'Apex Technologies', 'owner@apextech.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'apextech');
-- #07: zenithdigital (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000007', 'zenithdigital', 'Zenith Digital', 'owner@zenithdigital.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'zenithdigital');
-- #08: novasoft (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000008', 'novasoft', 'Nova Software', 'owner@novasoft.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'novasoft');
-- #09: auroraio (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000009', 'auroraio', 'Aurora IO', 'owner@auroraio.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'auroraio');
-- #10: quantumlab (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000000a', 'quantumlab', 'Quantum Lab', 'owner@quantumlab.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'quantumlab');
-- #11: helixai (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000000b', 'helixai', 'Helix AI', 'owner@helixai.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'helixai');
-- #12: orbitcloud (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000000c', 'orbitcloud', 'Orbit Cloud', 'owner@orbitcloud.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'orbitcloud');
-- #13: fusionhq (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000000d', 'fusionhq', 'Fusion HQ', 'owner@fusionhq.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'fusionhq');
-- #14: nextgensol (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000000e', 'nextgensol', 'NextGen Solutions', 'owner@nextgensol.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'nextgensol');
-- #15: atlasnet (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000000f', 'atlasnet', 'Atlas Networks', 'owner@atlasnet.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'atlasnet');
-- #16: prismatic (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000010', 'prismatic', 'Prismatic Analytics', 'owner@prismatic.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'prismatic');
-- #17: vortexdata (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000011', 'vortexdata', 'Vortex Data', 'owner@vortexdata.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'vortexdata');
-- #18: titansec (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000012', 'titansec', 'Titan Security', 'owner@titansec.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'titansec');
-- #19: phoenixv (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000013', 'phoenixv', 'Phoenix Ventures', 'owner@phoenixv.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'phoenixv');
-- #20: cobaltdev (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000014', 'cobaltdev', 'Cobalt Dev', 'owner@cobaltdev.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'cobaltdev');
-- #21: amberfinance (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000015', 'amberfinance', 'Amber Finance', 'owner@amberfinance.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'amberfinance');
-- #22: sageapp (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000016', 'sageapp', 'Sage Consulting', 'owner@sageapp.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'sageapp');
-- #23: cedarhealth (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000017', 'cedarhealth', 'Cedar Health', 'owner@cedarhealth.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'cedarhealth');
-- #24: lumenlearn (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000018', 'lumenlearn', 'Lumen EdTech', 'owner@lumenlearn.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'lumenlearn');
-- #25: solarisretail (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000019', 'solarisretail', 'Solaris Retail', 'owner@solarisretail.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'solarisretail');
-- #26: meridianlog (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000001a', 'meridianlog', 'Meridian Logistics', 'owner@meridianlog.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'meridianlog');
-- #27: zephyrmedia (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000001b', 'zephyrmedia', 'Zephyr Media', 'owner@zephyrmedia.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'zephyrmedia');
-- #28: onyxgaming (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000001c', 'onyxgaming', 'Onyx Gaming', 'owner@onyxgaming.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'onyxgaming');
-- #29: chromemoto (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000001d', 'chromemoto', 'Chrome Automotive', 'owner@chromemoto.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'chromemoto');
-- #30: cyantravel (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000001e', 'cyantravel', 'Cyan Travel', 'owner@cyantravel.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'cyantravel');
-- #31: indigorealty (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000001f', 'indigorealty', 'Indigo Real Estate', 'owner@indigorealty.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'indigorealty');
-- #32: emberfin (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000020', 'emberfin', 'Ember Finance', 'owner@emberfin.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'emberfin');
-- #33: grovefarm (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000021', 'grovefarm', 'Grove Agriculture', 'owner@grovefarm.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'grovefarm');
-- #34: fluxmfg (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000022', 'fluxmfg', 'Flux Manufacturing', 'owner@fluxmfg.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'fluxmfg');
-- #35: arcenergy (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000023', 'arcenergy', 'Arc Energy', 'owner@arcenergy.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'arcenergy');
-- #36: crestlaw (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000024', 'crestlaw', 'Crest Legal', 'owner@crestlaw.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'crestlaw');
-- #37: waveship (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000025', 'waveship', 'Wave Maritime', 'owner@waveship.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'waveship');
-- #38: peakmine (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000026', 'peakmine', 'Peak Mining', 'owner@peakmine.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'peakmine');
-- #39: deltaair (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000027', 'deltaair', 'Delta Aviation', 'owner@deltaair.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'deltaair');
-- #40: sierradef (ACTIVE)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000028', 'sierradef', 'Sierra Defense', 'owner@sierradef.local', 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'sierradef');
-- #41: bravotel (SUSPENDED)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000029', 'bravotel', 'Bravo Telecom', 'owner@bravotel.local', 'SUSPENDED',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'bravotel');
-- #42: foxtrotmusic (SUSPENDED)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000002a', 'foxtrotmusic', 'Foxtrot Music', 'owner@foxtrotmusic.local', 'SUSPENDED',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'foxtrotmusic');
-- #43: wellnessco (SUSPENDED)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000002b', 'wellnessco', 'WellnessCo', 'owner@wellnessco.local', 'SUSPENDED',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'wellnessco');
-- #44: pharmahub (SUSPENDED)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000002c', 'pharmahub', 'Pharma Hub', 'owner@pharmahub.local', 'SUSPENDED',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'pharmahub');
-- #45: apparelx (SUSPENDED)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000002d', 'apparelx', 'Apparel X', 'owner@apparelx.local', 'SUSPENDED',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'apparelx');
-- #46: beautyplus (PENDING)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000002e', 'beautyplus', 'Beauty Plus', 'owner@beautyplus.local', 'PENDING',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'beautyplus');
-- #47: kilofood (PENDING)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-00000000002f', 'kilofood', 'Kilo Foods', 'owner@kilofood.local', 'PENDING',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'kilofood');
-- #48: limasport (PENDING)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000030', 'limasport', 'Lima Sport', 'owner@limasport.local', 'PENDING',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'limasport');
-- #49: maplegrp (PENDING)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000031', 'maplegrp', 'Maple Group', 'owner@maplegrp.local', 'PENDING',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'maplegrp');
-- #50: stoneworks (PENDING)
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
SELECT '10000000-0000-0000-0000-000000000032', 'stoneworks', 'Stone Works', 'owner@stoneworks.local', 'PENDING',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE slug = 'stoneworks');

-- ─── 11. Client Apps (extra) ─────────────────────────────────────────────
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'acme-ui', 'Acme Corp UI',
       'UI application for Acme Corp', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'acme-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'techcorp-ui', 'Tech Corp UI',
       'UI application for Tech Corp', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'techcorp-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'innovatek-ui', 'InnovaTek UI',
       'UI application for InnovaTek', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'innovatek-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', 'nexuslab-ui', 'Nexus Lab UI',
       'UI application for Nexus Lab', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'nexuslab-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', 'velocity-ui', 'Velocity Systems UI',
       'UI application for Velocity Systems', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'velocity-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000006', 'apextech-ui', 'Apex Technologies UI',
       'UI application for Apex Technologies', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'apextech-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000007', 'zenithdigital-ui', 'Zenith Digital UI',
       'UI application for Zenith Digital', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'zenithdigital-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000008', 'novasoft-ui', 'Nova Software UI',
       'UI application for Nova Software', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'novasoft-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000009', 'auroraio-ui', 'Aurora IO UI',
       'UI application for Aurora IO', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'auroraio-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-00000000000a', 'quantumlab-ui', 'Quantum Lab UI',
       'UI application for Quantum Lab', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'quantumlab-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-00000000000b', 'helixai-ui', 'Helix AI UI',
       'UI application for Helix AI', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'helixai-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-00000000000c', 'orbitcloud-ui', 'Orbit Cloud UI',
       'UI application for Orbit Cloud', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'orbitcloud-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-00000000000d', 'fusionhq-ui', 'Fusion HQ UI',
       'UI application for Fusion HQ', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'fusionhq-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-00000000000e', 'nextgensol-ui', 'NextGen Solutions UI',
       'UI application for NextGen Solutions', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'nextgensol-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000000f', '10000000-0000-0000-0000-00000000000f', 'atlasnet-ui', 'Atlas Networks UI',
       'UI application for Atlas Networks', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'atlasnet-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000010', 'prismatic-ui', 'Prismatic Analytics UI',
       'UI application for Prismatic Analytics', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'prismatic-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000011', 'vortexdata-ui', 'Vortex Data UI',
       'UI application for Vortex Data', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'vortexdata-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000012', 'titansec-ui', 'Titan Security UI',
       'UI application for Titan Security', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'titansec-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000013', '10000000-0000-0000-0000-000000000013', 'phoenixv-ui', 'Phoenix Ventures UI',
       'UI application for Phoenix Ventures', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'phoenixv-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000014', '10000000-0000-0000-0000-000000000014', 'cobaltdev-ui', 'Cobalt Dev UI',
       'UI application for Cobalt Dev', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'cobaltdev-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000015', '10000000-0000-0000-0000-000000000015', 'amberfinance-ui', 'Amber Finance UI',
       'UI application for Amber Finance', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'amberfinance-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000016', '10000000-0000-0000-0000-000000000016', 'sageapp-ui', 'Sage Consulting UI',
       'UI application for Sage Consulting', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'sageapp-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000017', '10000000-0000-0000-0000-000000000017', 'cedarhealth-ui', 'Cedar Health UI',
       'UI application for Cedar Health', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'cedarhealth-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000018', '10000000-0000-0000-0000-000000000018', 'lumenlearn-ui', 'Lumen EdTech UI',
       'UI application for Lumen EdTech', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'lumenlearn-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000019', '10000000-0000-0000-0000-000000000019', 'solarisretail-ui', 'Solaris Retail UI',
       'UI application for Solaris Retail', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'solarisretail-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000001a', '10000000-0000-0000-0000-00000000001a', 'meridianlog-ui', 'Meridian Logistics UI',
       'UI application for Meridian Logistics', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'meridianlog-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000001b', '10000000-0000-0000-0000-00000000001b', 'zephyrmedia-ui', 'Zephyr Media UI',
       'UI application for Zephyr Media', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'zephyrmedia-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000001c', '10000000-0000-0000-0000-00000000001c', 'onyxgaming-ui', 'Onyx Gaming UI',
       'UI application for Onyx Gaming', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'onyxgaming-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000001d', '10000000-0000-0000-0000-00000000001d', 'chromemoto-ui', 'Chrome Automotive UI',
       'UI application for Chrome Automotive', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'chromemoto-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000001e', '10000000-0000-0000-0000-00000000001e', 'cyantravel-ui', 'Cyan Travel UI',
       'UI application for Cyan Travel', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'cyantravel-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000001f', '10000000-0000-0000-0000-00000000001f', 'indigorealty-ui', 'Indigo Real Estate UI',
       'UI application for Indigo Real Estate', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'indigorealty-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000020', '10000000-0000-0000-0000-000000000020', 'emberfin-ui', 'Ember Finance UI',
       'UI application for Ember Finance', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'emberfin-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000021', '10000000-0000-0000-0000-000000000021', 'grovefarm-ui', 'Grove Agriculture UI',
       'UI application for Grove Agriculture', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'grovefarm-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000022', '10000000-0000-0000-0000-000000000022', 'fluxmfg-ui', 'Flux Manufacturing UI',
       'UI application for Flux Manufacturing', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'fluxmfg-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000023', '10000000-0000-0000-0000-000000000023', 'arcenergy-ui', 'Arc Energy UI',
       'UI application for Arc Energy', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'arcenergy-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000024', '10000000-0000-0000-0000-000000000024', 'crestlaw-ui', 'Crest Legal UI',
       'UI application for Crest Legal', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'crestlaw-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000025', '10000000-0000-0000-0000-000000000025', 'waveship-ui', 'Wave Maritime UI',
       'UI application for Wave Maritime', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'waveship-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000026', '10000000-0000-0000-0000-000000000026', 'peakmine-ui', 'Peak Mining UI',
       'UI application for Peak Mining', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'peakmine-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000027', '10000000-0000-0000-0000-000000000027', 'deltaair-ui', 'Delta Aviation UI',
       'UI application for Delta Aviation', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'deltaair-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000028', '10000000-0000-0000-0000-000000000028', 'sierradef-ui', 'Sierra Defense UI',
       'UI application for Sierra Defense', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'sierradef-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000029', '10000000-0000-0000-0000-000000000029', 'bravotel-ui', 'Bravo Telecom UI',
       'UI application for Bravo Telecom', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'bravotel-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000002a', '10000000-0000-0000-0000-00000000002a', 'foxtrotmusic-ui', 'Foxtrot Music UI',
       'UI application for Foxtrot Music', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'foxtrotmusic-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000002b', '10000000-0000-0000-0000-00000000002b', 'wellnessco-ui', 'WellnessCo UI',
       'UI application for WellnessCo', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'wellnessco-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000002c', '10000000-0000-0000-0000-00000000002c', 'pharmahub-ui', 'Pharma Hub UI',
       'UI application for Pharma Hub', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'pharmahub-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000002d', '10000000-0000-0000-0000-00000000002d', 'apparelx-ui', 'Apparel X UI',
       'UI application for Apparel X', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'apparelx-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000002e', '10000000-0000-0000-0000-00000000002e', 'beautyplus-ui', 'Beauty Plus UI',
       'UI application for Beauty Plus', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'beautyplus-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-00000000002f', '10000000-0000-0000-0000-00000000002f', 'kilofood-ui', 'Kilo Foods UI',
       'UI application for Kilo Foods', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'kilofood-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000030', '10000000-0000-0000-0000-000000000030', 'limasport-ui', 'Lima Sport UI',
       'UI application for Lima Sport', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'limasport-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000031', '10000000-0000-0000-0000-000000000031', 'maplegrp-ui', 'Maple Group UI',
       'UI application for Maple Group', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'maplegrp-ui');
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
SELECT '20000000-0000-0000-0000-000000000032', '10000000-0000-0000-0000-000000000032', 'stoneworks-ui', 'Stone Works UI',
       'UI application for Stone Works', 'PUBLIC', NULL, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_apps WHERE client_id = 'stoneworks-ui');

-- ─── 12. Redirect URIs (extra) ────────────────────────────────────────────
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000001');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000002');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000003');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000004');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000005');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000006');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000007');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000008');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000009');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000000a');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000000b');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000000c');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000000d');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000000e');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000000f');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000010');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000011');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000012');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000013');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000014');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000015');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000016');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000017');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000018');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000019');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000001a');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000001b');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000001c');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000001d');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000001e');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000001f');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000020');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000021');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000022');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000023');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000024');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000025');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000026');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000027');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000028');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000029');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000002a');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000002b');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000002c');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000002d');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000002e');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-00000000002f');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000030');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000031');
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
SELECT '30000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'http://localhost:5173/callback', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM client_redirect_uris WHERE id = '30000000-0000-0000-0000-000000000032');

-- ─── 13. Allowed Grants (extra) ───────────────────────────────────────────
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000001');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000001');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000002');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000002');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000003');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000003');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000004');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000004');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000005');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000005');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000006');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000006');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000007');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000007');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000008');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000008');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000009');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000009');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000000a');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000000a');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000000b');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000000b');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000000c');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000000c');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000000d');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000000d');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000000e');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000000e');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000000f');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000000f');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000010');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000010');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000011');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000011');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000012');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000012');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000013');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000013');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000014');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000014');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000015');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000015');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000016');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000016');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000017');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000017');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000018');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000018');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000019');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000019');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000001a');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000001a');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000001b');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000001b');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000001c');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000001c');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000001d');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000001d');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000001e');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000001e');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000001f');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000001f');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000020');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000020');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000021');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000021');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000022');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000022');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000023');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000023');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000024');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000024');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000025');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000025');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000026');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000026');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000027');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000027');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000028');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000028');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000029');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000029');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000002a');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000002a');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000002b');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000002b');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000002c');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000002c');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000002d');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000002d');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000002e');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000002e');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-00000000002f');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-00000000002f');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000030');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000030');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000031');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000031');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '40000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'AUTHORIZATION_CODE'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '40000000-0000-0000-0000-000000000032');
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT '50000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'REFRESH_TOKEN'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_grants WHERE id = '50000000-0000-0000-0000-000000000032');

-- ─── 14. Allowed Scopes (extra) ───────────────────────────────────────────
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000001');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000001');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000001');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000002');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000002');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000002');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000003');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000003');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000003');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000004');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000004');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000004');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000005');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000005');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000005');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000006');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000006');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000006');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000007');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000007');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000007');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000008');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000008');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000008');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000009');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000009');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000009');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000000a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000000a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000000a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000000b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000000b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000000b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000000c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000000c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000000c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000000d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000000d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000000d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000000e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000000e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000000e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000000f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000000f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000000f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000010');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000010');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000010');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000011');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000011');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000011');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000012');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000012');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000012');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000013');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000013');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000013');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000014');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000014');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000014');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000015');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000015');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000015');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000016');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000016');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000016');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000017');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000017');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000017');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000018');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000018');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000018');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000019');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000019');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000019');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000001a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000001a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000001a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000001b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000001b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000001b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000001c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000001c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000001c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000001d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000001d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000001d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000001e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000001e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000001e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000001f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000001f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000001f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000020');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000020');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000020');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000021');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000021');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000021');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000022');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000022');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000022');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000023');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000023');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000023');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000024');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000024');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000024');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000025');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000025');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000025');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000026');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000026');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000026');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000027');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000027');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000027');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000028');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000028');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000028');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000029');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000029');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000029');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000002a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000002a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000002a');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000002b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000002b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000002b');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000002c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000002c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000002c');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000002d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000002d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000002d');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000002e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000002e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000002e');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-00000000002f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-00000000002f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-00000000002f');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000030');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000030');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000030');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000031');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000031');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000031');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '60000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'openid'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '60000000-0000-0000-0000-000000000032');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '70000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'profile'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '70000000-0000-0000-0000-000000000032');
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
SELECT '80000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'email'
WHERE NOT EXISTS (SELECT 1 FROM client_allowed_scopes WHERE id = '80000000-0000-0000-0000-000000000032');

-- ─── 15. Tenant Users (extra) — Admin1234! / DevUser1! ────────────────────
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'acme_admin', 'admin@acme.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Acme', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000001');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'acme_user', 'user@acme.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Acme', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000001');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'techcorp_admin', 'admin@techcorp.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Tech', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000002');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'techcorp_user', 'user@techcorp.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Tech', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000002');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'innovatek_admin', 'admin@innovatek.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'InnovaTek', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000003');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'innovatek_user', 'user@innovatek.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'InnovaTek', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000003');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', 'nexuslab_admin', 'admin@nexuslab.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Nexus', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000004');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', 'nexuslab_user', 'user@nexuslab.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Nexus', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000004');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', 'velocity_admin', 'admin@velocity.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Velocity', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000005');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', 'velocity_user', 'user@velocity.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Velocity', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000005');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000006', 'apextech_admin', 'admin@apextech.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Apex', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000006');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000006', 'apextech_user', 'user@apextech.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Apex', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000006');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000007', 'zenithdigital_admin', 'admin@zenithdigital.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Zenith', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000007');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000007', 'zenithdigital_user', 'user@zenithdigital.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Zenith', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000007');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000008', 'novasoft_admin', 'admin@novasoft.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Nova', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000008');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000008', 'novasoft_user', 'user@novasoft.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Nova', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000008');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000009', 'auroraio_admin', 'admin@auroraio.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Aurora', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000009');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000009', 'auroraio_user', 'user@auroraio.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Aurora', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000009');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-00000000000a', 'quantumlab_admin', 'admin@quantumlab.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Quantum', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000000a');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-00000000000a', 'quantumlab_user', 'user@quantumlab.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Quantum', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000000a');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-00000000000b', 'helixai_admin', 'admin@helixai.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Helix', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000000b');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-00000000000b', 'helixai_user', 'user@helixai.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Helix', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000000b');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-00000000000c', 'orbitcloud_admin', 'admin@orbitcloud.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Orbit', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000000c');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-00000000000c', 'orbitcloud_user', 'user@orbitcloud.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Orbit', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000000c');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-00000000000d', 'fusionhq_admin', 'admin@fusionhq.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Fusion', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000000d');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-00000000000d', 'fusionhq_user', 'user@fusionhq.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Fusion', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000000d');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-00000000000e', 'nextgensol_admin', 'admin@nextgensol.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'NextGen', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000000e');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-00000000000e', 'nextgensol_user', 'user@nextgensol.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'NextGen', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000000e');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000000f', '10000000-0000-0000-0000-00000000000f', 'atlasnet_admin', 'admin@atlasnet.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Atlas', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000000f');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000000f', '10000000-0000-0000-0000-00000000000f', 'atlasnet_user', 'user@atlasnet.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Atlas', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000000f');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000010', 'prismatic_admin', 'admin@prismatic.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Prismatic', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000010');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000010', 'prismatic_user', 'user@prismatic.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Prismatic', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000010');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000011', 'vortexdata_admin', 'admin@vortexdata.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Vortex', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000011');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000011', 'vortexdata_user', 'user@vortexdata.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Vortex', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000011');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000012', 'titansec_admin', 'admin@titansec.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Titan', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000012');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000012', 'titansec_user', 'user@titansec.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Titan', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000012');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000013', '10000000-0000-0000-0000-000000000013', 'phoenixv_admin', 'admin@phoenixv.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Phoenix', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000013');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000013', '10000000-0000-0000-0000-000000000013', 'phoenixv_user', 'user@phoenixv.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Phoenix', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000013');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000014', '10000000-0000-0000-0000-000000000014', 'cobaltdev_admin', 'admin@cobaltdev.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Cobalt', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000014');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000014', '10000000-0000-0000-0000-000000000014', 'cobaltdev_user', 'user@cobaltdev.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Cobalt', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000014');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000015', '10000000-0000-0000-0000-000000000015', 'amberfinance_admin', 'admin@amberfinance.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Amber', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000015');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000015', '10000000-0000-0000-0000-000000000015', 'amberfinance_user', 'user@amberfinance.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Amber', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000015');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000016', '10000000-0000-0000-0000-000000000016', 'sageapp_admin', 'admin@sageapp.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Sage', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000016');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000016', '10000000-0000-0000-0000-000000000016', 'sageapp_user', 'user@sageapp.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Sage', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000016');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000017', '10000000-0000-0000-0000-000000000017', 'cedarhealth_admin', 'admin@cedarhealth.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Cedar', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000017');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000017', '10000000-0000-0000-0000-000000000017', 'cedarhealth_user', 'user@cedarhealth.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Cedar', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000017');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000018', '10000000-0000-0000-0000-000000000018', 'lumenlearn_admin', 'admin@lumenlearn.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Lumen', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000018');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000018', '10000000-0000-0000-0000-000000000018', 'lumenlearn_user', 'user@lumenlearn.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Lumen', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000018');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000019', '10000000-0000-0000-0000-000000000019', 'solarisretail_admin', 'admin@solarisretail.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Solaris', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000019');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000019', '10000000-0000-0000-0000-000000000019', 'solarisretail_user', 'user@solarisretail.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Solaris', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000019');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000001a', '10000000-0000-0000-0000-00000000001a', 'meridianlog_admin', 'admin@meridianlog.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Meridian', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000001a');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000001a', '10000000-0000-0000-0000-00000000001a', 'meridianlog_user', 'user@meridianlog.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Meridian', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000001a');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000001b', '10000000-0000-0000-0000-00000000001b', 'zephyrmedia_admin', 'admin@zephyrmedia.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Zephyr', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000001b');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000001b', '10000000-0000-0000-0000-00000000001b', 'zephyrmedia_user', 'user@zephyrmedia.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Zephyr', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000001b');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000001c', '10000000-0000-0000-0000-00000000001c', 'onyxgaming_admin', 'admin@onyxgaming.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Onyx', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000001c');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000001c', '10000000-0000-0000-0000-00000000001c', 'onyxgaming_user', 'user@onyxgaming.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Onyx', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000001c');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000001d', '10000000-0000-0000-0000-00000000001d', 'chromemoto_admin', 'admin@chromemoto.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Chrome', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000001d');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000001d', '10000000-0000-0000-0000-00000000001d', 'chromemoto_user', 'user@chromemoto.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Chrome', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000001d');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000001e', '10000000-0000-0000-0000-00000000001e', 'cyantravel_admin', 'admin@cyantravel.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Cyan', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000001e');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000001e', '10000000-0000-0000-0000-00000000001e', 'cyantravel_user', 'user@cyantravel.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Cyan', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000001e');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000001f', '10000000-0000-0000-0000-00000000001f', 'indigorealty_admin', 'admin@indigorealty.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Indigo', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000001f');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000001f', '10000000-0000-0000-0000-00000000001f', 'indigorealty_user', 'user@indigorealty.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Indigo', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000001f');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000020', '10000000-0000-0000-0000-000000000020', 'emberfin_admin', 'admin@emberfin.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Ember', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000020');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000020', '10000000-0000-0000-0000-000000000020', 'emberfin_user', 'user@emberfin.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Ember', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000020');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000021', '10000000-0000-0000-0000-000000000021', 'grovefarm_admin', 'admin@grovefarm.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Grove', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000021');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000021', '10000000-0000-0000-0000-000000000021', 'grovefarm_user', 'user@grovefarm.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Grove', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000021');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000022', '10000000-0000-0000-0000-000000000022', 'fluxmfg_admin', 'admin@fluxmfg.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Flux', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000022');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000022', '10000000-0000-0000-0000-000000000022', 'fluxmfg_user', 'user@fluxmfg.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Flux', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000022');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000023', '10000000-0000-0000-0000-000000000023', 'arcenergy_admin', 'admin@arcenergy.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Arc', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000023');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000023', '10000000-0000-0000-0000-000000000023', 'arcenergy_user', 'user@arcenergy.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Arc', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000023');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000024', '10000000-0000-0000-0000-000000000024', 'crestlaw_admin', 'admin@crestlaw.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Crest', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000024');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000024', '10000000-0000-0000-0000-000000000024', 'crestlaw_user', 'user@crestlaw.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Crest', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000024');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000025', '10000000-0000-0000-0000-000000000025', 'waveship_admin', 'admin@waveship.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Wave', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000025');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000025', '10000000-0000-0000-0000-000000000025', 'waveship_user', 'user@waveship.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Wave', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000025');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000026', '10000000-0000-0000-0000-000000000026', 'peakmine_admin', 'admin@peakmine.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Peak', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000026');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000026', '10000000-0000-0000-0000-000000000026', 'peakmine_user', 'user@peakmine.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Peak', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000026');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000027', '10000000-0000-0000-0000-000000000027', 'deltaair_admin', 'admin@deltaair.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Delta', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000027');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000027', '10000000-0000-0000-0000-000000000027', 'deltaair_user', 'user@deltaair.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Delta', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000027');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000028', '10000000-0000-0000-0000-000000000028', 'sierradef_admin', 'admin@sierradef.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Sierra', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000028');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000028', '10000000-0000-0000-0000-000000000028', 'sierradef_user', 'user@sierradef.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Sierra', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000028');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000029', '10000000-0000-0000-0000-000000000029', 'bravotel_admin', 'admin@bravotel.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Bravo', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000029');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000029', '10000000-0000-0000-0000-000000000029', 'bravotel_user', 'user@bravotel.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Bravo', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000029');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000002a', '10000000-0000-0000-0000-00000000002a', 'foxtrotmusic_admin', 'admin@foxtrotmusic.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Foxtrot', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000002a');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000002a', '10000000-0000-0000-0000-00000000002a', 'foxtrotmusic_user', 'user@foxtrotmusic.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Foxtrot', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000002a');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000002b', '10000000-0000-0000-0000-00000000002b', 'wellnessco_admin', 'admin@wellnessco.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'WellnessCo', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000002b');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000002b', '10000000-0000-0000-0000-00000000002b', 'wellnessco_user', 'user@wellnessco.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'WellnessCo', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000002b');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000002c', '10000000-0000-0000-0000-00000000002c', 'pharmahub_admin', 'admin@pharmahub.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Pharma', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000002c');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000002c', '10000000-0000-0000-0000-00000000002c', 'pharmahub_user', 'user@pharmahub.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Pharma', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000002c');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000002d', '10000000-0000-0000-0000-00000000002d', 'apparelx_admin', 'admin@apparelx.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Apparel', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000002d');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000002d', '10000000-0000-0000-0000-00000000002d', 'apparelx_user', 'user@apparelx.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Apparel', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000002d');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000002e', '10000000-0000-0000-0000-00000000002e', 'beautyplus_admin', 'admin@beautyplus.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Beauty', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000002e');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000002e', '10000000-0000-0000-0000-00000000002e', 'beautyplus_user', 'user@beautyplus.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Beauty', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000002e');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-00000000002f', '10000000-0000-0000-0000-00000000002f', 'kilofood_admin', 'admin@kilofood.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Kilo', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-00000000002f');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-00000000002f', '10000000-0000-0000-0000-00000000002f', 'kilofood_user', 'user@kilofood.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Kilo', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-00000000002f');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000030', '10000000-0000-0000-0000-000000000030', 'limasport_admin', 'admin@limasport.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Lima', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000030');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000030', '10000000-0000-0000-0000-000000000030', 'limasport_user', 'user@limasport.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Lima', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000030');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000031', '10000000-0000-0000-0000-000000000031', 'maplegrp_admin', 'admin@maplegrp.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Maple', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000031');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000031', '10000000-0000-0000-0000-000000000031', 'maplegrp_user', 'user@maplegrp.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Maple', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000031');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT '90000000-0000-0000-0000-000000000032', '10000000-0000-0000-0000-000000000032', 'stoneworks_admin', 'admin@stoneworks.local',
       '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
       'Stone', 'Admin', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = '90000000-0000-0000-0000-000000000032');
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
SELECT 'a0000000-0000-0000-0000-000000000032', '10000000-0000-0000-0000-000000000032', 'stoneworks_user', 'user@stoneworks.local',
       '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
       'Stone', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tenant_users WHERE id = 'a0000000-0000-0000-0000-000000000032');

-- ─── 16. App Roles (extra) ────────────────────────────────────────────────
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'admin', 'Admin', 'Administrator role for acme', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000001');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'user', 'User', 'Standard user role for acme', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000001');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'admin', 'Admin', 'Administrator role for techcorp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000002');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'user', 'User', 'Standard user role for techcorp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000002');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'admin', 'Admin', 'Administrator role for innovatek', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000003');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'user', 'User', 'Standard user role for innovatek', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000003');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'admin', 'Admin', 'Administrator role for nexuslab', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000004');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'user', 'User', 'Standard user role for nexuslab', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000004');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'admin', 'Admin', 'Administrator role for velocity', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000005');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'user', 'User', 'Standard user role for velocity', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000005');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'admin', 'Admin', 'Administrator role for apextech', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000006');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'user', 'User', 'Standard user role for apextech', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000006');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'admin', 'Admin', 'Administrator role for zenithdigital', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000007');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'user', 'User', 'Standard user role for zenithdigital', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000007');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'admin', 'Admin', 'Administrator role for novasoft', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000008');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'user', 'User', 'Standard user role for novasoft', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000008');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'admin', 'Admin', 'Administrator role for auroraio', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000009');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'user', 'User', 'Standard user role for auroraio', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000009');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'admin', 'Admin', 'Administrator role for quantumlab', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000000a');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'user', 'User', 'Standard user role for quantumlab', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000000a');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'admin', 'Admin', 'Administrator role for helixai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000000b');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'user', 'User', 'Standard user role for helixai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000000b');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'admin', 'Admin', 'Administrator role for orbitcloud', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000000c');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'user', 'User', 'Standard user role for orbitcloud', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000000c');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'admin', 'Admin', 'Administrator role for fusionhq', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000000d');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'user', 'User', 'Standard user role for fusionhq', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000000d');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'admin', 'Admin', 'Administrator role for nextgensol', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000000e');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'user', 'User', 'Standard user role for nextgensol', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000000e');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'admin', 'Admin', 'Administrator role for atlasnet', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000000f');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'user', 'User', 'Standard user role for atlasnet', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000000f');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'admin', 'Admin', 'Administrator role for prismatic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000010');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'user', 'User', 'Standard user role for prismatic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000010');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'admin', 'Admin', 'Administrator role for vortexdata', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000011');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'user', 'User', 'Standard user role for vortexdata', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000011');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'admin', 'Admin', 'Administrator role for titansec', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000012');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'user', 'User', 'Standard user role for titansec', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000012');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'admin', 'Admin', 'Administrator role for phoenixv', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000013');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'user', 'User', 'Standard user role for phoenixv', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000013');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'admin', 'Admin', 'Administrator role for cobaltdev', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000014');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'user', 'User', 'Standard user role for cobaltdev', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000014');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'admin', 'Admin', 'Administrator role for amberfinance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000015');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'user', 'User', 'Standard user role for amberfinance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000015');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'admin', 'Admin', 'Administrator role for sageapp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000016');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'user', 'User', 'Standard user role for sageapp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000016');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'admin', 'Admin', 'Administrator role for cedarhealth', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000017');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'user', 'User', 'Standard user role for cedarhealth', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000017');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'admin', 'Admin', 'Administrator role for lumenlearn', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000018');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'user', 'User', 'Standard user role for lumenlearn', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000018');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'admin', 'Admin', 'Administrator role for solarisretail', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000019');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'user', 'User', 'Standard user role for solarisretail', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000019');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'admin', 'Admin', 'Administrator role for meridianlog', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000001a');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'user', 'User', 'Standard user role for meridianlog', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000001a');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'admin', 'Admin', 'Administrator role for zephyrmedia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000001b');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'user', 'User', 'Standard user role for zephyrmedia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000001b');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'admin', 'Admin', 'Administrator role for onyxgaming', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000001c');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'user', 'User', 'Standard user role for onyxgaming', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000001c');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'admin', 'Admin', 'Administrator role for chromemoto', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000001d');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'user', 'User', 'Standard user role for chromemoto', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000001d');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'admin', 'Admin', 'Administrator role for cyantravel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000001e');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'user', 'User', 'Standard user role for cyantravel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000001e');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'admin', 'Admin', 'Administrator role for indigorealty', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000001f');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'user', 'User', 'Standard user role for indigorealty', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000001f');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'admin', 'Admin', 'Administrator role for emberfin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000020');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'user', 'User', 'Standard user role for emberfin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000020');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'admin', 'Admin', 'Administrator role for grovefarm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000021');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'user', 'User', 'Standard user role for grovefarm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000021');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'admin', 'Admin', 'Administrator role for fluxmfg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000022');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'user', 'User', 'Standard user role for fluxmfg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000022');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'admin', 'Admin', 'Administrator role for arcenergy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000023');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'user', 'User', 'Standard user role for arcenergy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000023');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'admin', 'Admin', 'Administrator role for crestlaw', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000024');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'user', 'User', 'Standard user role for crestlaw', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000024');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'admin', 'Admin', 'Administrator role for waveship', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000025');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'user', 'User', 'Standard user role for waveship', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000025');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'admin', 'Admin', 'Administrator role for peakmine', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000026');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'user', 'User', 'Standard user role for peakmine', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000026');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'admin', 'Admin', 'Administrator role for deltaair', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000027');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'user', 'User', 'Standard user role for deltaair', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000027');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'admin', 'Admin', 'Administrator role for sierradef', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000028');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'user', 'User', 'Standard user role for sierradef', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000028');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'admin', 'Admin', 'Administrator role for bravotel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000029');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'user', 'User', 'Standard user role for bravotel', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000029');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'admin', 'Admin', 'Administrator role for foxtrotmusic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000002a');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'user', 'User', 'Standard user role for foxtrotmusic', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000002a');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'admin', 'Admin', 'Administrator role for wellnessco', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000002b');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'user', 'User', 'Standard user role for wellnessco', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000002b');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'admin', 'Admin', 'Administrator role for pharmahub', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000002c');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'user', 'User', 'Standard user role for pharmahub', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000002c');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'admin', 'Admin', 'Administrator role for apparelx', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000002d');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'user', 'User', 'Standard user role for apparelx', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000002d');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'admin', 'Admin', 'Administrator role for beautyplus', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000002e');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'user', 'User', 'Standard user role for beautyplus', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000002e');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'admin', 'Admin', 'Administrator role for kilofood', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-00000000002f');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'user', 'User', 'Standard user role for kilofood', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-00000000002f');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'admin', 'Admin', 'Administrator role for limasport', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000030');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'user', 'User', 'Standard user role for limasport', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000030');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'admin', 'Admin', 'Administrator role for maplegrp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000031');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'user', 'User', 'Standard user role for maplegrp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000031');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'b0000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'admin', 'Admin', 'Administrator role for stoneworks', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'b0000000-0000-0000-0000-000000000032');
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
SELECT 'c0000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'user', 'User', 'Standard user role for stoneworks', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM app_roles WHERE id = 'c0000000-0000-0000-0000-000000000032');

-- ─── 17. Memberships (extra) ──────────────────────────────────────────────
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000001');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000001');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000002');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000002');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000003', '90000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000003');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000003');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000004', '90000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000004');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000004', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000004');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000005', '90000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000005');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000005', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000005');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000006', '90000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000006');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000006', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000006');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000007', '90000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000007');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000007', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000007');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000008', '90000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000008');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000008', 'a0000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000008', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000008');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000009', '90000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000009');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000009', 'a0000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000009', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000009');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000000a', '90000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000000a');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000000a', 'a0000000-0000-0000-0000-00000000000a', '20000000-0000-0000-0000-00000000000a', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000000a');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000000b', '90000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000000b');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000000b', 'a0000000-0000-0000-0000-00000000000b', '20000000-0000-0000-0000-00000000000b', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000000b');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000000c', '90000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000000c');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000000c', 'a0000000-0000-0000-0000-00000000000c', '20000000-0000-0000-0000-00000000000c', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000000c');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000000d', '90000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000000d');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000000d', 'a0000000-0000-0000-0000-00000000000d', '20000000-0000-0000-0000-00000000000d', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000000d');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000000e', '90000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000000e');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000000e', 'a0000000-0000-0000-0000-00000000000e', '20000000-0000-0000-0000-00000000000e', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000000e');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000000f', '90000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000000f');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000000f', 'a0000000-0000-0000-0000-00000000000f', '20000000-0000-0000-0000-00000000000f', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000000f');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000010', '90000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000010');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000010', 'a0000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000010', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000010');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000011', '90000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000011');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000011', 'a0000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000011', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000011');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000012', '90000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000012');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000012', 'a0000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000012', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000012');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000013', '90000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000013');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000013', 'a0000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000013', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000013');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000014', '90000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000014');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000014', 'a0000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000014', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000014');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000015', '90000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000015');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000015', 'a0000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000015', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000015');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000016', '90000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000016');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000016', 'a0000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000016', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000016');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000017', '90000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000017');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000017', 'a0000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000017', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000017');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000018', '90000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000018');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000018', 'a0000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000018', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000018');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000019', '90000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000019');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000019', 'a0000000-0000-0000-0000-000000000019', '20000000-0000-0000-0000-000000000019', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000019');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000001a', '90000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000001a');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000001a', 'a0000000-0000-0000-0000-00000000001a', '20000000-0000-0000-0000-00000000001a', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000001a');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000001b', '90000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000001b');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000001b', 'a0000000-0000-0000-0000-00000000001b', '20000000-0000-0000-0000-00000000001b', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000001b');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000001c', '90000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000001c');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000001c', 'a0000000-0000-0000-0000-00000000001c', '20000000-0000-0000-0000-00000000001c', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000001c');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000001d', '90000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000001d');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000001d', 'a0000000-0000-0000-0000-00000000001d', '20000000-0000-0000-0000-00000000001d', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000001d');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000001e', '90000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000001e');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000001e', 'a0000000-0000-0000-0000-00000000001e', '20000000-0000-0000-0000-00000000001e', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000001e');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000001f', '90000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000001f');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000001f', 'a0000000-0000-0000-0000-00000000001f', '20000000-0000-0000-0000-00000000001f', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000001f');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000020', '90000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000020');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000020', 'a0000000-0000-0000-0000-000000000020', '20000000-0000-0000-0000-000000000020', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000020');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000021', '90000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000021');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000021', 'a0000000-0000-0000-0000-000000000021', '20000000-0000-0000-0000-000000000021', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000021');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000022', '90000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000022');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000022', 'a0000000-0000-0000-0000-000000000022', '20000000-0000-0000-0000-000000000022', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000022');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000023', '90000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000023');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000023', 'a0000000-0000-0000-0000-000000000023', '20000000-0000-0000-0000-000000000023', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000023');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000024', '90000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000024');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000024', 'a0000000-0000-0000-0000-000000000024', '20000000-0000-0000-0000-000000000024', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000024');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000025', '90000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000025');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000025', 'a0000000-0000-0000-0000-000000000025', '20000000-0000-0000-0000-000000000025', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000025');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000026', '90000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000026');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000026', 'a0000000-0000-0000-0000-000000000026', '20000000-0000-0000-0000-000000000026', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000026');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000027', '90000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000027');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000027', 'a0000000-0000-0000-0000-000000000027', '20000000-0000-0000-0000-000000000027', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000027');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000028', '90000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000028');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000028', 'a0000000-0000-0000-0000-000000000028', '20000000-0000-0000-0000-000000000028', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000028');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000029', '90000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000029');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000029', 'a0000000-0000-0000-0000-000000000029', '20000000-0000-0000-0000-000000000029', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000029');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000002a', '90000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000002a');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000002a', 'a0000000-0000-0000-0000-00000000002a', '20000000-0000-0000-0000-00000000002a', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000002a');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000002b', '90000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000002b');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000002b', 'a0000000-0000-0000-0000-00000000002b', '20000000-0000-0000-0000-00000000002b', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000002b');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000002c', '90000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000002c');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000002c', 'a0000000-0000-0000-0000-00000000002c', '20000000-0000-0000-0000-00000000002c', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000002c');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000002d', '90000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000002d');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000002d', 'a0000000-0000-0000-0000-00000000002d', '20000000-0000-0000-0000-00000000002d', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000002d');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000002e', '90000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000002e');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000002e', 'a0000000-0000-0000-0000-00000000002e', '20000000-0000-0000-0000-00000000002e', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000002e');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-00000000002f', '90000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-00000000002f');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-00000000002f', 'a0000000-0000-0000-0000-00000000002f', '20000000-0000-0000-0000-00000000002f', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-00000000002f');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000030', '90000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000030');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000030', 'a0000000-0000-0000-0000-000000000030', '20000000-0000-0000-0000-000000000030', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000030');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000031', '90000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000031');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000031', 'a0000000-0000-0000-0000-000000000031', '20000000-0000-0000-0000-000000000031', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000031');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'd0000000-0000-0000-0000-000000000032', '90000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'd0000000-0000-0000-0000-000000000032');
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
SELECT 'e0000000-0000-0000-0000-000000000032', 'a0000000-0000-0000-0000-000000000032', '20000000-0000-0000-0000-000000000032', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM memberships WHERE id = 'e0000000-0000-0000-0000-000000000032');

-- ─── 18. Membership Roles (extra) — H2: sin columna assigned_at ───────────
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000001' AND role_id = 'b0000000-0000-0000-0000-000000000001');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000001' AND role_id = 'c0000000-0000-0000-0000-000000000001');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000002' AND role_id = 'b0000000-0000-0000-0000-000000000002');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000002' AND role_id = 'c0000000-0000-0000-0000-000000000002');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000003' AND role_id = 'b0000000-0000-0000-0000-000000000003');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000003' AND role_id = 'c0000000-0000-0000-0000-000000000003');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000004' AND role_id = 'b0000000-0000-0000-0000-000000000004');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000004' AND role_id = 'c0000000-0000-0000-0000-000000000004');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000005'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000005' AND role_id = 'b0000000-0000-0000-0000-000000000005');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000005'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000005' AND role_id = 'c0000000-0000-0000-0000-000000000005');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000006'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000006' AND role_id = 'b0000000-0000-0000-0000-000000000006');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000006'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000006' AND role_id = 'c0000000-0000-0000-0000-000000000006');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000007'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000007' AND role_id = 'b0000000-0000-0000-0000-000000000007');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000007'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000007' AND role_id = 'c0000000-0000-0000-0000-000000000007');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000008'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000008' AND role_id = 'b0000000-0000-0000-0000-000000000008');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000008'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000008' AND role_id = 'c0000000-0000-0000-0000-000000000008');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000009'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000009' AND role_id = 'b0000000-0000-0000-0000-000000000009');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000009'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000009' AND role_id = 'c0000000-0000-0000-0000-000000000009');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000000a', 'b0000000-0000-0000-0000-00000000000a'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000000a' AND role_id = 'b0000000-0000-0000-0000-00000000000a');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000000a', 'c0000000-0000-0000-0000-00000000000a'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000000a' AND role_id = 'c0000000-0000-0000-0000-00000000000a');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000000b', 'b0000000-0000-0000-0000-00000000000b'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000000b' AND role_id = 'b0000000-0000-0000-0000-00000000000b');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000000b', 'c0000000-0000-0000-0000-00000000000b'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000000b' AND role_id = 'c0000000-0000-0000-0000-00000000000b');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000000c', 'b0000000-0000-0000-0000-00000000000c'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000000c' AND role_id = 'b0000000-0000-0000-0000-00000000000c');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000000c', 'c0000000-0000-0000-0000-00000000000c'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000000c' AND role_id = 'c0000000-0000-0000-0000-00000000000c');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000000d', 'b0000000-0000-0000-0000-00000000000d'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000000d' AND role_id = 'b0000000-0000-0000-0000-00000000000d');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000000d', 'c0000000-0000-0000-0000-00000000000d'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000000d' AND role_id = 'c0000000-0000-0000-0000-00000000000d');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000000e', 'b0000000-0000-0000-0000-00000000000e'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000000e' AND role_id = 'b0000000-0000-0000-0000-00000000000e');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000000e', 'c0000000-0000-0000-0000-00000000000e'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000000e' AND role_id = 'c0000000-0000-0000-0000-00000000000e');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000000f', 'b0000000-0000-0000-0000-00000000000f'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000000f' AND role_id = 'b0000000-0000-0000-0000-00000000000f');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000000f', 'c0000000-0000-0000-0000-00000000000f'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000000f' AND role_id = 'c0000000-0000-0000-0000-00000000000f');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000010'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000010' AND role_id = 'b0000000-0000-0000-0000-000000000010');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000010'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000010' AND role_id = 'c0000000-0000-0000-0000-000000000010');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000011'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000011' AND role_id = 'b0000000-0000-0000-0000-000000000011');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000011'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000011' AND role_id = 'c0000000-0000-0000-0000-000000000011');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000012', 'b0000000-0000-0000-0000-000000000012'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000012' AND role_id = 'b0000000-0000-0000-0000-000000000012');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000012'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000012' AND role_id = 'c0000000-0000-0000-0000-000000000012');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000013', 'b0000000-0000-0000-0000-000000000013'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000013' AND role_id = 'b0000000-0000-0000-0000-000000000013');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000013'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000013' AND role_id = 'c0000000-0000-0000-0000-000000000013');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000014', 'b0000000-0000-0000-0000-000000000014'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000014' AND role_id = 'b0000000-0000-0000-0000-000000000014');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000014'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000014' AND role_id = 'c0000000-0000-0000-0000-000000000014');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000015', 'b0000000-0000-0000-0000-000000000015'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000015' AND role_id = 'b0000000-0000-0000-0000-000000000015');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000015', 'c0000000-0000-0000-0000-000000000015'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000015' AND role_id = 'c0000000-0000-0000-0000-000000000015');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000016', 'b0000000-0000-0000-0000-000000000016'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000016' AND role_id = 'b0000000-0000-0000-0000-000000000016');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000016', 'c0000000-0000-0000-0000-000000000016'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000016' AND role_id = 'c0000000-0000-0000-0000-000000000016');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000017', 'b0000000-0000-0000-0000-000000000017'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000017' AND role_id = 'b0000000-0000-0000-0000-000000000017');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000017', 'c0000000-0000-0000-0000-000000000017'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000017' AND role_id = 'c0000000-0000-0000-0000-000000000017');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000018', 'b0000000-0000-0000-0000-000000000018'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000018' AND role_id = 'b0000000-0000-0000-0000-000000000018');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000018', 'c0000000-0000-0000-0000-000000000018'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000018' AND role_id = 'c0000000-0000-0000-0000-000000000018');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000019', 'b0000000-0000-0000-0000-000000000019'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000019' AND role_id = 'b0000000-0000-0000-0000-000000000019');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000019', 'c0000000-0000-0000-0000-000000000019'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000019' AND role_id = 'c0000000-0000-0000-0000-000000000019');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000001a', 'b0000000-0000-0000-0000-00000000001a'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000001a' AND role_id = 'b0000000-0000-0000-0000-00000000001a');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000001a', 'c0000000-0000-0000-0000-00000000001a'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000001a' AND role_id = 'c0000000-0000-0000-0000-00000000001a');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000001b', 'b0000000-0000-0000-0000-00000000001b'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000001b' AND role_id = 'b0000000-0000-0000-0000-00000000001b');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000001b', 'c0000000-0000-0000-0000-00000000001b'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000001b' AND role_id = 'c0000000-0000-0000-0000-00000000001b');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000001c', 'b0000000-0000-0000-0000-00000000001c'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000001c' AND role_id = 'b0000000-0000-0000-0000-00000000001c');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000001c', 'c0000000-0000-0000-0000-00000000001c'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000001c' AND role_id = 'c0000000-0000-0000-0000-00000000001c');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000001d', 'b0000000-0000-0000-0000-00000000001d'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000001d' AND role_id = 'b0000000-0000-0000-0000-00000000001d');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000001d', 'c0000000-0000-0000-0000-00000000001d'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000001d' AND role_id = 'c0000000-0000-0000-0000-00000000001d');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000001e', 'b0000000-0000-0000-0000-00000000001e'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000001e' AND role_id = 'b0000000-0000-0000-0000-00000000001e');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000001e', 'c0000000-0000-0000-0000-00000000001e'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000001e' AND role_id = 'c0000000-0000-0000-0000-00000000001e');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000001f', 'b0000000-0000-0000-0000-00000000001f'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000001f' AND role_id = 'b0000000-0000-0000-0000-00000000001f');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000001f', 'c0000000-0000-0000-0000-00000000001f'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000001f' AND role_id = 'c0000000-0000-0000-0000-00000000001f');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000020', 'b0000000-0000-0000-0000-000000000020'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000020' AND role_id = 'b0000000-0000-0000-0000-000000000020');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000020', 'c0000000-0000-0000-0000-000000000020'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000020' AND role_id = 'c0000000-0000-0000-0000-000000000020');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000021', 'b0000000-0000-0000-0000-000000000021'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000021' AND role_id = 'b0000000-0000-0000-0000-000000000021');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000021', 'c0000000-0000-0000-0000-000000000021'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000021' AND role_id = 'c0000000-0000-0000-0000-000000000021');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000022', 'b0000000-0000-0000-0000-000000000022'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000022' AND role_id = 'b0000000-0000-0000-0000-000000000022');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000022', 'c0000000-0000-0000-0000-000000000022'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000022' AND role_id = 'c0000000-0000-0000-0000-000000000022');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000023', 'b0000000-0000-0000-0000-000000000023'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000023' AND role_id = 'b0000000-0000-0000-0000-000000000023');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000023', 'c0000000-0000-0000-0000-000000000023'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000023' AND role_id = 'c0000000-0000-0000-0000-000000000023');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000024', 'b0000000-0000-0000-0000-000000000024'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000024' AND role_id = 'b0000000-0000-0000-0000-000000000024');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000024', 'c0000000-0000-0000-0000-000000000024'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000024' AND role_id = 'c0000000-0000-0000-0000-000000000024');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000025', 'b0000000-0000-0000-0000-000000000025'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000025' AND role_id = 'b0000000-0000-0000-0000-000000000025');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000025', 'c0000000-0000-0000-0000-000000000025'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000025' AND role_id = 'c0000000-0000-0000-0000-000000000025');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000026', 'b0000000-0000-0000-0000-000000000026'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000026' AND role_id = 'b0000000-0000-0000-0000-000000000026');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000026', 'c0000000-0000-0000-0000-000000000026'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000026' AND role_id = 'c0000000-0000-0000-0000-000000000026');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000027', 'b0000000-0000-0000-0000-000000000027'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000027' AND role_id = 'b0000000-0000-0000-0000-000000000027');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000027', 'c0000000-0000-0000-0000-000000000027'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000027' AND role_id = 'c0000000-0000-0000-0000-000000000027');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000028', 'b0000000-0000-0000-0000-000000000028'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000028' AND role_id = 'b0000000-0000-0000-0000-000000000028');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000028', 'c0000000-0000-0000-0000-000000000028'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000028' AND role_id = 'c0000000-0000-0000-0000-000000000028');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000029', 'b0000000-0000-0000-0000-000000000029'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000029' AND role_id = 'b0000000-0000-0000-0000-000000000029');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000029', 'c0000000-0000-0000-0000-000000000029'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000029' AND role_id = 'c0000000-0000-0000-0000-000000000029');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000002a', 'b0000000-0000-0000-0000-00000000002a'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000002a' AND role_id = 'b0000000-0000-0000-0000-00000000002a');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000002a', 'c0000000-0000-0000-0000-00000000002a'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000002a' AND role_id = 'c0000000-0000-0000-0000-00000000002a');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000002b', 'b0000000-0000-0000-0000-00000000002b'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000002b' AND role_id = 'b0000000-0000-0000-0000-00000000002b');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000002b', 'c0000000-0000-0000-0000-00000000002b'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000002b' AND role_id = 'c0000000-0000-0000-0000-00000000002b');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000002c', 'b0000000-0000-0000-0000-00000000002c'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000002c' AND role_id = 'b0000000-0000-0000-0000-00000000002c');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000002c', 'c0000000-0000-0000-0000-00000000002c'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000002c' AND role_id = 'c0000000-0000-0000-0000-00000000002c');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000002d', 'b0000000-0000-0000-0000-00000000002d'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000002d' AND role_id = 'b0000000-0000-0000-0000-00000000002d');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000002d', 'c0000000-0000-0000-0000-00000000002d'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000002d' AND role_id = 'c0000000-0000-0000-0000-00000000002d');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000002e', 'b0000000-0000-0000-0000-00000000002e'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000002e' AND role_id = 'b0000000-0000-0000-0000-00000000002e');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000002e', 'c0000000-0000-0000-0000-00000000002e'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000002e' AND role_id = 'c0000000-0000-0000-0000-00000000002e');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-00000000002f', 'b0000000-0000-0000-0000-00000000002f'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-00000000002f' AND role_id = 'b0000000-0000-0000-0000-00000000002f');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-00000000002f', 'c0000000-0000-0000-0000-00000000002f'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-00000000002f' AND role_id = 'c0000000-0000-0000-0000-00000000002f');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000030', 'b0000000-0000-0000-0000-000000000030'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000030' AND role_id = 'b0000000-0000-0000-0000-000000000030');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000030', 'c0000000-0000-0000-0000-000000000030'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000030' AND role_id = 'c0000000-0000-0000-0000-000000000030');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000031', 'b0000000-0000-0000-0000-000000000031'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000031' AND role_id = 'b0000000-0000-0000-0000-000000000031');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000031', 'c0000000-0000-0000-0000-000000000031'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000031' AND role_id = 'c0000000-0000-0000-0000-000000000031');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'd0000000-0000-0000-0000-000000000032', 'b0000000-0000-0000-0000-000000000032'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'd0000000-0000-0000-0000-000000000032' AND role_id = 'b0000000-0000-0000-0000-000000000032');
INSERT INTO membership_roles (membership_id, role_id)
SELECT 'e0000000-0000-0000-0000-000000000032', 'c0000000-0000-0000-0000-000000000032'
WHERE NOT EXISTS (SELECT 1 FROM membership_roles WHERE membership_id = 'e0000000-0000-0000-0000-000000000032' AND role_id = 'c0000000-0000-0000-0000-000000000032');

-- ════════════════════════════════════════════════════════════════════════════
