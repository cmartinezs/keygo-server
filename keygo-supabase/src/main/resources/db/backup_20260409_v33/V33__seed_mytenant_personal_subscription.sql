-- =============================================================================
-- V33: Seed — MyTenant con Plan Personal de Plataforma (5 períodos de facturación)
--
-- Crea un escenario de prueba completo con jerarquía completa de roles:
--
-- 1. PlatformUser "mytenant_owner" (contractor global de plataforma)
--    Email: mytenant@mycompany.local
--    Status: ACTIVE
--    Platform Roles: KEYGO_ACCOUNT_ADMIN + KEYGO_USER
--    PK estable: 22222222-2222-2222-2222-000000000001
--
-- 2. Contractor (1:1 con PlatformUser anterior)
--    Status: ACTIVE
--    PK estable: 22222222-2222-2222-2222-100000000001
--
-- 3. Contrato de Plataforma (app_contracts)
--    client_app_id = NULL (plan de plataforma)
--    Plan: Personal v1.0
--    Status: ACTIVE
--    Período: 2026-04-01 → 2026-08-31 (5 meses)
--    PK estable: 22222222-2222-2222-2222-200000000001
--
-- 4. Suscripción de Plataforma (app_subscriptions)
--    client_app_id = NULL (suscripción de plataforma)
--    Status: ACTIVE
--    Período actual: 2026-04-01 → 2026-05-01 (primer mes)
--    PK estable: 22222222-2222-2222-2222-300000000001
--
-- 5. Tenant "MyTenant"
--    Slug: mytenant
--    Status: ACTIVE
--    Vinculado al contractor anterior
--    Tenant Roles: MY_ADMIN, MY_USER
--    PK estable: 22222222-2222-2222-2222-400000000001
--
-- 6. ClientApp "TenApp"
--    Client ID: tenapp
--    Tenant: MyTenant
--    Status: ACTIVE
--    App Roles: admin-app, user-app
--    PK estable: 22222222-2222-2222-2222-500000000001
--
-- 7. 3 TenantUsers en MyTenant (con Tenant Roles + App Memberships)
--    alice_smith / alice@mytenant.local (ACTIVE) → MY_ADMIN + admin-app
--    bob_jones / bob@mytenant.local (ACTIVE) → MY_USER + user-app
--    carol_white / carol@mytenant.local (ACTIVE) → MY_USER + user-app
--    Contraseña común: Admin1234! (bcrypt)
--    Platform Role (todos): KEYGO_USER
--
-- 8. 5 Invoices (facturas)
--    Períodos: 2026-04, 2026-05, 2026-06, 2026-07, 2026-08
--    Status: ISSUED
--    Total: USD 99.00 + 0 (sin tax en seed)
--    Vinculadas a la suscripción de plataforma
--
-- PKs estables para reproducibilidad:
--   PlatformUser: 22222222-2222-2222-2222-000000000001
--   Contractor:   22222222-2222-2222-2222-100000000001
--   Contract:     22222222-2222-2222-2222-200000000001
--   Subscription: 22222222-2222-2222-2222-300000000001
--   Tenant:       22222222-2222-2222-2222-400000000001
--   Tenant Roles: 22222222-2222-2222-2222-411111111/411111112
--   ClientApp:    22222222-2222-2222-2222-500000000001
--   App Roles:    22222222-2222-2222-2222-511111111/511111112
--   TenantUsers:  22222222-2222-2222-2222-600000000001/002/003
--   Memberships:  22222222-2222-2222-2222-610000000001/002/003
--   Invoices:     22222222-2222-2222-2222-700000000001..005
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1) PlatformUser "mytenant_owner" (contractor de plataforma)
--    Email: mytenant@mycompany.local
--    Contraseña: Admin1234! (bcrypt hash)
-- ---------------------------------------------------------------------------
INSERT INTO platform_users (
  id,
  email,
  username,
  password_hash,
  first_name,
  last_name,
  status,
  created_at,
  updated_at
)
VALUES (
  '22222222-2222-2222-2222-000000000001',
  'mytenant@mycompany.local',
  'mytenant_owner',
  '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
  'MyTenant',
  'Owner',
  'ACTIVE',
  '2026-04-01 10:00:00+00',
  '2026-04-01 10:00:00+00'
)
ON CONFLICT (email) DO UPDATE
SET
  username      = EXCLUDED.username,
  password_hash = EXCLUDED.password_hash,
  first_name    = EXCLUDED.first_name,
  last_name     = EXCLUDED.last_name,
  status        = EXCLUDED.status,
  updated_at    = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 2) Contractor (1:1 con PlatformUser)
