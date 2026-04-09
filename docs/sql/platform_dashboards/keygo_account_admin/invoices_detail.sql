-- Purpose: drill-down invoice list in contractor scope.
-- Parameters: :contractor_id, :from_ts, :to_ts, :limit, :offset
SELECT
    i.invoice_number,
    i.status,
    i.issue_date,
    i.due_date,
    i.total,
    i.currency,
    ca.client_id,
    sub.status AS subscription_status
FROM invoices i
JOIN client_apps ca ON ca.id = i.client_app_id
LEFT JOIN app_subscriptions sub ON sub.id = i.subscription_id
WHERE i.contractor_id = CAST(:contractor_id AS uuid)
  AND i.issue_date BETWEEN CAST(:from_ts AS date) AND CAST(:to_ts AS date)
ORDER BY i.issue_date DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
