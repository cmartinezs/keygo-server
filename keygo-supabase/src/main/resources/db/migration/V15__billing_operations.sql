-- ============================================================================
-- V15__billing_operations.sql
-- Billing execution data: payments, invoices, usage, payment methods and
-- tenant fiscal profiles.
-- ============================================================================

CREATE TABLE payment_transactions (
    id                    UUID           NOT NULL DEFAULT gen_random_uuid(),
    contractor_id         UUID           NOT NULL,
    client_app_id         UUID           NOT NULL,
    contract_id           UUID,
    subscription_id       UUID,
    provider              VARCHAR(20)    NOT NULL,
    provider_reference    VARCHAR(255),
    amount                NUMERIC(12,2)  NOT NULL,
    currency              VARCHAR(3)     NOT NULL DEFAULT 'USD',
    status                VARCHAR(20)    NOT NULL,
    paid_at               TIMESTAMPTZ,
    refunded_at           TIMESTAMPTZ,
    provider_payload      JSONB          NOT NULL DEFAULT '{}'::jsonb,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_payment_transactions PRIMARY KEY (id),
    CONSTRAINT fk_payment_transactions_contractor
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_transactions_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_transactions_contract
        FOREIGN KEY (contract_id) REFERENCES app_contracts(id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_transactions_subscription
        FOREIGN KEY (subscription_id) REFERENCES app_subscriptions(id) ON DELETE SET NULL,
    CONSTRAINT chk_payment_transactions_provider CHECK (
        provider IN ('MOCK', 'MANUAL', 'MERCADOPAGO', 'STRIPE', 'OTHER')
    ),
    CONSTRAINT chk_payment_transactions_status CHECK (
        status IN ('PENDING', 'APPROVED', 'FAILED', 'REFUNDED', 'CANCELLED')
    )
);

CREATE INDEX idx_payment_transactions_status ON payment_transactions(status);
CREATE INDEX idx_payment_transactions_contractor_time ON payment_transactions(contractor_id, created_at DESC);

CREATE TRIGGER trg_payment_transactions_updated_at
    BEFORE UPDATE ON payment_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE invoices (
    id                        UUID           NOT NULL DEFAULT gen_random_uuid(),
    contractor_id             UUID           NOT NULL,
    client_app_id             UUID           NOT NULL,
    subscription_id           UUID           NOT NULL,
    invoice_number            VARCHAR(50)    NOT NULL,
    status                    VARCHAR(20)    NOT NULL,
    issue_date                DATE           NOT NULL,
    due_date                  DATE           NOT NULL,
    period_start              DATE           NOT NULL,
    period_end                DATE           NOT NULL,
    currency                  VARCHAR(3)     NOT NULL DEFAULT 'USD',
    subtotal                  NUMERIC(12,2)  NOT NULL,
    tax_amount                NUMERIC(12,2)  NOT NULL DEFAULT 0,
    total                     NUMERIC(12,2)  NOT NULL,
    paid_at                   TIMESTAMPTZ,
    billing_name_snapshot     VARCHAR(300),
    billing_tax_id_snapshot   VARCHAR(100),
    billing_address_snapshot  TEXT,
    plan_name_snapshot        VARCHAR(100),
    plan_version_snapshot     VARCHAR(20),
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_invoices PRIMARY KEY (id),
    CONSTRAINT uq_invoices_invoice_number UNIQUE (invoice_number),
    CONSTRAINT fk_invoices_contractor
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoices_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoices_subscription
        FOREIGN KEY (subscription_id) REFERENCES app_subscriptions(id) ON DELETE RESTRICT,
    CONSTRAINT chk_invoices_status CHECK (
        status IN ('DRAFT', 'ISSUED', 'PAID', 'VOID', 'OVERDUE')
    )
);

CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_contractor_time ON invoices(contractor_id, issue_date DESC);

CREATE TRIGGER trg_invoices_updated_at
    BEFORE UPDATE ON invoices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE usage_counters (
    id            UUID            NOT NULL DEFAULT gen_random_uuid(),
    contractor_id UUID            NOT NULL,
    client_app_id UUID            NOT NULL,
    metric_code   VARCHAR(100)    NOT NULL,
    period_start  TIMESTAMPTZ     NOT NULL,
    period_end    TIMESTAMPTZ     NOT NULL,
    used_value    NUMERIC(18,4)   NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT pk_usage_counters PRIMARY KEY (id),
    CONSTRAINT uq_usage_counters_scope UNIQUE (
        client_app_id, contractor_id, metric_code, period_start, period_end
    ),
    CONSTRAINT fk_usage_counters_contractor
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE CASCADE,
    CONSTRAINT fk_usage_counters_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_usage_counters_updated_at
    BEFORE UPDATE ON usage_counters
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE tenant_billing_profiles (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID           NOT NULL,
    billing_type     VARCHAR(20)    NOT NULL,
    display_name     VARCHAR(255)   NOT NULL,
    legal_name       VARCHAR(255),
    tax_id           VARCHAR(100),
    billing_email    CITEXT         NOT NULL,
    billing_address  TEXT,
    is_default       BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_tenant_billing_profiles PRIMARY KEY (id),
    CONSTRAINT fk_tenant_billing_profiles_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT chk_tenant_billing_profiles_type CHECK (
        billing_type IN ('PERSONAL', 'COMPANY')
    )
);

CREATE UNIQUE INDEX uq_tenant_billing_profiles_default
    ON tenant_billing_profiles(tenant_id)
    WHERE is_default = TRUE;

CREATE TRIGGER trg_tenant_billing_profiles_updated_at
    BEFORE UPDATE ON tenant_billing_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE payment_methods (
    id                  UUID           NOT NULL DEFAULT gen_random_uuid(),
    contractor_id       UUID           NOT NULL,
    provider            VARCHAR(20)    NOT NULL,
    method_type         VARCHAR(30)    NOT NULL,
    external_reference  VARCHAR(255)   NOT NULL,
    display_label       VARCHAR(255)   NOT NULL,
    brand               VARCHAR(50),
    last4               VARCHAR(4),
    exp_month           INTEGER,
    exp_year            INTEGER,
    status              VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    is_default          BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_payment_methods PRIMARY KEY (id),
    CONSTRAINT uq_payment_methods_external_reference UNIQUE (provider, external_reference),
    CONSTRAINT fk_payment_methods_contractor
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE CASCADE,
    CONSTRAINT chk_payment_methods_provider CHECK (
        provider IN ('STRIPE', 'MERCADOPAGO', 'PAYPAL', 'MANUAL', 'MOCK')
    ),
    CONSTRAINT chk_payment_methods_status CHECK (
        status IN ('ACTIVE', 'INACTIVE', 'EXPIRED')
    )
);

CREATE UNIQUE INDEX uq_payment_methods_default
    ON payment_methods(contractor_id)
    WHERE is_default = TRUE;

CREATE TRIGGER trg_payment_methods_updated_at
    BEFORE UPDATE ON payment_methods
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE payment_transactions IS 'Payment events linked to a contract and/or subscription.';
COMMENT ON TABLE invoices IS 'Immutable historical invoice snapshots.';
COMMENT ON TABLE usage_counters IS 'Usage measured per contractor, app, metric and period.';
COMMENT ON TABLE tenant_billing_profiles IS 'Fiscal profile data stored per tenant.';
COMMENT ON TABLE payment_methods IS 'External payment method references only. PAN and CVV never persist.';
