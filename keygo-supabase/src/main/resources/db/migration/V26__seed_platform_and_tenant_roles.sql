-- =============================================================================
-- V26: Seeds — Platform Roles + Tenant Roles (datos de desarrollo)
--
-- Seed para entorno local/dev. NUNCA usar en producción sin revisión.
--
-- Seeds:
--   1) platform_roles: KEYGO_ADMIN, KEYGO_ACCOUNT_ADMIN, KEYGO_USER
--   2) platform_user_roles: keygo_admin → KEYGO_ADMIN
--                           keygo_tenant_admin → KEYGO_ACCOUNT_ADMIN
--                           keygo_contractor → KEYGO_USER
--   3) tenant_roles en keygo: KEYGO_ADMIN_INTERNAL, KEYGO_EDITOR, KEYGO_VIEWER
--   4) tenant_roles en demo: DEMO_ADMIN, DEMO_USER
--   5) tenant_user_roles: keygo_admin → KEYGO_ADMIN_INTERNAL
--                         demo_admin → DEMO_ADMIN
--
-- Convención: FKs resueltas por subquery sobre campos semánticos (slug, username).
-- PKs estables para idempotencia en resets de DB.
-- =============================================================================

-- ── 1) Platform Roles ────────────────────────────────────────────────────────
INSERT INTO platform_roles (id, code, name, description) VALUES
  ('aaaaaaaa-0000-0000-0000-000000000001', 'keygo_admin',
   'Keygo Administrator',
   'Full platform access: manage tenants, billing, platform config, and all system operations'),
  ('aaaaaaaa-0000-0000-0000-000000000002', 'keygo_account_admin',
   'Keygo Account Administrator',
   'Manage tenant onboarding, billing contracts, and account lifecycle'),
  ('aaaaaaaa-0000-0000-0000-000000000003', 'keygo_user',
   'Keygo Platform User',
   'Basic authenticated access to the Keygo platform (e.g. contractors, support staff)')
ON CONFLICT (code) DO UPDATE SET
  name        = EXCLUDED.name,
  description = EXCLUDED.description,
  updated_at  = CURRENT_TIMESTAMP;

-- ── 2) Platform User Role Assignments ────────────────────────────────────────
-- keygo_admin (TenantUser en keygo) → KEYGO_ADMIN
INSERT INTO platform_user_roles (id, tenant_user_id, platform_role_id, assigned_at)
VALUES (
  'aaaaaaaa-0001-0000-0000-000000000001',
  (SELECT tu.id FROM tenant_users tu
   JOIN tenants t ON t.id = tu.tenant_id
   WHERE t.slug = 'keygo' AND tu.username = 'keygo_admin'),
  (SELECT id FROM platform_roles WHERE code = 'keygo_admin'),
  now()
)
ON CONFLICT (tenant_user_id, platform_role_id) DO NOTHING;

-- keygo_tenant_admin → KEYGO_ACCOUNT_ADMIN
INSERT INTO platform_user_roles (id, tenant_user_id, platform_role_id, assigned_at)
VALUES (
  'aaaaaaaa-0001-0000-0000-000000000002',
  (SELECT tu.id FROM tenant_users tu
   JOIN tenants t ON t.id = tu.tenant_id
   WHERE t.slug = 'keygo' AND tu.username = 'keygo_tenant_admin'),
  (SELECT id FROM platform_roles WHERE code = 'keygo_account_admin'),
  now()
)
ON CONFLICT (tenant_user_id, platform_role_id) DO NOTHING;

-- keygo_contractor → KEYGO_USER
INSERT INTO platform_user_roles (id, tenant_user_id, platform_role_id, assigned_at)
VALUES (
  'aaaaaaaa-0001-0000-0000-000000000003',
  (SELECT tu.id FROM tenant_users tu
   JOIN tenants t ON t.id = tu.tenant_id
   WHERE t.slug = 'keygo' AND tu.username = 'keygo_contractor'),
  (SELECT id FROM platform_roles WHERE code = 'keygo_user'),
  now()
)
ON CONFLICT (tenant_user_id, platform_role_id) DO NOTHING;

-- ── 3) Tenant Roles — tenant keygo ───────────────────────────────────────────
INSERT INTO tenant_roles (id, tenant_id, code, name, description, active) VALUES
  ('bbbbbbbb-0000-0000-0000-000000000001',
   (SELECT id FROM tenants WHERE slug = 'keygo'),
   'KEYGO_ADMIN_INTERNAL', 'KeyGo Internal Admin',
   'Full administrative access within the keygo tenant', true),
  ('bbbbbbbb-0000-0000-0000-000000000002',
   (SELECT id FROM tenants WHERE slug = 'keygo'),
   'KEYGO_EDITOR', 'KeyGo Editor',
   'Read and write access to keygo tenant resources', true),
  ('bbbbbbbb-0000-0000-0000-000000000003',
   (SELECT id FROM tenants WHERE slug = 'keygo'),
   'KEYGO_VIEWER', 'KeyGo Viewer',
   'Read-only access to keygo tenant resources', true)
ON CONFLICT (tenant_id, code) DO UPDATE SET
  name        = EXCLUDED.name,
  description = EXCLUDED.description,
  active      = EXCLUDED.active,
  updated_at  = CURRENT_TIMESTAMP;

-- ── 4) Tenant Roles — tenant demo ────────────────────────────────────────────
INSERT INTO tenant_roles (id, tenant_id, code, name, description, active) VALUES
  ('cccccccc-0000-0000-0000-000000000001',
   (SELECT id FROM tenants WHERE slug = 'demo'),
   'DEMO_ADMIN', 'Demo Admin',
   'Administrative access within the demo tenant', true),
  ('cccccccc-0000-0000-0000-000000000002',
   (SELECT id FROM tenants WHERE slug = 'demo'),
   'DEMO_USER', 'Demo User',
   'Regular user access within the demo tenant', true)
ON CONFLICT (tenant_id, code) DO UPDATE SET
  name        = EXCLUDED.name,
  description = EXCLUDED.description,
  active      = EXCLUDED.active,
  updated_at  = CURRENT_TIMESTAMP;

-- ── 5) Tenant User Role Assignments ──────────────────────────────────────────
-- keygo_admin → KEYGO_ADMIN_INTERNAL (en keygo)
INSERT INTO tenant_user_roles (id, tenant_user_id, tenant_role_id, assigned_at)
VALUES (
  'bbbbbbbb-0001-0000-0000-000000000001',
  (SELECT tu.id FROM tenant_users tu
   JOIN tenants t ON t.id = tu.tenant_id
   WHERE t.slug = 'keygo' AND tu.username = 'keygo_admin'),
  (SELECT tr.id FROM tenant_roles tr
   JOIN tenants t ON t.id = tr.tenant_id
   WHERE t.slug = 'keygo' AND tr.code = 'KEYGO_ADMIN_INTERNAL'),
  now()
)
ON CONFLICT DO NOTHING;

-- demo_admin → DEMO_ADMIN (en demo)
INSERT INTO tenant_user_roles (id, tenant_user_id, tenant_role_id, assigned_at)
VALUES (
  'cccccccc-0001-0000-0000-000000000001',
  (SELECT tu.id FROM tenant_users tu
   JOIN tenants t ON t.id = tu.tenant_id
   WHERE t.slug = 'demo' AND tu.username = 'demo_admin'),
  (SELECT tr.id FROM tenant_roles tr
   JOIN tenants t ON t.id = tr.tenant_id
   WHERE t.slug = 'demo' AND tr.code = 'DEMO_ADMIN'),
  now()
)
ON CONFLICT DO NOTHING;
