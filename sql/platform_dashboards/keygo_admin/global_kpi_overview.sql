-- Purpose: aggregate high-level global KPIs for admin dashboard cards.
-- Detail pair: global_kpi_overview_detail.sql
-- Parameters: :from_ts, :to_ts
SELECT
    (SELECT COUNT(*) FROM contractors) AS total_contractors,
    (SELECT COUNT(*) FROM contractors WHERE status = 'ACTIVE') AS active_contractors,
    (SELECT COUNT(*) FROM tenants) AS total_tenants,
    (SELECT COUNT(*) FROM tenants WHERE status = 'ACTIVE') AS active_tenants,
    (SELECT COUNT(*) FROM client_apps) AS total_client_apps,
    (SELECT COUNT(*) FROM client_apps WHERE status = 'ACTIVE') AS active_client_apps,
    (SELECT COUNT(*) FROM audit_events
      WHERE severity = 'CRITICAL'
        AND occurred_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)) AS total_critical_events,
    (SELECT COALESCE(SUM(base_price), 0)
       FROM app_plan_billing_options
      WHERE billing_period = 'MONTHLY') AS estimated_mrr_catalog;
