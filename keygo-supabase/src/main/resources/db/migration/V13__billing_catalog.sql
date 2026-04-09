-- ============================================================================
-- V13__billing_catalog.sql
-- Commercial catalog for tenant apps.
-- ============================================================================

CREATE TABLE app_plans (
    id             UUID           NOT NULL DEFAULT gen_random_uuid(),
    client_app_id  UUID           NOT NULL,
    code           VARCHAR(50)    NOT NULL,
    name           VARCHAR(100)   NOT NULL,
    description    TEXT,
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    is_public      BOOLEAN        NOT NULL DEFAULT TRUE,
    sort_order     INTEGER        NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_plans PRIMARY KEY (id),
    CONSTRAINT uq_app_plans_client_app_code UNIQUE (client_app_id, code),
    CONSTRAINT fk_app_plans_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE,
    CONSTRAINT chk_app_plans_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_app_plans_code CHECK (code IN ('FREE', 'PERSONAL', 'TEAM', 'BUSINESS', 'FLEX', 'ENTERPRISE'))
);

CREATE TRIGGER trg_app_plans_updated_at
    BEFORE UPDATE ON app_plans
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE app_plan_versions (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    app_plan_id      UUID           NOT NULL,
    version          VARCHAR(20)    NOT NULL,
    currency         VARCHAR(3)     NOT NULL DEFAULT 'USD',
    setup_fee        NUMERIC(12,2)  NOT NULL DEFAULT 0,
    trial_days       INTEGER        NOT NULL DEFAULT 0,
    effective_from   DATE           NOT NULL,
    effective_to     DATE,
    status           VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_plan_versions PRIMARY KEY (id),
    CONSTRAINT uq_app_plan_versions UNIQUE (app_plan_id, version),
    CONSTRAINT fk_app_plan_versions_plan
        FOREIGN KEY (app_plan_id) REFERENCES app_plans(id) ON DELETE RESTRICT,
    CONSTRAINT chk_app_plan_versions_status CHECK (status IN ('ACTIVE', 'DEPRECATED', 'INACTIVE'))
);

CREATE TRIGGER trg_app_plan_versions_updated_at
    BEFORE UPDATE ON app_plan_versions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE app_plan_billing_options (
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    app_plan_version_id  UUID           NOT NULL,
    billing_period       VARCHAR(20)    NOT NULL,
    base_price           NUMERIC(12,2)  NOT NULL DEFAULT 0,
    discount_pct         NUMERIC(5,2)   NOT NULL DEFAULT 0,
    is_default           BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_plan_billing_options PRIMARY KEY (id),
    CONSTRAINT uq_app_plan_billing_options UNIQUE (app_plan_version_id, billing_period),
    CONSTRAINT fk_app_plan_billing_options_version
        FOREIGN KEY (app_plan_version_id) REFERENCES app_plan_versions(id) ON DELETE CASCADE,
    CONSTRAINT chk_app_plan_billing_options_period CHECK (
        billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')
    ),
    CONSTRAINT chk_app_plan_billing_options_discount CHECK (
        discount_pct >= 0 AND discount_pct <= 100
    )
);

CREATE TRIGGER trg_app_plan_billing_options_updated_at
    BEFORE UPDATE ON app_plan_billing_options
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE app_plan_entitlements (
    id                   UUID           NOT NULL DEFAULT gen_random_uuid(),
    app_plan_version_id  UUID           NOT NULL,
    metric_code          VARCHAR(100)   NOT NULL,
    metric_type          VARCHAR(20)    NOT NULL,
    limit_value          NUMERIC(18,4),
    period_type          VARCHAR(20)    NOT NULL DEFAULT 'NONE',
    enforcement_mode     VARCHAR(20)    NOT NULL DEFAULT 'HARD',
    is_enabled           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_plan_entitlements PRIMARY KEY (id),
    CONSTRAINT uq_app_plan_entitlements UNIQUE (app_plan_version_id, metric_code),
    CONSTRAINT fk_app_plan_entitlements_version
        FOREIGN KEY (app_plan_version_id) REFERENCES app_plan_versions(id) ON DELETE CASCADE,
    CONSTRAINT chk_app_plan_entitlements_metric_type CHECK (
        metric_type IN ('QUOTA', 'BOOLEAN', 'RATE')
    ),
    CONSTRAINT chk_app_plan_entitlements_period_type CHECK (
        period_type IN ('NONE', 'DAY', 'MONTH', 'YEAR')
    ),
    CONSTRAINT chk_app_plan_entitlements_enforcement CHECK (
        enforcement_mode IN ('HARD', 'SOFT')
    )
);

CREATE TRIGGER trg_app_plan_entitlements_updated_at
    BEFORE UPDATE ON app_plan_entitlements
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE app_plans IS 'Commercial plans exposed by a specific client app.';
COMMENT ON COLUMN app_plans.client_app_id IS 'Internal FK to client_apps.id. Billing relations remain anchored to technical UUID keys.';
COMMENT ON TABLE app_plan_versions IS 'Immutable snapshots used by contracts and subscriptions.';
COMMENT ON TABLE app_plan_billing_options IS 'Billing cadence options. Zero rows can represent a free plan.';
COMMENT ON COLUMN app_plan_entitlements.limit_value IS 'Null means unlimited.';
