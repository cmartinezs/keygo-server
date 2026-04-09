-- Purpose: reusable date bucket pattern for time series queries.
-- Parameters: :from_ts, :to_ts
SELECT generate_series(
           date_trunc('day', CAST(:from_ts AS timestamptz)),
           date_trunc('day', CAST(:to_ts AS timestamptz)),
           interval '1 day'
       ) AS bucket_start;
