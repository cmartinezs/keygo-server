-- Purpose: reusable filtered audit event base set.
-- Parameters: :from_ts, :to_ts, :tenant_id, :contractor_id, :client_app_id, :platform_user_id
SELECT ae.*
FROM audit_events ae
WHERE ae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
  AND (:tenant_id IS NULL OR ae.tenant_id = CAST(:tenant_id AS uuid))
  AND (:contractor_id IS NULL OR ae.contractor_id = CAST(:contractor_id AS uuid))
  AND (:client_app_id IS NULL OR ae.client_app_id = CAST(:client_app_id AS uuid))
  AND (:platform_user_id IS NULL OR ae.actor_platform_user_id = CAST(:platform_user_id AS uuid));
