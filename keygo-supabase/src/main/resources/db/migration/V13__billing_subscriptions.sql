-- =============================================================================
-- V13: Billing Subscriptions — relacion activa de facturacion (modelo v2)
--
-- app_subscriptions: suscripcion activa entre un Contractor y una version de
--   plan de una ClientApp. El Contractor reemplaza al modelo polimorfco anterior
--   (subscriber_tenant_id / subscriber_tenant_user_id). Solo 1 suscripcion
--   activa por Contractor por ClientApp.
-- payment_transactions: registro de cada evento de pago (activacion, renovacion).
-- =============================================================================

-- ---------------------------------------------------------------------------
-- app_subscriptions (modelo v2 — contractor-centric)
-- ---------------------------------------------------------------------------
CREATE TABLE app_subscriptions (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id             UUID         NOT NULL REFERENCES client_apps(id)       ON DELETE RESTRICT,
    app_plan_version_id       UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE RESTRICT,
    contract_id               UUID         REFERENCES app_contracts(id)              ON DELETE SET NULL,

    -- Contractor que realiza la suscripcion (reemplaza subscriber_tenant_id /
    -- subscriber_tenant_user_id del modelo v1)
    contractor_id             UUID         NOT NULL REFERENCES contractors(id) ON DELETE RESTRICT,

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
    -- Solo 1 suscripcion activa por Contractor por ClientApp
    CONSTRAINT uq_app_subscriptions_app_contractor UNIQUE (client_app_id, contractor_id)
);

CREATE INDEX idx_app_subscriptions_client_app   ON app_subscriptions(client_app_id);
CREATE INDEX idx_app_subscriptions_contractor   ON app_subscriptions(contractor_id);
CREATE INDEX idx_app_subscriptions_status       ON app_subscriptions(status);
CREATE INDEX idx_app_subscriptions_contract     ON app_subscriptions(contract_id) WHERE contract_id IS NOT NULL;

CREATE TRIGGER app_subscriptions_updated_at
    BEFORE UPDATE ON app_subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  app_subscriptions IS 'Active billing relationship between a Contractor and a plan version of a ClientApp (model v2). Replaces the polymorphic subscriber_tenant_id / subscriber_tenant_user_id design.';
COMMENT ON COLUMN app_subscriptions.contractor_id IS 'The contractor who holds this subscription. One subscription per contractor per app.';

-- ---------------------------------------------------------------------------
-- payment_transactions
-- Registro de cada evento de pago (activacion inicial, renovacion, etc.)
-- ---------------------------------------------------------------------------
CREATE TABLE payment_transactions (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    contract_id        UUID          REFERENCES app_contracts(id)     ON DELETE SET NULL,
    subscription_id    UUID          REFERENCES app_subscriptions(id) ON DELETE SET NULL,
    provider           VARCHAR(50)   NOT NULL DEFAULT 'MOCK',
    provider_reference VARCHAR(255),
    amount             NUMERIC(12,2) NOT NULL,
    currency           VARCHAR(3)    NOT NULL DEFAULT 'USD',
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

COMMENT ON TABLE  payment_transactions IS 'Record of each payment event (initial activation, renewal, etc.). raw_response stores PSP payload for auditing.';
COMMENT ON COLUMN payment_transactions.provider IS 'MOCK = dev/test; MANUAL = no PSP; others = real PSP integration';

