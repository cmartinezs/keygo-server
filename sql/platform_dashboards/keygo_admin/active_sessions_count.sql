-- Purpose: aggregate count of active platform and OAuth sessions.
-- Detail pair: active_sessions_detail.sql
-- Parameters: :from_ts, :to_ts
WITH platform_base AS (
    SELECT *
    FROM platform_sessions
    WHERE started_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
),
oauth_base AS (
    SELECT *
    FROM oauth_sessions
    WHERE created_at BETWEEN CAST(:from_ts AS timestamptz) AND CAST(:to_ts AS timestamptz)
)
SELECT
    (SELECT COUNT(*) FROM platform_base WHERE status = 'ACTIVE') AS active_platform_sessions,
    (SELECT COUNT(*) FROM oauth_base WHERE status = 'ACTIVE') AS active_oauth_sessions,
    (SELECT AVG(EXTRACT(EPOCH FROM (COALESCE(ended_at, now()) - started_at))) FROM platform_base) AS average_platform_session_seconds;