--    FK platform_user_id → platform_users.email (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO contractors (
  id,
  platform_user_id,
  status,
  created_at,
  updated_at
)
VALUES (
  '22222222-2222-2222-2222-100000000001',
  (SELECT id FROM platform_users WHERE email = 'mytenant@mycompany.local'),
  'ACTIVE',
  '2026-04-01 10:00:00+00',
  '2026-04-01 10:00:00+00'
)
ON CONFLICT (platform_user_id) DO UPDATE
SET
  status     = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 3) Contrato de Plataforma (app_contracts)
--    client_app_id = NULL (plan de plataforma)
--    Plan: Personal v1.0 (PK estable de V17: 55555555-5555-5555-5555-100000000004)
--    FK contractor_id → contractors.platform_user_id (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO app_contracts (
  id,
  client_app_id,
  selected_plan_version_id,
  contractor_id,
  billing_period,
  status,
  contractor_email,
  contractor_first_name,
  contractor_last_name,
  company_name,
  company_tax_id,
  email_verified_at,
  payment_verified_at,
  expires_at,
  created_at,
  updated_at
)
VALUES (
  '22222222-2222-2222-2222-200000000001',
  NULL,  -- Plan de plataforma
  '55555555-5555-5555-5555-100000000004',  -- PERSONAL v1.0
  '22222222-2222-2222-2222-100000000001',  -- contractor
  'MONTHLY',
  'ACTIVE',
  'mytenant@mycompany.local',
  'MyTenant',
  'Owner',
  'MyTenant Company Inc.',
  'USA-TAX-ID-001',
  '2026-04-01 10:00:00+00',
  '2026-04-01 10:05:00+00',
  '2027-04-01 00:00:00+00',
  '2026-04-01 10:00:00+00',
  '2026-04-01 10:00:00+00'
)
ON CONFLICT (id) DO UPDATE
SET
  status              = EXCLUDED.status,
  payment_verified_at = EXCLUDED.payment_verified_at,
  updated_at          = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 4) Suscripción de Plataforma (app_subscriptions)
--    client_app_id = NULL
--    Período inicial: 2026-04-01 → 2026-05-01
--    FK contractor_id → contractors.platform_user_id (subquery)
--    FK contract_id → app_contracts.id (PK estable)
--    FK app_plan_version_id → app_plan_versions.id (PERSONAL v1.0)
-- ---------------------------------------------------------------------------
INSERT INTO app_subscriptions (
  id,
  client_app_id,
  app_plan_version_id,
  contract_id,
  contractor_id,
  status,
  current_period_start,
  current_period_end,
  cancel_at_period_end,
  auto_renew,
  next_billing_at,
  created_at,
  updated_at
)
VALUES (
  '22222222-2222-2222-2222-300000000001',
  NULL,  -- Suscripción de plataforma
  '55555555-5555-5555-5555-100000000004',  -- PERSONAL v1.0
  '22222222-2222-2222-2222-200000000001',  -- contrato
  '22222222-2222-2222-2222-100000000001',  -- contractor
  'ACTIVE',
  '2026-04-01 00:00:00+00',
  '2026-05-01 00:00:00+00',
  FALSE,
  TRUE,
  '2026-05-01 00:00:00+00',
  '2026-04-01 10:00:00+00',
  '2026-04-01 10:00:00+00'
)
ON CONFLICT (id) DO UPDATE
SET
  status               = EXCLUDED.status,
  current_period_start = EXCLUDED.current_period_start,
  current_period_end   = EXCLUDED.current_period_end,
  updated_at           = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 5) Tenant "MyTenant"
