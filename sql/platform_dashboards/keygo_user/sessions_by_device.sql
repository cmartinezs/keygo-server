-- Purpose: aggregate sessions by device type for one user.
-- Detail pair: sessions_by_device_detail.sql
-- Parameters: :platform_user_id, :from_ts, :to_ts
SELECT
    COALESCE(ps.device_type, 'UNKNOWN') AS device_type,
    COUNT(*) AS session_count
FROM platform_sessions ps
WHERE ps.platform_user_id = CAST(:platform_user_id AS uuid)
  AND ps.started_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY COALESCE(ps.device_type, 'UNKNOWN')
ORDER BY session_count DESC;
