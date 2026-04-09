# Acceptance Criteria

- Flyway migrate OK
- Flyway validate OK
- Hibernate validate OK

## Architecture

- platform_user != tenant_user
- RBAC separated
- contractor != tenant_user

## Integrity

- No cross-tenant relationships
- DB enforces constraints

## Audit

- Events stored
- Queryable
- Drill-down possible

## Dashboards

- SQL separated from migrations
- Organized by profile

## UI support

- sessions viewable
- activity viewable
