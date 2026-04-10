-- Purpose: self-service profile summary.
-- Parameters: :platform_user_id
SELECT
    pu.id,
    pu.email,
    pu.display_name,
    pu.status,
    pu.email_verified_at,
    pu.last_login_at,
    pnp.security_alerts_email,
    pnp.security_alerts_in_app,
    pnp.billing_alerts_email,
    pnp.product_updates_email,
    pnp.weekly_digest
FROM platform_users pu
LEFT JOIN platform_user_notification_preferences pnp
       ON pnp.platform_user_id = pu.id
WHERE pu.id = CAST(:platform_user_id AS uuid);
