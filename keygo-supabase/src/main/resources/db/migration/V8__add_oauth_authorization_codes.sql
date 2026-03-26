-- V8__add_oauth_authorization_codes.sql
-- Tabla para almacenar códigos de autorización OAuth 2.0

CREATE TABLE IF NOT EXISTS authorization_codes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(256) NOT NULL UNIQUE,
  client_app_id UUID NOT NULL,
  tenant_id UUID NOT NULL,
  user_id UUID NOT NULL,
  code_challenge VARCHAR(256) NOT NULL,
  code_challenge_method VARCHAR(10) NOT NULL CHECK (code_challenge_method IN ('plain', 'S256')),
  requested_scopes TEXT NOT NULL,
  redirect_uri VARCHAR(2048) NOT NULL,
  status VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'used', 'expired', 'revoked')),
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  used_at TIMESTAMP WITH TIME ZONE,

  CONSTRAINT fk_authorization_codes_client_app FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE,
  CONSTRAINT fk_authorization_codes_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
  CONSTRAINT fk_authorization_codes_user FOREIGN KEY (user_id) REFERENCES tenant_users(id) ON DELETE CASCADE
);

-- Índices para búsquedas rápidas
CREATE INDEX idx_authorization_codes_code ON authorization_codes(code);
CREATE INDEX idx_authorization_codes_tenant_id ON authorization_codes(tenant_id);
CREATE INDEX idx_authorization_codes_client_app_id ON authorization_codes(client_app_id);
CREATE INDEX idx_authorization_codes_user_id ON authorization_codes(user_id);
CREATE INDEX idx_authorization_codes_status ON authorization_codes(status);
CREATE INDEX idx_authorization_codes_expires_at ON authorization_codes(expires_at);

