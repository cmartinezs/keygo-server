-- V25: Add registration_policy column to client_apps
-- Enables self-registration flow control per app
ALTER TABLE client_apps
ADD COLUMN registration_policy VARCHAR(50) NOT NULL DEFAULT 'OPEN_NO_MEMBERSHIP';

-- Add comment for clarity
COMMENT ON COLUMN client_apps.registration_policy IS
  'Self-registration policy: OPEN_AUTO_ACTIVE, OPEN_AUTO_PENDING, OPEN_NO_MEMBERSHIP, or INVITE_ONLY';
