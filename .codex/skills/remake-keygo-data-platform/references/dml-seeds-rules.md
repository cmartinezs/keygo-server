# Seed Rules

- Use semantic subqueries for FK resolution
- Avoid hardcoded FK UUIDs
- Use ON CONFLICT

## Required Data

- Platform users:
    - admin
    - tenant_admin
    - user

- Tenants:
    - keygo (internal)
    - demo
    - acme

- Contractors:
    - at least one active

## Important

- DO NOT auto-link all users to keygo tenant
