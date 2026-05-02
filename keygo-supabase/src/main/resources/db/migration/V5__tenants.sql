-- ============================================================================
-- V5__tenants.sql
-- Tenant container separated from global identity.
-- ============================================================================

CREATE TABLE tenants (
    id                    UUID           NOT NULL DEFAULT gen_random_uuid(),
    slug                  VARCHAR(100)   NOT NULL,
    name                  VARCHAR(255)   NOT NULL,
    status                VARCHAR(20)    NOT NULL,
    contractor_id         UUID,
    is_internal_reserved  BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uq_tenants_slug UNIQUE (slug),
    CONSTRAINT chk_tenants_slug CHECK (regexp_match(slug, '^[a-z0-9][a-z0-9-]{1,98}[a-z0-9]$') IS NOT NULL),
    CONSTRAINT chk_tenants_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_tenants_contractor_id ON tenants(contractor_id) WHERE contractor_id IS NOT NULL;

CREATE TRIGGER trg_tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

ALTER TABLE platform_user_roles
    ADD CONSTRAINT fk_platform_user_roles_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

COMMENT ON TABLE tenants IS 'Tenant boundary for organizations. It is not the root identity of a person.';
COMMENT ON COLUMN tenants.contractor_id IS 'Nullable until contractors are created. Internal reserved tenants like keygo keep this null.';
COMMENT ON COLUMN tenants.is_internal_reserved IS 'True for platform-managed tenants such as the internal keygo tenant.';
