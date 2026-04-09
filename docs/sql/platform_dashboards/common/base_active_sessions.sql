-- Purpose: reusable active platform and OAuth sessions base set.
-- Parameters: :platform_user_id, :tenant_id, :client_app_id
SELECT
    ps.id AS platform_session_id,
    os.id AS oauth_session_id,
    ps.platform_user_id,
    os.tenant_id,
    os.client_app_id,
    ps.status AS platform_session_status,
    os.status AS oauth_session_status,
    ps.started_at,
    COALESCE(os.last_accessed_at, ps.last_accessed_at) AS last_accessed_at
FROM platform_sessions ps
LEFT JOIN oauth_sessions os ON os.platform_session_id = ps.id
WHERE ps.status = 'ACTIVE'
  AND (:platform_user_id IS NULL OR ps.platform_user_id = CAST(:platform_user_id AS uuid))
  AND (:tenant_id IS NULL OR os.tenant_id = CAST(:tenant_id AS uuid))
  AND (:client_app_id IS NULL OR os.client_app_id = CAST(:client_app_id AS uuid));
