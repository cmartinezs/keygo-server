-- =========================================================
-- V10: Rename membership-related tables to plural form
-- Renombrar tablas de membresía de singular a plural
-- =========================================================
-- Version: 10.0
-- Date: 2026-03-22
-- Description: Renames app_role, membership, and membership_role to their plural forms
--              (app_roles, memberships, membership_roles) to follow the PostgreSQL naming
--              convention used by all other tables in this schema. This corrects an
--              inconsistency introduced in V7 where these tables were created in singular.
-- Descripción: Renombra app_role, membership y membership_role a sus formas en plural
--              (app_roles, memberships, membership_roles) para seguir la convención de
--              nomenclatura PostgreSQL usada por todas las demás tablas del esquema.
--              Corrige una inconsistencia introducida en V7.
-- =========================================================

-- ---------------------------------------------------------
-- 1. Rename: app_role → app_roles
-- ---------------------------------------------------------

-- Rename table
ALTER TABLE app_role RENAME TO app_roles;

-- Rename foreign key constraint
ALTER TABLE app_roles RENAME CONSTRAINT fk_app_role_client_app TO fk_app_roles_client_app;

-- Rename unique constraint (and its implicit index)
ALTER TABLE app_roles RENAME CONSTRAINT uq_app_role_client_app_code TO uq_app_roles_client_app_code;

-- Rename check constraint
ALTER TABLE app_roles RENAME CONSTRAINT ck_app_role_code TO ck_app_roles_code;

-- Rename explicit indexes
ALTER INDEX IF EXISTS idx_app_role_client_app_id RENAME TO idx_app_roles_client_app_id;
ALTER INDEX IF EXISTS idx_app_role_code RENAME TO idx_app_roles_code;

-- ---------------------------------------------------------
-- 2. Rename: membership → memberships
-- ---------------------------------------------------------

-- Rename table
ALTER TABLE membership RENAME TO memberships;

-- Rename foreign key constraints
ALTER TABLE memberships RENAME CONSTRAINT fk_membership_user TO fk_memberships_user;
ALTER TABLE memberships RENAME CONSTRAINT fk_membership_client_app TO fk_memberships_client_app;

-- Rename unique constraint (and its implicit index)
ALTER TABLE memberships RENAME CONSTRAINT uq_membership_user_app TO uq_memberships_user_app;

-- Rename explicit indexes
ALTER INDEX IF EXISTS idx_membership_user_id RENAME TO idx_memberships_user_id;
ALTER INDEX IF EXISTS idx_membership_client_app_id RENAME TO idx_memberships_client_app_id;
ALTER INDEX IF EXISTS idx_membership_status RENAME TO idx_memberships_status;

-- ---------------------------------------------------------
-- 3. Rename: membership_role → membership_roles
--    NOTE: This table's FK fk_membership_role_membership previously referenced
--    the table "membership" (now "memberships"). PostgreSQL updates the FK target
--    automatically on table rename; no manual update needed for the FK reference.
--    Similarly, fk_membership_role_app_role referenced "app_role" (now "app_roles").
-- ---------------------------------------------------------

-- Rename table
ALTER TABLE membership_role RENAME TO membership_roles;

-- Rename primary key constraint (also renames its implicit index)
ALTER TABLE membership_roles RENAME CONSTRAINT pk_membership_role TO pk_membership_roles;

-- Rename foreign key constraints
ALTER TABLE membership_roles RENAME CONSTRAINT fk_membership_role_membership TO fk_membership_roles_membership;
ALTER TABLE membership_roles RENAME CONSTRAINT fk_membership_role_app_role TO fk_membership_roles_app_role;

-- Rename explicit indexes
ALTER INDEX IF EXISTS idx_membership_role_role_id RENAME TO idx_membership_roles_role_id;

-- ---------------------------------------------------------
-- Comments
-- ---------------------------------------------------------
COMMENT ON TABLE app_roles        IS 'Application-scoped roles. Each role belongs to a single client app. / Roles por aplicación. Cada rol pertenece a una única app cliente.';
COMMENT ON TABLE memberships      IS 'User access grants to a client app. One membership = one user + one app. / Acceso de usuario a una app cliente. Una membresía = un usuario + una app.';
COMMENT ON TABLE membership_roles IS 'Join table: memberships ↔ app_roles (many-to-many). / Tabla de unión: memberships ↔ app_roles (muchos-a-muchos).';

