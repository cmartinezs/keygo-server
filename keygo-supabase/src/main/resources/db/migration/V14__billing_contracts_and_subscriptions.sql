-- ============================================================================
-- V14__billing_contracts_and_subscriptions.sql
-- Contract lifecycle and active subscription lifecycle.
-- ============================================================================

CREATE TABLE app_contracts (
    id                            UUID           NOT NULL DEFAULT gen_random_uuid(),
    contractor_id                 UUID           NOT NULL,
    client_app_id                 UUID           NOT NULL,
    app_plan_version_id           UUID           NOT NULL,
    created_by_platform_user_id   UUID,
    billing_period                VARCHAR(20)    NOT NULL,
    status                        VARCHAR(40)    NOT NULL,
    billing_contact_email         CITEXT         NOT NULL,
    billing_contact_name          VARCHAR(255),
    contractor_email_verified_at  TIMESTAMPTZ,
    payment_verified_at           TIMESTAMPTZ,
    expires_at                    TIMESTAMPTZ    NOT NULL,
    created_at                    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                    TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_contracts PRIMARY KEY (id),
    CONSTRAINT fk_app_contracts_contractor
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_contracts_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_contracts_app_plan_version
        FOREIGN KEY (app_plan_version_id) REFERENCES app_plan_versions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_contracts_created_by_platform_user
        FOREIGN KEY (created_by_platform_user_id) REFERENCES platform_users(id) ON DELETE SET NULL,
    CONSTRAINT chk_app_contracts_billing_period CHECK (
        billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')
    ),
    CONSTRAINT chk_app_contracts_status CHECK (
        status IN (
            'DRAFT',
            'PENDING_EMAIL_VERIFICATION',
            'PENDING_PAYMENT',
            'READY_TO_ACTIVATE',
            'ACTIVE',
            'SUPERSEDED',
            'FINALIZED',
            'CANCELLED',
            'EXPIRED',
            'FAILED'
        )
    )
);

CREATE UNIQUE INDEX uq_app_contracts_active_by_contractor_app
    ON app_contracts(contractor_id, client_app_id)
    WHERE status = 'ACTIVE';

CREATE TRIGGER trg_app_contracts_updated_at
    BEFORE UPDATE ON app_contracts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE app_subscriptions (
    id                        UUID           NOT NULL DEFAULT gen_random_uuid(),
    contractor_id             UUID           NOT NULL,
    client_app_id             UUID           NOT NULL,
    app_plan_version_id       UUID           NOT NULL,
    contract_id               UUID,
    status                    VARCHAR(20)    NOT NULL,
    current_period_start      TIMESTAMPTZ    NOT NULL,
    current_period_end        TIMESTAMPTZ    NOT NULL,
    cancel_at_period_end      BOOLEAN        NOT NULL DEFAULT FALSE,
    cancelled_at              TIMESTAMPTZ,
    next_billing_at           TIMESTAMPTZ,
    auto_renew                BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_app_subscriptions PRIMARY KEY (id),
    CONSTRAINT fk_app_subscriptions_contractor
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_subscriptions_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_subscriptions_app_plan_version
        FOREIGN KEY (app_plan_version_id) REFERENCES app_plan_versions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_subscriptions_contract
        FOREIGN KEY (contract_id) REFERENCES app_contracts(id) ON DELETE SET NULL,
    CONSTRAINT chk_app_subscriptions_status CHECK (
        status IN ('PENDING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED', 'CANCELLED', 'EXPIRED')
    )
);

CREATE UNIQUE INDEX uq_app_subscriptions_active_by_contractor_app
    ON app_subscriptions(contractor_id, client_app_id)
    WHERE status IN ('PENDING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED');

CREATE TRIGGER trg_app_subscriptions_updated_at
    BEFORE UPDATE ON app_subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE app_contracts IS 'Contract lifecycle before and during subscription activation. Billing email verification is independent from account verification.';
COMMENT ON TABLE app_subscriptions IS 'Active recurring or one-time subscription state per contractor and app.';
