-- =============================================================================
-- V29: Seed platform_users + rename keygo_account_admin role
--
-- RFC: docs/rfc/restructure-multitenant — Phase F
--
-- Steps:
--   1) Rename platform role keygo_account_admin → keygo_tenant_admin
--   2) Insert platform_users (4 keygo users with stable UUIDs)
--   3) Assign platform_user_roles (using platform_user_id FK from V28)
--   4) Link tenant_users.platform_user_id → platform_users by matching email
--
-- Credentials (ONLY dev/local — NEVER use in production):
--   All 4 users: Admin1234!
--   BCrypt hash (cost=10): $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
--   (Same hash used in V16 seed_foundation and V18 seed_contractors for consistency)
--
-- PKs estables (platform_users):
--   keygo_admin          = 00000000-0000-4000-a000-000000000001
--   keygo_tenant_admin   = 00000000-0000-4000-a000-000000000002
--   keygo_user           = 00000000-0000-4000-a000-000000000003
--   keygo_contractor     = 00000000-0000-4000-a000-000000000004
--
-- Convención: FKs resueltas por subquery sobre campos semánticos (email, code, slug).
-- =============================================================================

-- ─── Step 1: Rename platform role keygo_account_admin → keygo_tenant_admin ────
UPDATE platform_roles
SET code = 'keygo_tenant_admin',
    name = 'KeyGo Tenant Admin',
    updated_at = now()
WHERE code = 'keygo_account_admin';

-- ─── Step 2: Insert platform_users ────────────────────────────────────────────
INSERT INTO platform_users (id, email, username, password_hash, first_name, last_name, status)
VALUES
  ('00000000-0000-4000-a000-000000000001', 'admin@keygo.local',      'keygo_admin',
   '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
   'KeyGo', 'Admin', 'ACTIVE'),

  ('00000000-0000-4000-a000-000000000002', 'tenant-admin@keygo.local', 'keygo_tenant_admin',
   '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
   'KeyGo', 'Tenant Admin', 'ACTIVE'),

  ('00000000-0000-4000-a000-000000000003', 'user@keygo.local',       'keygo_user',
   '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
   'KeyGo', 'User', 'ACTIVE'),

  ('00000000-0000-4000-a000-000000000004', 'contractor@keygo.local', 'keygo_contractor',
   '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
   'KeyGo', 'Contractor', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- ─── Step 3: Assign platform_user_roles ───────────────────────────────────────
-- V28 cleared platform_user_roles and changed FK to platform_users.id.
-- Re-seed all assignments using the new platform_user_id column.

-- 3a) All 4 users get KEYGO_USER base role
INSERT INTO platform_user_roles (platform_user_id, platform_role_id)
SELECT pu.id, pr.id
FROM platform_users pu
CROSS JOIN platform_roles pr
WHERE pr.code = 'keygo_user'
  AND pu.username IN ('keygo_admin', 'keygo_tenant_admin', 'keygo_user', 'keygo_contractor')
ON CONFLICT DO NOTHING;

-- 3b) keygo_admin gets KEYGO_ADMIN
INSERT INTO platform_user_roles (platform_user_id, platform_role_id)
SELECT pu.id, pr.id
FROM platform_users pu
CROSS JOIN platform_roles pr
WHERE pr.code = 'keygo_admin'
  AND pu.username = 'keygo_admin'
ON CONFLICT DO NOTHING;

-- 3c) keygo_tenant_admin and keygo_contractor get KEYGO_TENANT_ADMIN
INSERT INTO platform_user_roles (platform_user_id, platform_role_id)
SELECT pu.id, pr.id
FROM platform_users pu
CROSS JOIN platform_roles pr
WHERE pr.code = 'keygo_tenant_admin'
  AND pu.username IN ('keygo_tenant_admin', 'keygo_contractor')
ON CONFLICT DO NOTHING;

-- ─── Step 4: Link tenant_users.platform_user_id → platform_users ─────────────
-- Match by email within the keygo tenant only (seed users).
UPDATE tenant_users tu
SET platform_user_id = pu.id
FROM platform_users pu
WHERE tu.email = pu.email
  AND tu.tenant_id = (SELECT id FROM tenants WHERE slug = 'keygo')
  AND tu.platform_user_id IS NULL;
