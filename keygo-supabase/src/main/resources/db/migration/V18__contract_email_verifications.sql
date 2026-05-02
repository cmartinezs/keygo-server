-- ============================================================================
-- V18__contract_email_verifications.sql
-- Contract email verification persistence for pre-contractor onboarding.
-- ============================================================================

ALTER TABLE app_contracts
    ALTER COLUMN contractor_id DROP NOT NULL;

CREATE TABLE contract_email_verifications (
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    contract_id   UUID         NOT NULL,
    code          VARCHAR(10)  NOT NULL,
    expires_at    TIMESTAMPTZ  NOT NULL,
    used_at       TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_contract_email_verifications PRIMARY KEY (id),
    CONSTRAINT uq_contract_email_verifications_contract UNIQUE (contract_id),
    CONSTRAINT fk_contract_email_verifications_contract
        FOREIGN KEY (contract_id) REFERENCES app_contracts(id) ON DELETE CASCADE,
    CONSTRAINT chk_contract_email_verifications_code
        CHECK (regexp_match(code, '^[0-9A-Za-z-]{4,10}$') IS NOT NULL)
);

CREATE INDEX idx_contract_email_verifications_code
    ON contract_email_verifications(code);

CREATE TRIGGER trg_contract_email_verifications_updated_at
    BEFORE UPDATE ON contract_email_verifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE contract_email_verifications IS 'Verification codes for contract onboarding before contractor/platform user provisioning.';
COMMENT ON COLUMN contract_email_verifications.contract_id IS 'App contract being verified before payment and account provisioning.';
