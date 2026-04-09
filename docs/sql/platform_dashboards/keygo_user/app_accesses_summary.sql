-- Purpose: aggregate app access summary for a platform user.
-- Detail pair: app_accesses_detail.sql
-- Parameters: :platform_user_id, :from_ts, :to_ts
SELECT
    pae.client_app_id,
    ca.client_id,
    COUNT(*) AS total_events
FROM platform_activity_events pae
JOIN client_apps ca ON ca.id = pae.client_app_id
WHERE pae.platform_user_id = CAST(:platform_user_id AS uuid)
  AND pae.client_app_id IS NOT NULL
  AND pae.occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
GROUP BY pae.client_app_id, ca.client_id
ORDER BY total_events DESC;
