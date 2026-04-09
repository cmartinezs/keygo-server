-- Purpose: drill-down for tenant activity ranking.
-- Parameters: :tenant_id, :from_ts, :to_ts, :limit, :offset
SELECT
    ae.occurred_at,
    ae.event_category,
    ae.event_type,
    ae.event_outcome,
    ae.summary,
    ae.actor_platform_user_id,
    pu.email,
    ae.client_app_id,
    ca.client_id
FROM audit_events ae
LEFT JOIN platform_users pu ON pu.id = ae.actor_platform_user_id
LEFT JOIN client_apps ca ON ca.id = ae.client_app_id
WHERE ae.tenant_id = CAST(:tenant_id AS uuid)
  AND ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY ae.occurred_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
