-- Purpose: drill-down usage counters by metric and app.
-- Parameters: :contractor_id, :from_ts, :to_ts, :limit, :offset
SELECT
    uc.metric_code,
    uc.used_value,
    uc.period_start,
    uc.period_end,
    ca.client_id,
    c.display_name AS contractor_name
FROM usage_counters uc
JOIN client_apps ca ON ca.id = uc.client_app_id
JOIN contractors c ON c.id = uc.contractor_id
WHERE uc.contractor_id = CAST(:contractor_id AS uuid)
  AND uc.period_start >= CAST(:from_ts AS timestamptz)
  AND uc.period_end <= CAST(:to_ts AS timestamptz)
ORDER BY uc.metric_code, uc.period_start DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
