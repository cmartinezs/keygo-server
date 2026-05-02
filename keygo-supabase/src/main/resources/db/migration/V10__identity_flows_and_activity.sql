-- ============================================================================
-- V10__identity_flows_and_activity.sql
-- Global identity support flows and self-service activity feed.
-- ============================================================================

CREATE TABLE email_verifications (
    id                UUID           NOT NULL DEFAULT gen_random_uuid(),
    platform_user_id  UUID           NOT NULL,
    code              VARCHAR(10)    NOT NULL,
    expires_at        TIMESTAMPTZ    NOT NULL,
    used_at           TIMESTAMPTZ,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_email_verifications PRIMARY KEY (id),
    CONSTRAINT fk_email_verifications_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT chk_email_verifications_code CHECK (regexp_match(code, '^[0-9A-Za-z-]{4,10}$') IS NOT NULL)
);

CREATE UNIQUE INDEX uq_email_verifications_active_user
    ON email_verifications(platform_user_id)
    WHERE used_at IS NULL;

CREATE TRIGGER trg_email_verifications_updated_at
    BEFORE UPDATE ON email_verifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE password_reset_tokens (
    id                UUID           NOT NULL DEFAULT gen_random_uuid(),
    platform_user_id  UUID           NOT NULL,
    token_hash        VARCHAR(128)   NOT NULL,
    expires_at        TIMESTAMPTZ    NOT NULL,
    used_at           TIMESTAMPTZ,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT uq_password_reset_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT chk_password_reset_tokens_hash CHECK (regexp_match(token_hash, '^[A-Fa-f0-9]{64,128}$') IS NOT NULL)
);

CREATE UNIQUE INDEX uq_password_reset_tokens_active_user
    ON password_reset_tokens(platform_user_id)
    WHERE used_at IS NULL;

CREATE TRIGGER trg_password_reset_tokens_updated_at
    BEFORE UPDATE ON password_reset_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE platform_user_notification_preferences (
    id                         UUID          NOT NULL DEFAULT gen_random_uuid(),
    platform_user_id           UUID          NOT NULL,
    security_alerts_email      BOOLEAN       NOT NULL DEFAULT TRUE,
    security_alerts_in_app     BOOLEAN       NOT NULL DEFAULT TRUE,
    billing_alerts_email       BOOLEAN       NOT NULL DEFAULT TRUE,
    product_updates_email      BOOLEAN       NOT NULL DEFAULT FALSE,
    weekly_digest              BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at                 TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_platform_user_notification_preferences PRIMARY KEY (id),
    CONSTRAINT uq_platform_user_notification_preferences_user UNIQUE (platform_user_id),
    CONSTRAINT fk_platform_user_notification_preferences_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_platform_user_notification_preferences_updated_at
    BEFORE UPDATE ON platform_user_notification_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE platform_activity_events (
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    platform_user_id     UUID           NOT NULL,
    tenant_id            UUID,
    client_app_id        UUID,
    platform_session_id  UUID,
    oauth_session_id     UUID,
    event_type           VARCHAR(100)   NOT NULL,
    event_category       VARCHAR(50)    NOT NULL,
    metadata             JSONB          NOT NULL DEFAULT '{}'::jsonb,
    occurred_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_platform_activity_events PRIMARY KEY (id),
    CONSTRAINT fk_platform_activity_events_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_platform_activity_events_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL,
    CONSTRAINT fk_platform_activity_events_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE SET NULL,
    CONSTRAINT fk_platform_activity_events_platform_session
        FOREIGN KEY (platform_session_id) REFERENCES platform_sessions(id) ON DELETE SET NULL,
    CONSTRAINT fk_platform_activity_events_oauth_session
        FOREIGN KEY (oauth_session_id) REFERENCES oauth_sessions(id) ON DELETE SET NULL
);

CREATE INDEX idx_platform_activity_events_user_time
    ON platform_activity_events(platform_user_id, occurred_at DESC);

CREATE INDEX idx_platform_activity_events_category_time
    ON platform_activity_events(event_category, occurred_at DESC);

COMMENT ON TABLE email_verifications IS 'Platform account email verification codes.';
COMMENT ON TABLE password_reset_tokens IS 'Hashed password reset tokens for platform accounts.';
COMMENT ON TABLE platform_user_notification_preferences IS 'Global notification preferences for a platform account.';
COMMENT ON TABLE platform_activity_events IS 'Self-service friendly activity feed by platform account.';
