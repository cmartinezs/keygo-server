-- Purpose: reusable contractor scope projection.
-- Parameters: :contractor_id
SELECT c.*
FROM contractors c
WHERE (:contractor_id IS NULL OR c.id = CAST(:contractor_id AS uuid));
