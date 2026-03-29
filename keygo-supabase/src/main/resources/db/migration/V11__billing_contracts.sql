-- =============================================================================
-- V11: Billing Contracts — proceso de onboarding/checkout por ClientApp
-- app_contracts: representa el flujo de contratación antes de crear la
-- suscripción activa. Rastrea verificación de email y pago antes de
-- activar la suscripción.
--
-- El campo verification_code almacena el código de verificación de email
-- del contrato (independiente del de registro de usuario en email_verifications).
-- =============================================================================
CREATE TABLE app_contracts (
    id                           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id                UUID         NOT NULL REFERENCES client_apps(id)     ON DELETE RESTRICT,
    selected_plan_version_id     UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE RESTRICT,

    billing_period               VARCHAR(20)  NOT NULL,

    -- Establecidos tras la activación (null hasta entonces).
    subscriber_tenant_id         UUID         REFERENCES tenants(id)      ON DELETE SET NULL,
    subscriber_tenant_user_id    UUID         REFERENCES tenant_users(id) ON DELETE SET NULL,

    status                       VARCHAR(40)  NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION',

    -- Datos del contratante (siempre requeridos)
    contractor_email             VARCHAR(255) NOT NULL,
    contractor_first_name        VARCHAR(100) NOT NULL,
    contractor_last_name         VARCHAR(100) NOT NULL,

    -- Datos de empresa (para onboarding empresarial)
    company_name                 VARCHAR(200),
    company_slug                 VARCHAR(100) UNIQUE,
    company_tax_id               VARCHAR(100),
    company_address              TEXT,

    -- Código de verificación de email del contrato (6 dígitos, 30 min TTL)
    verification_code            VARCHAR(10),
    verification_code_expires_at TIMESTAMPTZ,

    -- Timestamps de trazabilidad
    email_verified_at            TIMESTAMPTZ,
    payment_verified_at          TIMESTAMPTZ,
    expires_at                   TIMESTAMPTZ  NOT NULL,
    created_at                   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_app_contracts_billing_period CHECK (billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')),
    CONSTRAINT chk_app_contracts_status         CHECK (status         IN (
        'PENDING_EMAIL_VERIFICATION',
        'PENDING_PAYMENT',
        'READY_TO_ACTIVATE',
        'ACTIVATED',
        'CANCELLED',
        'EXPIRED',
        'FAILED'
    )),
    -- Como máximo un suscriptor por contrato
    CONSTRAINT chk_app_contracts_single_subscriber CHECK (
        NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL)
    )
);

CREATE INDEX idx_app_contracts_client_app ON app_contracts(client_app_id);
CREATE INDEX idx_app_contracts_status     ON app_contracts(status);
CREATE INDEX idx_app_contracts_contractor_email ON app_contracts(contractor_email);
CREATE INDEX idx_app_contracts_sub_tenant
    ON app_contracts(subscriber_tenant_id)
    WHERE subscriber_tenant_id IS NOT NULL;
CREATE INDEX idx_app_contracts_sub_user
    ON app_contracts(subscriber_tenant_user_id)
    WHERE subscriber_tenant_user_id IS NOT NULL;

CREATE TRIGGER app_contracts_updated_at
    BEFORE UPDATE ON app_contracts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  app_contracts IS 'Billing onboarding/checkout flow per ClientApp. Creates subscription on activation.';
COMMENT ON COLUMN app_contracts.company_slug    IS 'Becomes the new Tenant slug on activation';
COMMENT ON COLUMN app_contracts.verification_code IS '6-digit numeric code for contract email verification (independent of user registration codes)';
COMMENT ON COLUMN app_contracts.verification_code_expires_at IS 'Contract verification code TTL (30 minutes)';
