-- V19: Billing invoices and usage counters
--
-- invoices: historical snapshot per billing period. Fields ending in _snapshot
-- capture the state at invoice time and are never updated retroactively.
--
-- usage_counters: atomic counters per (app, subscriber, metric, period).
-- Increments are done with UPDATE ... SET used_value = used_value + delta
-- for PostgreSQL-level atomicity without application-side locking.

-- ---------------------------------------------------------------------------
-- invoices
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    id                        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id           UUID          NOT NULL REFERENCES app_subscriptions(id) ON DELETE RESTRICT,
    invoice_number            VARCHAR(50)   NOT NULL UNIQUE,
    status                    VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                                CHECK (status IN ('DRAFT', 'ISSUED', 'PAID', 'VOID', 'OVERDUE')),
    issue_date                DATE          NOT NULL,
    due_date                  DATE          NOT NULL,
    period_start              DATE          NOT NULL,
    period_end                DATE          NOT NULL,
    currency                  VARCHAR(3)    NOT NULL DEFAULT 'MXN',
    subtotal                  NUMERIC(12,2) NOT NULL,
    tax_amount                NUMERIC(12,2) NOT NULL DEFAULT 0,
    total                     NUMERIC(12,2) NOT NULL,
    -- Historical snapshots — immutable after creation
    billing_name_snapshot     VARCHAR(300),
    billing_tax_id_snapshot   VARCHAR(100),
    billing_address_snapshot  TEXT,
    plan_name_snapshot        VARCHAR(100),
    plan_version_snapshot     VARCHAR(20),
    pdf_url                   TEXT,
    created_at                TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_invoices_subscription ON invoices(subscription_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status        ON invoices(status);

-- ---------------------------------------------------------------------------
-- usage_counters
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usage_counters (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id             UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    -- Polymorphic subscriber — exactly one must be non-null
    subscriber_tenant_id      UUID         REFERENCES tenants(id) ON DELETE CASCADE,
    subscriber_tenant_user_id UUID         REFERENCES tenant_users(id) ON DELETE CASCADE,
    metric_code               VARCHAR(100) NOT NULL,
    period_start              TIMESTAMPTZ  NOT NULL,
    period_end                TIMESTAMPTZ  NOT NULL,
    used_value                BIGINT       NOT NULL DEFAULT 0,
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),

    UNIQUE (client_app_id, subscriber_tenant_id,      metric_code, period_start, period_end),
    UNIQUE (client_app_id, subscriber_tenant_user_id, metric_code, period_start, period_end),

    CONSTRAINT chk_usage_single_subscriber CHECK (
        NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_usage_app_tenant ON usage_counters(client_app_id, subscriber_tenant_id);
CREATE INDEX IF NOT EXISTS idx_usage_app_user   ON usage_counters(client_app_id, subscriber_tenant_user_id);

