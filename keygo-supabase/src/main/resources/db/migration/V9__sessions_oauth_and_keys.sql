-- ============================================================================
-- V9__sessions_oauth_and_keys.sql
-- Separation between platform session and app/OAuth session.
-- Includes signing keys and OAuth artifacts with strong tenant/app integrity.
-- ============================================================================

CREATE TABLE platform_sessions (
    id                  UUID           NOT NULL DEFAULT gen_random_uuid(),
    platform_user_id    UUID           NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    expires_at          TIMESTAMPTZ    NOT NULL,
    last_accessed_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    user_agent          TEXT,
    login_method        VARCHAR(30),
    auth_strength       VARCHAR(30),
    device_fingerprint  VARCHAR(255),
    browser_name        VARCHAR(100),
    browser_version     VARCHAR(50),
    os_name             VARCHAR(100),
    os_version          VARCHAR(50),
    device_type         VARCHAR(50),
    ip_address          VARCHAR(64),
    forwarded_for       TEXT,
    country_code        VARCHAR(10),
    region              VARCHAR(100),
    city                VARCHAR(100),
    timezone_detected   VARCHAR(50),
    started_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    ended_at            TIMESTAMPTZ,
    termination_reason  VARCHAR(50),
    last_route          VARCHAR(500),
    last_activity_at    TIMESTAMPTZ,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_platform_sessions PRIMARY KEY (id),
    CONSTRAINT fk_platform_sessions_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT chk_platform_sessions_status CHECK (status IN ('ACTIVE', 'TERMINATED', 'EXPIRED')),
    CONSTRAINT chk_platform_sessions_login_method CHECK (
        login_method IS NULL OR login_method IN ('PASSWORD', 'MAGIC_LINK', 'MFA', 'SOCIAL', 'API_BOOTSTRAP', 'SYSTEM')
    ),
    CONSTRAINT chk_platform_sessions_auth_strength CHECK (
        auth_strength IS NULL OR auth_strength IN ('LOW', 'MEDIUM', 'HIGH')
    ),
    CONSTRAINT chk_platform_sessions_device_type CHECK (
        device_type IS NULL OR device_type IN ('DESKTOP', 'MOBILE', 'TABLET', 'BOT', 'UNKNOWN')
    )
);

CREATE INDEX idx_platform_sessions_platform_user_status ON platform_sessions(platform_user_id, status);
CREATE INDEX idx_platform_sessions_expires_at ON platform_sessions(expires_at);

CREATE TRIGGER trg_platform_sessions_updated_at
    BEFORE UPDATE ON platform_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE signing_keys (
    id                     UUID           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id              UUID,
    kid                    VARCHAR(255)   NOT NULL,
    algorithm              VARCHAR(20)    NOT NULL,
    public_jwk             JSONB          NOT NULL,
    private_key_encrypted  TEXT           NOT NULL,
    status                 VARCHAR(20)    NOT NULL,
    activated_at           TIMESTAMPTZ,
    retired_at             TIMESTAMPTZ,
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_signing_keys PRIMARY KEY (id),
    CONSTRAINT uq_signing_keys_kid UNIQUE (kid),
    CONSTRAINT fk_signing_keys_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL,
    CONSTRAINT chk_signing_keys_status CHECK (status IN ('ACTIVE', 'RETIRED')),
    CONSTRAINT chk_signing_keys_algorithm CHECK (algorithm IN ('RS256', 'RS384', 'RS512', 'ES256'))
);

CREATE INDEX idx_signing_keys_tenant_status ON signing_keys(tenant_id, status);

CREATE TRIGGER trg_signing_keys_updated_at
    BEFORE UPDATE ON signing_keys
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE oauth_sessions (
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    platform_session_id  UUID           NOT NULL,
    platform_user_id     UUID           NOT NULL,
    tenant_id            UUID           NOT NULL,
    tenant_user_id       UUID,
    client_app_id        UUID           NOT NULL,
    signing_key_id       UUID,
    status               VARCHAR(20)    NOT NULL,
    expires_at           TIMESTAMPTZ    NOT NULL,
    last_accessed_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    login_prompt         VARCHAR(30),
    auth_context_class   VARCHAR(100),
    granted_scopes       TEXT,
    consent_required     BOOLEAN        NOT NULL DEFAULT FALSE,
    consent_granted      BOOLEAN        NOT NULL DEFAULT FALSE,
    issued_ip_address    VARCHAR(64),
    started_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    ended_at             TIMESTAMPTZ,
    termination_reason   VARCHAR(50),
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_oauth_sessions PRIMARY KEY (id),
    CONSTRAINT uq_oauth_sessions_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT uq_oauth_sessions_id_client_app UNIQUE (id, client_app_id),
    CONSTRAINT fk_oauth_sessions_platform_session
        FOREIGN KEY (platform_session_id) REFERENCES platform_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_oauth_sessions_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_oauth_sessions_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_oauth_sessions_tenant_user
        FOREIGN KEY (tenant_user_id, tenant_id)
        REFERENCES tenant_users(id, tenant_id) ON DELETE RESTRICT,
    CONSTRAINT fk_oauth_sessions_client_app
        FOREIGN KEY (client_app_id, tenant_id)
        REFERENCES client_apps(id, tenant_id) ON DELETE RESTRICT,
    CONSTRAINT fk_oauth_sessions_signing_key
        FOREIGN KEY (signing_key_id) REFERENCES signing_keys(id) ON DELETE SET NULL,
    CONSTRAINT chk_oauth_sessions_status CHECK (status IN ('ACTIVE', 'TERMINATED', 'EXPIRED'))
);

