-- ============================================================
-- data-local.sql — Seed mínimo para perfil LOCAL (H2 file-based)
-- ============================================================
-- Idempotente: usa INSERT ... ON CONFLICT DO NOTHING.
-- Corre en cada arranque; no duplica datos si el archivo ya existe.
--
-- Credenciales dev (¡NUNCA usar en producción!):
--   Tenant:  keygo
--   App:     keygo-ui  (client_id)  — PUBLIC/PKCE, sin secret
--   Usuario: keygo_admin / admin@keygo.local / Admin1234!
-- ============================================================

-- ─── 1. Tenant ───────────────────────────────────────────────
INSERT INTO tenants (id, slug, name, owner_email, status, created_at, updated_at)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'keygo',
    'KeyGo',
    'admin@keygo.local',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (slug) DO NOTHING;

-- ─── 2. Client App (PUBLIC → PKCE, sin hashed_secret) ────────
INSERT INTO client_apps (id, tenant_id, client_id, name, description, type, hashed_secret, status, created_at, updated_at)
VALUES (
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'keygo-ui',
    'KeyGo UI',
    'Frontend local de desarrollo',
    'PUBLIC',
    NULL,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (client_id) DO NOTHING;

-- ─── 3. Redirect URI ─────────────────────────────────────────
INSERT INTO client_redirect_uris (id, client_app_id, uri, created_at)
VALUES (
    'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'http://localhost:5173/callback',
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- ─── 4. Grants permitidos ────────────────────────────────────
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
VALUES ('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'AUTHORIZATION_CODE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
VALUES ('f3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'REFRESH_TOKEN')
ON CONFLICT (id) DO NOTHING;

-- ─── 5. Scopes permitidos ────────────────────────────────────
INSERT INTO client_allowed_scopes (id, client_app_id, scope)
VALUES ('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'openid')
ON CONFLICT (id) DO NOTHING;

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
VALUES ('f6eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'profile')
ON CONFLICT (id) DO NOTHING;

INSERT INTO client_allowed_scopes (id, client_app_id, scope)
VALUES ('f7eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'email')
ON CONFLICT (id) DO NOTHING;

-- ─── 6. Usuario admin ────────────────────────────────────────
-- Contraseña: Admin1234!  (BCrypt cost=10)
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status, created_at, updated_at)
VALUES (
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'keygo_admin',
    'admin@keygo.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
    'KeyGo',
    'Admin',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- ─── 7. Rol admin en keygo-ui ────────────────────────────────
INSERT INTO app_roles (id, client_app_id, code, display_name, description, created_at, updated_at)
VALUES (
    'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin',
    'Administrador',
    'Rol de administrador local',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- ─── 8. Membresía: keygo_admin → keygo-ui ───────────────────
INSERT INTO memberships (id, user_id, client_app_id, status, created_at, updated_at)
VALUES (
    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- ─── 9. Rol asignado a la membresía ─────────────────────────
INSERT INTO membership_roles (membership_id, role_id)
VALUES (
    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
) ON CONFLICT (membership_id, role_id) DO NOTHING;

