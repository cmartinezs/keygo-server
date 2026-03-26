-- V13: Extend tenant_users with OIDC standard profile claims (§5.3)
-- Extends tenant_users table with additional OIDC profile claims.
-- These fields are part of the canonical identity at the tenant level,
-- not per-app (which would go in membership_attributes — future V14).
--
-- OIDC §5.3 claim mapping:
--   phone_number        → "phone" scope
--   locale              → "profile" scope (BCP47, e.g. "es-MX")
--   zoneinfo            → "profile" scope (tz database, e.g. "America/Mexico_City")
--   profile_picture_url → "profile" scope (external URL)
--   birthdate           → "profile" scope (ISO 8601 date, e.g. "1990-01-15")
--   website             → "profile" scope (URL)
--
-- Decisión de diseño: el perfil canónico del usuario vive en tenant_users
-- (nivel tenant), no en memberships (nivel app). Esto sigue OIDC §5.3 y
-- los patrones de Auth0/Keycloak donde "el usuario tiene un perfil, las
-- apps tienen atributos de membresía".

ALTER TABLE tenant_users
  ADD COLUMN IF NOT EXISTS phone_number        VARCHAR(30),
  ADD COLUMN IF NOT EXISTS locale              VARCHAR(10),
  ADD COLUMN IF NOT EXISTS zoneinfo            VARCHAR(50),
  ADD COLUMN IF NOT EXISTS profile_picture_url TEXT,
  ADD COLUMN IF NOT EXISTS birthdate           DATE,
  ADD COLUMN IF NOT EXISTS website             VARCHAR(2048);

COMMENT ON COLUMN tenant_users.phone_number        IS 'OIDC phone_number claim — phone scope';
COMMENT ON COLUMN tenant_users.locale              IS 'OIDC locale claim — BCP47 (e.g. es-MX) — profile scope';
COMMENT ON COLUMN tenant_users.zoneinfo            IS 'OIDC zoneinfo claim — tz database (e.g. America/Mexico_City) — profile scope';
COMMENT ON COLUMN tenant_users.profile_picture_url IS 'OIDC picture claim — external URL — profile scope';
COMMENT ON COLUMN tenant_users.birthdate           IS 'OIDC birthdate claim — ISO 8601 date — profile scope';
COMMENT ON COLUMN tenant_users.website             IS 'OIDC website claim — URL — profile scope';

