-- Purpose: aggregate denied actions in managed contractor scope.
-- Detail pair: denied_actions_detail.sql
-- Parameters: :contractor_id, :from_ts, :to_ts
SELECT
    ae.event_category,
    COUNT(*) AS denied_count
FROM audit_events ae
WHERE ae.contractor_id = CAST(:contractor_id AS uuid)
  AND ae.event_outcome = 'DENIED'
  AND ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY ae.event_category
ORDER BY denied_count DESC;