--    Slug: mytenant
--    Status: ACTIVE
--    FK contractor_id → contractors.platform_user_id (estable)
-- ---------------------------------------------------------------------------
INSERT INTO tenants (
  id,
  slug,
  name,
  owner_email,
  status,
  contractor_id,
  created_at,
  updated_at
)
VALUES (
  '22222222-2222-2222-2222-400000000001',
  'mytenant',
  'MyTenant',
  'mytenant@mycompany.local',
  'ACTIVE',
  '22222222-2222-2222-2222-100000000001',
  '2026-04-01 10:00:00+00',
  '2026-04-01 10:00:00+00'
)
ON CONFLICT (slug) DO UPDATE
SET
  name          = EXCLUDED.name,
  owner_email   = EXCLUDED.owner_email,
  status        = EXCLUDED.status,
  contractor_id = EXCLUDED.contractor_id,
  updated_at    = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 6) ClientApp "TenApp" en el tenant MyTenant
--    Client ID: tenapp
--    Status: ACTIVE
--    FK tenant_id → tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (
  id,
  tenant_id,
  client_id,
  client_secret,
  name,
  description,
  status,
  allow_insecure_http_for_dev,
  created_at,
  updated_at
)
VALUES (
  '22222222-2222-2222-2222-500000000001',
  (SELECT id FROM tenants WHERE slug = 'mytenant'),
  'tenapp',
  'secret-tenapp-dev-001',  -- Dev secret (never for production)
  'TenApp',
  'Test application for MyTenant',
  'ACTIVE',
  FALSE,
  '2026-04-01 10:00:00+00',
  '2026-04-01 10:00:00+00'
)
ON CONFLICT (tenant_id, client_id) DO UPDATE
SET
  name             = EXCLUDED.name,
  description      = EXCLUDED.description,
  status           = EXCLUDED.status,
  updated_at       = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 6b) Tenant Roles en MyTenant
--     MY_ADMIN: administrador del tenant
--     MY_USER: usuario regular del tenant
--     FK tenant_id → tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO tenant_roles (
  id,
  tenant_id,
  code,
  name,
  description,
  active,
  created_at,
  updated_at
)
VALUES
  (
    '22222222-2222-2222-2222-411111111',
    (SELECT id FROM tenants WHERE slug = 'mytenant'),
    'MY_ADMIN',
    'MyTenant Administrator',
    'Full administrative access within the MyTenant tenant',
    TRUE,
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  ),
  (
    '22222222-2222-2222-2222-411111112',
    (SELECT id FROM tenants WHERE slug = 'mytenant'),
    'MY_USER',
    'MyTenant User',
    'Regular user access within the MyTenant tenant',
    TRUE,
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  )
ON CONFLICT (tenant_id, code) DO UPDATE
SET
  name        = EXCLUDED.name,
  description = EXCLUDED.description,
  active      = EXCLUDED.active,
  updated_at  = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 6c) App Roles en TenApp
--     admin-app: administrador de la aplicación
--     user-app: usuario regular de la aplicación
--     FK client_app_id → client_apps.client_id (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO app_roles (
  id,
  client_app_id,
  code,
  display_name,
  description,
  created_at,
  updated_at
)
VALUES
  (
    '22222222-2222-2222-2222-511111111',
    (SELECT id FROM client_apps WHERE client_id = 'tenapp'),
    'admin-app',
    'TenApp Administrator',
    'Full administrative access within the TenApp application',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  ),
  (
    '22222222-2222-2222-2222-511111112',
    (SELECT id FROM client_apps WHERE client_id = 'tenapp'),
    'user-app',
    'TenApp User',
    'Regular user access within the TenApp application',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  )
ON CONFLICT (client_app_id, code) DO UPDATE
SET
  display_name = EXCLUDED.display_name,
  description  = EXCLUDED.description,
  updated_at   = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 7) 3 TenantUsers en MyTenant
