-- Purpose: drill-down tenant activity for one platform user.
-- Parameters: :platform_user_id, :tenant_id, :from_ts, :to_ts, :limit, :offset
SELECT
    pae.occurred_at,
    pae.event_category,
    pae.event_type,
    ca.client_id,
    pae.metadata
FROM platform_activity_events pae
LEFT JOIN client_apps ca ON ca.id = pae.client_app_id
WHERE pae.platform_user_id = CAST(:platform_user_id AS uuid)
  AND pae.tenant_id = CAST(:tenant_id AS uuid)
  AND pae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY pae.occurred_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
