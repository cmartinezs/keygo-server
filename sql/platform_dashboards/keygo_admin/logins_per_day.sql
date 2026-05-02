-- Purpose: aggregate successful and failed logins by day.
-- Detail pair: login_events_detail.sql
-- Parameters: :from_ts, :to_ts
SELECT
    date_trunc('day', ae.occurred_at) AS day_bucket,
    COUNT(*) FILTER (WHERE ae.event_type = 'LOGIN_SUCCESS') AS successful_logins,
    COUNT(*) FILTER (WHERE ae.event_type = 'LOGIN_FAILURE') AS failed_logins
FROM audit_events ae
WHERE ae.event_category = 'AUTH'
  AND ae.event_type IN ('LOGIN_SUCCESS', 'LOGIN_FAILURE')
  AND ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY date_trunc('day', ae.occurred_at)
ORDER BY day_bucket;