CREATE OR REPLACE FUNCTION validate_oauth_session_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_platform_session_user UUID;
    v_tenant_user_platform_user UUID;
    v_client_is_internal BOOLEAN;
    v_tenant_is_internal BOOLEAN;
BEGIN
    SELECT platform_user_id
      INTO v_platform_session_user
      FROM platform_sessions
     WHERE id = NEW.platform_session_id;

    IF v_platform_session_user IS DISTINCT FROM NEW.platform_user_id THEN
        RAISE EXCEPTION 'oauth_sessions.platform_user_id must match platform_sessions.platform_user_id';
    END IF;

    SELECT is_internal, t.is_internal_reserved
      INTO v_client_is_internal, v_tenant_is_internal
      FROM client_apps ca
      JOIN tenants t ON t.id = ca.tenant_id
     WHERE ca.id = NEW.client_app_id
       AND ca.tenant_id = NEW.tenant_id;

    IF NEW.tenant_user_id IS NULL THEN
        IF COALESCE(v_client_is_internal, FALSE) = FALSE OR COALESCE(v_tenant_is_internal, FALSE) = FALSE THEN
            RAISE EXCEPTION 'tenant_user_id can be null only for internal client apps in reserved tenants';
        END IF;
    ELSE
        SELECT platform_user_id
          INTO v_tenant_user_platform_user
          FROM tenant_users
         WHERE id = NEW.tenant_user_id
           AND tenant_id = NEW.tenant_id;

        IF v_tenant_user_platform_user IS DISTINCT FROM NEW.platform_user_id THEN
            RAISE EXCEPTION 'oauth_sessions tenant_user_id must belong to the same platform_user_id';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_oauth_sessions_validate
    BEFORE INSERT OR UPDATE ON oauth_sessions
    FOR EACH ROW
    EXECUTE FUNCTION validate_oauth_session_context();

CREATE TRIGGER trg_oauth_sessions_updated_at
    BEFORE UPDATE ON oauth_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE authorization_codes (
    id                    UUID           NOT NULL DEFAULT gen_random_uuid(),
    code_hash             VARCHAR(128)   NOT NULL,
    platform_session_id   UUID           NOT NULL,
    platform_user_id      UUID           NOT NULL,
    tenant_id             UUID           NOT NULL,
    tenant_user_id        UUID,
    client_app_id         UUID           NOT NULL,
    redirect_uri          VARCHAR(2048)  NOT NULL,
    requested_scopes      TEXT           NOT NULL,
    code_challenge        VARCHAR(255),
    code_challenge_method VARCHAR(10),
    status                VARCHAR(20)    NOT NULL,
    expires_at            TIMESTAMPTZ    NOT NULL,
    used_at               TIMESTAMPTZ,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_authorization_codes PRIMARY KEY (id),
    CONSTRAINT uq_authorization_codes_code_hash UNIQUE (code_hash),
    CONSTRAINT fk_authorization_codes_platform_session
        FOREIGN KEY (platform_session_id) REFERENCES platform_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_authorization_codes_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_authorization_codes_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_authorization_codes_tenant_user
        FOREIGN KEY (tenant_user_id, tenant_id)
        REFERENCES tenant_users(id, tenant_id) ON DELETE RESTRICT,
    CONSTRAINT fk_authorization_codes_client_app
        FOREIGN KEY (client_app_id, tenant_id)
        REFERENCES client_apps(id, tenant_id) ON DELETE RESTRICT,
    CONSTRAINT chk_authorization_codes_status CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED', 'REVOKED')),
    CONSTRAINT chk_authorization_codes_code_hash CHECK (regexp_match(code_hash, '^[A-Fa-f0-9]{64,128}$') IS NOT NULL),
    CONSTRAINT chk_authorization_codes_pkce_method CHECK (
        code_challenge_method IS NULL OR code_challenge_method IN ('plain', 'S256')
    )
);

