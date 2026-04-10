-- Purpose: aggregate audit events by category and day.
-- Detail pair: audit_events_detail.sql
-- Parameters: :from_ts, :to_ts
SELECT
    date_trunc('day', occurred_at) AS day_bucket,
    event_category,
    COUNT(*) AS total_events
FROM audit_events
WHERE occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY date_trunc('day', occurred_at), event_category
ORDER BY day_bucket, event_category;
