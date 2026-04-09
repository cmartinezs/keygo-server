-- ============================================================================
-- V3__platform_users.sql
-- Global platform identity. No tenant-scoped credentials live here.
-- ============================================================================

CREATE TABLE platform_users (
    id                    UUID            NOT NULL DEFAULT gen_random_uuid(),
    email                 CITEXT          NOT NULL,
    password_hash         VARCHAR(255)    NOT NULL,
    first_name            VARCHAR(100),
    last_name             VARCHAR(100),
    display_name          VARCHAR(255),
    phone_number          VARCHAR(30),
    locale                VARCHAR(10),
    zoneinfo              VARCHAR(50),
    profile_picture_url   TEXT,
    birthdate             DATE,
    website               VARCHAR(2048),
    status                VARCHAR(30)     NOT NULL,
    email_verified_at     TIMESTAMPTZ,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT pk_platform_users PRIMARY KEY (id),
    CONSTRAINT uq_platform_users_email UNIQUE (email),
    CONSTRAINT chk_platform_users_status CHECK (
        status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'RESET_PASSWORD', 'DELETED')
    )
);

CREATE INDEX idx_platform_users_status ON platform_users(status);
CREATE INDEX idx_platform_users_last_login_at ON platform_users(last_login_at);

CREATE TRIGGER trg_platform_users_updated_at
    BEFORE UPDATE ON platform_users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE platform_users IS 'Global identity root for Keygo platform accounts.';
COMMENT ON COLUMN platform_users.email IS 'Case-insensitive unique email for the platform account.';
COMMENT ON COLUMN platform_users.password_hash IS 'BCrypt or equivalent password hash. Raw passwords never persist.';
COMMENT ON COLUMN platform_users.status IS 'Global account lifecycle. RESET_PASSWORD represents forced password recovery flow.';
COMMENT ON COLUMN platform_users.display_name IS 'Preferred profile name shown across platform and tenants.';
COMMENT ON COLUMN platform_users.email_verified_at IS 'Null until the platform account email is verified.';
