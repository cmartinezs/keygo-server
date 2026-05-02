-- Assigns KEYGO_USER (GLOBAL scope) to all platform_users with no platform role assigned.
INSERT INTO platform_user_roles (id, platform_user_id, role_id, scope_type)
SELECT
    gen_random_uuid(),
    pu.id,
    pr.id,
    'GLOBAL'
FROM platform_users pu
JOIN platform_roles pr ON pr.code = 'KEYGO_USER'
WHERE NOT EXISTS (
    SELECT 1 FROM platform_user_roles pur
    WHERE pur.platform_user_id = pu.id
);
