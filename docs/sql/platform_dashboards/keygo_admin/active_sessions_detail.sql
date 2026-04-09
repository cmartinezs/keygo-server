-- Purpose: drill-down active sessions detail.
-- Parameters: :limit, :offset
SELECT
    ps.id AS platform_session_id,
    ps.platform_user_id,
    pu.email,
    ps.status AS platform_status,
    ps.device_type,
    ps.browser_name,
    ps.os_name,
    ps.ip_address,
    ps.started_at,
    ps.last_activity_at,
    os.id AS oauth_session_id,
    t.slug AS tenant_slug,
    ca.client_id
FROM platform_sessions ps
JOIN platform_users pu ON pu.id = ps.platform_user_id
LEFT JOIN oauth_sessions os ON os.platform_session_id = ps.id AND os.status = 'ACTIVE'
LEFT JOIN tenants t ON t.id = os.tenant_id
LEFT JOIN client_apps ca ON ca.id = os.client_app_id
WHERE ps.status = 'ACTIVE'
ORDER BY ps.started_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
