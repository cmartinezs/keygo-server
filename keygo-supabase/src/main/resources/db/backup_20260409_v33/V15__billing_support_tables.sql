-- =============================================================================
-- V14: Billing Support Tables — perfiles fiscales y métodos de pago por Tenant
-- tenant_billing_profiles : datos fiscales/facturación por Tenant.
--   PERSONAL = persona física (individual / sole proprietor)
--   COMPANY  = persona moral (organización / corporación)
--   Un Tenant puede tener múltiples perfiles; uno es el default.
-- payment_methods : tokens PSP por Tenant.
--   NUNCA almacena datos crudos de tarjeta (PAN, CVV) — solo display info y
--   tokens del proveedor.
--   Soporta CARD, PAYPAL, BANK_TRANSFER y MOCK (dev/pruebas).
--   Un Tenant puede tener múltiples métodos; uno es el default.
-- Ambas tablas eliminan en cascada cuando el Tenant propietario es eliminado.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- tenant_billing_profiles
-- ---------------------------------------------------------------------------
CREATE TABLE tenant_billing_profiles (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,

    -- PERSONAL = persona física / individual
    -- COMPANY  = persona moral / empresa
    billing_type    VARCHAR(20)  NOT NULL,

    -- Nombre completo (persona) o razón social (empresa)
    billing_name    VARCHAR(300) NOT NULL,

    -- Identificación fiscal
    tax_id          VARCHAR(100),    -- RFC / NIT / RUT / VAT
    tax_regime      VARCHAR(100),    -- Código de régimen SAT (p.ej. '601', '612')

    -- Domicilio fiscal
    address_line1   VARCHAR(300),
    address_line2   VARCHAR(300),
    city            VARCHAR(100),
    state           VARCHAR(100),
    country         VARCHAR(2)   NOT NULL DEFAULT 'MX', -- ISO 3166-1 alpha-2
    postal_code     VARCHAR(20),

    -- Contacto
    contact_email   VARCHAR(255),
    contact_phone   VARCHAR(50),

    -- Exactamente un perfil por tenant debe tener is_default = TRUE.
    -- Aplicado con índice único parcial (ver abajo).
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_tenant_billing_profiles_type CHECK (billing_type IN ('PERSONAL', 'COMPANY'))
);

-- Índice único parcial: a lo sumo un perfil de facturación default por tenant
CREATE UNIQUE INDEX uq_tenant_billing_profiles_default
    ON tenant_billing_profiles(tenant_id)
    WHERE is_default = TRUE;

CREATE INDEX idx_tenant_billing_profiles_tenant
    ON tenant_billing_profiles(tenant_id);

CREATE TRIGGER tenant_billing_profiles_updated_at
    BEFORE UPDATE ON tenant_billing_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  tenant_billing_profiles IS 'Fiscal / billing data per Tenant. Used to generate invoices and CFDI.';
COMMENT ON COLUMN tenant_billing_profiles.billing_type IS 'PERSONAL = persona física / individual; COMPANY = persona moral / empresa';
COMMENT ON COLUMN tenant_billing_profiles.tax_id       IS 'Tax identification number (RFC, NIT, RUT, VAT, EIN, etc.)';
COMMENT ON COLUMN tenant_billing_profiles.tax_regime   IS 'SAT tax regime code (e.g. 601 = General de Ley, 612 = Personas Físicas con Actividades Empresariales)';
COMMENT ON COLUMN tenant_billing_profiles.country      IS 'ISO 3166-1 alpha-2 country code (default MX)';

-- ---------------------------------------------------------------------------
-- payment_methods
-- Tokens PSP por Tenant. NUNCA almacena datos crudos de tarjeta.
-- ---------------------------------------------------------------------------
CREATE TABLE payment_methods (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,

    -- Proveedor de pago
    provider        VARCHAR(50)  NOT NULL,

    -- Tipo de método de pago
    method_type     VARCHAR(20)  NOT NULL,

    -- Token PSP (referencia al método de pago almacenado en el proveedor).
    -- En producción debe almacenarse cifrado en reposo.
    -- Stripe: pm_xxx / cus_xxx. MercadoPago: customer_id + card_id.
    -- NULL para tipos MOCK y MANUAL.
    provider_token  VARCHAR(500),

    -- ── Datos de tarjeta para display (NO el PAN completo) ────────────────
    last_four       VARCHAR(4),
    card_brand      VARCHAR(50),    -- VISA, MASTERCARD, AMEX, CARNET, etc.
    expiry_month    SMALLINT,
    expiry_year     SMALLINT,

    -- ── PayPal ────────────────────────────────────────────────────────────
    paypal_email    VARCHAR(255),

    -- ── Display ───────────────────────────────────────────────────────────
    -- Label legible para el UI (p.ej. "VISA **** 4242", "PayPal admin@acme.com")
    display_label   VARCHAR(100),

    -- Exactamente un método por tenant debe tener is_default = TRUE.
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,

    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_payment_methods_provider     CHECK (provider     IN ('STRIPE', 'MERCADOPAGO', 'PAYPAL', 'MANUAL', 'MOCK')),
    CONSTRAINT chk_payment_methods_type         CHECK (method_type  IN ('CARD', 'PAYPAL', 'BANK_TRANSFER', 'MOCK')),
    CONSTRAINT chk_payment_methods_status       CHECK (status       IN ('ACTIVE', 'EXPIRED', 'REVOKED')),
    CONSTRAINT chk_payment_methods_expiry_month CHECK (expiry_month BETWEEN 1 AND 12),
    CONSTRAINT chk_payment_methods_expiry_year  CHECK (expiry_year  >= 2020)
);

-- Índice único parcial: a lo sumo un método de pago default por tenant
CREATE UNIQUE INDEX uq_payment_methods_default
    ON payment_methods(tenant_id)
    WHERE is_default = TRUE;

CREATE INDEX idx_payment_methods_tenant
    ON payment_methods(tenant_id);

CREATE INDEX idx_payment_methods_status
    ON payment_methods(tenant_id, status);

CREATE TRIGGER payment_methods_updated_at
    BEFORE UPDATE ON payment_methods
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE  payment_methods IS 'PSP payment method tokens per Tenant. Never stores raw card data (PAN/CVV).';
COMMENT ON COLUMN payment_methods.provider_token IS 'Opaque reference to the payment method stored at the PSP. Encrypt at rest in production.';
COMMENT ON COLUMN payment_methods.last_four      IS 'Last 4 digits of the card for display only. Never store the full PAN here.';
COMMENT ON COLUMN payment_methods.display_label  IS 'Human-readable label for the UI (e.g. "VISA **** 4242" or "PayPal admin@acme.com").';

