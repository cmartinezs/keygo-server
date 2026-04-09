-- Purpose: drill-down sessions grouped by one device type.
-- Parameters: :platform_user_id, :from_ts, :to_ts, :limit, :offset
SELECT
    ps.id,
    ps.device_type,
    ps.browser_name,
    ps.browser_version,
    ps.os_name,
    ps.os_version,
    ps.started_at,
    ps.ended_at,
    ps.last_route
FROM platform_sessions ps
WHERE ps.platform_user_id = CAST(:platform_user_id AS uuid)
  AND ps.started_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY ps.started_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
