-- Purpose: recent activity feed for self-service UI.
-- Parameters: :platform_user_id, :from_ts, :to_ts, :limit, :offset
SELECT
    pae.occurred_at,
    pae.event_category,
    pae.event_type,
    t.slug AS tenant_slug,
    ca.client_id,
    pae.metadata
FROM platform_activity_events pae
LEFT JOIN tenants t ON t.id = pae.tenant_id
LEFT JOIN client_apps ca ON ca.id = pae.client_app_id
WHERE pae.platform_user_id = CAST(:platform_user_id AS uuid)
  AND pae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY pae.occurred_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
