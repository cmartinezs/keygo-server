-- Purpose: aggregate tenant access summary for a platform user.
-- Detail pair: tenant_accesses_detail.sql
-- Parameters: :platform_user_id, :from_ts, :to_ts
SELECT
    t.id AS tenant_id,
    t.slug,
    COUNT(*) AS total_events
FROM platform_activity_events pae
JOIN tenants t ON t.id = pae.tenant_id
WHERE pae.platform_user_id = CAST(:platform_user_id AS uuid)
  AND pae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY t.id, t.slug
ORDER BY total_events DESC;
