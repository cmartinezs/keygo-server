-- ============================================================================
-- V19__app_contract_onboarding_snapshot.sql
-- Persist onboarding contact/company snapshot directly in app_contracts.
-- ============================================================================

ALTER TABLE app_contracts
    ADD COLUMN contractor_first_name VARCHAR(100),
    ADD COLUMN contractor_last_name  VARCHAR(100),
    ADD COLUMN company_name          VARCHAR(255),
    ADD COLUMN company_tax_id        VARCHAR(100),
    ADD COLUMN company_address       TEXT;

UPDATE app_contracts
SET contractor_first_name = NULLIF(split_part(trim(billing_contact_name), ' ', 1), ''),
    contractor_last_name = COALESCE(
        NULLIF(trim(substr(trim(billing_contact_name), length(split_part(trim(billing_contact_name), ' ', 1)) + 1)), ''),
        NULLIF(split_part(trim(billing_contact_name), ' ', 1), '')
    )
WHERE billing_contact_name IS NOT NULL
  AND (contractor_first_name IS NULL OR contractor_last_name IS NULL);

UPDATE app_contracts ac
SET company_name = COALESCE(c.legal_name, c.display_name),
    company_tax_id = c.tax_id
FROM contractors c
WHERE ac.contractor_id = c.id
  AND (ac.company_name IS NULL OR ac.company_tax_id IS NULL);

COMMENT ON COLUMN app_contracts.contractor_first_name IS 'Snapshot of the contractor contact first name captured during onboarding.';
COMMENT ON COLUMN app_contracts.contractor_last_name IS 'Snapshot of the contractor contact last name captured during onboarding.';
COMMENT ON COLUMN app_contracts.company_name IS 'Optional company or legal entity name captured during onboarding.';
COMMENT ON COLUMN app_contracts.company_tax_id IS 'Optional company tax identifier captured during onboarding.';
COMMENT ON COLUMN app_contracts.company_address IS 'Optional company billing address captured during onboarding.';
