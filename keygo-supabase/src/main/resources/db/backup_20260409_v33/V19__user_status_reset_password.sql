-- =============================================================================
-- V19 — Ampliar constraint de status en tenant_users: agregar RESET_PASSWORD
-- =============================================================================
-- El estado RESET_PASSWORD indica que el usuario fue aprovisionado con una
-- contraseña temporal (ej. creado automáticamente al verificar un contrato de
-- billing) y debe cambiarla en su primer inicio de sesión.
--
-- Prerrequisito: V5__tenant_users.sql (constraint chk_tenant_users_status)
-- Próxima migración: V20__...
-- =============================================================================

-- Eliminar el constraint anterior y recrearlo con el nuevo valor
ALTER TABLE tenant_users
    DROP CONSTRAINT chk_tenant_users_status,
    ADD  CONSTRAINT chk_tenant_users_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING', 'RESET_PASSWORD'));

-- Actualizar el comentario del campo
COMMENT ON COLUMN tenant_users.status IS
    'PENDING = email not verified  ACTIVE = operational  SUSPENDED = access blocked  RESET_PASSWORD = must change temporary password';

