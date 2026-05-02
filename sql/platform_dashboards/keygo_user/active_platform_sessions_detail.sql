-- Purpose: drill-down active platform sessions for one user.
-- Parameters: :platform_user_id, :limit, :offset
SELECT
    ps.id,
    ps.device_type,
    ps.browser_name,
    ps.os_name,
    ps.ip_address,
    ps.country_code,
    ps.city,
    ps.started_at,
    ps.last_activity_at,
    ps.status,
    ps.termination_reason
FROM platform_sessions ps
WHERE ps.platform_user_id = CAST(:platform_user_id AS uuid)
ORDER BY ps.started_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
