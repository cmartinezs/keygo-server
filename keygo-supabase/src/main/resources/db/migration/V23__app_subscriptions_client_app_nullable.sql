-- V23: Make client_app_id nullable in app_subscriptions
-- Aligns with V22: platform-level contracts (NULL client_app_id) can have platform-level subscriptions
ALTER TABLE app_subscriptions ALTER COLUMN client_app_id DROP NOT NULL;
