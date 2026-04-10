-- Purpose: drill-down active sessions within contractor scope.
-- Parameters: :contractor_id, :limit, :offset
SELECT DISTINCT
    ps.id AS platform_session_id,
    pu.email,
    ps.device_type,
    ps.browser_name,
    ps.ip_address,
    os.id AS oauth_session_id,
    t.slug AS tenant_slug,
    ca.client_id
FROM audit_events ae
JOIN platform_sessions ps ON ps.id = ae.platform_session_id
LEFT JOIN platform_users pu ON pu.id = ps.platform_user_id
LEFT JOIN oauth_sessions os ON os.id = ae.oauth_session_id
LEFT JOIN tenants t ON t.id = os.tenant_id
LEFT JOIN client_apps ca ON ca.id = os.client_app_id
WHERE ae.contractor_id = CAST(:contractor_id AS uuid)
  AND ps.status = 'ACTIVE'
ORDER BY ps.started_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
