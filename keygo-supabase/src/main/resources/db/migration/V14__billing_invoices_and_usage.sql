-- =============================================================================
-- V14: Billing Invoices + Usage Counters (modelo v2)
--
-- invoices       : snapshot historico por periodo de facturacion. Los campos
--   *_snapshot capturan el estado al momento de la factura y nunca se actualizan.
-- usage_counters : contadores atomicos por (app, contractor, metrica, periodo).
--   Modelo v2: el suscriptor es siempre un Contractor (reemplaza el modelo
--   polimorfco subscriber_tenant_id / subscriber_tenant_user_id del v1).
--   Los incrementos se hacen con UPDATE ... SET used_value = used_value + delta
--   para atomicidad a nivel PostgreSQL sin bloqueos a nivel de aplicacion.
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
    currency                 VARCHAR(3)    NOT NULL DEFAULT 'USD',
    subtotal                 NUMERIC(12,2) NOT NULL,
    tax_amount               NUMERIC(12,2) NOT NULL DEFAULT 0,
    total                    NUMERIC(12,2) NOT NULL,
    -- Snapshots historicos — inmutables tras la creacion
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
CREATE INDEX idx_invoices_status       ON invoices(status);

COMMENT ON TABLE  invoices IS 'Historical billing snapshot per subscription period. *_snapshot fields are immutable after creation.';
COMMENT ON COLUMN invoices.billing_name_snapshot IS 'Billing name at invoice time (person name or company legal name)';
COMMENT ON COLUMN invoices.billing_tax_id_snapshot IS 'Tax ID (RFC/NIT/VAT) at invoice time';
COMMENT ON COLUMN invoices.plan_name_snapshot IS 'Plan name at invoice time (immutable even if plan name changes later)';

-- ---------------------------------------------------------------------------
-- usage_counters (modelo v2 — contractor-centric)
-- Contadores atomicos por (app, contractor, metrica, periodo).
-- ---------------------------------------------------------------------------
CREATE TABLE usage_counters (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id UUID         NOT NULL REFERENCES client_apps(id)  ON DELETE CASCADE,

    -- Contractor al que pertenece el contador.
    -- Reemplaza el modelo polimorfco subscriber_tenant_id / subscriber_tenant_user_id.
    contractor_id UUID         NOT NULL REFERENCES contractors(id) ON DELETE CASCADE,

    metric_code   VARCHAR(100) NOT NULL,
    period_start  TIMESTAMPTZ  NOT NULL,
    period_end    TIMESTAMPTZ  NOT NULL,
    used_value    BIGINT       NOT NULL DEFAULT 0,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_usage_counters_contractor_metric
        UNIQUE (client_app_id, contractor_id, metric_code, period_start, period_end)
);

CREATE INDEX idx_usage_counters_app_contractor ON usage_counters(client_app_id, contractor_id);
CREATE INDEX idx_usage_counters_contractor     ON usage_counters(contractor_id);

COMMENT ON TABLE  usage_counters IS 'Atomic usage counters per (app, contractor, metric, period). Increment with UPDATE ... SET used_value = used_value + delta. Model v2: subscriber is always a Contractor.';
COMMENT ON COLUMN usage_counters.metric_code IS 'Business metric code (e.g. MAX_TENANTS, MAX_TENANT_USERS). Must match app_plan_entitlements.metric_code.';
COMMENT ON COLUMN usage_counters.contractor_id IS 'Contractor who owns the counter. Replaces polymorphic subscriber_tenant_id / subscriber_tenant_user_id from model v1.';

