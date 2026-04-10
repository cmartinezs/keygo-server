-- Purpose: aggregate active platform sessions for one user.
-- Detail pair: active_platform_sessions_detail.sql
-- Parameters: :platform_user_id
SELECT COUNT(*) AS active_platform_sessions
FROM platform_sessions ps
WHERE ps.platform_user_id = CAST(:platform_user_id AS uuid)
  AND ps.status = 'ACTIVE';
