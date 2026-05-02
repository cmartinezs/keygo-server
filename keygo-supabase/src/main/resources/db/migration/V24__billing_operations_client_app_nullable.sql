-- V24: Make client_app_id nullable in billing operation tables
-- Aligns with V22/V23: platform-level contracts have no specific client app
ALTER TABLE invoices              ALTER COLUMN client_app_id DROP NOT NULL;
ALTER TABLE payment_transactions  ALTER COLUMN client_app_id DROP NOT NULL;
ALTER TABLE usage_counters        ALTER COLUMN client_app_id DROP NOT NULL;
