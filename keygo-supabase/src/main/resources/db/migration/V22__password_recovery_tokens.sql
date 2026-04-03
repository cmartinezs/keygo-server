-- V22__password_recovery_tokens.sql
-- Recovery tokens for self-service forgot-password flow (30 min TTL)

CREATE TABLE password_recovery_tokens (
  id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_user_id  UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
  token           VARCHAR(64) NOT NULL,
  expires_at      TIMESTAMPTZ NOT NULL,
  used_at         TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_user_id)
);

CREATE INDEX idx_pwd_recovery_token ON password_recovery_tokens(token);

COMMENT ON TABLE password_recovery_tokens IS 'Recovery tokens for self-service forgot-password flow (30 min TTL)';
COMMENT ON COLUMN password_recovery_tokens.token IS 'UUID without hyphens (32 hex chars) — one active token per user';
COMMENT ON COLUMN password_recovery_tokens.used_at IS 'NULL while unused; set to now() when the token is consumed';
