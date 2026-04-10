-- Purpose: aggregate active platform and OAuth sessions within contractor scope.
-- Detail pair: active_sessions_detail.sql
-- Parameters: :contractor_id
SELECT
    COUNT(DISTINCT ps.id) AS active_platform_sessions,
    COUNT(DISTINCT os.id) AS active_oauth_sessions
FROM audit_events ae
JOIN platform_sessions ps ON ps.id = ae.platform_session_id
LEFT JOIN oauth_sessions os ON os.id = ae.oauth_session_id
WHERE ae.contractor_id = CAST(:contractor_id AS uuid)
  AND ps.status = 'ACTIVE';