CREATE OR REPLACE FUNCTION validate_authorization_code_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_platform_session_user UUID;
    v_tenant_user_platform_user UUID;
BEGIN
    SELECT platform_user_id
      INTO v_platform_session_user
      FROM platform_sessions
     WHERE id = NEW.platform_session_id;

    IF v_platform_session_user IS DISTINCT FROM NEW.platform_user_id THEN
        RAISE EXCEPTION 'authorization_codes.platform_user_id must match platform_sessions.platform_user_id';
    END IF;

    IF NEW.tenant_user_id IS NOT NULL THEN
        SELECT platform_user_id
          INTO v_tenant_user_platform_user
          FROM tenant_users
         WHERE id = NEW.tenant_user_id
           AND tenant_id = NEW.tenant_id;

        IF v_tenant_user_platform_user IS DISTINCT FROM NEW.platform_user_id THEN
            RAISE EXCEPTION 'authorization_codes tenant_user_id must belong to the same platform user';
        END IF;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM client_redirect_uris cru
        WHERE cru.client_app_id = NEW.client_app_id
          AND cru.uri = NEW.redirect_uri
    ) THEN
        RAISE EXCEPTION 'authorization_codes.redirect_uri must exist in client_redirect_uris';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_authorization_codes_validate
    BEFORE INSERT OR UPDATE ON authorization_codes
    FOR EACH ROW
    EXECUTE FUNCTION validate_authorization_code_context();

CREATE TRIGGER trg_authorization_codes_updated_at
    BEFORE UPDATE ON authorization_codes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE refresh_tokens (
    id                 UUID           NOT NULL DEFAULT gen_random_uuid(),
    token_hash         VARCHAR(64)    NOT NULL,
    oauth_session_id   UUID           NOT NULL,
    platform_user_id   UUID           NOT NULL,
    tenant_id          UUID           NOT NULL,
    tenant_user_id     UUID,
    client_app_id      UUID           NOT NULL,
    signing_key_id     UUID,
    requested_scopes   TEXT           NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    expires_at         TIMESTAMPTZ    NOT NULL,
    used_at            TIMESTAMPTZ,
    replaced_by_id     UUID,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_oauth_session
        FOREIGN KEY (oauth_session_id) REFERENCES oauth_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_tenant_user
        FOREIGN KEY (tenant_user_id, tenant_id)
        REFERENCES tenant_users(id, tenant_id) ON DELETE RESTRICT,
    CONSTRAINT fk_refresh_tokens_client_app
        FOREIGN KEY (client_app_id, tenant_id)
        REFERENCES client_apps(id, tenant_id) ON DELETE RESTRICT,
    CONSTRAINT fk_refresh_tokens_signing_key
        FOREIGN KEY (signing_key_id) REFERENCES signing_keys(id) ON DELETE SET NULL,
    CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL,
    CONSTRAINT chk_refresh_tokens_status CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED', 'REVOKED')),
    CONSTRAINT chk_refresh_tokens_token_hash CHECK (regexp_match(token_hash, '^[A-Fa-f0-9]{64}$') IS NOT NULL)
);

CREATE OR REPLACE FUNCTION validate_refresh_token_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_oauth_session RECORD;
BEGIN
    SELECT platform_user_id, tenant_id, tenant_user_id, client_app_id
      INTO v_oauth_session
      FROM oauth_sessions
     WHERE id = NEW.oauth_session_id;

    IF v_oauth_session.platform_user_id IS DISTINCT FROM NEW.platform_user_id
       OR v_oauth_session.tenant_id IS DISTINCT FROM NEW.tenant_id
       OR v_oauth_session.client_app_id IS DISTINCT FROM NEW.client_app_id
       OR v_oauth_session.tenant_user_id IS DISTINCT FROM NEW.tenant_user_id THEN
        RAISE EXCEPTION 'refresh_tokens context must match oauth_sessions context exactly';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_refresh_tokens_validate
    BEFORE INSERT OR UPDATE ON refresh_tokens
    FOR EACH ROW
    EXECUTE FUNCTION validate_refresh_token_context();

CREATE TRIGGER trg_refresh_tokens_updated_at
    BEFORE UPDATE ON refresh_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE platform_sessions IS 'Global platform sessions for account access, activity and security UX.';
COMMENT ON TABLE oauth_sessions IS 'Tenant/app-level OAuth sessions tied to a platform session.';
COMMENT ON TABLE authorization_codes IS 'Authorization codes stored as hashes, never in plain text.';
COMMENT ON TABLE refresh_tokens IS 'Refresh tokens stored only as SHA-256 hashes. Context integrity is validated against oauth_sessions.';
COMMENT ON TABLE signing_keys IS 'JWT signing keys. tenant_id null means a global platform key.';
