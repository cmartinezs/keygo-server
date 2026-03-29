-- =============================================================================
-- V12: Billing Subscriptions — relación activa de facturación
-- app_subscriptions  : suscripción activa entre un suscriptor y una versión
--   de plan de una ClientApp.
--   Exactamente uno de (subscriber_tenant_id, subscriber_tenant_user_id) es no-nulo.
-- payment_transactions: registro de cada evento de pago (inicial, renovación, etc.)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- app_subscriptions
-- ---------------------------------------------------------------------------
CREATE TABLE app_subscriptions (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id             UUID         NOT NULL REFERENCES client_apps(id)       ON DELETE RESTRICT,
    app_plan_version_id       UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE RESTRICT,
    contract_id               UUID         REFERENCES app_contracts(id)              ON DELETE SET NULL,

    -- Suscriptor polimórfico — exactamente uno debe ser no-nulo
    subscriber_tenant_id      UUID         REFERENCES tenants(id)      ON DELETE RESTRICT,
    subscriber_tenant_user_id UUID         REFERENCES tenant_users(id) ON DELETE RESTRICT,

    status                    VARCHAR(20)  NOT NULL DEFAULT 'PENDING',

    current_period_start      TIMESTAMPTZ  NOT NULL,
    current_period_end        TIMESTAMPTZ  NOT NULL,
    cancel_at_period_end      BOOLEAN      NOT NULL DEFAULT FALSE,
    cancelled_at              TIMESTAMPTZ,
    next_billing_at           TIMESTAMPTZ,
    auto_renew                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_app_subscriptions_status CHECK (status IN (
        'PENDING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED', 'CANCELLED', 'EXPIRED'
    )),
    -- Una suscripción activa por app por suscriptor B2B
    CONSTRAINT uq_app_subscriptions_app_tenant UNIQUE (client_app_id, subscriber_tenant_id),
    -- Una suscripción activa por app por suscriptor B2C
    CONSTRAINT uq_app_subscriptions_app_user   UNIQUE (client_app_id, subscriber_tenant_user_id),
    CONSTRAINT chk_app_subscriptions_single_subscriber CHECK (
        NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL)
    )
);

CREATE INDEX idx_app_subscriptions_client_app ON app_subscriptions(client_app_id);
CREATE INDEX idx_app_subscriptions_status     ON app_subscriptions(status);
CREATE INDEX idx_app_subscriptions_sub_tenant
    ON app_subscriptions(subscriber_tenant_id)
    WHERE subscriber_tenant_id IS NOT NULL;
CREATE INDEX idx_app_subscriptions_sub_user
    ON app_subscriptions(subscriber_tenant_user_id)
    WHERE subscriber_tenant_user_id IS NOT NULL;

CREATE TRIGGER app_subscriptions_updated_at
    BEFORE UPDATE ON app_subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE app_subscriptions IS 'Active billing relationship between a subscriber and a plan version of a ClientApp. Exactly one of subscriber_tenant_id / subscriber_tenant_user_id must be non-null.';

-- ---------------------------------------------------------------------------
-- payment_transactions
-- Registro de cada evento de pago (activación inicial, renovación, etc.)
-- ---------------------------------------------------------------------------
CREATE TABLE payment_transactions (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id        UUID          REFERENCES app_contracts(id)    ON DELETE SET NULL,
    subscription_id    UUID          REFERENCES app_subscriptions(id) ON DELETE SET NULL,
    provider           VARCHAR(50)   NOT NULL DEFAULT 'MOCK',
    provider_reference VARCHAR(255),
    amount             NUMERIC(12,2) NOT NULL,
    currency           VARCHAR(3)    NOT NULL DEFAULT 'MXN',
    status             VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_at            TIMESTAMPTZ,
    raw_response       JSONB,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_payment_tx_provider CHECK (provider IN ('MANUAL', 'MOCK', 'MERCADOPAGO', 'STRIPE', 'OTHER')),
    CONSTRAINT chk_payment_tx_status   CHECK (status   IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED'))
);

CREATE INDEX idx_payment_tx_contract     ON payment_transactions(contract_id);
CREATE INDEX idx_payment_tx_subscription ON payment_transactions(subscription_id);
CREATE INDEX idx_payment_tx_status       ON payment_transactions(status);

COMMENT ON TABLE  payment_transactions IS 'Record of each payment event (initial activation, renewal, etc.). raw_response stores PSP payload.';
COMMENT ON COLUMN payment_transactions.provider IS 'MOCK = dev/test; MANUAL = no PSP; others = real PSP';

