-- ============================================================================
-- V12__contractors.sql
-- Billing anchor based on global identity, not tenant-local accounts.
-- ============================================================================

CREATE TABLE contractors (
    id                                UUID           NOT NULL DEFAULT gen_random_uuid(),
    type                              VARCHAR(20)    NOT NULL,
    display_name                      VARCHAR(255)   NOT NULL,
    legal_name                        VARCHAR(255),
    tax_id                            VARCHAR(100),
    billing_email                     CITEXT         NOT NULL,
    primary_contact_platform_user_id  UUID,
    status                            VARCHAR(20)    NOT NULL,
    created_at                        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_contractors PRIMARY KEY (id),
    CONSTRAINT fk_contractors_primary_contact
        FOREIGN KEY (primary_contact_platform_user_id)
        REFERENCES platform_users(id) ON DELETE SET NULL,
    CONSTRAINT chk_contractors_type CHECK (type IN ('PERSON', 'COMPANY')),
    CONSTRAINT chk_contractors_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_contractors_status ON contractors(status);
CREATE INDEX idx_contractors_billing_email ON contractors(billing_email);

CREATE TRIGGER trg_contractors_updated_at
    BEFORE UPDATE ON contractors
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE contractor_users (
    contractor_id       UUID         NOT NULL,
    platform_user_id    UUID         NOT NULL,
    role_code           VARCHAR(50)  NOT NULL,
    assigned_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_contractor_users PRIMARY KEY (contractor_id, platform_user_id, role_code),
    CONSTRAINT fk_contractor_users_contractor
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE CASCADE,
    CONSTRAINT fk_contractor_users_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT chk_contractor_users_role_code CHECK (
        role_code IN ('OWNER', 'BILLING_ADMIN', 'VIEWER')
    )
);

ALTER TABLE tenants
    ADD CONSTRAINT fk_tenants_contractor
    FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE SET NULL;

ALTER TABLE platform_user_roles
    ADD CONSTRAINT fk_platform_user_roles_contractor
    FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE CASCADE;

ALTER TABLE audit_events
    ADD CONSTRAINT fk_audit_events_contractor
    FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE SET NULL;

COMMENT ON TABLE contractors IS 'Billing entity representing a person or company that owns contracts and subscriptions.';
COMMENT ON TABLE contractor_users IS 'Multiple platform users can administer a contractor account without relying on tenant-local identities.';
