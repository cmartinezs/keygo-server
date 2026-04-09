-- =============================================================================
-- V30: Billing Contractor → PlatformUser + Billing Unificado
--
-- Cambios principales:
--   1) contractors: tenant_user_id → platform_user_id (FK a platform_users)
--   2) app_plans: client_app_id nullable (NULL = plan de plataforma)
--   3) app_contracts: client_app_id nullable (NULL = contrato de plataforma)
--   4) app_subscriptions: client_app_id nullable (NULL = suscripción de plataforma)
--   5) Migración de datos: vincular contractor a platform_user, marcar planes
--      del seed keygo-ui como planes de plataforma
--   6) Asignar KEYGO_TENANT_ADMIN al contractor existente
--
-- Modelo:
--   client_app_id IS NULL → billing de plataforma (KeyGo)
--   client_app_id IS NOT NULL → billing de app (ClientApp de un tenant)
-- =============================================================================

-- ─── 1) contractors: agregar platform_user_id, migrar datos, eliminar tenant_user_id ───

-- 1a) Nueva columna
ALTER TABLE contractors
    ADD COLUMN platform_user_id UUID;

-- 1b) Migrar datos: buscar platform_user por email del tenant_user vinculado
UPDATE contractors c
SET platform_user_id = pu.id
FROM tenant_users tu
JOIN platform_users pu ON LOWER(pu.email) = LOWER(tu.email)
WHERE c.tenant_user_id = tu.id;

-- 1c) Hacer NOT NULL + UNIQUE + FK
ALTER TABLE contractors
    ALTER COLUMN platform_user_id SET NOT NULL;

ALTER TABLE contractors
    ADD CONSTRAINT uq_contractors_platform_user UNIQUE (platform_user_id);

ALTER TABLE contractors
    ADD CONSTRAINT fk_contractors_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE RESTRICT;

-- 1d) Eliminar FK y columna tenant_user_id
ALTER TABLE contractors
    DROP CONSTRAINT contractors_tenant_user_id_fkey,
    DROP CONSTRAINT contractors_tenant_user_id_key,
    DROP COLUMN tenant_user_id;

-- 1e) Índice
CREATE INDEX idx_contractors_platform_user ON contractors(platform_user_id);

COMMENT ON COLUMN contractors.platform_user_id IS
    '1:1 link to PlatformUser — global identity of the contractor.';


-- ─── 2) app_plans: client_app_id nullable ──────────────────────────────────────

-- 2a) subscriber_type: agregar PLATFORM
ALTER TABLE app_plans
    DROP CONSTRAINT chk_app_plans_subscriber_type;
ALTER TABLE app_plans
    ADD CONSTRAINT chk_app_plans_subscriber_type
        CHECK (subscriber_type IN ('TENANT', 'TENANT_USER', 'PLATFORM'));

-- 2b) Hacer client_app_id nullable
ALTER TABLE app_plans
    ALTER COLUMN client_app_id DROP NOT NULL;

-- 2c) Reemplazar unique constraint por índices parciales
ALTER TABLE app_plans
    DROP CONSTRAINT uq_app_plans_app_code;

CREATE UNIQUE INDEX uq_app_plans_app_code
    ON app_plans(client_app_id, code) WHERE client_app_id IS NOT NULL;

CREATE UNIQUE INDEX uq_app_plans_platform_code
    ON app_plans(code) WHERE client_app_id IS NULL;

-- 2d) Reemplazar índice de catálogo público
DROP INDEX idx_app_plans_client_app_status;
CREATE INDEX idx_app_plans_app_status
    ON app_plans(client_app_id, status) WHERE client_app_id IS NOT NULL AND is_public = TRUE;
CREATE INDEX idx_app_plans_platform_status
    ON app_plans(status) WHERE client_app_id IS NULL AND is_public = TRUE;

COMMENT ON COLUMN app_plans.client_app_id IS
    'NULL = platform plan (offered by KeyGo). NOT NULL = app plan (offered by a ClientApp).';
