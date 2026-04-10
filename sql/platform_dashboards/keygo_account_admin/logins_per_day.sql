-- Purpose: aggregate successful logins by day within contractor scope.
-- Detail pair: login_events_detail.sql
-- Parameters: :contractor_id, :from_ts, :to_ts
SELECT
    date_trunc('day', ae.occurred_at) AS day_bucket,
    COUNT(*) AS successful_logins
FROM audit_events ae
WHERE ae.contractor_id = CAST(:contractor_id AS uuid)
  AND ae.event_category = 'AUTH'
  AND ae.event_type = 'LOGIN_SUCCESS'
  AND ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY date_trunc('day', ae.occurred_at)
ORDER BY day_bucket;
