-- =============================================================================
-- V9: Email Verifications — codigos de verificacion para auto-registro
-- La fila con mayor created_at por usuario es el codigo activo.
-- Codigo de 6 digitos (SecureRandom), expira en 30 minutos.
-- Un codigo ya usado (used_at IS NOT NULL) no puede reutilizarse.
-- =============================================================================
CREATE TABLE email_verifications (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_user_id UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
    code           VARCHAR(10) NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL,
    used_at        TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_email_verifications_user ON email_verifications(tenant_user_id);
CREATE INDEX idx_email_verifications_code ON email_verifications(code);
COMMENT ON TABLE  email_verifications          IS 'Email verification codes for user self-registration (ResendVerificationEmailUseCase)';
COMMENT ON COLUMN email_verifications.code     IS '6-digit numeric code generated with SecureRandom';
COMMENT ON COLUMN email_verifications.expires_at IS 'Code expires 30 minutes after creation';
COMMENT ON COLUMN email_verifications.used_at  IS 'Timestamp of successful verification. Non-null = already used.';
