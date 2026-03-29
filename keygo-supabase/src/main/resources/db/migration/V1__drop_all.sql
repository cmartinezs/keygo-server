-- =============================================================================
-- V1: Drop all — pizarrón limpio
-- Elimina TODAS las tablas (CASCADE maneja FKs) y funciones utilitarias.
-- Idempotente: usa IF EXISTS en todo.
-- Prerequisito: ./docs/scripts/db/clean.sh  (borra flyway_schema_history)
-- =============================================================================

-- ── Billing payment support ───────────────────────────────────────────────────
DROP TABLE IF EXISTS payment_methods          CASCADE;
DROP TABLE IF EXISTS tenant_billing_profiles  CASCADE;

-- ── Billing invoicing ─────────────────────────────────────────────────────────
DROP TABLE IF EXISTS usage_counters CASCADE;
DROP TABLE IF EXISTS invoices       CASCADE;

-- ── Billing subscriptions ─────────────────────────────────────────────────────
DROP TABLE IF EXISTS payment_transactions CASCADE;
DROP TABLE IF EXISTS app_subscriptions    CASCADE;

-- ── Billing contracts ─────────────────────────────────────────────────────────
DROP TABLE IF EXISTS app_contracts CASCADE;

-- ── Billing catalog ───────────────────────────────────────────────────────────
DROP TABLE IF EXISTS app_plan_entitlements CASCADE;
DROP TABLE IF EXISTS app_plan_versions     CASCADE;
DROP TABLE IF EXISTS app_plans             CASCADE;

-- ── Auth / OAuth2 ─────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS email_verifications CASCADE;
DROP TABLE IF EXISTS refresh_tokens      CASCADE;
DROP TABLE IF EXISTS sessions            CASCADE;
DROP TABLE IF EXISTS authorization_codes CASCADE;
DROP TABLE IF EXISTS signing_keys        CASCADE;

-- ── Memberships (plural y legado singular) ────────────────────────────────────
DROP TABLE IF EXISTS membership_roles CASCADE;
DROP TABLE IF EXISTS memberships      CASCADE;
DROP TABLE IF EXISTS app_roles        CASCADE;
DROP TABLE IF EXISTS membership_role  CASCADE;
DROP TABLE IF EXISTS membership       CASCADE;
DROP TABLE IF EXISTS app_role         CASCADE;

-- ── Client Apps ───────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS client_allowed_scopes CASCADE;
DROP TABLE IF EXISTS client_allowed_grants CASCADE;
DROP TABLE IF EXISTS client_redirect_uris  CASCADE;
DROP TABLE IF EXISTS client_apps           CASCADE;

-- ── Tenant Users ──────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS tenant_users CASCADE;

-- ── Tenants ───────────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS tenants CASCADE;

-- ── Tablas legado (usuarios y OAuth globales, nunca usados en el modelo activo)
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles       CASCADE;
DROP TABLE IF EXISTS permissions      CASCADE;
DROP TABLE IF EXISTS roles            CASCADE;
DROP TABLE IF EXISTS audit_logs       CASCADE;
DROP TABLE IF EXISTS oauth_tokens     CASCADE;
DROP TABLE IF EXISTS oauth_providers  CASCADE;
DROP TABLE IF EXISTS users            CASCADE;

-- ── Funciones utilitarias ─────────────────────────────────────────────────────
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
