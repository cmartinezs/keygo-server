-- Purpose: drill-down audit events globally.
-- Parameters: :from_ts, :to_ts, :limit, :offset
SELECT
    ae.occurred_at,
    ae.event_category,
    ae.event_type,
    ae.event_action,
    ae.event_outcome,
    ae.severity,
    ae.summary,
    ae.request_id,
    ae.correlation_id
FROM audit_events ae
WHERE ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY ae.occurred_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
