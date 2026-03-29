-- =============================================================================
-- V6: Memberships — roles y acceso de usuarios a apps
-- app_roles      : roles definidos por app (no globales)
-- memberships    : acceso de un TenantUser a una ClientApp
-- membership_roles: roles asignados dentro de una membresia (PK compuesta)
-- =============================================================================
CREATE TABLE app_roles (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    code          VARCHAR(50)  NOT NULL,
    display_name  VARCHAR(255),
    description   TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_roles_app_code UNIQUE (client_app_id, code),
    CONSTRAINT chk_app_roles_code    CHECK (code ~ '^[a-z][a-z0-9_-]*$')
);
CREATE TABLE memberships (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
    client_app_id UUID        NOT NULL REFERENCES client_apps(id)  ON DELETE CASCADE,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_memberships_user_app UNIQUE (user_id, client_app_id),
    CONSTRAINT chk_memberships_status  CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING'))
);
CREATE TABLE membership_roles (
    membership_id UUID        NOT NULL REFERENCES memberships(id) ON DELETE CASCADE,
    role_id       UUID        NOT NULL REFERENCES app_roles(id)   ON DELETE CASCADE,
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_membership_roles PRIMARY KEY (membership_id, role_id)
);
CREATE INDEX idx_app_roles_client_app_id   ON app_roles(client_app_id);
CREATE INDEX idx_memberships_user_id       ON memberships(user_id);
CREATE INDEX idx_memberships_client_app_id ON memberships(client_app_id);
CREATE INDEX idx_memberships_status        ON memberships(status);
CREATE INDEX idx_membership_roles_role_id  ON membership_roles(role_id);
CREATE TRIGGER app_roles_updated_at
    BEFORE UPDATE ON app_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER memberships_updated_at
    BEFORE UPDATE ON memberships
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE app_roles        IS 'Roles defined per ClientApp (not global). Code must be lowercase alphanumeric + underscores/hyphens.';
COMMENT ON TABLE memberships      IS 'Access grant from a TenantUser to a ClientApp';
COMMENT ON TABLE membership_roles IS 'Many-to-many: membership <-> app_role. PK is composite — no separate id column.';
