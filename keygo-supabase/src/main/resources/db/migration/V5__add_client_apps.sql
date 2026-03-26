-- V5: Client Applications model
-- Adds tables for OAuth2 client applications registered under a tenant.

CREATE TABLE client_apps (
    id           UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    tenant_id    UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    client_id    VARCHAR(255) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    type         VARCHAR(20)  NOT NULL CHECK (type IN ('PUBLIC', 'CONFIDENTIAL')),
    hashed_secret VARCHAR(255),
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE client_redirect_uris (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    client_app_id  UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    uri            VARCHAR(2048) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE client_allowed_grants (
    id             UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    client_app_id  UUID        NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    grant_type     VARCHAR(50) NOT NULL
);

CREATE TABLE client_allowed_scopes (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    client_app_id  UUID         NOT NULL REFERENCES client_apps(id) ON DELETE CASCADE,
    scope          VARCHAR(100) NOT NULL
);

CREATE INDEX idx_client_apps_tenant_id  ON client_apps (tenant_id);
CREATE INDEX idx_client_apps_client_id  ON client_apps (client_id);
CREATE INDEX idx_client_apps_status     ON client_apps (status);
CREATE INDEX idx_redirect_uris_app      ON client_redirect_uris (client_app_id);
CREATE INDEX idx_allowed_grants_app     ON client_allowed_grants (client_app_id);
CREATE INDEX idx_allowed_scopes_app     ON client_allowed_scopes (client_app_id);

