-- Purpose: drill-down list for platform user aggregates.
-- Parameters: :from_ts, :to_ts, :limit, :offset
SELECT
    pu.id,
    pu.email,
    pu.display_name,
    pu.status,
    pu.email_verified_at,
    pu.last_login_at,
    pu.created_at
FROM platform_users pu
WHERE pu.created_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
   OR pu.status = 'ACTIVE'
ORDER BY pu.created_at DESC
LIMIT CAST(:limit AS integer)
OFFSET CAST(:offset AS integer);
