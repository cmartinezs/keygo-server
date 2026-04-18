-- Adds is_default flag to app_roles.
-- At most one role per app can be the default (enforced by unique partial index).
-- Application layer ensures at least one default exists once the first role is created.

ALTER TABLE app_roles
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT false;

CREATE UNIQUE INDEX uq_app_roles_client_app_default
    ON app_roles(client_app_id)
    WHERE is_default = true;

-- Mark the first (lowest UUID) role per app as default for existing data.
UPDATE app_roles r
SET is_default = true
WHERE r.id = (
    SELECT id FROM app_roles r2
    WHERE r2.client_app_id = r.client_app_id
    ORDER BY r2.created_at, r2.id
    LIMIT 1
);
