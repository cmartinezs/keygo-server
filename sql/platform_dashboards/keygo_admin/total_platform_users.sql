-- Purpose: aggregate total and active platform users.
-- Detail pair: platform_users_detail.sql
-- Parameters: :from_ts, :to_ts
SELECT
    COUNT(*) AS total_platform_users,
    COUNT(*) FILTER (WHERE status = 'ACTIVE') AS active_platform_users,
    COUNT(*) FILTER (WHERE created_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)) AS new_platform_users_in_period
FROM platform_users;
