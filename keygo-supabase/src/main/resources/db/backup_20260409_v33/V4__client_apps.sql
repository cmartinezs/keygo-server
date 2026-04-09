-- =============================================================================
-- V4: Client Apps — aplicaciones OAuth2 registradas bajo un Tenant
-- client_apps        : la app en sí (PUBLIC=SPA/mobile, CONFIDENTIAL=backend)
-- client_redirect_uris   : URIs de redirección permitidas (sin wildcards)
-- client_allowed_grants  : grant types autorizados por app
-- client_allowed_scopes  : scopes autorizados por app
-- =============================================================================
CREATE TABLE client_apps (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    client_id     VARCHAR(255) NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    type          VARCHAR(20)  NOT NULL,
    hashed_secret VARCHAR(255),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_client_apps_type   CHECK (type   IN ('PUBLIC', 'CONFIDENTIAL')),
    CONSTRAINT chk_client_apps_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING'))
);
CREATE TABLE client_redirect_uris (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id UUID          NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    uri           VARCHAR(2048) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE TABLE client_allowed_grants (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id UUID        NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    grant_type    VARCHAR(50) NOT NULL
);
CREATE TABLE client_allowed_scopes (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    scope         VARCHAR(100) NOT NULL
);
CREATE INDEX idx_client_apps_tenant_id ON client_apps(tenant_id);
CREATE INDEX idx_client_apps_client_id ON client_apps(client_id);
CREATE INDEX idx_client_apps_status    ON client_apps(status);
CREATE INDEX idx_redirect_uris_app     ON client_redirect_uris(client_app_id);
CREATE INDEX idx_allowed_grants_app    ON client_allowed_grants(client_app_id);
CREATE INDEX idx_allowed_scopes_app    ON client_allowed_scopes(client_app_id);
CREATE TRIGGER client_apps_updated_at
    BEFORE UPDATE ON client_apps
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
COMMENT ON TABLE  client_apps IS 'OAuth2 client applications registered under a Tenant';
COMMENT ON COLUMN client_apps.type IS 'PUBLIC = SPA/mobile (no secret) | CONFIDENTIAL = server-side (has secret)';
