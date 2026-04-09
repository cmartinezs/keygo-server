-- =============================================================================
-- V12: Billing Contracts — flujo de onboarding/checkout (modelo v2)
--
-- app_contracts: proceso de contratacion previo a la suscripcion activa.
-- Modelo v2 (contractor-centric):
--   - contractor_id: NULL hasta verificar email (se crea/encuentra el Contractor).
--     NOT NULL desde PENDING_PAYMENT en adelante.
--   - Los campos subscriber_tenant_id / subscriber_tenant_user_id / company_slug
--     han sido ELIMINADOS. Los tenants se crean manualmente por el contratante
--     despues de activar el contrato, dentro del limite MAX_TENANTS del plan.
--   - El codigo de verificacion de email del contrato es INDEPENDIENTE de
--     email_verifications (el suscriptor puede no existir como TenantUser aun).
--
-- Estados del ciclo de vida del contrato:
--   PENDING_EMAIL_VERIFICATION  -> PENDING_PAYMENT -> READY_TO_ACTIVATE
--   READY_TO_ACTIVATE -> ACTIVE -> SUPERSEDED | FINALIZED
--   En cualquier punto -> CANCELLED | EXPIRED | FAILED
-- =============================================================================
CREATE TABLE app_contracts (
    id                           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id                UUID         NOT NULL REFERENCES client_apps(id)       ON DELETE RESTRICT,
    selected_plan_version_id     UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE RESTRICT,
    -- contractor_id es NULL hasta que se verifica el email y se crea/identifica
    -- al Contractor. A partir de PENDING_PAYMENT siempre es NOT NULL.
    contractor_id                UUID         REFERENCES contractors(id) ON DELETE RESTRICT,
    billing_period               VARCHAR(20)  NOT NULL,
    status                       VARCHAR(40)  NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION',
    -- Datos capturados durante el onboarding (antes de que exista Contractor)
    contractor_email             VARCHAR(255) NOT NULL,
    contractor_first_name        VARCHAR(100) NOT NULL,
    contractor_last_name         VARCHAR(100) NOT NULL,
    -- Datos de empresa (opcionales, para facturacion B2B)
    company_name                 VARCHAR(200),
    company_tax_id               VARCHAR(100),
    company_address              TEXT,
    -- Codigo de verificacion de email del contrato (6 digitos, 30 min TTL)
    verification_code            VARCHAR(10),
    verification_code_expires_at TIMESTAMPTZ,
    -- Timestamps de trazabilidad
    email_verified_at            TIMESTAMPTZ,
    payment_verified_at          TIMESTAMPTZ,
    expires_at                   TIMESTAMPTZ  NOT NULL,
    created_at                   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_app_contracts_billing_period CHECK (billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')),
    CONSTRAINT chk_app_contracts_status CHECK (status IN (
        'PENDING_EMAIL_VERIFICATION',
        'PENDING_PAYMENT',
        'READY_TO_ACTIVATE',
        'ACTIVE',
        'SUPERSEDED',
        'FINALIZED',
        'CANCELLED',
        'EXPIRED',
        'FAILED'
    ))
);
-- Indice: solo 1 contrato ACTIVE por contractor (invariante de negocio)
CREATE UNIQUE INDEX uq_app_contracts_active_contractor
    ON app_contracts(contractor_id)
    WHERE status = 'ACTIVE' AND contractor_id IS NOT NULL;
CREATE INDEX idx_app_contracts_client_app        ON app_contracts(client_app_id);
CREATE INDEX idx_app_contracts_contractor_id     ON app_contracts(contractor_id) WHERE contractor_id IS NOT NULL;
CREATE INDEX idx_app_contracts_status            ON app_contracts(status);
CREATE INDEX idx_app_contracts_contractor_email  ON app_contracts(contractor_email);
CREATE TRIGGER app_contracts_updated_at
    BEFORE UPDATE ON app_contracts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE  app_contracts IS 'Billing onboarding/checkout flow per ClientApp (contractor-centric model v2). Subscription created only after full activation.';
COMMENT ON COLUMN app_contracts.contractor_id IS 'NULL until email verified. Set when Contractor is created/identified during PENDING_PAYMENT transition.';
COMMENT ON COLUMN app_contracts.verification_code IS '6-digit numeric code for contract email verification (independent from user registration codes in email_verifications).';
COMMENT ON COLUMN app_contracts.verification_code_expires_at IS 'Contract verification code TTL (30 minutes by default).';
COMMENT ON COLUMN app_contracts.company_name IS 'Optional company name for B2B billing. Used in invoice snapshots.';
