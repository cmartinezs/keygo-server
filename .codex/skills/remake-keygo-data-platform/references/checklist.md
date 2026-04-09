# Checklist

## Core Model
- [ ] platform_users created
- [ ] tenant_users separated
- [ ] app_memberships implemented
- [ ] client_apps tenant-scoped

## RBAC
- [ ] platform_roles created
- [ ] tenant_roles created
- [ ] app_roles created
- [ ] hierarchy implemented safely

## Integrity
- [ ] no cross-tenant memberships
- [ ] role assignment constrained to app
- [ ] FK composite constraints implemented

## Sessions
- [ ] platform_sessions created
- [ ] oauth_sessions created
- [ ] session metadata enriched

## Audit
- [ ] audit_events implemented
- [ ] payload table implemented
- [ ] indexing strategy defined

## Billing
- [ ] contractors use platform_user
- [ ] subscriptions consistent

## Dashboards
- [ ] SQL folder created
- [ ] aggregate + detail enforced

## Docs
- [ ] MIGRATIONS.md updated
- [ ] DATA_MODEL.md updated
- [ ] ENTITY_RELATIONSHIPS.md updated
