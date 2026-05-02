# DDL Rules

- PostgreSQL only
- Use gen_random_uuid()
- Use CITEXT for emails
- All enums must be CHECK constraints
- All tables must have:
    - created_at
    - updated_at
- Use trigger for updated_at

## Integrity Rules

- Composite FK required for:
    - tenant_users
    - memberships
    - roles

- Prevent:
    - cross-tenant joins
    - orphan memberships
    - invalid role assignments

## Naming

- pk_<table>
- fk_<table>_<ref>
- uq_<table>_<columns>
- idx_<table>_<columns>

## Documentation

- COMMENT ON TABLE required
- COMMENT ON COLUMN for critical fields
