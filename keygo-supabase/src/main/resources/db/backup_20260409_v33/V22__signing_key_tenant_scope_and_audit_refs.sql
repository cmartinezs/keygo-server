-- =============================================================================
-- V22: Relaciones huérfanas — signing_keys scoped por tenant + auditoría de
--      clave firmante en sessions y refresh_tokens
-- =============================================================================

-- 1. signing_keys: añadir tenant_id (nullable = clave global/platform-wide)
ALTER TABLE signing_keys
  ADD COLUMN tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE;

CREATE INDEX idx_signing_keys_tenant ON signing_keys(tenant_id);

COMMENT ON COLUMN signing_keys.tenant_id IS
  'Tenant propietario de la clave. NULL = clave global de plataforma (fallback para todos los tenants).';

-- 2. sessions: registrar qué clave firmó los tokens de esta sesión (nullable = sesiones legacy)
ALTER TABLE sessions
  ADD COLUMN signing_key_id UUID REFERENCES signing_keys(id);

CREATE INDEX idx_sessions_signing_key ON sessions(signing_key_id);

COMMENT ON COLUMN sessions.signing_key_id IS
  'Clave RSA que firmó el access_token de apertura de esta sesión. NULL = sesión creada antes de esta migración.';

-- 3. refresh_tokens: registrar qué clave firmó el access_token del RT (nullable = RT legacy)
ALTER TABLE refresh_tokens
  ADD COLUMN signing_key_id UUID REFERENCES signing_keys(id);

CREATE INDEX idx_refresh_tokens_signing_key ON refresh_tokens(signing_key_id);

COMMENT ON COLUMN refresh_tokens.signing_key_id IS
  'Clave RSA que firmó el access_token emitido junto a este refresh token. NULL = RT creado antes de esta migración.';

