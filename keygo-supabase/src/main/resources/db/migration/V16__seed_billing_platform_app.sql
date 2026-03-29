-- =============================================================================
-- V16: Seed — keygo-platform ClientApp para el tenant keygo
--
-- Crea la aplicación "keygo-platform" que KeyGo usa para gestionar su propio
-- billing (dogfood). Esta app es CONFIDENTIAL y usa CLIENT_CREDENTIALS grant
-- para operaciones M2M de facturación.
--
-- Los planes de keygo-platform (FREE/STARTER/BUSINESS/ENTERPRISE) se
-- crean en V17 para separar claramente el registro de la app del catálogo.
--
-- UUIDs estables para re-ejecución idempotente:
--   keygo tenant       : 11111111-1111-1111-1111-111111111111  (desde V15)
--   keygo-platform app : 11111111-1111-1111-1111-333333333333  (nuevo)
--   billing_admin role : 11111111-1111-1111-1111-500000000001  (nuevo)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- keygo-platform: ClientApp interna propiedad del tenant keygo
-- ---------------------------------------------------------------------------
INSERT INTO client_apps (
    id, tenant_id, client_id, name, description, type, hashed_secret, status
)
VALUES (
    '11111111-1111-1111-1111-333333333333',
    '11111111-1111-1111-1111-111111111111',
    'keygo-platform',
    'KeyGo Platform',
    'Internal app used by KeyGo to manage IAM platform billing and subscriptions',
    'CONFIDENTIAL',
    NULL,  -- sin secret para billing interno de plataforma
    'ACTIVE'
)
ON CONFLICT (client_id) DO UPDATE
SET
    tenant_id   = EXCLUDED.tenant_id,
    name        = EXCLUDED.name,
    description = EXCLUDED.description,
    type        = EXCLUDED.type,
    status      = EXCLUDED.status,
    updated_at  = CURRENT_TIMESTAMP;

-- ── Allowed grants: CLIENT_CREDENTIALS para operaciones M2M ───────────────
INSERT INTO client_allowed_grants (id, client_app_id, grant_type)
SELECT gen_random_uuid(), '11111111-1111-1111-1111-333333333333', 'CLIENT_CREDENTIALS'
WHERE NOT EXISTS (
    SELECT 1 FROM client_allowed_grants
    WHERE client_app_id = '11111111-1111-1111-1111-333333333333'
      AND grant_type = 'CLIENT_CREDENTIALS'
);

-- ── App roles de keygo-platform ───────────────────────────────────────────
INSERT INTO app_roles (id, client_app_id, code, display_name, description)
VALUES (
    '11111111-1111-1111-1111-500000000001',
    '11111111-1111-1111-1111-333333333333',
    'billing_admin',
    'Billing Admin',
    'Can manage plans and subscriptions for the KeyGo platform'
)
ON CONFLICT (client_app_id, code) DO UPDATE
SET
    display_name = EXCLUDED.display_name,
    description  = EXCLUDED.description,
    updated_at   = CURRENT_TIMESTAMP;

