-- Purpose: aggregate invoice and payment status summary within contractor scope.
-- Detail pair: invoices_detail.sql
-- Parameters: :contractor_id, :from_ts, :to_ts
SELECT
    COUNT(*) FILTER (WHERE i.status = 'PAID') AS paid_invoices,
    COUNT(*) FILTER (WHERE i.status = 'OVERDUE') AS overdue_invoices,
    COUNT(*) FILTER (WHERE i.status = 'ISSUED') AS issued_invoices,
    COALESCE(SUM(pt.amount) FILTER (WHERE pt.status = 'APPROVED'), 0) AS approved_payments_total
FROM invoices i
LEFT JOIN payment_transactions pt
       ON pt.contractor_id = i.contractor_id
      AND pt.created_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
WHERE i.contractor_id = CAST(:contractor_id AS uuid)
  AND i.issue_date BETWEEN CAST(:from_ts AS date) AND CAST(:to_ts AS date);
