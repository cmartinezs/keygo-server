# Database Schema — Diseño y Arquitectura

**Propósito:** Documentar schema de base de datos, relaciones entre entidades, índices y estrategia de migraciones.

---

## Entity Relationship Diagram (ERD)

```
┌─────────────────┐
│    tenants      │
├─────────────────┤
│ id (PK)         │
│ slug (UNIQUE)   │
│ name            │
│ created_at      │
│ removed_at      │  ← Soft delete
└─────────────────┘
    ↑       ↓
    │      (1:N)
    │       │
    │   ┌─────────────────┐
    │   │  users          │
    │   ├─────────────────┤
    │   │ id (PK)         │
    │   │ tenant_id (FK)  │
    │   │ email           │
    │   │ username        │
    │   │ password_hash   │
    │   │ created_at      │
    │   │ removed_at      │
    │   └─────────────────┘
    │        ↓ (1:N)
    │   ┌─────────────────────┐
    │   │ user_credentials    │
    │   ├─────────────────────┤
    │   │ id (PK)             │
    │   │ user_id (FK)        │
    │   │ provider            │ ← "password", "oauth", "saml"
    │   │ provider_user_id    │
    │   │ created_at          │
    │   └─────────────────────┘
    │
    └──→ ┌──────────────────────┐
         │ tenant_roles         │
         ├──────────────────────┤
         │ id (PK)              │
         │ tenant_id (FK)       │
         │ name (UNIQUE)        │
         │ description          │
         │ created_at           │
         └──────────────────────┘
              ↑ (M:N)
              │
         ┌──────────────────────┐
         │ tenant_user_roles    │
         ├──────────────────────┤
         │ id (PK)              │
         │ user_id (FK)         │
         │ role_id (FK)         │
         │ granted_by (FK)      │ ← User who granted
         │ created_at           │
         └──────────────────────┘

┌──────────────────────┐
│ platform_roles       │ ← Well-known roles
├──────────────────────┤
│ id (PK)              │
│ code (ENUM)          │
│ name                 │
│ description          │
│ created_at           │
└──────────────────────┘
    ↓ (1:N)
┌──────────────────────┐
│ platform_permissions │
├──────────────────────┤
│ id (PK)              │
│ platform_role_id(FK) │
│ permission_code      │
│ created_at           │
└──────────────────────┘

┌──────────────────────┐     ┌─────────────────────┐
│    oauth_clients     │────→│ oauth_client_scopes │
├──────────────────────┤     ├─────────────────────┤
│ id (PK)              │     │ id (PK)             │
│ tenant_id (FK)       │     │ client_id (FK)      │
│ client_id            │     │ scope               │
│ client_secret_hash   │     │ created_at          │
│ redirect_uris        │     └─────────────────────┘
│ grant_types          │
│ created_at           │
│ removed_at           │
└──────────────────────┘

┌─────────────────────┐
│ audit_logs          │
├─────────────────────┤
│ id (PK)             │
│ tenant_id (FK)      │
│ event_type          │
│ actor_id (FK)       │
│ resource_id         │
│ changes (JSONB)     │ ← What changed
│ created_at          │
└─────────────────────┘

┌─────────────────────┐
│ sessions            │
├─────────────────────┤
│ id (PK)             │
│ user_id (FK)        │
│ token_hash          │
│ expires_at          │
│ created_at          │
│ revoked_at          │
└─────────────────────┘
```

---

## Core Tables

### tenants

```sql
CREATE TABLE tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug VARCHAR(255) NOT NULL UNIQUE,  -- URL-friendly identifier
  name VARCHAR(255) NOT NULL,
  status VARCHAR(50) DEFAULT 'ACTIVE',  -- ACTIVE, SUSPENDED, ARCHIVED
  
  -- Metadata
  config JSONB,  -- Custom configuration per tenant
  
  -- Audit
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by UUID,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  removed_at TIMESTAMP NULL,  -- Soft delete
  
  CONSTRAINT tenant_slug_format CHECK (slug ~ '^[a-z0-9_-]+$')
);

CREATE UNIQUE INDEX idx_tenant_slug ON tenants(slug) WHERE removed_at IS NULL;
CREATE INDEX idx_tenant_status ON tenants(status);
```

