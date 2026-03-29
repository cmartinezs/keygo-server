-- V18: Billing subscriptions and payment transactions
--
-- app_subscriptions: the active billing relationship between a subscriber
-- (Tenant for B2B or TenantUser for B2C) and a plan version of a ClientApp.
-- Exactly one of (subscriber_tenant_id, subscriber_tenant_user_id) must be non-null.
--
-- payment_transactions: one per billing event (initial activation, renewal, etc.)

-- ---------------------------------------------------------------------------
-- app_subscriptions
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_subscriptions (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id             UUID         NOT NULL REFERENCES client_apps(id) ON DELETE RESTRICT,
    app_plan_version_id       UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE RESTRICT,
    contract_id               UUID         REFERENCES app_contracts(id) ON DELETE SET NULL,

    -- Polymorphic subscriber — exactly one must be non-null
    subscriber_tenant_id      UUID         REFERENCES tenants(id) ON DELETE RESTRICT,
    subscriber_tenant_user_id UUID         REFERENCES tenant_users(id) ON DELETE RESTRICT,

    status                    VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN (
                                    'PENDING', 'ACTIVE', 'PAST_DUE',
                                    'SUSPENDED', 'CANCELLED', 'EXPIRED'
                                )),
    current_period_start      TIMESTAMPTZ  NOT NULL,
    current_period_end        TIMESTAMPTZ  NOT NULL,
    cancel_at_period_end      BOOLEAN      NOT NULL DEFAULT FALSE,
    cancelled_at              TIMESTAMPTZ,
    next_billing_at           TIMESTAMPTZ,
    auto_renew                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- One active subscription per app per B2B subscriber
    UNIQUE (client_app_id, subscriber_tenant_id),
    -- One active subscription per app per B2C subscriber
    UNIQUE (client_app_id, subscriber_tenant_user_id),

    CONSTRAINT chk_subscriptions_single_subscriber CHECK (
        NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_app_subscriptions_app    ON app_subscriptions(client_app_id);
CREATE INDEX IF NOT EXISTS idx_app_subscriptions_status ON app_subscriptions(status);
CREATE INDEX IF NOT EXISTS idx_app_subscriptions_sub_t
    ON app_subscriptions(subscriber_tenant_id)
    WHERE subscriber_tenant_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_app_subscriptions_sub_u
    ON app_subscriptions(subscriber_tenant_user_id)
    WHERE subscriber_tenant_user_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- payment_transactions
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_transactions (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id      UUID          REFERENCES app_contracts(id) ON DELETE SET NULL,
    subscription_id  UUID          REFERENCES app_subscriptions(id) ON DELETE SET NULL,
    provider         VARCHAR(50)   NOT NULL DEFAULT 'MOCK'
                       CHECK (provider IN ('MANUAL', 'MOCK', 'MERCADOPAGO', 'STRIPE', 'OTHER')),
    provider_reference VARCHAR(255),
    amount           NUMERIC(12,2) NOT NULL,
    currency         VARCHAR(3)    NOT NULL DEFAULT 'MXN',
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                       CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')),
    paid_at          TIMESTAMPTZ,
    raw_response     JSONB,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_payment_tx_contract     ON payment_transactions(contract_id);
CREATE INDEX IF NOT EXISTS idx_payment_tx_subscription ON payment_transactions(subscription_id);
CREATE INDEX IF NOT EXISTS idx_payment_tx_status       ON payment_transactions(status);

