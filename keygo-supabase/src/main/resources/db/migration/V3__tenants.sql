-- =============================================================================
-- V3: Tenants — unidad raíz de aislamiento multi-tenancy
-- Todo lo demás en el sistema pertenece a un Tenant.
-- status=PENDING:   tenant en onboarding vía contrato de billing.
-- status=ACTIVE:    operativo.
-- status=SUSPENDED: acceso bloqueado temporalmente.
-- status=DELETED:   eliminación lógica (los registros se conservan).
-- contractor_id: NULL para tenants de sistema/plataforma.
--   NOT NULL cuando el tenant fue creado por un contratante (modelo billing v2).
--   La FK a contractors se agrega en V11 (tabla contractors aún no existe aquí).
-- =============================================================================
CREATE TABLE tenants (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    slug           VARCHAR(100) NOT NULL UNIQUE,
    name           VARCHAR(255) NOT NULL,
    owner_email    VARCHAR(255) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    contractor_id  UUID,                          -- FK → contractors(id) agregada en V11
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_tenants_slug_format
        CHECK (slug ~ '^[a-z0-9][a-z0-9\-]*[a-z0-9]$' OR slug ~ '^[a-z0-9]{3,}$'),
    CONSTRAINT chk_tenants_slug_min_length
        CHECK (char_length(slug) >= 3),
    CONSTRAINT chk_tenants_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING', 'DELETED'))
);
CREATE INDEX idx_tenants_slug          ON tenants(slug);
CREATE INDEX idx_tenants_status        ON tenants(status);
CREATE INDEX idx_tenants_contractor_id ON tenants(contractor_id) WHERE contractor_id IS NOT NULL;
CREATE TRIGGER tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE  tenants               IS 'Top-level multi-tenancy isolation unit. Every other entity belongs to a Tenant.';
COMMENT ON COLUMN tenants.slug          IS 'URL-friendly unique identifier (lowercase, alphanumeric + hyphens, min 3 chars)';
COMMENT ON COLUMN tenants.status        IS 'PENDING = onboarding | ACTIVE = operational | SUSPENDED = access blocked | DELETED = logical delete';
COMMENT ON COLUMN tenants.owner_email   IS 'Email of the tenant administrator (owner)';
COMMENT ON COLUMN tenants.contractor_id IS 'NULL = system/platform tenant (keygo, demo). NOT NULL = created by a contractor within MAX_TENANTS entitlement. FK added in V11.';
