-- V16: Billing catalog — app-scoped plans, versions, and entitlements
--
-- Each ClientApp can define its own plan catalog.
-- Plans have a subscriber_type that determines whether they target
-- TENANT (company/B2B) or TENANT_USER (individual/B2C) subscribers.
-- An app can have plans of both types simultaneously (e.g. acme-eval:
-- individual teacher plans + institutional school plans).
--
-- Idempotency: all DDL uses IF NOT EXISTS.

-- ---------------------------------------------------------------------------
-- app_plans: one catalog per ClientApp
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_plans (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id   UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    -- TENANT      → company/B2B plan  (school, org, enterprise)
    -- TENANT_USER → individual/B2C plan (teacher, employee, end-user)
    subscriber_type VARCHAR(20)  NOT NULL
                      CHECK (subscriber_type IN ('TENANT', 'TENANT_USER')),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_public       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (client_app_id, code)
);

-- Composite partial index for the most frequent query: public catalog by app + subscriber type
CREATE INDEX IF NOT EXISTS idx_app_plans_client_app_type
    ON app_plans(client_app_id, subscriber_type, status)
    WHERE is_public = TRUE;

-- ---------------------------------------------------------------------------
-- app_plan_versions: immutable snapshots of a plan's pricing & period
-- Subscriptions always point to a specific version; existing subscriptions
-- are never affected when a new version is published.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_plan_versions (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    app_plan_id     UUID          NOT NULL REFERENCES app_plans(id) ON DELETE RESTRICT,
    version         VARCHAR(20)   NOT NULL,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'MXN',
    billing_period  VARCHAR(20)   NOT NULL
                      CHECK (billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')),
    base_price      NUMERIC(12,2) NOT NULL DEFAULT 0,
    setup_fee       NUMERIC(12,2) NOT NULL DEFAULT 0,
    trial_days      INT           NOT NULL DEFAULT 0,
    effective_from  DATE          NOT NULL,
    effective_to    DATE,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'INACTIVE', 'DEPRECATED')),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (app_plan_id, version)
);

CREATE INDEX IF NOT EXISTS idx_app_plan_versions_plan_id
    ON app_plan_versions(app_plan_id, status);

-- ---------------------------------------------------------------------------
-- app_plan_entitlements: limits and feature flags per plan version
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_plan_entitlements (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    app_plan_version_id UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE CASCADE,
    -- Business-defined metric code (e.g. MAX_TENANT_USERS, EVALUACIONES_POR_MES, ALLOW_SSO)
    metric_code         VARCHAR(100) NOT NULL,
    metric_type         VARCHAR(20)  NOT NULL
                          CHECK (metric_type IN ('QUOTA', 'BOOLEAN', 'RATE')),
    limit_value         BIGINT,           -- NULL = unlimited for QUOTA/RATE
    period_type         VARCHAR(20)  NOT NULL DEFAULT 'NONE'
                          CHECK (period_type IN ('NONE', 'DAY', 'MONTH')),
    enforcement_mode    VARCHAR(20)  NOT NULL DEFAULT 'HARD'
                          CHECK (enforcement_mode IN ('HARD', 'SOFT')),
    is_enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE (app_plan_version_id, metric_code)
);

