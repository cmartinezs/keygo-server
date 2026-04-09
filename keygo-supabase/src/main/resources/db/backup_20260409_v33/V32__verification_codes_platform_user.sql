-- =============================================================================
-- V32 — verification_codes: soporte para usuarios de plataforma
--
-- Agrega columna platform_user_id (nullable) con FK a platform_users.
-- Relaja el constraint para aceptar tenant_user_id O platform_user_id (uno de los dos).
-- =============================================================================

-- 1. Agregar columna nullable
ALTER TABLE verification_codes
    ADD COLUMN platform_user_id UUID REFERENCES platform_users(id) ON DELETE CASCADE;

-- 2. Reemplazar constraint: al menos uno de los dos debe ser no-null
ALTER TABLE verification_codes
    DROP CONSTRAINT chk_vc_tenant_user_not_null;

ALTER TABLE verification_codes
    ADD CONSTRAINT chk_vc_user_not_null
        CHECK (tenant_user_id IS NOT NULL OR platform_user_id IS NOT NULL);

-- 3. Relajar NOT NULL en tenant_user_id (ahora puede ser null si platform_user_id está)
ALTER TABLE verification_codes
    ALTER COLUMN tenant_user_id DROP NOT NULL;

-- 4. Índice para búsqueda por platform_user + purpose
CREATE INDEX idx_vc_platform_user ON verification_codes(platform_user_id);

-- 5. Índice parcial único: un solo código activo por platform_user + purpose
CREATE UNIQUE INDEX uq_vc_platform_user_purpose
    ON verification_codes(platform_user_id, purpose)
    WHERE used_at IS NULL AND platform_user_id IS NOT NULL;

COMMENT ON COLUMN verification_codes.platform_user_id IS 'FK a platform_users — usado cuando el código es para un usuario de plataforma (no de tenant)';
