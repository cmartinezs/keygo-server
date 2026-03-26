-- =========================================================
-- KeyGo Initial Schema for Supabase
-- Esquema inicial de KeyGo para Supabase
-- =========================================================
-- Version: 1.0
-- Description: Creates initial tables for authentication and user management
-- Descripción: Crea las tablas iniciales para autenticación y gestión de usuarios
-- =========================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================================
-- USERS TABLE / TABLA DE USUARIOS
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT users_username_min_length CHECK (char_length(username) >= 3),
    CONSTRAINT users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Index for email lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_is_active ON users(is_active);

-- =========================================================
-- ROLES TABLE / TABLA DE ROLES
-- =========================================================
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT roles_name_format CHECK (name ~* '^[A-Z_]+$')
);

-- =========================================================
-- USER_ROLES TABLE / TABLA DE ROLES DE USUARIO
-- =========================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    granted_by UUID REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- =========================================================
-- PERMISSIONS TABLE / TABLA DE PERMISOS
-- =========================================================
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) UNIQUE NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT permissions_action_valid CHECK (action IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'EXECUTE'))
);

CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_action ON permissions(action);

-- =========================================================
-- ROLE_PERMISSIONS TABLE / TABLA DE PERMISOS DE ROL
-- =========================================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- =========================================================
-- SESSIONS TABLE / TABLA DE SESIONES
-- =========================================================
CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    refresh_token VARCHAR(500) UNIQUE,
    ip_address INET,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_activity_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_token ON sessions(token);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);
CREATE INDEX idx_sessions_is_active ON sessions(is_active, expires_at);

-- =========================================================
-- AUDIT_LOGS TABLE / TABLA DE LOGS DE AUDITORÍA
-- =========================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    resource_id UUID,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- =========================================================
-- TRIGGER FUNCTIONS / FUNCIONES DE TRIGGER
-- =========================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to users table
CREATE TRIGGER users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to roles table
CREATE TRIGGER roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =========================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- Políticas de seguridad a nivel de fila
-- =========================================================

-- Enable RLS on users table
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read their own data
CREATE POLICY users_select_own ON users
    FOR SELECT
    USING (auth.uid() = id);

-- Policy: Users can update their own data
CREATE POLICY users_update_own ON users
    FOR UPDATE
    USING (auth.uid() = id);

-- Enable RLS on sessions table
ALTER TABLE sessions ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read their own sessions
CREATE POLICY sessions_select_own ON sessions
    FOR SELECT
    USING (auth.uid() = user_id);

-- Policy: Users can delete their own sessions
CREATE POLICY sessions_delete_own ON sessions
    FOR DELETE
    USING (auth.uid() = user_id);

-- Enable RLS on audit_logs table
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read their own audit logs
CREATE POLICY audit_logs_select_own ON audit_logs
    FOR SELECT
    USING (auth.uid() = user_id);

-- =========================================================
-- COMMENTS / COMENTARIOS
-- =========================================================

COMMENT ON TABLE users IS 'User accounts / Cuentas de usuario';
COMMENT ON TABLE roles IS 'System roles / Roles del sistema';
COMMENT ON TABLE permissions IS 'System permissions / Permisos del sistema';
COMMENT ON TABLE user_roles IS 'User-role assignments / Asignaciones de roles a usuarios';
COMMENT ON TABLE role_permissions IS 'Role-permission assignments / Asignaciones de permisos a roles';
COMMENT ON TABLE sessions IS 'Active user sessions / Sesiones activas de usuarios';
COMMENT ON TABLE audit_logs IS 'Audit trail of system actions / Registro de auditoría de acciones del sistema';