### users

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  
  -- Identity
  email VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  
  -- Password (nullable for SSO-only users)
  password_hash VARCHAR(255),
  password_salt VARCHAR(255),
  password_changed_at TIMESTAMP,
  
  -- Status
  status VARCHAR(50) DEFAULT 'ACTIVE',  -- ACTIVE, SUSPENDED, LOCKED
  email_verified_at TIMESTAMP,
  phone_verified_at TIMESTAMP,
  
  -- Audit
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by UUID,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  removed_at TIMESTAMP NULL,  -- Soft delete
  
  CONSTRAINT unique_email_per_tenant UNIQUE (tenant_id, email) WHERE removed_at IS NULL,
  CONSTRAINT unique_username_per_tenant UNIQUE (tenant_id, username) WHERE removed_at IS NULL
);

CREATE INDEX idx_user_tenant_id ON users(tenant_id);
CREATE INDEX idx_user_email ON users(email) WHERE removed_at IS NULL;
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_created_at ON users(created_at DESC);
```

### user_credentials

```sql
CREATE TABLE user_credentials (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  
  -- Type of credential
  provider VARCHAR(100) NOT NULL,  -- "password", "google", "okta", "azure"
  provider_user_id VARCHAR(500),   -- External ID from provider
  
  -- Metadata
  metadata JSONB,  -- Provider-specific data
  
  -- Status
  verified_at TIMESTAMP,
  expires_at TIMESTAMP,
  
  -- Audit
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT unique_provider_credential UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_credential_user_id ON user_credentials(user_id);
CREATE INDEX idx_credential_provider ON user_credentials(provider);
```

### tenant_roles

```sql
CREATE TABLE tenant_roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  
  -- Role definition
  name VARCHAR(255) NOT NULL,
  description TEXT,
  
  -- Type: built-in or custom
  is_builtin BOOLEAN DEFAULT FALSE,
  
  -- Permissions (as JSON list)
  permissions JSONB DEFAULT '[]'::jsonb,
  
  -- Audit
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by UUID,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT unique_role_per_tenant UNIQUE (tenant_id, name)
);

CREATE INDEX idx_tenant_role_tenant_id ON tenant_roles(tenant_id);
```

### tenant_user_roles

```sql
CREATE TABLE tenant_user_roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  role_id UUID NOT NULL REFERENCES tenant_roles(id),
  
  -- Who granted this role
  granted_by UUID REFERENCES users(id),
  
  -- Expiry (optional)
  expires_at TIMESTAMP,
  
  -- Audit
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  CONSTRAINT unique_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_user_role_user_id ON tenant_user_roles(user_id);
CREATE INDEX idx_user_role_role_id ON tenant_user_roles(role_id);
CREATE INDEX idx_user_role_expires_at ON tenant_user_roles(expires_at) WHERE expires_at IS NOT NULL;
```

### oauth_clients

```sql
CREATE TABLE oauth_clients (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID REFERENCES tenants(id),  -- NULL for platform clients
  
  -- Client identification
  client_id VARCHAR(255) NOT NULL UNIQUE,
  client_secret_hash VARCHAR(255) NOT NULL,  -- Hashed
  
  -- Configuration
  client_name VARCHAR(255) NOT NULL,
  redirect_uris TEXT NOT NULL,  -- Space-separated
  grant_types TEXT NOT NULL,    -- "authorization_code refresh_token"
  scope TEXT NOT NULL,          -- "openid profile email"
  
  -- Token lifetimes (in seconds)
  access_token_validity 3600,
  refresh_token_validity 86400,
  
  -- Settings
  require_pkce BOOLEAN DEFAULT TRUE,
  allow_introspection BOOLEAN DEFAULT TRUE,
  
  -- Audit
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by UUID,
  removed_at TIMESTAMP NULL,
  
  CONSTRAINT client_id_unique UNIQUE (client_id) WHERE removed_at IS NULL
);

CREATE INDEX idx_oauth_client_tenant_id ON oauth_clients(tenant_id);
```

### sessions

```sql
CREATE TABLE sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  
  -- Token info
  token_hash VARCHAR(255) NOT NULL,  -- SHA256 hash of JWT
  
  -- Device info
  user_agent VARCHAR(500),
  ip_address INET,
  
  -- Validity
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  revoked_at TIMESTAMP,
  
  CONSTRAINT token_hash_unique UNIQUE (token_hash)
);

