-- V15: Reset seed user passwords to known values
--
-- Context:
--   V2 and V14 seeded users with a BCrypt hash whose plaintext was never documented.
--   This migration replaces those hashes with verified hashes for known dev credentials.
--
-- DO NOT use these credentials in production environments.
--
-- Seed credentials (dev/local ONLY):
-- ─────────────────────────────────────────────────────────────────
--  Table         | username             | email                    | password      | tenant  | role
-- ─────────────────────────────────────────────────────────────────
--  users (legacy)| admin                | admin@keygo.local        | Admin1234!    | –       | ADMIN (legacy)
--  tenant_users  | keygo_admin          | admin@keygo.local        | Admin1234!    | keygo   | admin
--  tenant_users  | keygo_tenant_admin   | tenant-admin@keygo.local | Admin1234!    | keygo   | admin_tenant
--  tenant_users  | keygo_user           | user@keygo.local         | Admin1234!    | keygo   | user_tenant
--  tenant_users  | demo_admin           | admin@demo.local         | DevAdmin1!    | demo    | demo_admin
--  tenant_users  | demo_user            | user@demo.local          | DevUser1!     | demo    | demo_user
-- ─────────────────────────────────────────────────────────────────
--
-- Hash generation (Spring BCryptPasswordEncoder, cost=10):
--   Admin1234! → $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
--   DevAdmin1! → $2a$10$VmQ.AQnJb11Ld9nqD9hCfurlSAO6wDIYPv12HXN/f2O6RXWANXr6G
--   DevUser1!  → $2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq

-- ---------------------------------------------------------------------------
-- 1) Legacy users table (V2 seed: admin@keygo.local)
-- ---------------------------------------------------------------------------
UPDATE users
SET
  password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
  updated_at    = CURRENT_TIMESTAMP
WHERE email = 'admin@keygo.local';

-- ---------------------------------------------------------------------------
-- 2) Tenant users — keygo tenant (all share Admin1234!)
-- ---------------------------------------------------------------------------
UPDATE tenant_users
SET
  password_hash = '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',
  updated_at    = CURRENT_TIMESTAMP
WHERE tenant_id = '11111111-1111-1111-1111-111111111111'
  AND id IN (
    '11111111-1111-1111-1111-000000000001',  -- keygo_admin
    '11111111-1111-1111-1111-000000000002',  -- keygo_tenant_admin
    '11111111-1111-1111-1111-000000000003'   -- keygo_user
  );

-- ---------------------------------------------------------------------------
-- 3) Tenant users — demo tenant
-- ---------------------------------------------------------------------------
-- demo_admin → DevAdmin1!
UPDATE tenant_users
SET
  password_hash = '$2a$10$VmQ.AQnJb11Ld9nqD9hCfurlSAO6wDIYPv12HXN/f2O6RXWANXr6G',
  updated_at    = CURRENT_TIMESTAMP
WHERE id = '22222222-2222-2222-2222-000000000001';  -- demo_admin

-- demo_user → DevUser1!
UPDATE tenant_users
SET
  password_hash = '$2a$10$aEfgQKzl/bJRGDK.ZkYX9uUywwiPZGjlugmdU9xdWZm/Jlf3qkcBq',
  updated_at    = CURRENT_TIMESTAMP
WHERE id = '22222222-2222-2222-2222-000000000002';  -- demo_user

