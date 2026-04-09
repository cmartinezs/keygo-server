---
name: remake-keygo-data-platform
description: Rebuild Flyway baseline for Keygo IAM with correct architecture (platform identity, tenant separation, RBAC, audit system, enriched sessions, and dashboard SQL).
---

# Purpose
This skill defines the correct architecture and workflow to rebuild the Keygo data platform from scratch.

# When to use
- Flyway migration redesign
- IAM architecture refactor
- Multitenant database modeling
- Audit system implementation
- Dashboard SQL generation

# Core Architecture

## Identity
- platform_users is the global identity
- tenant_users is only membership
- app_memberships define app access

## RBAC
- Platform RBAC (KEYGO roles) is independent
- Tenant RBAC is independent
- App RBAC is independent

## Audit
- Append-only audit_events
- Must capture UI + backend + system actions
- Must support aggregation + drill-down

## Sessions
- platform_sessions = global
- oauth_sessions = app-level

## SQL Principles
- Strong FK integrity
- Composite keys for tenant isolation
- No cross-tenant leakage

## Dashboards
- SQL outside Flyway
- Every aggregate must have detail query

# Execution Workflow

1. Rebuild schema from scratch
2. Apply strict relational integrity
3. Implement RBAC per scope
4. Implement audit system
5. Enrich sessions model
6. Implement billing model
7. Create seed data
8. Generate dashboard SQL queries
9. Update documentation

# Required References
Before execution, read:

- references/checklist.md
- references/ddl-rules.md
- references/audit-model.md
- references/dashboard-sql.md
- references/acceptance.md
- references/anti-patterns.md

# Output Requirements

- Flyway migrations fully rebuilt
- SQL constraints enforced at DB level
- Audit system implemented
- Sessions enriched
- Dashboard SQL queries created
- Documentation aligned with SQL

# Anti-pattern warning

DO NOT:
- Use tenant_users as identity
- Put platform RBAC inside app_roles
- Allow cross-tenant relationships
- Leave validation only in backend
- Mix dashboard SQL with migrations
