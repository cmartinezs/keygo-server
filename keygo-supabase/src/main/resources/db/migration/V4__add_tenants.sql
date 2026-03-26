-- =========================================================
-- Add Tenants Table
-- Agregar tabla de tenants
-- =========================================================
-- Version: 4.0
-- Description: Introduces the tenant as the top-level isolation unit of the system.
--              Every future resource (users, client apps, memberships) will reference a tenant.
-- Descripción: Introduce el tenant como unidad de aislamiento de nivel superior del sistema.
--              Cada recurso futuro (usuarios, apps cliente, membresías) referenciará un tenant.
-- =========================================================

-- Create tenants table
CREATE TABLE IF NOT EXISTS tenants (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    slug        VARCHAR(100) UNIQUE NOT NULL,
    name        VARCHAR(255) NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT tenants_slug_format
        CHECK (slug ~ '^[a-z0-9][a-z0-9\-]*[a-z0-9]$' OR slug ~ '^[a-z0-9]{3,}$'),
    CONSTRAINT tenants_slug_min_length
        CHECK (char_length(slug) >= 3),
    CONSTRAINT tenants_status_valid
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING'))
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_tenants_slug   ON tenants(slug);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants(status);

-- Auto-update trigger for updated_at
CREATE TRIGGER tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE  tenants              IS 'Top-level multi-tenancy isolation unit / Unidad de aislamiento multi-tenancy de nivel superior';
COMMENT ON COLUMN tenants.slug         IS 'URL-friendly unique identifier (lowercase alphanumeric + hyphens)';
COMMENT ON COLUMN tenants.status       IS 'Tenant lifecycle status: ACTIVE | SUSPENDED | PENDING';
COMMENT ON COLUMN tenants.owner_email  IS 'Email of the tenant owner / administrator';