CREATE INDEX idx_session_user_id ON sessions(user_id);
CREATE INDEX idx_session_expires_at ON sessions(expires_at);
CREATE UNIQUE INDEX idx_session_active ON sessions(token_hash) WHERE revoked_at IS NULL;
```

### audit_logs

```sql
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  
  -- Event info
  event_type VARCHAR(100) NOT NULL,  -- "USER_CREATED", "ROLE_ASSIGNED", etc.
  severity VARCHAR(50) DEFAULT 'INFO',  -- INFO, WARNING, ERROR, CRITICAL
  
  -- Actor
  actor_id UUID,  -- NULL for system events
  actor_type VARCHAR(50) DEFAULT 'USER',  -- "USER", "SYSTEM", "API_KEY"
  
  -- Resource
  resource_type VARCHAR(100),  -- "USER", "TENANT", "OAUTH_CLIENT"
  resource_id UUID,
  
  -- Changes
  old_values JSONB,
  new_values JSONB,
  changes JSONB,  -- Computed delta
  
  -- Tracking
  trace_id VARCHAR(36),  -- Correlation ID
  
  -- Audit
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_log_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_log_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_log_resource_type ON audit_logs(resource_type);
CREATE INDEX idx_audit_log_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_log_trace_id ON audit_logs(trace_id);
```

---

## Partitioning Strategy

### Audit Logs Partitioning

Para audit logs (tabla que crece sin límite), usar time-based partitioning:

```sql
CREATE TABLE audit_logs (
  -- ... columns ...
) PARTITION BY RANGE (created_at);

CREATE TABLE audit_logs_2026_01 PARTITION OF audit_logs
  FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

CREATE TABLE audit_logs_2026_02 PARTITION OF audit_logs
  FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- Automated partition creation
-- → Job que crea nuevas particiones mensualmente
```

---

## Indexes Strategy

| Table | Index | Purpose |
|---|---|---|
| users | `(tenant_id, email)` | Find user by email in tenant |
| users | `(status)` | Filter active users |
| tenant_user_roles | `(user_id, role_id)` | Prevent duplicates |
| oauth_clients | `(client_id)` WHERE removed_at IS NULL | Unique active client |
| audit_logs | `(tenant_id, created_at DESC)` | Recent logs for tenant |
| sessions | `(user_id, expires_at)` | Check valid sessions |

---

## Data Integrity Constraints

### Foreign Keys

```sql
ALTER TABLE users 
  ADD CONSTRAINT fk_user_tenant 
  FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

ALTER TABLE tenant_user_roles 
  ADD CONSTRAINT fk_user_role_user 
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE tenant_user_roles 
  ADD CONSTRAINT fk_user_role_role 
  FOREIGN KEY (role_id) REFERENCES tenant_roles(id) ON DELETE RESTRICT;
```

### Check Constraints

```sql
-- Email format
ALTER TABLE users 
  ADD CONSTRAINT check_email_format 
  CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$');

-- Status values
ALTER TABLE users 
  ADD CONSTRAINT check_status_values 
  CHECK (status IN ('ACTIVE', 'SUSPENDED', 'LOCKED', 'PENDING'));

-- Slug format
ALTER TABLE tenants 
  ADD CONSTRAINT check_slug_format 
  CHECK (slug ~ '^[a-z0-9_-]+$');
```

---

## Migration Strategy

### Flyway Version Control

```
db/migration/
├── V1__Initial_schema.sql
├── V2__Add_user_credentials.sql
├── V3__Rename_column_to_email.sql
├── V4__Add_audit_logs_table.sql
├── V5__Add_oauth_clients_table.sql
└── V6__Create_sessions_table.sql
```

### Migration Principles

1. **Never delete columns** (backward compat)
   - Add new column, migrate data, deprecate old column, delete in next major
   
2. **Reversible** (can rollback)
   - Each migration should be reversible or documented why it's not
   
3. **Tested** (run against test DB first)
   - Verify migration doesn't break app
   - Test rollback
   
4. **Documented** (explain intent)
   ```sql
   -- V10__Add_mfa_enabled.sql
   -- Purpose: Enable MFA feature for users
   -- Rollback: DROP COLUMN users.mfa_enabled
   
   ALTER TABLE users ADD COLUMN mfa_enabled BOOLEAN DEFAULT FALSE;
   ```

### Data Migrations

For schema changes that require data transformation:

```sql
-- V7__Rename_password_column.sql
-- Old schema: password (plaintext - WRONG!)
-- New schema: password_hash + password_salt

