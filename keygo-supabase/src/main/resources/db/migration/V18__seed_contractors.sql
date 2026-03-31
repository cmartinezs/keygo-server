-- =============================================================================
-- V18: Seed — Contractors del tenant keygo (modelo billing v2)
--
-- Crea el conjunto de datos minimo para demostrar el modelo de contractors:
--
-- 1. Usuario contratante (TenantUser en el tenant keygo):
--    keygo_contractor / contractor@keygo.local (contrasena: Admin1234!)
--    Representa a una empresa ficticia "Acme Corp" que contrata el plan PERSONAL.
--
-- 2. Registro en contractors:
--    Vinculado al TenantUser anterior, status=ACTIVE.
--
-- 3. Contrato (app_contracts) ACTIVE:
--    Plan PERSONAL v1.0, MONTHLY, contractor_id resuelto por subquery.
--
-- 4. Suscripcion activa (app_subscriptions):
--    Plan PERSONAL v1.0, status=ACTIVE, vinculada al contrato.
--
-- 5. Tenant "acme" como tenant creado por el contractor (contractor_id NOT NULL).
--    Representa el primer tenant que Acme Corp creo despues de contratar.
--
-- 6. Actualizacion del tenant "demo":
--    Vincula el tenant "demo" al contractor "keygo_contractor"
--    para ilustrar la relacion (demo fue creado por este contratante).
--
-- PKs estables (para estabilidad entre resets):
--   TenantUser:  keygo_contractor=11111111-1111-1111-1111-000000000010
--   Contractor:  acme_contractor=88888888-8888-8888-8888-000000000001
--   Contract:    acme_contract=99999999-9999-9999-9999-000000000001
--   Subscription:acme_sub=99999999-9999-9999-9999-000000000002
--   Tenant acme: aaaaaaaa-aaaa-aaaa-aaaa-000000000001
-- =============================================================================
-- ---------------------------------------------------------------------------
-- 1) TenantUser contratante en el tenant keygo
--    FK tenant_id → tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO tenant_users (id, tenant_id, username, email, password_hash, first_name, last_name, status)
VALUES (
  '11111111-1111-1111-1111-000000000010',
  (SELECT id FROM tenants WHERE slug = 'keygo'),
  'keygo_contractor',
  'contractor@keygo.local',
  '$2a$10$S9xpydnQYvODm7wulFBkd.EnJTyaIfiRLZCpp4FCOIN1N4.mzXIFm',  -- Admin1234!
  'Ana', 'Acme', 'ACTIVE'
)
ON CONFLICT (tenant_id, username) DO UPDATE
SET
  email         = EXCLUDED.email,
  password_hash = EXCLUDED.password_hash,
  first_name    = EXCLUDED.first_name,
  last_name     = EXCLUDED.last_name,
  status        = EXCLUDED.status,
  updated_at    = CURRENT_TIMESTAMP;
-- Membership del contractor en keygo-ui con rol user_tenant
-- (acceso basico a la plataforma para gestionar sus tenants)
INSERT INTO memberships (id, user_id, client_app_id, status)
SELECT
  '11111111-1111-1111-1111-400000000010',
  (SELECT id FROM tenant_users WHERE username = 'keygo_contractor'
    AND tenant_id = (SELECT id FROM tenants WHERE slug = 'keygo')),
  (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
  'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM memberships m
  WHERE m.user_id = (SELECT id FROM tenant_users WHERE username = 'keygo_contractor'
    AND tenant_id = (SELECT id FROM tenants WHERE slug = 'keygo'))
    AND m.client_app_id = (SELECT id FROM client_apps WHERE client_id = 'keygo-ui')
);
INSERT INTO membership_roles (membership_id, role_id, assigned_at)
SELECT DISTINCT m.id, ar.id, NOW()
FROM memberships m
JOIN tenant_users tu ON tu.id = m.user_id
JOIN tenants t ON t.id = tu.tenant_id
JOIN client_apps ca ON ca.id = m.client_app_id
JOIN app_roles ar ON ar.client_app_id = ca.id AND ar.code = 'user_tenant'
WHERE t.slug = 'keygo'
  AND tu.username = 'keygo_contractor'
  AND ca.client_id = 'keygo-ui'
  AND NOT EXISTS (
    SELECT 1 FROM membership_roles mr
    WHERE mr.membership_id = m.id AND mr.role_id = ar.id
  );
