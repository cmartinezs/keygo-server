# Testing Playbook

Use this reference when generating or reviewing tests for a Spring Boot 4.x hexagonal backend.

## 1. Domain tests

Test domain objects without Spring:

- Validate invariants
- Check state transitions
- Verify domain events or calculated outcomes
- Prefer focused tests over framework-heavy fixtures

Example targets:

- `Order` cannot be confirmed without lines
- `EmailAddress` rejects invalid values
- `Money` preserves currency rules

## 2. Application tests

Test use cases with fake ports:

- Build the application service directly
- Provide in-memory or mocked outbound ports
- Assert orchestration, transaction intent, and decisions
- Verify the use case calls the right ports with the right data

Keep these tests fast and numerous.

## 3. Inbound adapter tests

For REST controllers or listeners:

- Verify request validation and status codes
- Verify mapping from transport DTOs to application commands
- Verify error translation
- Do not re-test business rules already covered in domain/application tests

Use MVC or web-slice tests only when they add confidence.

## 4. Outbound adapter tests

For persistence adapters:

- Verify entity-to-domain mapping
- Verify custom query behavior
- Verify optimistic locking or unique constraints when relevant

For external client adapters:

- Verify request composition
- Verify response mapping and error handling
- Verify retry or timeout decisions only where that logic exists

## 5. End-to-end tests

Keep a small number of full-stack tests for critical flows:

- Happy path of the most valuable use case
- One representative failure path
- One cross-adapter integration path if messaging or external APIs are essential

## Naming guidance

Use test names that describe business behavior, for example:

- `should_create_order_when_customer_is_active`
- `should_reject_payment_when_invoice_is_overdue`
- `should_map_duplicate_key_to_conflict_response`

## Common mistakes

Avoid these patterns:

- Starting Spring for pure domain tests
- Mocking every collaborator in controller tests and recreating the whole application behavior there
- Asserting implementation details instead of business outcomes
- Depending only on end-to-end tests for domain logic confidence
