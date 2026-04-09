-- =============================================================================
-- V5: Tenant Users — identidad de usuario con alcance de tenant
-- Email y username unicos por tenant (no globalmente).
-- Incluye claims OIDC §5.3 de perfil (locale, zoneinfo, phone, etc.)
-- status=PENDING: creado pero sin verificar email (auto-registro).
-- =============================================================================
CREATE TABLE tenant_users (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    username            VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    -- OIDC §5.3 profile claims
    phone_number        VARCHAR(30),
    locale              VARCHAR(10),
    zoneinfo            VARCHAR(50),
    profile_picture_url TEXT,
    birthdate           DATE,
    website             VARCHAR(2048),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_tenant_users_email    UNIQUE (tenant_id, email),
    CONSTRAINT uq_tenant_users_username UNIQUE (tenant_id, username),
    CONSTRAINT chk_tenant_users_status  CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING'))
);
CREATE INDEX idx_tenant_users_tenant_id ON tenant_users(tenant_id);
CREATE INDEX idx_tenant_users_email     ON tenant_users(email);
CREATE INDEX idx_tenant_users_status    ON tenant_users(status);
CREATE TRIGGER tenant_users_updated_at
    BEFORE UPDATE ON tenant_users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE  tenant_users                    IS 'User identity scoped to a Tenant';
COMMENT ON COLUMN tenant_users.status             IS 'PENDING = email not verified | ACTIVE = operational | SUSPENDED = access blocked';
COMMENT ON COLUMN tenant_users.phone_number        IS 'OIDC phone_number claim — phone scope';
COMMENT ON COLUMN tenant_users.locale              IS 'OIDC locale claim — BCP47 (e.g. es-MX)';
COMMENT ON COLUMN tenant_users.zoneinfo            IS 'OIDC zoneinfo claim — tz database (e.g. America/Mexico_City)';
COMMENT ON COLUMN tenant_users.profile_picture_url IS 'OIDC picture claim — external URL';
COMMENT ON COLUMN tenant_users.birthdate           IS 'OIDC birthdate claim — ISO 8601 date';
COMMENT ON COLUMN tenant_users.website             IS 'OIDC website claim — URL';
