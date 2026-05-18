-- Adds access_restriction column to client_apps.
-- Controls token-time access: CLOSED (default) requires ACTIVE membership; OPEN_JOIN allows any tenant user.

ALTER TABLE client_apps
    ADD COLUMN access_restriction VARCHAR(50) NOT NULL DEFAULT 'CLOSED';
