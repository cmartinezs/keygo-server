-- V7__add_memberships.sql
-- Add support for memberships (user access to apps) and app-scoped roles

-- Table: app_role
-- Represents a role within a single client app
CREATE TABLE IF NOT EXISTS app_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_app_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    display_name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_app_role_client_app FOREIGN KEY (client_app_id)
        REFERENCES client_apps(id) ON DELETE CASCADE,
    CONSTRAINT uq_app_role_client_app_code UNIQUE (client_app_id, code),
    CONSTRAINT ck_app_role_code CHECK (code ~ '^[a-z][a-z0-9_-]*$')
);

CREATE INDEX idx_app_role_client_app_id ON app_role(client_app_id);
CREATE INDEX idx_app_role_code ON app_role(code);

-- Table: membership
-- Represents a user's access to a single client app
CREATE TABLE IF NOT EXISTS membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    client_app_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id)
        REFERENCES tenant_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_client_app FOREIGN KEY (client_app_id)
        REFERENCES client_apps(id) ON DELETE CASCADE,
    CONSTRAINT uq_membership_user_app UNIQUE (user_id, client_app_id)
);

CREATE INDEX idx_membership_user_id ON membership(user_id);
CREATE INDEX idx_membership_client_app_id ON membership(client_app_id);
CREATE INDEX idx_membership_status ON membership(status);

-- Table: membership_role
-- Join table: connects memberships to app roles (many-to-many)
CREATE TABLE IF NOT EXISTS membership_role (
    membership_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_membership_role PRIMARY KEY (membership_id, role_id),
    CONSTRAINT fk_membership_role_membership FOREIGN KEY (membership_id)
        REFERENCES membership(id) ON DELETE CASCADE,
    CONSTRAINT fk_membership_role_app_role FOREIGN KEY (role_id)
        REFERENCES app_role(id) ON DELETE CASCADE
);

CREATE INDEX idx_membership_role_role_id ON membership_role(role_id);

