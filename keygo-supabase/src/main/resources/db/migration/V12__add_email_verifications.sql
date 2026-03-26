-- V12: Add email_verifications table for tenant user registration flow
-- Each row represents one verification attempt; the latest row per user is the active one.
CREATE TABLE email_verifications (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_user_id  UUID        NOT NULL
                                  REFERENCES tenant_users(id) ON DELETE CASCADE,
    code            VARCHAR(10) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    used_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_email_verifications_tenant_user_id
    ON email_verifications (tenant_user_id);

CREATE INDEX idx_email_verifications_code
    ON email_verifications (code);