COMMENT ON COLUMN app_plans.subscriber_type IS
    'PLATFORM = KeyGo platform plan. TENANT = B2B app plan. TENANT_USER = B2C app plan.';


-- ─── 3) app_contracts: client_app_id nullable ──────────────────────────────────

ALTER TABLE app_contracts
    ALTER COLUMN client_app_id DROP NOT NULL;

-- Reemplazar índice
DROP INDEX IF EXISTS idx_app_contracts_client_app;
CREATE INDEX idx_app_contracts_app
    ON app_contracts(client_app_id) WHERE client_app_id IS NOT NULL;
CREATE INDEX idx_app_contracts_platform
    ON app_contracts(status) WHERE client_app_id IS NULL;

COMMENT ON COLUMN app_contracts.client_app_id IS
    'NULL = platform contract (KeyGo plan). NOT NULL = app contract (ClientApp plan).';


-- ─── 4) app_subscriptions: client_app_id nullable ──────────────────────────────

ALTER TABLE app_subscriptions
    ALTER COLUMN client_app_id DROP NOT NULL;

-- Reemplazar unique constraint: split en parciales
ALTER TABLE app_subscriptions
    DROP CONSTRAINT uq_app_subscriptions_app_contractor;

CREATE UNIQUE INDEX uq_app_subscriptions_app_contractor
    ON app_subscriptions(client_app_id, contractor_id)
    WHERE client_app_id IS NOT NULL AND status IN ('PENDING', 'ACTIVE', 'PAST_DUE');

CREATE UNIQUE INDEX uq_app_subscriptions_platform_contractor
    ON app_subscriptions(contractor_id)
    WHERE client_app_id IS NULL AND status IN ('PENDING', 'ACTIVE', 'PAST_DUE');

-- Reemplazar índices
DROP INDEX IF EXISTS idx_app_subscriptions_client_app;
CREATE INDEX idx_app_subscriptions_app
    ON app_subscriptions(client_app_id) WHERE client_app_id IS NOT NULL;
CREATE INDEX idx_app_subscriptions_platform
    ON app_subscriptions(contractor_id, status) WHERE client_app_id IS NULL;

COMMENT ON COLUMN app_subscriptions.client_app_id IS
    'NULL = platform subscription (KeyGo). NOT NULL = app subscription (ClientApp).';


-- ─── 5) Migrar planes/contratos/suscripciones del seed keygo-ui a plataforma ──

-- 5a) Planes: los del seed keygo-ui pasan a ser planes de plataforma
UPDATE app_plans
SET client_app_id = NULL,
    subscriber_type = 'PLATFORM'
WHERE client_app_id = (
    SELECT ca.id FROM client_apps ca
    JOIN tenants t ON t.id = ca.tenant_id
    WHERE t.slug = 'keygo' AND ca.client_id = 'keygo-ui'
);

-- 5b) Contratos del seed
UPDATE app_contracts
SET client_app_id = NULL
WHERE client_app_id = (
    SELECT ca.id FROM client_apps ca
    JOIN tenants t ON t.id = ca.tenant_id
    WHERE t.slug = 'keygo' AND ca.client_id = 'keygo-ui'
);

-- 5c) Suscripciones del seed
UPDATE app_subscriptions
SET client_app_id = NULL
WHERE client_app_id = (
    SELECT ca.id FROM client_apps ca
    JOIN tenants t ON t.id = ca.tenant_id
    WHERE t.slug = 'keygo' AND ca.client_id = 'keygo-ui'
);


-- ─── 6) Asignar KEYGO_TENANT_ADMIN al contractor existente (si no lo tiene) ───

INSERT INTO platform_user_roles (platform_user_id, platform_role_id)
SELECT c.platform_user_id, pr.id
FROM contractors c
JOIN platform_roles pr ON pr.code = 'keygo_tenant_admin'
WHERE NOT EXISTS (
    SELECT 1 FROM platform_user_roles pur
    WHERE pur.platform_user_id = c.platform_user_id
    AND pur.platform_role_id = pr.id
);
