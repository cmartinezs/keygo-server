-- =============================================================================
-- V7: Auth — codigos de autorizacion OAuth2 y claves de firma JWT
-- authorization_codes: codigos PKCE del flujo Authorization Code
-- signing_keys: claves RSA para firma de tokens JWT (RS256/RS384/RS512)
-- =============================================================================
CREATE TABLE authorization_codes (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    code                  VARCHAR(256)  NOT NULL UNIQUE,
    client_app_id         UUID          NOT NULL REFERENCES client_apps(id)   ON DELETE CASCADE,
    tenant_id             UUID          NOT NULL REFERENCES tenants(id)        ON DELETE CASCADE,
    user_id               UUID          NOT NULL REFERENCES tenant_users(id)   ON DELETE CASCADE,
    code_challenge        VARCHAR(256)  NOT NULL,
    code_challenge_method VARCHAR(10)   NOT NULL,
    requested_scopes      TEXT          NOT NULL,
    redirect_uri          VARCHAR(2048) NOT NULL,
    status                VARCHAR(20)   NOT NULL DEFAULT 'pending',
    expires_at            TIMESTAMPTZ   NOT NULL,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    used_at               TIMESTAMPTZ,
    CONSTRAINT chk_auth_codes_method CHECK (code_challenge_method IN ('plain', 'S256')),
    CONSTRAINT chk_auth_codes_status CHECK (status IN ('pending', 'used', 'expired', 'revoked'))
);
CREATE INDEX idx_auth_codes_code       ON authorization_codes(code);
CREATE INDEX idx_auth_codes_client_app ON authorization_codes(client_app_id);
CREATE INDEX idx_auth_codes_tenant     ON authorization_codes(tenant_id);
CREATE INDEX idx_auth_codes_user       ON authorization_codes(user_id);
CREATE INDEX idx_auth_codes_status     ON authorization_codes(status);
CREATE INDEX idx_auth_codes_expires_at ON authorization_codes(expires_at);
COMMENT ON TABLE  authorization_codes        IS 'OAuth2 PKCE authorization codes — short-lived (10 min), single-use';
COMMENT ON COLUMN authorization_codes.status IS 'lowercase: pending | used | expired | revoked';
-- =============================================================================
CREATE TABLE signing_keys (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    kid              VARCHAR(100) NOT NULL UNIQUE,
    algorithm        VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    public_material  TEXT         NOT NULL,
    private_material TEXT,
    activated_at     TIMESTAMPTZ  NOT NULL,
    retired_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_signing_keys_status    CHECK (status    IN ('ACTIVE', 'RETIRED', 'REVOKED')),
    CONSTRAINT chk_signing_keys_algorithm CHECK (algorithm IN ('RS256', 'RS384', 'RS512'))
);
CREATE INDEX idx_signing_keys_status ON signing_keys(status);
COMMENT ON TABLE  signing_keys                  IS 'RSA key pairs for JWT signing. One ACTIVE key at a time; RETIRED keys kept for token validation.';
COMMENT ON COLUMN signing_keys.kid              IS 'Key ID used in JWT header kid claim';
COMMENT ON COLUMN signing_keys.private_material IS 'PEM-encoded private key. Encrypt at rest with KMS in production.';
