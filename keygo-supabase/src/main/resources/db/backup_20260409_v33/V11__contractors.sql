-- =============================================================================
-- V11: Contractors — entidad central del billing v2
--
-- Un Contractor es la persona física o empresa que firma contratos con la
-- plataforma. Tiene una relación 1:1 con un TenantUser en el tenant del
-- proveedor (p. ej. el tenant "keygo").
--
-- Ciclo de vida:
--   PENDING  → email verificado, antes de pagar (creado en verify-email del contrato)
--   ACTIVE   → primer contrato activado exitosamente
--   SUSPENDED→ suspendido por impago u otra razón administrativa
--
-- Tras crear esta tabla se agregan:
--   1. FK contractors.tenant_user_id → tenant_users(id)
--   2. FK tenants.contractor_id      → contractors(id)  (backfill de V3)
--
-- Esto cierra el ciclo bidireccional del modelo v2:
--   contractors --< tenants         (un contratante puede crear N tenants)
--   contractors  1- tenant_users    (1 cuenta de usuario en el tenant proveedor)
-- =============================================================================
-- ---------------------------------------------------------------------------
-- 1) Tabla contractors
-- ---------------------------------------------------------------------------
CREATE TABLE contractors (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_user_id UUID         NOT NULL UNIQUE REFERENCES tenant_users(id) ON DELETE RESTRICT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_contractors_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED'))
);
CREATE INDEX idx_contractors_status ON contractors(status);
CREATE TRIGGER contractors_updated_at
    BEFORE UPDATE ON contractors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE  contractors                IS 'Central billing entity. Represents the person/company that signs contracts with the platform.';
COMMENT ON COLUMN contractors.tenant_user_id IS '1:1 link to a TenantUser in the provider tenant. The contractor always has a user account there.';
COMMENT ON COLUMN contractors.status         IS 'PENDING = email verified, awaiting first payment | ACTIVE = first contract activated | SUSPENDED = payment issue';
-- ---------------------------------------------------------------------------
-- 2) Agregar FK tenants.contractor_id → contractors(id)
--    La columna ya existe (creada en V3 sin FK para evitar dependencia circular).
--    ON DELETE SET NULL: si se elimina el contractor, el tenant queda huérfano
--    pero no se elimina (protección histórica).
-- ---------------------------------------------------------------------------
ALTER TABLE tenants
    ADD CONSTRAINT fk_tenants_contractor_id
        FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE SET NULL;
COMMENT ON COLUMN tenants.contractor_id IS 'NULL = system/platform tenant. NOT NULL = created by contractor within MAX_TENANTS entitlement.';
