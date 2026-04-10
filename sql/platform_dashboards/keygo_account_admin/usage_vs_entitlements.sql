-- Purpose: aggregate usage against entitlement limits within contractor scope.
-- Detail pair: usage_detail.sql
-- Parameters: :contractor_id, :from_ts, :to_ts
SELECT
    uc.metric_code,
    SUM(uc.used_value) AS used_value,
    MAX(ape.limit_value) AS entitlement_limit
FROM usage_counters uc
JOIN app_subscriptions sub
  ON sub.contractor_id = uc.contractor_id
 AND sub.client_app_id = uc.client_app_id
 AND sub.status = 'ACTIVE'
JOIN app_plan_entitlements ape
  ON ape.app_plan_version_id = sub.app_plan_version_id
 AND ape.metric_code = uc.metric_code
WHERE uc.contractor_id = CAST(:contractor_id AS uuid)
  AND uc.period_start >= CAST(:from_ts AS timestamptz)
  AND uc.period_end <= CAST(:to_ts AS timestamptz)
GROUP BY uc.metric_code
ORDER BY uc.metric_code;
