# Audit Model

## Core Table: audit_events

Must include:
- actor
- tenant
- contractor
- app
- session
- request
- event_type
- event_outcome

## Rules

- Append-only
- No updates
- Payload stored separately

## Event Types

### Auth
- login success/failure
- logout
- password reset

### Platform
- profile update
- session revoke

### Tenant/Admin
- user invite/remove
- role changes

### Billing
- contract created
- payment

### Security
- access denied
- suspicious activity

## Indexing

Required:
- occurred_at
- actor_id
- tenant_id
- event_type
