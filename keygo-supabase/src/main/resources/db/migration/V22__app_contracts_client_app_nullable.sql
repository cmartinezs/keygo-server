-- V22: Make client_app_id nullable in app_contracts
-- NULL = platform-level contract (no specific client app)
-- NOT NULL = app-level contract (tied to a specific client app)
ALTER TABLE app_contracts ALTER COLUMN client_app_id DROP NOT NULL;