--    alice_smith / alice@mytenant.local (ACTIVE)
--    bob_jones / bob@mytenant.local (ACTIVE)
--    carol_white / carol@mytenant.local (ACTIVE)
--    FK tenant_id → tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO tenant_users (
  id,
  tenant_id,
  username,
  email,
  password_hash,
  first_name,
  last_name,
  status,
  created_at,
  updated_at
)
VALUES
  (
    '22222222-2222-2222-2222-600000000001',
    (SELECT id FROM tenants WHERE slug = 'mytenant'),
    'alice_smith',
    'alice@mytenant.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'Alice',
    'Smith',
    'ACTIVE',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  ),
  (
    '22222222-2222-2222-2222-600000000002',
    (SELECT id FROM tenants WHERE slug = 'mytenant'),
    'bob_jones',
    'bob@mytenant.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'Bob',
    'Jones',
    'ACTIVE',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  ),
  (
    '22222222-2222-2222-2222-600000000003',
    (SELECT id FROM tenants WHERE slug = 'mytenant'),
    'carol_white',
    'carol@mytenant.local',
    '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
    'Carol',
    'White',
    'ACTIVE',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  )
ON CONFLICT (tenant_id, username) DO UPDATE
SET
  email     = EXCLUDED.email,
  first_name = EXCLUDED.first_name,
  last_name  = EXCLUDED.last_name,
  status    = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 7b) Platform User Roles para el contractor (mytenant_owner)
--     Roles: KEYGO_ACCOUNT_ADMIN + KEYGO_USER
--     FK platform_user_id → platform_users.email (subquery)
--     FK platform_role_id → platform_roles.code (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO platform_user_roles (
  platform_user_id,
  platform_role_id,
  assigned_at
)
VALUES
  (
    (SELECT id FROM platform_users WHERE email = 'mytenant@mycompany.local'),
    (SELECT id FROM platform_roles WHERE code = 'keygo_account_admin'),
    '2026-04-01 10:00:00+00'
  ),
  (
    (SELECT id FROM platform_users WHERE email = 'mytenant@mycompany.local'),
    (SELECT id FROM platform_roles WHERE code = 'keygo_user'),
    '2026-04-01 10:00:00+00'
  )
ON CONFLICT (platform_user_id, platform_role_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 7c) Tenant User Roles en MyTenant
--     alice_smith → MY_ADMIN
--     bob_jones → MY_USER
--     carol_white → MY_USER
--     FK tenant_user_id → tenant_users.username + tenants.slug (subquery)
--     FK tenant_role_id → tenant_roles.code + tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO tenant_user_roles (
  tenant_user_id,
  tenant_role_id,
  assigned_at
)
VALUES
  -- alice_smith → MY_ADMIN
  (
    (SELECT tu.id FROM tenant_users tu
     JOIN tenants t ON t.id = tu.tenant_id
     WHERE t.slug = 'mytenant' AND tu.username = 'alice_smith'),
    (SELECT tr.id FROM tenant_roles tr
     JOIN tenants t ON t.id = tr.tenant_id
     WHERE t.slug = 'mytenant' AND tr.code = 'MY_ADMIN'),
    '2026-04-01 10:00:00+00'
  ),
  -- bob_jones → MY_USER
  (
    (SELECT tu.id FROM tenant_users tu
     JOIN tenants t ON t.id = tu.tenant_id
     WHERE t.slug = 'mytenant' AND tu.username = 'bob_jones'),
    (SELECT tr.id FROM tenant_roles tr
     JOIN tenants t ON t.id = tr.tenant_id
     WHERE t.slug = 'mytenant' AND tr.code = 'MY_USER'),
    '2026-04-01 10:00:00+00'
  ),
  -- carol_white → MY_USER
  (
    (SELECT tu.id FROM tenant_users tu
     JOIN tenants t ON t.id = tu.tenant_id
     WHERE t.slug = 'mytenant' AND tu.username = 'carol_white'),
    (SELECT tr.id FROM tenant_roles tr
     JOIN tenants t ON t.id = tr.tenant_id
     WHERE t.slug = 'mytenant' AND tr.code = 'MY_USER'),
    '2026-04-01 10:00:00+00'
  )
ON CONFLICT (tenant_user_id, tenant_role_id) WHERE removed_at IS NULL DO NOTHING;

-- ---------------------------------------------------------------------------
-- 7d) Memberships en TenApp (vincular usuarios a la app)
--     alice_smith → ACTIVE
--     bob_jones → ACTIVE
--     carol_white → ACTIVE
--     FK user_id → tenant_users.username + tenants.slug (subquery)
--     FK client_app_id → client_apps.client_id (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO memberships (
  id,
  user_id,
  client_app_id,
  status,
  created_at,
  updated_at
)
VALUES
  (
    '22222222-2222-2222-2222-610000000001',
    (SELECT tu.id FROM tenant_users tu
     JOIN tenants t ON t.id = tu.tenant_id
     WHERE t.slug = 'mytenant' AND tu.username = 'alice_smith'),
    (SELECT id FROM client_apps WHERE client_id = 'tenapp'),
    'ACTIVE',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  ),
  (
    '22222222-2222-2222-2222-610000000002',
    (SELECT tu.id FROM tenant_users tu
     JOIN tenants t ON t.id = tu.tenant_id
     WHERE t.slug = 'mytenant' AND tu.username = 'bob_jones'),
    (SELECT id FROM client_apps WHERE client_id = 'tenapp'),
    'ACTIVE',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  ),
  (
    '22222222-2222-2222-2222-610000000003',
    (SELECT tu.id FROM tenant_users tu
     JOIN tenants t ON t.id = tu.tenant_id
     WHERE t.slug = 'mytenant' AND tu.username = 'carol_white'),
    (SELECT id FROM client_apps WHERE client_id = 'tenapp'),
    'ACTIVE',
    '2026-04-01 10:00:00+00',
    '2026-04-01 10:00:00+00'
  )
ON CONFLICT (user_id, client_app_id) DO UPDATE
SET
  status     = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- ---------------------------------------------------------------------------
-- 7e) Membership Roles (asignar app_roles a memberships)
--     alice_smith → admin-app
--     bob_jones → user-app
--     carol_white → user-app
--     FK membership_id → memberships (PK estables)
--     FK role_id → app_roles.code + client_apps.client_id (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO membership_roles (
  membership_id,
  role_id,
  assigned_at
)
VALUES
  -- alice_smith → admin-app
  (
    '22222222-2222-2222-2222-610000000001',
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'tenapp' AND ar.code = 'admin-app'),
    '2026-04-01 10:00:00+00'
  ),
  -- bob_jones → user-app
  (
    '22222222-2222-2222-2222-610000000002',
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'tenapp' AND ar.code = 'user-app'),
    '2026-04-01 10:00:00+00'
  ),
  -- carol_white → user-app
  (
    '22222222-2222-2222-2222-610000000003',
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'tenapp' AND ar.code = 'user-app'),
    '2026-04-01 10:00:00+00'
  )
