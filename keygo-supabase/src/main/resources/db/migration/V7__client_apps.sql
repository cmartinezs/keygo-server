-- ============================================================================
-- V7__client_apps.sql
-- Tenant-owned OAuth/OIDC client applications.
-- id        = technical UUID primary key used by internal relations and FKs.
-- client_id = public OAuth/OIDC client identifier resolved by protocol endpoints.
-- client_id remains globally unique by design because Keygo acts as a shared
-- authorization server and protocol resolution must stay unambiguous across tenants.
-- ============================================================================

CREATE TABLE client_apps (
    id             UUID           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id      UUID           NOT NULL,
    client_id      VARCHAR(255)   NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    type           VARCHAR(20)    NOT NULL,
    hashed_secret  VARCHAR(255),
    status         VARCHAR(20)    NOT NULL,
    is_internal    BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_client_apps PRIMARY KEY (id),
    CONSTRAINT uq_client_apps_client_id UNIQUE (client_id),
    CONSTRAINT uq_client_apps_id_tenant UNIQUE (id, tenant_id),
    CONSTRAINT fk_client_apps_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT chk_client_apps_type CHECK (type IN ('PUBLIC', 'CONFIDENTIAL')),
    CONSTRAINT chk_client_apps_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
    CONSTRAINT chk_client_apps_secret CHECK (
        (type = 'PUBLIC' AND hashed_secret IS NULL) OR
        (type = 'CONFIDENTIAL' AND hashed_secret IS NOT NULL)
    )
);

CREATE INDEX idx_client_apps_tenant_status ON client_apps(tenant_id, status);

CREATE TRIGGER trg_client_apps_updated_at
    BEFORE UPDATE ON client_apps
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE client_redirect_uris (
    id             UUID           NOT NULL DEFAULT gen_random_uuid(),
    client_app_id  UUID           NOT NULL,
    uri            VARCHAR(2048)  NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_client_redirect_uris PRIMARY KEY (id),
    CONSTRAINT uq_client_redirect_uris_app_uri UNIQUE (client_app_id, uri),
    CONSTRAINT fk_client_redirect_uris_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE,
    CONSTRAINT chk_client_redirect_uris_no_wildcards CHECK (position('*' in uri) = 0)
);

CREATE TABLE client_allowed_grants (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    client_app_id  UUID          NOT NULL,
    grant_type     VARCHAR(50)   NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_client_allowed_grants PRIMARY KEY (id),
    CONSTRAINT uq_client_allowed_grants UNIQUE (client_app_id, grant_type),
    CONSTRAINT fk_client_allowed_grants_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE
);

CREATE TABLE client_allowed_scopes (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    client_app_id  UUID          NOT NULL,
    scope          VARCHAR(100)  NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_client_allowed_scopes PRIMARY KEY (id),
    CONSTRAINT uq_client_allowed_scopes UNIQUE (client_app_id, scope),
    CONSTRAINT fk_client_allowed_scopes_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE
);

COMMENT ON TABLE client_apps IS 'OAuth/OIDC client applications owned by a tenant.';
COMMENT ON COLUMN client_apps.id IS 'Technical UUID primary key used by internal relations, joins and foreign keys.';
COMMENT ON COLUMN client_apps.client_id IS 'Public OAuth/OIDC client identifier used by protocol flows such as /authorize and /token. Deliberately unique globally to avoid cross-tenant ambiguity at the authorization server boundary.';
COMMENT ON COLUMN client_apps.tenant_id IS 'Tenant owner of the client application. Internal relations still target client_apps.id.';
COMMENT ON COLUMN client_apps.is_internal IS 'True for internal technical clients such as keygo-ui.';
COMMENT ON TABLE client_redirect_uris IS 'Exact redirect URIs. Wildcards are rejected at DB level.';
COMMENT ON COLUMN client_redirect_uris.client_app_id IS 'Internal FK to client_apps.id, never to the public client_id.';
COMMENT ON TABLE client_allowed_grants IS 'Allowed OAuth grants for a client app.';
COMMENT ON COLUMN client_allowed_grants.client_app_id IS 'Internal FK to client_apps.id, not a protocol lookup key.';
COMMENT ON TABLE client_allowed_scopes IS 'Allowed scopes for a client app.';
COMMENT ON COLUMN client_allowed_scopes.client_app_id IS 'Internal FK to client_apps.id, not a protocol lookup key.';
