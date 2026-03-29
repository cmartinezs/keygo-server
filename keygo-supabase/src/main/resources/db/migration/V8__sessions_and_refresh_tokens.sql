-- =============================================================================
-- V8: Sessions y Refresh Tokens
-- sessions      : sesion de usuario ligada a (tenant, app, user). Agrupa RTs.
-- refresh_tokens: hash SHA-256 del token plano. El plano NUNCA se persiste.
--   Rotacion: token anterior -> USED, replaced_by_id apunta al nuevo.
-- =============================================================================
CREATE TABLE sessions (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID        NOT NULL REFERENCES tenants(id)      ON DELETE CASCADE,
    client_app_id    UUID        NOT NULL REFERENCES client_apps(id)  ON DELETE CASCADE,
    user_id          UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at       TIMESTAMPTZ NOT NULL,
    last_accessed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    user_agent       TEXT,
    ip_address       VARCHAR(64),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_sessions_status CHECK (status IN ('ACTIVE', 'TERMINATED', 'EXPIRED'))
);
CREATE INDEX idx_sessions_user_tenant ON sessions(user_id, tenant_id);
CREATE INDEX idx_sessions_status      ON sessions(status);
COMMENT ON TABLE sessions IS 'User session grouping all refresh tokens for a (tenant, app, user) context';
-- =============================================================================
CREATE TABLE refresh_tokens (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash       VARCHAR(64) NOT NULL UNIQUE,
    session_id       UUID        NOT NULL REFERENCES sessions(id)     ON DELETE CASCADE,
    tenant_id        UUID        NOT NULL REFERENCES tenants(id)      ON DELETE CASCADE,
    client_app_id    UUID        NOT NULL REFERENCES client_apps(id)  ON DELETE CASCADE,
    user_id          UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
    requested_scopes TEXT        NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at       TIMESTAMPTZ NOT NULL,
    used_at          TIMESTAMPTZ,
    replaced_by_id   UUID        REFERENCES refresh_tokens(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_refresh_tokens_status CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED', 'REVOKED'))
);
CREATE INDEX idx_refresh_tokens_hash        ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_session     ON refresh_tokens(session_id);
CREATE INDEX idx_refresh_tokens_user_tenant ON refresh_tokens(user_id, tenant_id);
CREATE INDEX idx_refresh_tokens_status      ON refresh_tokens(status);
COMMENT ON TABLE  refresh_tokens                IS 'Hashed refresh tokens for OAuth2 token rotation (RFC 6749 §6)';
COMMENT ON COLUMN refresh_tokens.token_hash     IS 'SHA-256 hex (64 chars) of the raw token. Raw token is never stored.';
COMMENT ON COLUMN refresh_tokens.replaced_by_id IS 'Self-reference to the new token after rotation (audit trail).';
