-- =============================================================================
-- V10: Billing Catalog — catalogo de planes por ClientApp (proveedor)
-- app_plans           : planes ofrecidos por una ClientApp a sus suscriptores
-- app_plan_versions   : snapshots inmutables de precio/periodo
-- app_plan_entitlements: limites y feature flags por version de plan
--
-- subscriber_type distingue si el plan es B2B (TENANT) o B2C (TENANT_USER).
-- Una misma app puede tener planes de ambos tipos simultaneamente.
-- =============================================================================
CREATE TABLE app_plans (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id   UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    subscriber_type VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_public       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_plans_app_code        UNIQUE (client_app_id, code),
    CONSTRAINT chk_app_plans_status         CHECK (status          IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_app_plans_subscriber_type CHECK (subscriber_type IN ('TENANT', 'TENANT_USER'))
);
CREATE INDEX idx_app_plans_client_app_type
    ON app_plans(client_app_id, subscriber_type, status)
    WHERE is_public = TRUE;
CREATE TRIGGER app_plans_updated_at
    BEFORE UPDATE ON app_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE  app_plans                 IS 'Billing plans defined by a ClientApp. subscriber_type = TENANT (B2B) or TENANT_USER (B2C).';
COMMENT ON COLUMN app_plans.subscriber_type IS 'TENANT = B2B (company/org); TENANT_USER = B2C (individual)';
-- =============================================================================
CREATE TABLE app_plan_versions (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    app_plan_id    UUID          NOT NULL REFERENCES app_plans(id) ON DELETE RESTRICT,
    version        VARCHAR(20)   NOT NULL,
    currency       VARCHAR(3)    NOT NULL DEFAULT 'MXN',
    billing_period VARCHAR(20)   NOT NULL,
    base_price     NUMERIC(12,2) NOT NULL DEFAULT 0,
    setup_fee      NUMERIC(12,2) NOT NULL DEFAULT 0,
    trial_days     INT           NOT NULL DEFAULT 0,
    effective_from DATE          NOT NULL,
    effective_to   DATE,
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_plan_versions_plan_version UNIQUE (app_plan_id, version),
    CONSTRAINT chk_app_plan_versions_period CHECK (billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')),
    CONSTRAINT chk_app_plan_versions_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED'))
);
CREATE INDEX idx_app_plan_versions_plan ON app_plan_versions(app_plan_id, status);
COMMENT ON TABLE app_plan_versions IS 'Immutable price/period snapshots. Existing subscriptions are NOT affected when new versions are published.';
-- =============================================================================
CREATE TABLE app_plan_entitlements (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    app_plan_version_id UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE CASCADE,
    metric_code         VARCHAR(100) NOT NULL,
    metric_type         VARCHAR(20)  NOT NULL,
    limit_value         BIGINT,
    period_type         VARCHAR(20)  NOT NULL DEFAULT 'NONE',
    enforcement_mode    VARCHAR(20)  NOT NULL DEFAULT 'HARD',
    is_enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_app_plan_entitlements_version_metric UNIQUE (app_plan_version_id, metric_code),
    CONSTRAINT chk_app_plan_entitlements_metric_type  CHECK (metric_type      IN ('QUOTA', 'BOOLEAN', 'RATE')),
    CONSTRAINT chk_app_plan_entitlements_period_type  CHECK (period_type      IN ('NONE', 'DAY', 'MONTH')),
    CONSTRAINT chk_app_plan_entitlements_enforcement  CHECK (enforcement_mode IN ('HARD', 'SOFT'))
);
COMMENT ON TABLE  app_plan_entitlements IS 'Feature limits and flags per plan version';
COMMENT ON COLUMN app_plan_entitlements.limit_value IS 'NULL = unlimited for QUOTA/RATE types';
