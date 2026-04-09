-- ============================================================================
-- V8__app_rbac_and_memberships.sql
-- App access is modeled with app_memberships and app-scoped roles.
-- ============================================================================

CREATE TABLE app_roles (
    id            UUID           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID           NOT NULL,
    client_app_id UUID           NOT NULL,
    code          VARCHAR(50)    NOT NULL,
    display_name  VARCHAR(255),
    description   TEXT,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_roles PRIMARY KEY (id),
    CONSTRAINT uq_app_roles_client_app_code UNIQUE (client_app_id, code),
    CONSTRAINT uq_app_roles_id_client_app UNIQUE (id, client_app_id),
    CONSTRAINT uq_app_roles_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT fk_app_roles_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_roles_client_app_tenant
        FOREIGN KEY (client_app_id, tenant_id)
        REFERENCES client_apps(id, tenant_id) ON DELETE CASCADE,
    CONSTRAINT chk_app_roles_code CHECK (regexp_match(code, '^[a-z][a-z0-9_-]*$') IS NOT NULL)
);

CREATE TRIGGER trg_app_roles_updated_at
    BEFORE UPDATE ON app_roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE app_role_hierarchy (
    child_role_id   UUID         NOT NULL,
    parent_role_id  UUID         NOT NULL,
    client_app_id   UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_role_hierarchy PRIMARY KEY (child_role_id),
    CONSTRAINT fk_app_role_hierarchy_child
        FOREIGN KEY (child_role_id, client_app_id)
        REFERENCES app_roles(id, client_app_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_role_hierarchy_parent
        FOREIGN KEY (parent_role_id, client_app_id)
        REFERENCES app_roles(id, client_app_id) ON DELETE CASCADE,
    CONSTRAINT chk_app_role_hierarchy_not_self CHECK (child_role_id <> parent_role_id)
);

CREATE OR REPLACE FUNCTION validate_app_role_hierarchy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_depth INTEGER;
BEGIN
    WITH RECURSIVE lineage(role_id, depth) AS (
        SELECT NEW.parent_role_id, 1
        UNION ALL
        SELECT arh.parent_role_id, lineage.depth + 1
        FROM app_role_hierarchy arh
        JOIN lineage ON arh.child_role_id = lineage.role_id
        WHERE arh.client_app_id = NEW.client_app_id
    )
    SELECT COALESCE(MAX(depth), 0)
      INTO v_depth
      FROM lineage;

    IF EXISTS (
        WITH RECURSIVE lineage(role_id) AS (
            SELECT NEW.parent_role_id
            UNION ALL
            SELECT arh.parent_role_id
            FROM app_role_hierarchy arh
            JOIN lineage ON arh.child_role_id = lineage.role_id
            WHERE arh.client_app_id = NEW.client_app_id
        )
        SELECT 1
        FROM lineage
        WHERE role_id = NEW.child_role_id
    ) THEN
        RAISE EXCEPTION 'App role hierarchy cycle detected for client_app_id %', NEW.client_app_id;
    END IF;

    IF v_depth >= 5 THEN
        RAISE EXCEPTION 'App role hierarchy depth limit exceeded for client_app_id %', NEW.client_app_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_app_role_hierarchy_validate
    BEFORE INSERT OR UPDATE ON app_role_hierarchy
    FOR EACH ROW
    EXECUTE FUNCTION validate_app_role_hierarchy();

CREATE TABLE app_memberships (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    tenant_id      UUID          NOT NULL,
    tenant_user_id UUID          NOT NULL,
    client_app_id  UUID          NOT NULL,
    status         VARCHAR(20)   NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_memberships PRIMARY KEY (id),
    CONSTRAINT uq_app_memberships_tenant_user_app UNIQUE (tenant_user_id, client_app_id),
    CONSTRAINT uq_app_memberships_id_client_app UNIQUE (id, client_app_id),
    CONSTRAINT uq_app_memberships_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT fk_app_memberships_tenant_user
        FOREIGN KEY (tenant_user_id, tenant_id)
        REFERENCES tenant_users(id, tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_memberships_client_app_tenant
        FOREIGN KEY (client_app_id, tenant_id)
        REFERENCES client_apps(id, tenant_id) ON DELETE CASCADE,
    CONSTRAINT chk_app_memberships_status CHECK (
        status IN ('INVITED', 'PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED')
    )
);

CREATE INDEX idx_app_memberships_client_app_status ON app_memberships(client_app_id, status);
CREATE INDEX idx_app_memberships_tenant_user ON app_memberships(tenant_user_id);

CREATE TRIGGER trg_app_memberships_updated_at
    BEFORE UPDATE ON app_memberships
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE app_membership_roles (
    membership_id  UUID         NOT NULL,
    client_app_id  UUID         NOT NULL,
    role_id        UUID         NOT NULL,
    assigned_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_membership_roles PRIMARY KEY (membership_id, role_id),
    CONSTRAINT fk_app_membership_roles_membership
        FOREIGN KEY (membership_id, client_app_id)
        REFERENCES app_memberships(id, client_app_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_membership_roles_role
        FOREIGN KEY (role_id, client_app_id)
        REFERENCES app_roles(id, client_app_id) ON DELETE CASCADE
);

COMMENT ON TABLE app_roles IS 'App-specific RBAC catalog, isolated from platform and tenant RBAC.';
COMMENT ON COLUMN app_roles.client_app_id IS 'Internal FK to client_apps.id. App RBAC never depends on the public OAuth client_id.';
COMMENT ON TABLE app_role_hierarchy IS 'App role inheritance tree limited to one parent per role and max depth five.';
COMMENT ON TABLE app_memberships IS 'Access grant from a tenant membership to a specific app inside the same tenant.';
COMMENT ON COLUMN app_memberships.client_app_id IS 'Internal FK to client_apps.id. app_memberships models access using technical keys, not protocol identifiers.';
COMMENT ON TABLE app_membership_roles IS 'Role assignments for an app membership. Composite FKs forbid cross-app assignments.';
