-- Purpose: aggregate overdue invoice count.
-- Detail pair: overdue_invoices_detail.sql
-- Parameters: :from_ts, :to_ts
SELECT COUNT(*) AS overdue_invoices_count
FROM invoices i
WHERE i.status = 'OVERDUE'
  AND i.issue_date BETWEEN CAST(:from_ts AS date) AND CAST(:to_ts AS date);