ON CONFLICT (membership_id, role_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- 8) 5 Invoices (facturas) — períodos de facturación de plataforma
--    Períodos: 2026-04, 2026-05, 2026-06, 2026-07, 2026-08
--    Status: ISSUED
--    Total: USD 99.00 (plan Personal base)
--    FK subscription_id → app_subscriptions.id (estable)
-- ---------------------------------------------------------------------------
INSERT INTO invoices (
  id,
  subscription_id,
  invoice_number,
  status,
  issue_date,
  due_date,
  period_start,
  period_end,
  currency,
  subtotal,
  tax_amount,
  total,
  billing_name_snapshot,
  billing_tax_id_snapshot,
  billing_address_snapshot,
  plan_name_snapshot,
  plan_version_snapshot,
  created_at
)
VALUES
  -- Período 1: 2026-04
  (
    '22222222-2222-2222-2222-700000000001',
    '22222222-2222-2222-2222-300000000001',
    'INV-2026-04-0001',
    'ISSUED',
    '2026-04-01'::date,
    '2026-05-01'::date,
    '2026-04-01'::date,
    '2026-05-01'::date,
    'USD',
    99.00,
    0.00,
    99.00,
    'MyTenant Company Inc.',
    'USA-TAX-ID-001',
    'MyTenant, USA',
    'Personal',
    'v1.0',
    '2026-04-01 10:00:00+00'
  ),
  -- Período 2: 2026-05
  (
    '22222222-2222-2222-2222-700000000002',
    '22222222-2222-2222-2222-300000000001',
    'INV-2026-05-0001',
    'ISSUED',
    '2026-05-01'::date,
    '2026-06-01'::date,
    '2026-05-01'::date,
    '2026-06-01'::date,
    'USD',
    99.00,
    0.00,
    99.00,
    'MyTenant Company Inc.',
    'USA-TAX-ID-001',
    'MyTenant, USA',
    'Personal',
    'v1.0',
    '2026-05-01 10:00:00+00'
  ),
  -- Período 3: 2026-06
  (
    '22222222-2222-2222-2222-700000000003',
    '22222222-2222-2222-2222-300000000001',
    'INV-2026-06-0001',
    'ISSUED',
    '2026-06-01'::date,
    '2026-07-01'::date,
    '2026-06-01'::date,
    '2026-07-01'::date,
    'USD',
    99.00,
    0.00,
    99.00,
    'MyTenant Company Inc.',
    'USA-TAX-ID-001',
    'MyTenant, USA',
    'Personal',
    'v1.0',
    '2026-06-01 10:00:00+00'
  ),
  -- Período 4: 2026-07
  (
    '22222222-2222-2222-2222-700000000004',
    '22222222-2222-2222-2222-300000000001',
    'INV-2026-07-0001',
    'ISSUED',
    '2026-07-01'::date,
    '2026-08-01'::date,
    '2026-07-01'::date,
    '2026-08-01'::date,
    'USD',
    99.00,
    0.00,
    99.00,
    'MyTenant Company Inc.',
    'USA-TAX-ID-001',
    'MyTenant, USA',
    'Personal',
    'v1.0',
    '2026-07-01 10:00:00+00'
  ),
  -- Período 5: 2026-08
  (
    '22222222-2222-2222-2222-700000000005',
    '22222222-2222-2222-2222-300000000001',
    'INV-2026-08-0001',
    'ISSUED',
    '2026-08-01'::date,
    '2026-09-01'::date,
    '2026-08-01'::date,
    '2026-09-01'::date,
    'USD',
    99.00,
    0.00,
    99.00,
    'MyTenant Company Inc.',
    'USA-TAX-ID-001',
    'MyTenant, USA',
    'Personal',
    'v1.0',
    '2026-08-01 10:00:00+00'
  )