-- ---------------------------------------------------------------------------
-- 2) Registro en contractors
--    FK tenant_user_id → tenant_users.username + tenants.slug (subquery)
-- ---------------------------------------------------------------------------
INSERT INTO contractors (id, tenant_user_id, status)
VALUES (
  '88888888-8888-8888-8888-000000000001',
  (SELECT tu.id FROM tenant_users tu
   JOIN tenants t ON t.id = tu.tenant_id
   WHERE t.slug = 'keygo' AND tu.username = 'keygo_contractor'),
  'ACTIVE'
)
ON CONFLICT (tenant_user_id) DO UPDATE
SET
  status     = EXCLUDED.status,
  updated_at = CURRENT_TIMESTAMP;
-- ---------------------------------------------------------------------------
-- 3) Contrato ACTIVE para el contractor
--    FK client_app_id       → client_apps.client_id (subquery)
--    FK contractor_id       → contractors.tenant_user_id (subquery encadenada)
--    FK selected_plan_ver.  → UUID estable de PERSONAL v1.0
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
  expires_at
)
VALUES (
  '99999999-9999-9999-9999-000000000001',
  (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
  '55555555-5555-5555-5555-100000000004',  -- PERSONAL v1.0 (PK estable de V17)
  '88888888-8888-8888-8888-000000000001',  -- contractor acme
  'MONTHLY',
  'ACTIVE',
  'contractor@keygo.local',
  'Ana',
  'Acme',
  'Acme Corp S.A. de C.V.',
  'ACM260330ABC',
  '2026-03-30 10:00:00+00',
  '2026-03-30 10:05:00+00',
  '2027-03-30 00:00:00+00'
)
ON CONFLICT (id) DO UPDATE
SET
  status              = EXCLUDED.status,
  payment_verified_at = EXCLUDED.payment_verified_at,
  updated_at          = CURRENT_TIMESTAMP;
-- ---------------------------------------------------------------------------
-- 4) Suscripcion ACTIVE (periodo 2026-03-30 → 2026-04-30)
--    FK client_app_id   → client_apps.client_id (subquery)
--    FK app_plan_ver.   → UUID estable PERSONAL v1.0
--    FK contract_id     → UUID estable del contrato acme
--    FK contractor_id   → UUID estable del contractor acme
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
  next_billing_at
)
VALUES (
  '99999999-9999-9999-9999-000000000002',
  (SELECT id FROM client_apps WHERE client_id = 'keygo-ui'),
  '55555555-5555-5555-5555-100000000004',  -- PERSONAL v1.0
  '99999999-9999-9999-9999-000000000001',  -- contrato acme
  '88888888-8888-8888-8888-000000000001',  -- contractor acme
  'ACTIVE',
  '2026-03-30 00:00:00+00',
  '2026-04-30 00:00:00+00',
  FALSE,
  TRUE,
  '2026-04-30 00:00:00+00'
)
ON CONFLICT (client_app_id, contractor_id) DO UPDATE
SET
  app_plan_version_id  = EXCLUDED.app_plan_version_id,
  status               = EXCLUDED.status,
  current_period_start = EXCLUDED.current_period_start,
  current_period_end   = EXCLUDED.current_period_end,
  next_billing_at      = EXCLUDED.next_billing_at,
  updated_at           = CURRENT_TIMESTAMP;
-- ---------------------------------------------------------------------------
-- 5) Tenant "acme" — creado por el contractor keygo_contractor
--    contractor_id → UUID estable del contractor acme
-- ---------------------------------------------------------------------------
INSERT INTO tenants (id, slug, name, owner_email, status, contractor_id)
VALUES (
  'aaaaaaaa-aaaa-aaaa-aaaa-000000000001',
  'acme',
  'Acme Corp',
  'contractor@keygo.local',
  'ACTIVE',
  '88888888-8888-8888-8888-000000000001'  -- contractor acme
)
ON CONFLICT (slug) DO UPDATE
SET
  name          = EXCLUDED.name,
  owner_email   = EXCLUDED.owner_email,
  status        = EXCLUDED.status,
  contractor_id = EXCLUDED.contractor_id,
  updated_at    = CURRENT_TIMESTAMP;
-- ---------------------------------------------------------------------------
-- 6) Actualizar el tenant "demo" para vincularlo al contractor acme
--    (el tenant demo representa otro workspace que creo Acme Corp)
-- ---------------------------------------------------------------------------
UPDATE tenants
SET
  contractor_id = '88888888-8888-8888-8888-000000000001',
  updated_at    = CURRENT_TIMESTAMP
WHERE slug = 'demo'
  AND contractor_id IS NULL;
