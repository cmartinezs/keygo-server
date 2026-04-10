-- Purpose: recent security activity for a platform user.
-- Parameters: :platform_user_id, :from_ts, :to_ts, :limit, :offset
SELECT
    ae.occurred_at,
    ae.event_type,
    ae.event_outcome,
    ae.severity,
    ae.summary
FROM audit_events ae
WHERE ae.actor_platform_user_id = CAST(:platform_user_id AS uuid)
  AND ae.event_category IN ('AUTH', 'SECURITY')
  AND ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY ae.occurred_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
