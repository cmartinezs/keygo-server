-- V28 — Restricted tenant: no self-registration allowed in any app
-- Purpose: verify that /api/v1/tenants/public EXCLUDES tenants where ALL apps
--          have registration_policy IN (INVITE_ONLY, OPEN_NO_MEMBERSHIP).
--
-- Tenant: "Restricted Corp" (slug: restricted)
--   ├─ restricted-web   → INVITE_ONLY     (access by invitation only)
--   ├─ restricted-api   → INVITE_ONLY     (machine-to-machine, invite-only)
--   └─ restricted-admin → OPEN_NO_MEMBERSHIP (admin console, no membership created)
--
-- Expected behaviour after fix:
--   GET /api/v1/tenants/public         → does NOT include "restricted"
--   GET /api/v1/tenants/restricted/apps/public → returns empty list

-- ─── Tenant ───────────────────────────────────────────────────────────────────
INSERT INTO tenants (id, slug, name, status, created_at, updated_at)
VALUES
    ('13000000-0000-0000-0000-000000000006', 'restricted', 'Restricted Corp', 'ACTIVE',
     NOW(), NOW());

-- ─── Tenant roles ─────────────────────────────────────────────────────────────
INSERT INTO tenant_roles (id, tenant_id, code, display_name, description)
VALUES
    ('16000000-0000-0000-0000-000000000012', '13000000-0000-0000-0000-000000000006', 'owner', 'Owner', 'Restricted tenant owner'),
    ('16000000-0000-0000-0000-000000000013', '13000000-0000-0000-0000-000000000006', 'admin', 'Admin', 'Restricted tenant administrators'),
    ('16000000-0000-0000-0000-000000000014', '13000000-0000-0000-0000-000000000006', 'user',  'User',  'Restricted tenant regular users');

-- ─── Client apps ──────────────────────────────────────────────────────────────
INSERT INTO client_apps
    (id, tenant_id, client_id, name, description, type, hashed_secret, status, is_internal, registration_policy, created_at, updated_at)
VALUES
    ('17000000-0000-0000-0000-000000000015',
     '13000000-0000-0000-0000-000000000006',
     'restricted-web',
     'Restricted Web Portal',
     'Main web portal — access by invitation only',
     'PUBLIC', NULL, 'ACTIVE', FALSE, 'INVITE_ONLY',
     NOW(), NOW()),

    ('17000000-0000-0000-0000-000000000016',
     '13000000-0000-0000-0000-000000000006',
     'restricted-api',
     'Restricted API',
     'Machine-to-machine API — invite-only clients',
     'CONFIDENTIAL', '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm', 'ACTIVE', FALSE, 'INVITE_ONLY',
     NOW(), NOW()),

    ('17000000-0000-0000-0000-000000000017',
     '13000000-0000-0000-0000-000000000006',
     'restricted-admin',
     'Restricted Admin Console',
     'Internal admin console — no membership created on registration',
     'PUBLIC', NULL, 'ACTIVE', TRUE, 'OPEN_NO_MEMBERSHIP',
     NOW(), NOW());

-- ─── Redirect URIs ────────────────────────────────────────────────────────────
INSERT INTO client_redirect_uris (client_app_id, uri)
VALUES
    ('17000000-0000-0000-0000-000000000015', 'http://localhost:3010/callback'),
    ('17000000-0000-0000-0000-000000000017', 'http://localhost:3011/callback');

-- ─── Allowed grants ───────────────────────────────────────────────────────────
INSERT INTO client_allowed_grants (client_app_id, grant_type)
VALUES
    ('17000000-0000-0000-0000-000000000015', 'authorization_code'),
    ('17000000-0000-0000-0000-000000000015', 'refresh_token'),
    ('17000000-0000-0000-0000-000000000016', 'client_credentials'),
    ('17000000-0000-0000-0000-000000000017', 'authorization_code'),
    ('17000000-0000-0000-0000-000000000017', 'refresh_token');

-- ─── Allowed scopes ───────────────────────────────────────────────────────────
INSERT INTO client_allowed_scopes (client_app_id, scope)
VALUES
    ('17000000-0000-0000-0000-000000000015', 'openid'),
    ('17000000-0000-0000-0000-000000000015', 'profile'),
    ('17000000-0000-0000-0000-000000000015', 'email'),
    ('17000000-0000-0000-0000-000000000016', 'openid'),
    ('17000000-0000-0000-0000-000000000016', 'profile'),
    ('17000000-0000-0000-0000-000000000017', 'openid'),
    ('17000000-0000-0000-0000-000000000017', 'profile'),
    ('17000000-0000-0000-0000-000000000017', 'email');

-- ─── App roles ────────────────────────────────────────────────────────────────
INSERT INTO app_roles (id, tenant_id, client_app_id, code, display_name, description)
VALUES
    ('18000000-0000-0000-0000-000000000026',
     '13000000-0000-0000-0000-000000000006',
     '17000000-0000-0000-0000-000000000015',
     'admin', 'Admin', 'Restricted Web administrators'),
    ('18000000-0000-0000-0000-000000000027',
     '13000000-0000-0000-0000-000000000006',
     '17000000-0000-0000-0000-000000000015',
     'user',  'User',  'Restricted Web regular users'),
    ('18000000-0000-0000-0000-000000000028',
     '13000000-0000-0000-0000-000000000006',
     '17000000-0000-0000-0000-000000000016',
     'service', 'Service', 'Restricted API service accounts'),
    ('18000000-0000-0000-0000-000000000029',
     '13000000-0000-0000-0000-000000000006',
     '17000000-0000-0000-0000-000000000017',
     'admin', 'Admin', 'Restricted Admin Console administrators'),
    ('18000000-0000-0000-0000-000000000030',
     '13000000-0000-0000-0000-000000000006',
     '17000000-0000-0000-0000-000000000017',
     'operator', 'Operator', 'Restricted Admin Console operators');
