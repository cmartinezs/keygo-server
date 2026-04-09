-- Purpose: drill-down overdue invoices.
-- Parameters: :from_ts, :to_ts, :limit, :offset
SELECT
    i.invoice_number,
    i.issue_date,
    i.due_date,
    i.total,
    i.currency,
    c.display_name AS contractor_name,
    ca.client_id
FROM invoices i
JOIN contractors c ON c.id = i.contractor_id
JOIN client_apps ca ON ca.id = i.client_app_id
WHERE i.status = 'OVERDUE'
  AND i.issue_date BETWEEN CAST(:from_ts AS date) AND CAST(:to_ts AS date)
ORDER BY i.due_date ASC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
