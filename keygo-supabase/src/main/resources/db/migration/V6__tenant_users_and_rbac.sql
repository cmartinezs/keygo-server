-- ============================================================================
-- V6__tenant_users_and_rbac.sql
-- Tenant participation is distinct from platform identity.
-- Tenant RBAC is isolated from platform and app RBAC.
-- ============================================================================

CREATE TABLE tenant_users (
    id                     UUID           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id              UUID           NOT NULL,
    platform_user_id       UUID           NOT NULL,
    local_username         VARCHAR(100),
    display_name_override  VARCHAR(255),
    status                 VARCHAR(20)    NOT NULL,
    created_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_tenant_users PRIMARY KEY (id),
    CONSTRAINT uq_tenant_users_tenant_platform_user UNIQUE (tenant_id, platform_user_id),
    CONSTRAINT uq_tenant_users_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT fk_tenant_users_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_users_platform_user
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE,
    CONSTRAINT chk_tenant_users_status CHECK (
        status IN ('INVITED', 'PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED')
    )
);

CREATE UNIQUE INDEX uq_tenant_users_tenant_local_username
    ON tenant_users(tenant_id, local_username)
    WHERE local_username IS NOT NULL;

CREATE INDEX idx_tenant_users_platform_user ON tenant_users(platform_user_id);
CREATE INDEX idx_tenant_users_tenant_status ON tenant_users(tenant_id, status);

CREATE TRIGGER trg_tenant_users_updated_at
    BEFORE UPDATE ON tenant_users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE tenant_roles (
    id            UUID           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id     UUID           NOT NULL,
    code          VARCHAR(50)    NOT NULL,
    display_name  VARCHAR(255),
    description   TEXT,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_tenant_roles PRIMARY KEY (id),
    CONSTRAINT uq_tenant_roles_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uq_tenant_roles_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT fk_tenant_roles_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_tenant_roles_updated_at
    BEFORE UPDATE ON tenant_roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE tenant_role_hierarchy (
    child_role_id   UUID         NOT NULL,
    parent_role_id  UUID         NOT NULL,
    tenant_id       UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_tenant_role_hierarchy PRIMARY KEY (child_role_id),
    CONSTRAINT fk_tenant_role_hierarchy_child
        FOREIGN KEY (child_role_id, tenant_id)
        REFERENCES tenant_roles(id, tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_role_hierarchy_parent
        FOREIGN KEY (parent_role_id, tenant_id)
        REFERENCES tenant_roles(id, tenant_id) ON DELETE CASCADE,
    CONSTRAINT chk_tenant_role_hierarchy_not_self CHECK (child_role_id <> parent_role_id)
);

CREATE OR REPLACE FUNCTION validate_tenant_role_hierarchy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_depth INTEGER;
BEGIN
    WITH RECURSIVE lineage(role_id, depth) AS (
        SELECT NEW.parent_role_id, 1
        UNION ALL
        SELECT trh.parent_role_id, lineage.depth + 1
        FROM tenant_role_hierarchy trh
        JOIN lineage ON trh.child_role_id = lineage.role_id
        WHERE trh.tenant_id = NEW.tenant_id
    )
    SELECT COALESCE(MAX(depth), 0)
      INTO v_depth
      FROM lineage;

    IF EXISTS (
        WITH RECURSIVE lineage(role_id) AS (
            SELECT NEW.parent_role_id
            UNION ALL
            SELECT trh.parent_role_id
            FROM tenant_role_hierarchy trh
            JOIN lineage ON trh.child_role_id = lineage.role_id
            WHERE trh.tenant_id = NEW.tenant_id
        )
        SELECT 1
        FROM lineage
        WHERE role_id = NEW.child_role_id
    ) THEN
        RAISE EXCEPTION 'Tenant role hierarchy cycle detected for tenant %', NEW.tenant_id;
    END IF;

    IF v_depth >= 5 THEN
        RAISE EXCEPTION 'Tenant role hierarchy depth limit exceeded for tenant %', NEW.tenant_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_tenant_role_hierarchy_validate
    BEFORE INSERT OR UPDATE ON tenant_role_hierarchy
    FOR EACH ROW
    EXECUTE FUNCTION validate_tenant_role_hierarchy();

CREATE TABLE tenant_user_roles (
    tenant_user_id  UUID         NOT NULL,
    tenant_id       UUID         NOT NULL,
    role_id         UUID         NOT NULL,
    assigned_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_tenant_user_roles PRIMARY KEY (tenant_user_id, role_id),
    CONSTRAINT fk_tenant_user_roles_tenant_user
        FOREIGN KEY (tenant_user_id, tenant_id)
        REFERENCES tenant_users(id, tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_user_roles_role
        FOREIGN KEY (role_id, tenant_id)
        REFERENCES tenant_roles(id, tenant_id) ON DELETE CASCADE
);

CREATE INDEX idx_tenant_user_roles_tenant_role ON tenant_user_roles(tenant_id, role_id);

COMMENT ON TABLE tenant_users IS 'Membership of a platform user in a tenant. No global credentials are duplicated here.';
COMMENT ON COLUMN tenant_users.id IS 'Technical UUID primary key for internal tenant membership relations.';
COMMENT ON COLUMN tenant_users.platform_user_id IS 'FK to the global identity root. tenant_users never owns global credentials.';
COMMENT ON COLUMN tenant_users.local_username IS 'Optional tenant-local alias for UX or legacy mapping. It is not the global platform identity.';
COMMENT ON TABLE tenant_roles IS 'Tenant-scoped RBAC catalog.';
COMMENT ON TABLE tenant_role_hierarchy IS 'Tenant role inheritance tree. One parent per child role inside the same tenant.';
COMMENT ON TABLE tenant_user_roles IS 'Assignments of tenant roles to tenant memberships with tenant integrity enforced by composite FKs.';
