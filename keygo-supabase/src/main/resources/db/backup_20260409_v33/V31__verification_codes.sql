-- =============================================================================
-- V31 — Tabla unificada verification_codes
--
-- Consolida 3 tablas casi idénticas en una sola con discriminador 'purpose':
--   - email_verifications   (V9)  → purpose = 'EMAIL_VERIFICATION'
--   - password_reset_codes  (V23) → purpose = 'PASSWORD_RESET'
--   - password_recovery_tokens (V22, ya eliminado) → purpose = 'PASSWORD_RECOVERY'
--
-- Cada fila = un código/token de un solo uso con TTL configurable.
-- Un solo código activo (no usado) por usuario + propósito.
-- =============================================================================

CREATE TABLE verification_codes (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_user_id   UUID         REFERENCES tenant_users(id) ON DELETE CASCADE,
    purpose          VARCHAR(30)  NOT NULL,
    code             VARCHAR(64)  NOT NULL,
    expires_at       TIMESTAMPTZ  NOT NULL,
    used_at          TIMESTAMPTZ,
    metadata         JSONB,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_vc_purpose CHECK (purpose IN (
        'EMAIL_VERIFICATION', 'PASSWORD_RESET', 'PASSWORD_RECOVERY'
    )),
    CONSTRAINT chk_vc_tenant_user_not_null CHECK (tenant_user_id IS NOT NULL)
);

-- Un solo código activo (no usado) por usuario + propósito
CREATE UNIQUE INDEX uq_vc_tenant_user_purpose
    ON verification_codes(tenant_user_id, purpose)
    WHERE used_at IS NULL;

CREATE INDEX idx_vc_tenant_user ON verification_codes(tenant_user_id);
CREATE INDEX idx_vc_code ON verification_codes(code);
CREATE INDEX idx_vc_purpose ON verification_codes(purpose);

COMMENT ON TABLE verification_codes IS 'Tabla unificada de códigos de verificación: email, reset, recovery';
COMMENT ON COLUMN verification_codes.purpose IS 'Discriminador: EMAIL_VERIFICATION, PASSWORD_RESET, PASSWORD_RECOVERY';
COMMENT ON COLUMN verification_codes.code IS 'Código de 6 dígitos o token hex de hasta 64 chars';
COMMENT ON COLUMN verification_codes.metadata IS 'JSON opcional para datos específicos del flujo (e.g., request_id, tenant_slug)';
COMMENT ON COLUMN verification_codes.used_at IS 'NULL mientras no se use; set a now() cuando se consume';

-- Migrar datos existentes
INSERT INTO verification_codes (id, tenant_user_id, purpose, code, expires_at, used_at, created_at)
SELECT id, tenant_user_id, 'EMAIL_VERIFICATION', code, expires_at, used_at, created_at
FROM email_verifications;

INSERT INTO verification_codes (id, tenant_user_id, purpose, code, expires_at, used_at, created_at)
SELECT id, tenant_user_id, 'PASSWORD_RESET', code, expires_at, used_at, created_at
FROM password_reset_codes;

-- Drop tablas viejas (ya no se necesitan)
DROP TABLE IF EXISTS email_verifications;
DROP TABLE IF EXISTS password_reset_codes;
DROP TABLE IF EXISTS password_recovery_tokens;
