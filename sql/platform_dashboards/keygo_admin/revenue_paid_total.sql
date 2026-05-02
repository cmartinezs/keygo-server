-- Purpose: aggregate paid revenue, active contracts and active subscriptions.
-- Detail pair: paid_revenue_detail.sql
-- Parameters: :from_ts, :to_ts
SELECT
    COALESCE(SUM(pt.amount) FILTER (WHERE pt.status = 'APPROVED' AND pt.paid_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)), 0) AS total_revenue_paid,
    COUNT(DISTINCT ac.id) FILTER (WHERE ac.status = 'ACTIVE') AS total_contracts_active,
    COUNT(DISTINCT sub.id) FILTER (WHERE sub.status = 'ACTIVE') AS total_subscriptions_active
FROM payment_transactions pt
FULL JOIN app_contracts ac ON ac.id = pt.contract_id
FULL JOIN app_subscriptions sub ON sub.id = pt.subscription_id;
