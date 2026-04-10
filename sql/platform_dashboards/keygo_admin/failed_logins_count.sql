-- Purpose: aggregate failed logins and success rate.
-- Detail pair: failed_logins_detail.sql
-- Parameters: :from_ts, :to_ts
WITH failed AS (
    SELECT COUNT(*) AS failed_count
    FROM audit_events
    WHERE event_category = 'AUTH'
      AND event_type = 'LOGIN_FAILURE'
      AND occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
),
success AS (
    SELECT COUNT(*) AS success_count
    FROM audit_events
    WHERE event_category = 'AUTH'
      AND event_type = 'LOGIN_SUCCESS'
      AND occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
)
SELECT
    success.success_count AS total_successful_logins,
    failed.failed_count AS total_failed_logins,
    CASE
        WHEN success.success_count + failed.failed_count = 0 THEN 0
        ELSE ROUND((success.success_count::numeric / (success.success_count + failed.failed_count)::numeric) * 100, 2)
    END AS login_success_rate_pct
FROM failed, success;
