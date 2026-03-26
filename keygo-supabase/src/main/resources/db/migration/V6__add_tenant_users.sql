-- V6__add_tenant_users.sql
-- Creates the tenant_users table for tenant-scoped user identity (Phase 3)
-- Username and email are unique within a tenant (not globally)

CREATE TABLE tenant_users (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id    UUID        NOT NULL,
    username     VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name   VARCHAR(100),
    last_name    VARCHAR(100),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_tenant_users PRIMARY KEY (id),
    CONSTRAINT fk_tenant_users_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT uq_tenant_users_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT uq_tenant_users_tenant_username UNIQUE (tenant_id, username),
    CONSTRAINT chk_tenant_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING'))
);

CREATE INDEX idx_tenant_users_tenant_id ON tenant_users (tenant_id);
CREATE INDEX idx_tenant_users_email     ON tenant_users (email);
CREATE INDEX idx_tenant_users_username  ON tenant_users (username);
CREATE INDEX idx_tenant_users_status    ON tenant_users (status);

