-- Purpose: drill-down lists for global KPI overview.
-- Parameters: :limit, :offset
SELECT 'CONTRACTOR' AS entity_type, c.id::text AS entity_id, c.display_name AS label, c.status, c.created_at
FROM contractors c
UNION ALL
SELECT 'TENANT', t.id::text, t.slug, t.status, t.created_at
FROM tenants t
UNION ALL
SELECT 'CLIENT_APP', ca.id::text, ca.client_id, ca.status, ca.created_at
FROM client_apps ca
ORDER BY created_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
