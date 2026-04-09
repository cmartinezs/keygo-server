-- =============================================================================
-- V10: Billing Catalog — catalogo de planes por ClientApp (proveedor)
-- app_plans              : planes ofrecidos por una ClientApp a sus suscriptores
--                          sort_order controla el orden en la UI (menor = más barato)
-- app_plan_versions      : snapshots inmutables de configuración (sin precio)
-- app_plan_billing_options: períodos de facturación disponibles por versión
--                          (0 filas = plan gratuito; no se revisa el precio)
-- app_plan_entitlements  : límites y feature flags por versión de plan
-- =============================================================================
CREATE TABLE app_plans (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id   UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    subscriber_type VARCHAR(20)  NOT NULL DEFAULT 'TENANT',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_public       BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_plans_app_code UNIQUE (client_app_id, code),
    CONSTRAINT chk_app_plans_status  CHECK  (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_app_plans_subscriber_type CHECK (subscriber_type IN ('TENANT', 'TENANT_USER'))
);
CREATE INDEX idx_app_plans_client_app_status
    ON app_plans(client_app_id, status)
    WHERE is_public = TRUE;
CREATE TRIGGER app_plans_updated_at
    BEFORE UPDATE ON app_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE app_plans IS 'Billing plans defined by a ClientApp. sort_order controls display order (lower = cheaper / shown first).';
-- =============================================================================
CREATE TABLE app_plan_versions (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    app_plan_id    UUID          NOT NULL REFERENCES app_plans(id) ON DELETE RESTRICT,
    version        VARCHAR(20)   NOT NULL,
    currency       VARCHAR(3)    NOT NULL DEFAULT 'USD',
    setup_fee      NUMERIC(12,2) NOT NULL DEFAULT 0,
    trial_days     INT           NOT NULL DEFAULT 0,
    effective_from DATE          NOT NULL,
    effective_to   DATE,
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_plan_versions_plan_version UNIQUE (app_plan_id, version),
    CONSTRAINT chk_app_plan_versions_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED'))
);
CREATE INDEX idx_app_plan_versions_plan ON app_plan_versions(app_plan_id, status);
COMMENT ON TABLE app_plan_versions IS 'Immutable configuration snapshots. Pricing is defined in app_plan_billing_options. Existing subscriptions are NOT affected when new versions are published.';
-- =============================================================================
CREATE TABLE app_plan_billing_options (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    app_plan_version_id UUID          NOT NULL REFERENCES app_plan_versions(id) ON DELETE CASCADE,
    billing_period      VARCHAR(20)   NOT NULL,
    base_price          NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_pct        NUMERIC(5,2)  NOT NULL DEFAULT 0,
    is_default          BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_app_plan_billing_options_version_period UNIQUE (app_plan_version_id, billing_period),
    CONSTRAINT chk_app_plan_billing_options_period CHECK (billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')),
    CONSTRAINT chk_app_plan_billing_options_discount CHECK (discount_pct >= 0 AND discount_pct <= 100)
);
CREATE INDEX idx_app_plan_billing_options_version ON app_plan_billing_options(app_plan_version_id);
COMMENT ON TABLE app_plan_billing_options IS 'Available billing periods per plan version. Zero rows means the plan is free (no payment required). discount_pct is informational for UI (saving vs equivalent monthly periods).';
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
