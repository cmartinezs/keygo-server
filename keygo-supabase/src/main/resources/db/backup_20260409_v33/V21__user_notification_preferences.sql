-- V21__user_notification_preferences.sql
-- Tabla para persistir las preferencias de notificación del usuario (self-service)

CREATE TABLE user_notification_preferences (
  id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id                 UUID        NOT NULL REFERENCES tenant_users(id) ON DELETE CASCADE,
  tenant_id               UUID        NOT NULL REFERENCES tenants(id)      ON DELETE CASCADE,
  security_alerts_email   BOOLEAN     NOT NULL DEFAULT TRUE,
  security_alerts_in_app  BOOLEAN     NOT NULL DEFAULT TRUE,
  billing_alerts_email    BOOLEAN     NOT NULL DEFAULT TRUE,
  product_updates_email   BOOLEAN     NOT NULL DEFAULT FALSE,
  weekly_digest           BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, tenant_id)
);

CREATE INDEX idx_notif_prefs_user_tenant ON user_notification_preferences(user_id, tenant_id);
