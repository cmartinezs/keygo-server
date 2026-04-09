-- Purpose: aggregate contractor summary for an account admin.
-- Detail pair: contractor_summary_detail.sql
-- Parameters: :contractor_id
SELECT
    c.id AS contractor_id,
    c.display_name,
    c.status,
    COUNT(DISTINCT t.id) AS total_tenants_under_contractor,
    COUNT(DISTINCT t.id) FILTER (WHERE t.status = 'ACTIVE') AS active_tenants_under_contractor,
    COUNT(DISTINCT tu.id) AS total_users_under_contractor,
    COUNT(DISTINCT ca.id) AS total_apps_under_contractor,
    COUNT(DISTINCT am.id) FILTER (WHERE am.status = 'ACTIVE') AS total_active_memberships
FROM contractors c
LEFT JOIN tenants t ON t.contractor_id = c.id
LEFT JOIN tenant_users tu ON tu.tenant_id = t.id
LEFT JOIN client_apps ca ON ca.tenant_id = t.id
LEFT JOIN app_memberships am ON am.tenant_id = t.id
WHERE c.id = CAST(:contractor_id AS uuid)
GROUP BY c.id, c.display_name, c.status;
