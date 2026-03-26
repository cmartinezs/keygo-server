-- V9: Tabla de claves de firma RSA para emisión de tokens JWT
-- Fase 6 — Token signing & OIDC metadata
-- Fecha: 2026-03-22

CREATE TABLE IF NOT EXISTS signing_keys (
  id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  kid              VARCHAR(100) NOT NULL,
  algorithm        VARCHAR(20)  NOT NULL,
  status           VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE', 'RETIRED', 'REVOKED')),
  public_material  TEXT         NOT NULL,
  private_material TEXT,
  activated_at     TIMESTAMPTZ  NOT NULL,
  retired_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_signing_keys_kid UNIQUE (kid)
);

CREATE INDEX IF NOT EXISTS idx_signing_keys_status ON signing_keys (status);

