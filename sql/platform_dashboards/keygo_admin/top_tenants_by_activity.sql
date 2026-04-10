-- Purpose: rank tenants by audit event volume.
-- Detail pair: tenant_activity_detail.sql
-- Parameters: :from_ts, :to_ts, :limit
SELECT
    ae.tenant_id,
    t.slug,
    t.name,
    COUNT(*) AS total_events
FROM audit_events ae
JOIN tenants t ON t.id = ae.tenant_id
WHERE ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY ae.tenant_id, t.slug, t.name
ORDER BY total_events DESC
LIMIT CAST(:limit AS integer);
