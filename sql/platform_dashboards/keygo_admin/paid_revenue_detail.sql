-- Purpose: drill-down approved payments, invoices and contract references.
-- Parameters: :from_ts, :to_ts, :limit, :offset
SELECT
    pt.id,
    pt.paid_at,
    pt.amount,
    pt.currency,
    pt.provider,
    c.display_name AS contractor_name,
    ca.client_id,
    ac.status AS contract_status,
    sub.status AS subscription_status
FROM payment_transactions pt
JOIN contractors c ON c.id = pt.contractor_id
JOIN client_apps ca ON ca.id = pt.client_app_id
LEFT JOIN app_contracts ac ON ac.id = pt.contract_id
LEFT JOIN app_subscriptions sub ON sub.id = pt.subscription_id
WHERE pt.status = 'APPROVED'
  AND pt.paid_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
ORDER BY pt.paid_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
