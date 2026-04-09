-- Purpose: drill-down auth login events globally.
-- Parameters: :from_ts, :to_ts, :limit, :offset
SELECT
    ae.occurred_at,
    ae.event_type,
    ae.event_outcome,
    pu.email,
    t.slug AS tenant_slug,
    ca.client_id,
    ae.summary
FROM audit_events ae
LEFT JOIN platform_users pu ON pu.id = ae.actor_platform_user_id
LEFT JOIN tenants t ON t.id = ae.tenant_id
LEFT JOIN client_apps ca ON ca.id = ae.client_app_id
WHERE ae.event_category = 'AUTH'
  AND ae.event_type IN ('LOGIN_SUCCESS', 'LOGIN_FAILURE')
  AND ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY ae.occurred_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
