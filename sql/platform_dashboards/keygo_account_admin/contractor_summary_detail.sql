-- Purpose: drill-down tenants, apps and users under a contractor.
-- Parameters: :contractor_id, :limit, :offset
SELECT
    t.id AS tenant_id,
    t.slug,
    t.name,
    t.status AS tenant_status,
    ca.id AS client_app_id,
    ca.client_id,
    ca.status AS client_app_status,
    tu.id AS tenant_user_id,
    pu.email,
    tu.status AS tenant_user_status
FROM tenants t
LEFT JOIN client_apps ca ON ca.tenant_id = t.id
LEFT JOIN tenant_users tu ON tu.tenant_id = t.id
LEFT JOIN platform_users pu ON pu.id = tu.platform_user_id
WHERE t.contractor_id = CAST(:contractor_id AS uuid)
ORDER BY t.slug, ca.client_id NULLS LAST, pu.email NULLS LAST
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