ON CONFLICT (id) DO UPDATE
SET
  status     = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;

-- =============================================================================
-- Confirmación
-- =============================================================================
-- Esta migración crea:
--   1. PlatformUser: mytenant_owner (22222222-2222-2222-2222-000000000001)
--      Email: mytenant@mycompany.local
--      Contraseña: Admin1234! (bcrypt)
--      Platform Roles: KEYGO_ACCOUNT_ADMIN + KEYGO_USER
--   2. Contractor: ACTIVE (22222222-2222-2222-2222-100000000001)
--   3. Platform Contract: ACTIVE, Personal plan (22222222-2222-2222-2222-200000000001)
--   4. Platform Subscription: ACTIVE (22222222-2222-2222-2222-300000000001)
--   5. Tenant: mytenant / MyTenant (22222222-2222-2222-2222-400000000001)
--      Tenant Roles: MY_ADMIN, MY_USER
--   6. ClientApp: tenapp / TenApp (22222222-2222-2222-2222-500000000001)
--      App Roles: admin-app, user-app
--   7. 3 TenantUsers con jerarquía completa de roles:
--      • alice_smith (alice@mytenant.local)
--        - Platform Role: KEYGO_USER
--        - Tenant Role: MY_ADMIN
--        - Membership: ACTIVE
--        - App Role: admin-app
--        - Contraseña: Admin1234! (bcrypt)
--      • bob_jones (bob@mytenant.local)
--        - Platform Role: KEYGO_USER
--        - Tenant Role: MY_USER
--        - Membership: ACTIVE
--        - App Role: user-app
--        - Contraseña: Admin1234! (bcrypt)
--      • carol_white (carol@mytenant.local)
--        - Platform Role: KEYGO_USER
--        - Tenant Role: MY_USER
--        - Membership: ACTIVE
--        - App Role: user-app
--        - Contraseña: Admin1234! (bcrypt)
--   8. 5 Invoices: ISSUED (Apr-Aug 2026)
--
-- Todos los PKs son estables para reproducibilidad en resets de base de datos.
-- Todas las contraseñas usan bcrypt hash: $2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm
-- (Plain: Admin1234!)
