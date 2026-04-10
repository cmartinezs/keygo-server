-- Purpose: drill-down active OAuth sessions for one platform user.
-- Parameters: :platform_user_id, :limit, :offset
SELECT
    os.id,
    t.slug AS tenant_slug,
    ca.client_id,
    os.status,
    os.granted_scopes,
    os.started_at,
    os.last_accessed_at,
    os.termination_reason
FROM oauth_sessions os
JOIN tenants t ON t.id = os.tenant_id
JOIN client_apps ca ON ca.id = os.client_app_id
WHERE os.platform_user_id = CAST(:platform_user_id AS uuid)
ORDER BY os.started_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