ALTER TABLE users 
  ADD COLUMN password_hash VARCHAR(255),
  ADD COLUMN password_salt VARCHAR(255),
  ADD COLUMN password_changed_at TIMESTAMP;

-- Migrate existing data
UPDATE users 
  SET password_hash = password,
      password_salt = ''
  WHERE password IS NOT NULL;

-- Drop old column (in next major version)
-- ALTER TABLE users DROP COLUMN password;
```

### Deployment Safety

```bash
# 1. Test migration on staging
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/keygo_test

# 2. Deploy code (new migrations included)
kubectl set image deployment/keygo-server \
  keygo-server=ghcr.io/cmartinezs/keygo-server:v1.0.1

# 3. Migrations run automatically on startup
# → Check logs
kubectl logs -f deployment/keygo-server

# 4. Verify
curl http://localhost:8080/actuator/health | jq '.components.flyway'
```

---

## Backup & Recovery

### Full Backup (pg_dump)

```bash
# Backup
pg_dump -h localhost -U keygo keygo | gzip > backup.sql.gz

# Restore
gunzip < backup.sql.gz | psql -h localhost -U keygo keygo
```

### Point-in-Time Recovery (PITR)

```yaml
# PostgreSQL wal_level must be 'replica' or 'logical'
wal_level = replica
max_wal_senders = 10
wal_keep_size = 1GB

# Continuous archiving
archive_mode = on
archive_command = 'aws s3 cp %p s3://keygo-backups/wal/%f'
```

Restore to specific point in time:
```bash
# Restore from base backup
pg_basebackup -D /var/lib/postgresql/data

# WAL replay stops at recovery_target_time
recovery_target_time = '2026-04-10 10:30:00'
```

---

## Performance Tuning

### Connection Pooling

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Query Optimization

```sql
-- Analyze slow queries
EXPLAIN ANALYZE
SELECT * FROM users 
WHERE tenant_id = 'abc' 
AND email LIKE '%@example.com' 
AND created_at > NOW() - INTERVAL '7 days';

-- Create appropriate index
CREATE INDEX idx_users_tenant_email_date 
ON users(tenant_id, email, created_at DESC);
```

---

## Anti-Patterns: Evitar

### ❌ Hard delete

```sql
-- MAL: Data loss, audit trail broken
DELETE FROM users WHERE id = '...';
```

### ✅ Soft delete

```sql
-- BIEN: Data preserved, can recover
UPDATE users SET removed_at = NOW() WHERE id = '...';
-- Queries filter: WHERE removed_at IS NULL
```

---

### ❌ Storing sensitive data unencrypted

```sql
-- MAL
CREATE TABLE oauth_clients (
  client_secret VARCHAR(255)  -- Plaintext!
);
```

### ✅ Hash or encrypt sensitive data

```sql
-- BIEN
CREATE TABLE oauth_clients (
  client_secret_hash VARCHAR(255)  -- SHA256(secret + salt)
);
```

---

## Checklist: New Entity

- [ ] **Design ERD** — Show relationships, cardinality
- [ ] **Create table** — With NOT NULL, defaults, constraints
- [ ] **Create indexes** — On FK, search, sort columns
- [ ] **Soft delete** — Add removed_at TIMESTAMP if data should be preserved
- [ ] **Audit trail** — created_by, updated_at, created_at
- [ ] **Constraints** — FK, CHECK, UNIQUE where needed
- [ ] **Partition plan** — If table grows unbounded
- [ ] **Test migration** — Run on staging first
- [ ] **Document** — Add ERD diagram, index strategy

---

## Referencias

| Aspecto | Ubicación |
|---|---|
| **Migrations** | `keygo-supabase/src/main/resources/db/migration/` |
| **Entities** | `keygo-domain/src/main/java/io/cmartinezs/keygo/domain/*/entity/` |
| **Repositories** | `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/repository/` |
| **JPA Specs** | `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/repository/spec/` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 4  
**Próxima:** SECURITY_GUIDELINES.md
