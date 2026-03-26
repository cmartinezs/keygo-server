-- Fase 7: Refresh tokens y sesiones de usuario
-- V11__add_refresh_tokens_and_sessions.sql

-- ─────────────────────────────────────────────────────────────────────────────
-- Limpieza preventiva: elimina las tablas si existen con un esquema anterior
-- (necesario cuando la migración fue ejecutada parcialmente y dejó tablas
--  incompletas; CREATE TABLE IF NOT EXISTS las saltaría silenciosamente y
--  los índices fallarían con "column does not exist").
-- ─────────────────────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS sessions CASCADE;

-- ─────────────────────────────────────────────────────────────────────────────
-- Tabla: sessions
-- Representa una sesión de usuario vinculada a un tenant y una app cliente.
-- Una sesión agrupa todos los refresh tokens emitidos en ese contexto.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sessions (
  id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        UUID         NOT NULL REFERENCES tenants(id)       ON DELETE CASCADE,
  client_app_id    UUID         NOT NULL REFERENCES client_apps(id)   ON DELETE CASCADE,
  user_id          UUID         NOT NULL REFERENCES tenant_users(id)  ON DELETE CASCADE,
  status           VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE', 'TERMINATED', 'EXPIRED')),
  expires_at       TIMESTAMPTZ  NOT NULL,
  last_accessed_at TIMESTAMPTZ  NOT NULL,
  user_agent       TEXT,
  ip_address       VARCHAR(64),
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_tenant  ON sessions(user_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status        ON sessions(status);

-- ─────────────────────────────────────────────────────────────────────────────
-- Tabla: refresh_tokens
-- Almacena el hash SHA-256 (hex) del refresh token plano.
-- El token plano nunca se persiste; solo se devuelve al cliente en la emisión.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  token_hash       VARCHAR(64)  NOT NULL UNIQUE,  -- SHA-256 hex = 64 chars
  session_id       UUID         NOT NULL REFERENCES sessions(id)      ON DELETE CASCADE,
  tenant_id        UUID         NOT NULL REFERENCES tenants(id)       ON DELETE CASCADE,
  client_app_id    UUID         NOT NULL REFERENCES client_apps(id)   ON DELETE CASCADE,
  user_id          UUID         NOT NULL REFERENCES tenant_users(id)  ON DELETE CASCADE,
  requested_scopes TEXT         NOT NULL,
  status           VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED', 'REVOKED')),
  expires_at       TIMESTAMPTZ  NOT NULL,
  used_at          TIMESTAMPTZ,
  replaced_by_id   UUID         REFERENCES refresh_tokens(id),
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_hash        ON refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_session      ON refresh_tokens(session_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_tenant  ON refresh_tokens(user_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_status       ON refresh_tokens(status);

