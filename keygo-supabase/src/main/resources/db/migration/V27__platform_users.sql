-- =============================================================================
-- V27: Platform Users — identidad global de plataforma
-- Separación de la identidad de plataforma (cuenta global) del usuario de tenant.
-- RFC: docs/rfc/restructure-multitenant/02-modelo-identidad-multitenancy.md
-- =============================================================================

CREATE TABLE platform_users (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL UNIQUE,
    username            VARCHAR(100) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    status              VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    email_verified_at   TIMESTAMPTZ,
    phone_number        VARCHAR(30),
    locale              VARCHAR(10),
    zoneinfo            VARCHAR(50),
    profile_picture_url TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_platform_users_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING', 'RESET_PASSWORD'))
);

CREATE INDEX idx_platform_users_email    ON platform_users(email);
CREATE INDEX idx_platform_users_username ON platform_users(username);
CREATE INDEX idx_platform_users_status   ON platform_users(status);

CREATE TRIGGER trg_platform_users_updated_at
    BEFORE UPDATE ON platform_users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE platform_users IS
    'Global platform user identity. Separate from tenant_users which represents '
    'organizational membership. RFC: docs/rfc/restructure-multitenant.';
COMMENT ON COLUMN platform_users.email IS 'Globally unique email (platform-level identity).';
COMMENT ON COLUMN platform_users.status IS
    'ACTIVE | SUSPENDED | PENDING (email not verified) | RESET_PASSWORD';
