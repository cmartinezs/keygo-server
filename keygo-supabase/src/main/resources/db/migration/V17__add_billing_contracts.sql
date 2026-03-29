-- V17: Billing contracts — onboarding flow per ClientApp
--
-- A contract represents the onboarding/checkout process before an
-- AppSubscription is created. It tracks email verification and payment
-- confirmation before activating the subscription.
--
-- subscriber_type = TENANT      → company onboarding (creates new Tenant on activation)
-- subscriber_type = TENANT_USER → individual signup (creates or resolves TenantUser)
--
-- Constraint: subscriber_type on the contract must match subscriber_type
-- of the selected plan version's plan. Enforced at application level.

CREATE TABLE IF NOT EXISTS app_contracts (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id             UUID         NOT NULL REFERENCES client_apps(id) ON DELETE RESTRICT,
    selected_plan_version_id  UUID         NOT NULL REFERENCES app_plan_versions(id) ON DELETE RESTRICT,
    billing_period            VARCHAR(20)  NOT NULL
                                CHECK (billing_period IN ('MONTHLY', 'YEARLY', 'ONE_TIME')),
    subscriber_type           VARCHAR(20)  NOT NULL
                                CHECK (subscriber_type IN ('TENANT', 'TENANT_USER')),

    -- Set after activation (null until then).
    -- Exactly one of these must be non-null after ACTIVATED status.
    subscriber_tenant_id      UUID         REFERENCES tenants(id) ON DELETE SET NULL,
    subscriber_tenant_user_id UUID         REFERENCES tenant_users(id) ON DELETE SET NULL,

    status                    VARCHAR(40)  NOT NULL DEFAULT 'PENDING_EMAIL_VERIFICATION'
                                CHECK (status IN (
                                    'PENDING_EMAIL_VERIFICATION',
                                    'PENDING_PAYMENT',
                                    'READY_TO_ACTIVATE',
                                    'ACTIVATED',
                                    'CANCELLED',
                                    'EXPIRED',
                                    'FAILED'
                                )),

    -- Contractor info (always required)
    contractor_email          VARCHAR(255) NOT NULL,
    contractor_first_name     VARCHAR(100) NOT NULL,
    contractor_last_name      VARCHAR(100) NOT NULL,

    -- Company info (required only when subscriber_type = TENANT)
    company_name              VARCHAR(200),
    company_slug              VARCHAR(100),
    company_tax_id            VARCHAR(100),
    company_address           TEXT,

    -- Traceability timestamps
    email_verified_at         TIMESTAMPTZ,
    payment_verified_at       TIMESTAMPTZ,
    expires_at                TIMESTAMPTZ  NOT NULL,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- company_slug must be globally unique (becomes the new tenant slug on activation)
    CONSTRAINT uq_app_contracts_company_slug UNIQUE (company_slug),

    -- At most one subscriber link per contract
    CONSTRAINT chk_contracts_single_subscriber CHECK (
        NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_app_contracts_client_app ON app_contracts(client_app_id);
CREATE INDEX IF NOT EXISTS idx_app_contracts_status     ON app_contracts(status);
CREATE INDEX IF NOT EXISTS idx_app_contracts_email      ON app_contracts(contractor_email);
CREATE INDEX IF NOT EXISTS idx_app_contracts_sub_tenant
    ON app_contracts(subscriber_tenant_id)
    WHERE subscriber_tenant_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_app_contracts_sub_user
    ON app_contracts(subscriber_tenant_user_id)
    WHERE subscriber_tenant_user_id IS NOT NULL;

