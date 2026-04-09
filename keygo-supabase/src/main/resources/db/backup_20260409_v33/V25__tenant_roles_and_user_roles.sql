-- =============================================================================
-- V25: Tenant Roles — roles a nivel de tenant
--
-- Crea tablas para el segundo nivel de RBAC multi-ámbito:
--   - tenant_roles: roles definidos por cada tenant (ej. TENANT_ADMIN, EDITOR)
--   - tenant_user_roles: asignación N:N con soporte de soft-delete (removed_at)
--
-- Invariantes:
--   - Un tenant solo puede tener un rol con el mismo code (UNIQUE tenant_id, code)
--   - Una asignación activa es unique por (tenant_user_id, tenant_role_id) WHERE removed_at IS NULL
--   - Las asignaciones históricas (removed_at NOT NULL) se conservan para auditoría
-- =============================================================================

-- ── Tabla: tenant_roles ──────────────────────────────────────────────────────
CREATE TABLE tenant_roles (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_tenant_roles_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_tenant_roles_tenant_id ON tenant_roles(tenant_id);
CREATE INDEX idx_tenant_roles_code      ON tenant_roles(code);
CREATE INDEX idx_tenant_roles_active    ON tenant_roles(active);

CREATE TRIGGER tenant_roles_updated_at
    BEFORE UPDATE ON tenant_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  tenant_roles         IS 'Tenant-scoped role catalog; each tenant defines its own organizational roles';
COMMENT ON COLUMN tenant_roles.code    IS 'Role code unique within tenant — by convention UPPERCASE_WITH_UNDERSCORES';
COMMENT ON COLUMN tenant_roles.active  IS 'Inactive roles cannot be assigned but existing assignments remain until revoked';

-- ── Tabla: tenant_user_roles ─────────────────────────────────────────────────
CREATE TABLE tenant_user_roles (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_user_id   UUID         NOT NULL REFERENCES tenant_users(id)  ON DELETE CASCADE,
    tenant_role_id   UUID         NOT NULL REFERENCES tenant_roles(id)  ON DELETE CASCADE,
    assigned_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    removed_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Partial unique index: enforce at most one active assignment per (user, role)
CREATE UNIQUE INDEX uq_tenant_user_roles_active
    ON tenant_user_roles (tenant_user_id, tenant_role_id)
    WHERE removed_at IS NULL;

CREATE INDEX idx_tenant_user_roles_tenant_user_id ON tenant_user_roles(tenant_user_id);
CREATE INDEX idx_tenant_user_roles_tenant_role_id ON tenant_user_roles(tenant_role_id);
CREATE INDEX idx_tenant_user_roles_removed_at     ON tenant_user_roles(removed_at);

CREATE TRIGGER tenant_user_roles_updated_at
    BEFORE UPDATE ON tenant_user_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  tenant_user_roles              IS 'N:N assignment of tenant roles to tenant users with soft-delete (removed_at) for audit history';
COMMENT ON COLUMN tenant_user_roles.removed_at   IS 'NULL = active assignment; NOT NULL = revoked at this timestamp';
