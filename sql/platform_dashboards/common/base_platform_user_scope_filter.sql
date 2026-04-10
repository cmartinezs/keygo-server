-- Purpose: reusable platform user scope projection.
-- Parameters: :platform_user_id
SELECT pu.*
FROM platform_users pu
WHERE (:platform_user_id IS NULL OR pu.id = CAST(:platform_user_id AS uuid));
