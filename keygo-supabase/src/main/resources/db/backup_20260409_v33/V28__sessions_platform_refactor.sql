-- =============================================================================
-- V28: Sessions platform refactor + platform_user_roles FK correction
-- RFC: docs/rfc/restructure-multitenant
--
-- Steps:
--   1. Add platform_user_id nullable FK to tenant_users
--   2. Correct platform_user_roles FK: tenant_users → platform_users
--   3. Refactor sessions: add platform_user_id, make client_app_id nullable,
--      remove tenant_id and user_id
--   4. Update refresh_tokens: remove tenant_id and user_id,
--      add tenant_user_id nullable, make client_app_id nullable
-- =============================================================================

-- ─── Step 1: Link tenant_users → platform_users (nullable for MVP) ───────────

ALTER TABLE tenant_users
    ADD COLUMN platform_user_id UUID REFERENCES platform_users(id) ON DELETE SET NULL;

CREATE INDEX idx_tenant_users_platform_user ON tenant_users(platform_user_id)
    WHERE platform_user_id IS NOT NULL;

COMMENT ON COLUMN tenant_users.platform_user_id IS
    'Optional link to platform_users (global identity). '
    'NULL for tenant-app-only users who have no platform account.';

-- ─── Step 2: Correct platform_user_roles FK ──────────────────────────────────
-- Currently FK points to tenant_users.id; must point to platform_users.id.
-- Clear existing data first (dev-only; seed V29 will repopulate).

DELETE FROM platform_user_roles;

ALTER TABLE platform_user_roles
    DROP CONSTRAINT IF EXISTS platform_user_roles_tenant_user_id_fkey;

ALTER TABLE platform_user_roles
    RENAME COLUMN tenant_user_id TO platform_user_id;

ALTER TABLE platform_user_roles
    ADD CONSTRAINT platform_user_roles_platform_user_id_fkey
        FOREIGN KEY (platform_user_id) REFERENCES platform_users(id) ON DELETE CASCADE;

-- Drop old unique constraint and create new one with renamed column
ALTER TABLE platform_user_roles
    DROP CONSTRAINT IF EXISTS uq_platform_user_roles_user_role;
ALTER TABLE platform_user_roles
    ADD CONSTRAINT uq_platform_user_roles_user_role
        UNIQUE (platform_user_id, platform_role_id);

DROP INDEX IF EXISTS idx_platform_user_roles_tenant_user_id;
CREATE INDEX idx_platform_user_roles_platform_user ON platform_user_roles(platform_user_id);

COMMENT ON COLUMN platform_user_roles.platform_user_id IS
    'FK to platform_users (global user identity). Previously was tenant_user_id.';

-- ─── Step 3: Refactor sessions ───────────────────────────────────────────────
-- sessions.user_id (FK → tenant_users) and sessions.tenant_id → removed
-- sessions.platform_user_id (FK → platform_users) → added (nullable for MVP)
-- sessions.client_app_id → nullable (NULL = platform session)

-- Clear sessions and dependents (dev-only)
DELETE FROM refresh_tokens;
DELETE FROM sessions;

-- Drop existing FKs
ALTER TABLE sessions
    DROP CONSTRAINT IF EXISTS sessions_user_id_fkey,
    DROP CONSTRAINT IF EXISTS sessions_tenant_id_fkey,
    DROP CONSTRAINT IF EXISTS sessions_client_app_id_fkey;

-- Remove columns
ALTER TABLE sessions
    DROP COLUMN IF EXISTS tenant_id,
    DROP COLUMN IF EXISTS user_id;

-- Add platform_user_id (nullable for MVP — tenant app sessions may lack a linked platform user)
ALTER TABLE sessions
    ADD COLUMN platform_user_id UUID REFERENCES platform_users(id) ON DELETE CASCADE;

-- Make client_app_id nullable (NULL = platform session, NOT NULL = tenant app session)
ALTER TABLE sessions
    ALTER COLUMN client_app_id DROP NOT NULL;

ALTER TABLE sessions
    ADD CONSTRAINT sessions_client_app_id_fkey
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE;

COMMENT ON COLUMN sessions.platform_user_id IS
    'FK to platform_users. Set when the authenticated user has a platform account. '
    'NULL for tenant-only users without platform identity (MVP).';
COMMENT ON COLUMN sessions.client_app_id IS
    'FK to client_apps. NULL = platform session (KeyGo UI). '
    'NOT NULL = tenant app session (OAuth2 code exchange).';

DROP INDEX IF EXISTS idx_sessions_user_tenant;
CREATE INDEX idx_sessions_platform_user ON sessions(platform_user_id)
    WHERE platform_user_id IS NOT NULL;
CREATE INDEX idx_sessions_client_app ON sessions(client_app_id)
    WHERE client_app_id IS NOT NULL;

-- ─── Step 4: Update refresh_tokens ───────────────────────────────────────────
-- Remove tenant_id and user_id (derivable from session).
-- Add tenant_user_id (nullable) for fast role lookup in tenant app rotations.
-- Make client_app_id nullable (mirrors sessions).

ALTER TABLE refresh_tokens
    DROP CONSTRAINT IF EXISTS refresh_tokens_tenant_id_fkey,
    DROP CONSTRAINT IF EXISTS refresh_tokens_user_id_fkey,
    DROP CONSTRAINT IF EXISTS refresh_tokens_client_app_id_fkey;

ALTER TABLE refresh_tokens
    DROP COLUMN IF EXISTS tenant_id,
    DROP COLUMN IF EXISTS user_id;

-- Add tenant_user_id for fast role lookup during token rotation
ALTER TABLE refresh_tokens
    ADD COLUMN tenant_user_id UUID REFERENCES tenant_users(id) ON DELETE SET NULL;

-- Make client_app_id nullable (mirrors sessions)
ALTER TABLE refresh_tokens
    ALTER COLUMN client_app_id DROP NOT NULL;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT refresh_tokens_client_app_id_fkey
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE CASCADE;

COMMENT ON COLUMN refresh_tokens.tenant_user_id IS
    'Optional: tenant_user context for fast role lookup during token rotation. '
    'NULL for platform session refresh tokens.';
COMMENT ON COLUMN refresh_tokens.client_app_id IS
    'Optional: denormalized from session for quick lookup. '
    'NULL for platform session refresh tokens.';

CREATE INDEX idx_refresh_tokens_tenant_user ON refresh_tokens(tenant_user_id)
    WHERE tenant_user_id IS NOT NULL;
