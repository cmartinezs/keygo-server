-- V22: Agrega código de verificación de email al contrato
--
-- En el flujo de contratación, el suscriptor aún no tiene un TenantUser,
-- por lo que no puede usarse la tabla email_verifications (que requiere
-- tenant_user_id). El código se almacena directamente en el contrato y
-- se invalida al verificarse o al expirar.

ALTER TABLE app_contracts
    ADD COLUMN IF NOT EXISTS verification_code            VARCHAR(10),
    ADD COLUMN IF NOT EXISTS verification_code_expires_at TIMESTAMPTZ;

COMMENT ON COLUMN app_contracts.verification_code IS
    'Código numérico corto enviado a contractor_email para verificación de email en el flujo de contratación';
COMMENT ON COLUMN app_contracts.verification_code_expires_at IS
    'Timestamp tras el cual el código de verificación ha expirado (configurable, default 30 min)';

