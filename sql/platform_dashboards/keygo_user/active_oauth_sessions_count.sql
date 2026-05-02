-- Purpose: aggregate active OAuth sessions for one platform user.
-- Detail pair: active_oauth_sessions_detail.sql
-- Parameters: :platform_user_id
SELECT COUNT(*) AS active_oauth_sessions
FROM oauth_sessions os
WHERE os.platform_user_id = CAST(:platform_user_id AS uuid)
  AND os.status = 'ACTIVE';
