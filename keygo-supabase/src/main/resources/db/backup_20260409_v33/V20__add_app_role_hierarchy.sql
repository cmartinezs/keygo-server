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

-- ---------------------------------------------------------------------------
-- Seed: jerarquía de roles para keygo-ui
--
--   admin  ──inherits──▶  admin_tenant  ──inherits──▶  user_tenant
--
-- Convención: child_role_id hereda los permisos de parent_role_id.
-- Los roles más altos acumulan los permisos de los niveles inferiores.
-- FKs resueltos con subqueries semánticas (nunca UUID hardcodeado).
-- ---------------------------------------------------------------------------

-- keygo-ui: admin hereda de admin_tenant
INSERT INTO app_role_hierarchy (child_role_id, parent_role_id)
VALUES (
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'keygo-ui' AND ar.code = 'admin'),
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'keygo-ui' AND ar.code = 'admin_tenant')
)
ON CONFLICT (child_role_id) DO NOTHING;

-- keygo-ui: admin_tenant hereda de user_tenant
INSERT INTO app_role_hierarchy (child_role_id, parent_role_id)
VALUES (
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'keygo-ui' AND ar.code = 'admin_tenant'),
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'keygo-ui' AND ar.code = 'user_tenant')
)
ON CONFLICT (child_role_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Seed: jerarquía de roles para demo-ui
--
--   demo_admin  ──inherits──▶  demo_user
-- ---------------------------------------------------------------------------

-- demo-ui: demo_admin hereda de demo_user
INSERT INTO app_role_hierarchy (child_role_id, parent_role_id)
VALUES (
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'demo-ui' AND ar.code = 'demo_admin'),
    (SELECT ar.id FROM app_roles ar
     JOIN client_apps ca ON ca.id = ar.client_app_id
     WHERE ca.client_id = 'demo-ui' AND ar.code = 'demo_user')
)
ON CONFLICT (child_role_id) DO NOTHING;

