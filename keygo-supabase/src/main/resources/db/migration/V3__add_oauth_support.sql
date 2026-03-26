-- =========================================================
-- Add OAuth Support
-- Agregar soporte de OAuth
-- =========================================================
-- Version: 3.0
-- Description: Adds OAuth provider integration
-- Descripción: Agrega integración con proveedores OAuth
-- =========================================================

-- Create OAuth providers table
CREATE TABLE IF NOT EXISTS oauth_providers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    client_secret VARCHAR(255),
    authorization_url VARCHAR(500),
    token_url VARCHAR(500),
    user_info_url VARCHAR(500),
    scope VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT oauth_providers_name_format CHECK (name ~* '^[a-z0-9_-]+$')
);

-- Create OAuth tokens table
CREATE TABLE IF NOT EXISTS oauth_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_id UUID NOT NULL REFERENCES oauth_providers(id) ON DELETE CASCADE,
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    token_type VARCHAR(50),
    expires_at TIMESTAMP WITH TIME ZONE,
    scope VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, provider_id)
);

-- Indexes
CREATE INDEX idx_oauth_providers_name ON oauth_providers(name);
CREATE INDEX idx_oauth_providers_is_active ON oauth_providers(is_active);
CREATE INDEX idx_oauth_tokens_user_id ON oauth_tokens(user_id);
CREATE INDEX idx_oauth_tokens_provider_id ON oauth_tokens(provider_id);

-- Apply update trigger to oauth_providers
CREATE TRIGGER oauth_providers_updated_at
    BEFORE UPDATE ON oauth_providers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply update trigger to oauth_tokens
CREATE TRIGGER oauth_tokens_updated_at
    BEFORE UPDATE ON oauth_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE oauth_providers IS 'OAuth provider configurations / Configuraciones de proveedores OAuth';
COMMENT ON TABLE oauth_tokens IS 'OAuth tokens for users / Tokens OAuth de usuarios';

