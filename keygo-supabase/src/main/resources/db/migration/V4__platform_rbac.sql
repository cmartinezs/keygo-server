-- ============================================================================
-- V4__platform_rbac.sql
-- Platform RBAC stays independent from tenant and app RBAC.
-- Hierarchy is modeled as a simple tree: one parent per child role.
-- ============================================================================

CREATE TABLE platform_roles (
    id            UUID           NOT NULL DEFAULT gen_random_uuid(),
    code          VARCHAR(50)    NOT NULL,
    display_name  VARCHAR(255)   NOT NULL,
    description   TEXT,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_platform_roles PRIMARY KEY (id),
    CONSTRAINT uq_platform_roles_code UNIQUE (code),
    CONSTRAINT chk_platform_roles_code CHECK (regexp_match(code, '^[A-Z][A-Z0-9_]*$') IS NOT NULL)
);

CREATE TRIGGER trg_platform_roles_updated_at
    BEFORE UPDATE ON platform_roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE platform_role_hierarchy (
    child_role_id   UUID         NOT NULL,
    parent_role_id  UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_platform_role_hierarchy PRIMARY KEY (child_role_id),
    CONSTRAINT fk_platform_role_hierarchy_child
        FOREIGN KEY (child_role_id) REFERENCES platform_roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_platform_role_hierarchy_parent
        FOREIGN KEY (parent_role_id) REFERENCES platform_roles(id) ON DELETE CASCADE,
    CONSTRAINT chk_platform_role_hierarchy_not_self CHECK (child_role_id <> parent_role_id)
);

CREATE OR REPLACE FUNCTION validate_platform_role_hierarchy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_depth INTEGER;
BEGIN
    WITH RECURSIVE lineage(role_id, depth) AS (
        SELECT NEW.parent_role_id, 1
        UNION ALL
        SELECT prh.parent_role_id, lineage.depth + 1
        FROM platform_role_hierarchy prh
        JOIN lineage ON prh.child_role_id = lineage.role_id
    )
    SELECT COALESCE(MAX(depth), 0)
      INTO v_depth
      FROM lineage;

    IF EXISTS (
        WITH RECURSIVE lineage(role_id) AS (
            SELECT NEW.parent_role_id
            UNION ALL
            SELECT prh.parent_role_id
            FROM platform_role_hierarchy prh
            JOIN lineage ON prh.child_role_id = lineage.role_id
        )
        SELECT 1
        FROM lineage
        WHERE role_id = NEW.child_role_id
    ) THEN
        RAISE EXCEPTION 'Platform role hierarchy cycle detected for child role %', NEW.child_role_id;
    END IF;

    IF v_depth >= 5 THEN
        RAISE EXCEPTION 'Platform role hierarchy depth limit exceeded for child role %', NEW.child_role_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_platform_role_hierarchy_validate
    BEFORE INSERT OR UPDATE ON platform_role_hierarchy
    FOR EACH ROW
    EXECUTE FUNCTION validate_platform_role_hierarchy();

CREATE TABLE platform_user_roles (
    id                           UUID          NOT NULL DEFAULT gen_random_uuid(),
    platform_user_id             UUID          NOT NULL,
    role_id                      UUID          NOT NULL,
    scope_type                   VARCHAR(20)   NOT NULL,
    contractor_id                UUID,
    tenant_id                    UUID,
    assigned_at                  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_at                   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_platform_user_roles PRIMARY KEY (id),
    CONSTRAINT fk_platform_user_roles_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_platform_user_roles_role
        FOREIGN KEY (role_id) REFERENCES platform_roles(id) ON DELETE RESTRICT,
    CONSTRAINT chk_platform_user_roles_scope CHECK (
        (scope_type = 'GLOBAL' AND contractor_id IS NULL AND tenant_id IS NULL) OR
        (scope_type = 'CONTRACTOR' AND contractor_id IS NOT NULL AND tenant_id IS NULL) OR
        (scope_type = 'TENANT' AND contractor_id IS NULL AND tenant_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uq_platform_user_roles_global
    ON platform_user_roles(platform_user_id, role_id)
    WHERE scope_type = 'GLOBAL';

CREATE UNIQUE INDEX uq_platform_user_roles_contractor
    ON platform_user_roles(platform_user_id, role_id, contractor_id)
    WHERE scope_type = 'CONTRACTOR';

CREATE UNIQUE INDEX uq_platform_user_roles_tenant
    ON platform_user_roles(platform_user_id, role_id, tenant_id)
    WHERE scope_type = 'TENANT';

CREATE INDEX idx_platform_user_roles_platform_user ON platform_user_roles(platform_user_id);
CREATE INDEX idx_platform_user_roles_role_scope ON platform_user_roles(role_id, scope_type);

CREATE TRIGGER trg_platform_user_roles_updated_at
    BEFORE UPDATE ON platform_user_roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE platform_roles IS 'Global Keygo roles. These roles never live inside client app RBAC.';
COMMENT ON TABLE platform_role_hierarchy IS 'Platform role inheritance tree. One parent per child role.';
COMMENT ON TABLE platform_user_roles IS 'Platform role assignments with explicit scope: GLOBAL, CONTRACTOR or TENANT.';
COMMENT ON COLUMN platform_user_roles.contractor_id IS 'Nullable until contractors table exists. FK is added in V12.';
COMMENT ON COLUMN platform_user_roles.tenant_id IS 'Nullable until tenants table exists. FK is added in V5.';
