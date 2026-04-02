-- V20: Role hierarchy support
-- Adds the app_role_hierarchy table to allow roles within a single client app
-- to inherit from a parent role. Each role may have at most one parent.
-- Maximum depth is enforced at the application layer (max 5 levels).

CREATE TABLE app_role_hierarchy (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    child_role_id   UUID        NOT NULL REFERENCES app_roles(id) ON DELETE CASCADE,
    parent_role_id  UUID        NOT NULL REFERENCES app_roles(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_app_role_hierarchy        PRIMARY KEY (id),
    -- Each role may have at most one direct parent
    CONSTRAINT uq_role_hierarchy_child      UNIQUE (child_role_id),
    -- Self-referencing not allowed
    CONSTRAINT chk_no_self_ref              CHECK  (child_role_id <> parent_role_id)
);

CREATE INDEX idx_role_hierarchy_child  ON app_role_hierarchy (child_role_id);
CREATE INDEX idx_role_hierarchy_parent ON app_role_hierarchy (parent_role_id);

COMMENT ON TABLE  app_role_hierarchy                IS 'Direct parent-child relationships between roles within a client app.';
COMMENT ON COLUMN app_role_hierarchy.child_role_id  IS 'Role that inherits from parent_role_id.';
COMMENT ON COLUMN app_role_hierarchy.parent_role_id IS 'Role whose permissions are inherited by child_role_id.';
