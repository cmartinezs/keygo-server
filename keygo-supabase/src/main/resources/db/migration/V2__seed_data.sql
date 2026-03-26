-- Initial data seed
INSERT INTO roles (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN', 'System administrator'),
    ('00000000-0000-0000-0000-000000000002', 'USER', 'Standard user')
ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('10000000-0000-0000-0000-000000000001', 'users.create', 'users', 'CREATE', 'Create users'),
    ('10000000-0000-0000-0000-000000000002', 'users.read', 'users', 'READ', 'Read users')
ON CONFLICT (name) DO NOTHING;
INSERT INTO users (id, username, email, password_hash, first_name, last_name, is_active, is_verified) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin', 'admin@keygo.local', 
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'System', 'Administrator', true, true)
ON CONFLICT (username) DO NOTHING;
INSERT INTO user_roles (user_id, role_id) VALUES
    ('a0000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001')
ON CONFLICT (user_id, role_id) DO NOTHING;
