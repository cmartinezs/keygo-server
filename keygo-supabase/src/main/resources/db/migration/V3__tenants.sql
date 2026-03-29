-- =============================================================================
-- V3: Tenants — unidad raíz de aislamiento multi-tenancy
-- Todo lo demás en el sistema pertenece a un Tenant.
-- status=PENDING: tenant en onboarding vía contrato de billing.
-- status=ACTIVE:  operativo.
-- status=SUSPENDED: acceso bloqueado temporalmente.
-- =============================================================================
CREATE TABLE tenants (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    slug        VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_tenants_slug_format
        CHECK (slug ~ '^[a-z0-9][a-z0-9\-]*[a-z0-9]$' OR slug ~ '^[a-z0-9]{3,}$'),
    CONSTRAINT chk_tenants_slug_min_length
        CHECK (char_length(slug) >= 3),
    CONSTRAINT chk_tenants_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING'))
);
CREATE INDEX idx_tenants_slug   ON tenants(slug);
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE TRIGGER tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE  tenants             IS 'Top-level multi-tenancy isolation unit. Every other entity belongs to a Tenant.';
COMMENT ON COLUMN tenants.slug        IS 'URL-friendly unique identifier (lowercase, alphanumeric + hyphens, min 3 chars)';
COMMENT ON COLUMN tenants.status      IS 'PENDING = onboarding in progress | ACTIVE = operational | SUSPENDED = access blocked';
COMMENT ON COLUMN tenants.owner_email IS 'Email of the tenant administrator (owner)';
