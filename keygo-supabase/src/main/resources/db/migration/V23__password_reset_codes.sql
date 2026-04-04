-- =============================================================================
-- V23 — Tabla password_reset_codes: códigos de verificación para el flujo
--        de cambio de contraseña forzado (status=RESET_PASSWORD en tenant_users).
-- =============================================================================
-- Cuando un usuario con status=RESET_PASSWORD intenta iniciar sesión:
--   1. Se validan sus credenciales (contraseña temporal).
--   2. Se genera un código de 6 dígitos con TTL de 15 minutos y se almacena aquí.
--   3. Se envía el código al email del usuario.
--   4. El login devuelve 401 con código RESET_PASSWORD_REQUIRED.
--   5. El frontend redirige al formulario de reset de contraseña que recibe:
--      email, contraseña temporal (current), nueva contraseña, confirmación y código.
--
-- Restricciones:
--   - Un solo código activo por usuario (UNIQUE tenant_user_id → upsert).
--   - El campo used_at registra cuándo se consumió el código.
--   - Prerrequisito: V19__user_status_reset_password.sql
--   - Próxima migración: V24__...
-- =============================================================================

CREATE TABLE password_reset_codes (
  id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_user_id UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
  code           VARCHAR(6)  NOT NULL,
  expires_at     TIMESTAMPTZ NOT NULL,
  used_at        TIMESTAMPTZ,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_user_id)
);

CREATE INDEX idx_password_reset_codes_user ON password_reset_codes(tenant_user_id);

COMMENT ON TABLE  password_reset_codes IS 'Verification codes (6 digits, 15 min TTL) for the RESET_PASSWORD login-blocked flow';
COMMENT ON COLUMN password_reset_codes.code       IS '6-digit numeric code sent by email; one active code per user (upsert)';
COMMENT ON COLUMN password_reset_codes.expires_at IS 'Code expiry; generated at login-blocked moment + 15 minutes';
COMMENT ON COLUMN password_reset_codes.used_at    IS 'NULL while valid; set to now() when the code is successfully verified';

