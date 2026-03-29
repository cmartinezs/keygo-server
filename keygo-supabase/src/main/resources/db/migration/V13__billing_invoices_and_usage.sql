-- =============================================================================
-- V13: Billing Invoices + Usage Counters
-- invoices       : snapshot histórico por período de facturación. Los campos
--   *_snapshot capturan el estado al momento de la factura y nunca se actualizan.
-- usage_counters : contadores atómicos por (app, suscriptor, métrica, periodo).
--   Los incrementos se hacen con UPDATE ... SET used_value = used_value + delta
--   para atomicidad a nivel PostgreSQL sin bloqueos a nivel de aplicación.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- invoices
-- ---------------------------------------------------------------------------
CREATE TABLE invoices (
    id                       UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id          UUID          NOT NULL REFERENCES app_subscriptions(id) ON DELETE RESTRICT,
    invoice_number           VARCHAR(50)   NOT NULL UNIQUE,
    status                   VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    issue_date               DATE          NOT NULL,
    due_date                 DATE          NOT NULL,
    period_start             DATE          NOT NULL,
    period_end               DATE          NOT NULL,
    currency                 VARCHAR(3)    NOT NULL DEFAULT 'MXN',
    subtotal                 NUMERIC(12,2) NOT NULL,
    tax_amount               NUMERIC(12,2) NOT NULL DEFAULT 0,
    total                    NUMERIC(12,2) NOT NULL,
    -- Snapshots históricos — inmutables tras la creación
    billing_name_snapshot    VARCHAR(300),
    billing_tax_id_snapshot  VARCHAR(100),
    billing_address_snapshot TEXT,
    plan_name_snapshot       VARCHAR(100),
    plan_version_snapshot    VARCHAR(20),
    pdf_url                  TEXT,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_invoices_status CHECK (status IN ('DRAFT', 'ISSUED', 'PAID', 'VOID', 'OVERDUE'))
);

CREATE INDEX idx_invoices_subscription ON invoices(subscription_id);
CREATE INDEX idx_invoices_status        ON invoices(status);

COMMENT ON TABLE  invoices IS 'Historical billing snapshot per subscription period. *_snapshot fields are immutable after creation.';
COMMENT ON COLUMN invoices.billing_name_snapshot    IS 'Billing name at invoice time (person name or company legal name)';
COMMENT ON COLUMN invoices.billing_tax_id_snapshot  IS 'Tax ID (RFC/NIT/VAT) at invoice time';
COMMENT ON COLUMN invoices.plan_name_snapshot       IS 'Plan name at invoice time (immutable even if plan name changes later)';

-- ---------------------------------------------------------------------------
-- usage_counters
-- Contadores atómicos por (app, suscriptor, métrica, período).
-- ---------------------------------------------------------------------------
CREATE TABLE usage_counters (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id             UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,

    -- Suscriptor polimórfico — exactamente uno debe ser no-nulo
    subscriber_tenant_id      UUID         REFERENCES tenants(id)      ON DELETE CASCADE,
    subscriber_tenant_user_id UUID         REFERENCES tenant_users(id) ON DELETE CASCADE,

    metric_code               VARCHAR(100) NOT NULL,
    period_start              TIMESTAMPTZ  NOT NULL,
    period_end                TIMESTAMPTZ  NOT NULL,
    used_value                BIGINT       NOT NULL DEFAULT 0,
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_usage_counters_single_subscriber CHECK (
        NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL)
    ),
    CONSTRAINT uq_usage_counters_tenant_metric
        UNIQUE (client_app_id, subscriber_tenant_id,      metric_code, period_start, period_end),
    CONSTRAINT uq_usage_counters_user_metric
        UNIQUE (client_app_id, subscriber_tenant_user_id, metric_code, period_start, period_end)
);

CREATE INDEX idx_usage_counters_app_tenant ON usage_counters(client_app_id, subscriber_tenant_id);
CREATE INDEX idx_usage_counters_app_user   ON usage_counters(client_app_id, subscriber_tenant_user_id);

COMMENT ON TABLE usage_counters IS 'Atomic usage counters per (app, subscriber, metric, period). Increment with UPDATE ... SET used_value = used_value + delta. Exactly one of subscriber_*_id must be non-null.';
COMMENT ON COLUMN usage_counters.metric_code     IS 'Business metric code (e.g. MAX_TENANT_USERS, EVALUACIONES_POR_MES). Must match app_plan_entitlements.metric_code.';

