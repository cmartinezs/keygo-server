-- =============================================================================
-- V24: Platform Roles — roles a nivel de plataforma (Keygo)
--
-- Crea tablas para el primer nivel de RBAC multi-ámbito:
--   - platform_roles: catálogo de roles globales de la plataforma
--   - platform_user_roles: asignación de roles de plataforma a usuarios (via tenant_users)
--
-- NOTA ARQUITECTURAL: En el modelo actual, los usuarios de plataforma se representan
-- como TenantUsers en el tenant keygo. La FK referencia tenant_users.id.
-- Esto es una decisión pragmática; en el futuro se puede introducir una tabla platform_users
-- separada (T-112) sin romper este modelo.
-- =============================================================================

-- ── Tabla: platform_roles ────────────────────────────────────────────────────
CREATE TABLE platform_roles (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_platform_roles_code UNIQUE (code)
);

CREATE INDEX idx_platform_roles_code ON platform_roles(code);

CREATE TRIGGER platform_roles_updated_at
    BEFORE UPDATE ON platform_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  platform_roles      IS 'Catalog of global platform-level roles (e.g. KEYGO_ADMIN)';
COMMENT ON COLUMN platform_roles.code IS 'Unique role code — by convention lowercase with underscores';

-- ── Tabla: platform_user_roles ───────────────────────────────────────────────
CREATE TABLE platform_user_roles (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_user_id    UUID         NOT NULL REFERENCES tenant_users(id)   ON DELETE CASCADE,
    platform_role_id  UUID         NOT NULL REFERENCES platform_roles(id) ON DELETE CASCADE,
    assigned_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_platform_user_roles_user_role UNIQUE (tenant_user_id, platform_role_id)
);

CREATE INDEX idx_platform_user_roles_tenant_user_id   ON platform_user_roles(tenant_user_id);
CREATE INDEX idx_platform_user_roles_platform_role_id ON platform_user_roles(platform_role_id);

CREATE TRIGGER platform_user_roles_updated_at
    BEFORE UPDATE ON platform_user_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  platform_user_roles                IS 'N:N assignment of platform roles to users (via tenant_users in keygo tenant)';
COMMENT ON COLUMN platform_user_roles.tenant_user_id IS 'FK to tenant_users — the platform admin (must belong to keygo tenant)';
COMMENT ON COLUMN platform_user_roles.assigned_at    IS 'When the role was granted';
