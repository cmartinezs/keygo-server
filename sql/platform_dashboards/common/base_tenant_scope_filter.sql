-- Purpose: reusable tenant scope projection.
-- Parameters: :tenant_id
SELECT t.*
FROM tenants t
WHERE (:tenant_id IS NULL OR t.id = CAST(:tenant_id AS uuid));
