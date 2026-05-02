---
name: springboot-hexagonal-4x
description: Build, refactor, or review Spring Boot 4.x services using hexagonal architecture, ports and adapters, domain-centered boundaries, use-case orchestration, and framework isolation. Use when Codex needs to create a new backend module, reorganize a layered Spring Boot codebase into hexagonal form, define inbound or outbound ports, implement REST or messaging adapters, wire persistence without leaking JPA into the domain, or design tests that protect the application and domain layers.
---

# Spring Boot Hexagonal 4.x

## Overview

Use this skill to design or evolve Spring Boot 4.x backends where the domain and application logic stay independent from framework concerns. Keep the core model explicit, make use cases the orchestration point, and treat Spring, HTTP, messaging, and persistence as replaceable adapters.

## Working Rules

- Start by identifying the bounded context, core business language, and the use cases that matter.
- Keep `domain` free of Spring, JPA, Jackson, servlet APIs, and infrastructure annotations.
- Keep `application` focused on use-case coordination, transactions, authorization hooks, and port contracts.
- Put Spring-specific wiring in `infrastructure` or `bootstrap`, not in the core.
- Model inbound and outbound ports from the perspective of the use case, not from the database or controller.
- Prefer constructor injection everywhere.
- Make adapters thin: map, validate transport concerns, invoke the use case, map the result.
- Let aggregates and value objects enforce business invariants close to the domain.
- Avoid anemic services that just pass entities through repositories.
- Avoid exposing JPA entities beyond persistence adapters.

## Delivery Workflow

### 1. Identify the slice

Define these items before generating code:

- Business capability and bounded context
- Primary use cases
- Input and output models per use case
- Required external dependencies: database, HTTP APIs, queues, files, auth providers
- Cross-cutting constraints: transactions, idempotency, audit, security

If the request is broad, narrow it to one vertical slice first, then repeat.

### 2. Shape the hexagon

Use this dependency direction:

- `domain` depends on nothing internal to the project except shared kernel primitives when they are truly generic.
- `application` depends on `domain`.
- `infrastructure` depends on `application` and `domain`.
- `bootstrap` depends on all layers and hosts Spring Boot startup configuration.

Use package names that make the business intent obvious. Prefer a package-by-feature structure inside the bounded context over giant global `controller/service/repository` buckets.

Read [references/package-layout.md](references/package-layout.md) when you need a concrete package template.

### 3. Model the core

In `domain`:

- Create aggregates, entities, value objects, domain services, and domain events only when each concept has a clear business meaning.
- Encode invariants in constructors or factory methods.
- Prefer immutable command/result models unless mutation is part of the domain story.
- Make invalid states unrepresentable when possible.

Do not place these in the domain:

- JPA annotations
- DTOs tied to REST contracts
- Repository implementations
- `@Component`, `@Service`, `@Repository`
- Framework exceptions when a domain exception is more accurate

### 4. Define ports around use cases

Use these heuristics:

- Inbound port: what the outside world wants the application to do.
- Outbound port: what the use case needs from external systems.

Examples:

- `CreateOrderUseCase`
- `LoadCustomerPort`
- `SaveOrderPort`
- `PublishOrderCreatedPort`
- `ClockPort`
- `CurrentUserPort`

Prefer one use case interface per capability when it improves clarity. Group multiple operations only when they are cohesive and stable.

### 5. Implement application services

Application services should:

- Accept a command or query object
- Load or create domain objects through ports
- Invoke domain behavior
- Persist state through outbound ports
- Return a response model tailored to the use case

Application services should not:

- Parse HTTP details
- Build SQL
- Depend directly on Spring MVC or JPA repositories
- Contain business rules that belong inside the domain model

Use Spring annotations here only when they express application concerns cleanly, such as transaction boundaries. If you want stricter purity, move those annotations to configuration classes and expose the service through beans.

### 6. Add adapters

Inbound adapters can be:

- REST controllers
- Message listeners
- Scheduled jobs
- CLI handlers

Outbound adapters can be:

- JPA repositories plus mappers
- Feign or RestClient integrations
- Kafka publishers
- S3 or filesystem gateways

Adapter responsibilities:

- Translate transport or infrastructure models to application commands
- Handle serialization, status codes, retries, timeouts, headers, or persistence schemas
- Call the port or use case
- Translate results back out

Keep mapping code explicit when it preserves clarity. Introduce mapper utilities only after repetition appears.

### 7. Wire with Spring Boot 4.x

For Spring Boot 4.x codebases:

- Keep auto-configuration, bean wiring, and external client setup in infrastructure/bootstrap packages.
- Prefer explicit `@Configuration` classes for significant adapters so boundaries remain visible.
- Reserve component scanning for straightforward adapters; do not let scanning hide architectural decisions.
- Centralize configuration properties per adapter or module.
- Keep validation annotations at the edge for transport DTOs; convert to domain-safe types before entering the core.

### 8. Test by layer

Use a testing pyramid aligned to the architecture:

- Domain tests: pure unit tests, no Spring
- Application tests: use-case tests with fake or stub ports
- Adapter tests: controller, persistence, or client mapping behavior
- Few end-to-end tests: verify the most critical flows and wiring

Read [references/testing-playbook.md](references/testing-playbook.md) for a concrete testing approach.

## Refactor Playbook

When converting an existing layered project:

1. Identify one business flow, not the whole system.
2. Extract the use case boundary and input/output models.
3. Move business rules from controllers and services into domain objects or application services.
4. Introduce outbound ports around repositories and integrations.
5. Wrap current infrastructure behind adapters instead of rewriting everything at once.
6. Add tests around the extracted slice.
7. Repeat slice by slice.

Prefer strangler-style refactors over a big-bang rewrite.

## Decision Heuristics

Choose a value object when:

- Equality is by value
- It encapsulates validation or formatting rules
- It protects a primitive with business meaning

Choose an aggregate when:

- A cluster of objects shares invariants and transactional consistency
- One root should control mutation

Choose a domain service when:

- A business rule spans multiple entities and does not belong naturally to one aggregate

Choose an application service when:

- The work is orchestration across ports, transactions, and domain objects

Introduce a port when:

- The use case needs something external that should stay replaceable or mockable

Avoid extra abstraction when:

- The codebase is small and a new interface adds no architectural benefit
- There is only one obvious implementation and no test seam or boundary value

## Code Generation Guidelines

When asked to generate code, prefer this flow:

1. Create or confirm the package structure.
2. Generate domain types first.
3. Generate use case contracts and application services.
4. Generate inbound and outbound adapters.
5. Generate Spring configuration only after the boundaries are clear.
6. Add tests for the domain and application slices before broad integration tests.

Use descriptive names tied to the business language. Avoid generic names such as `UserService`, `UtilMapper`, or `CommonRepository` unless they reflect real domain meaning.

## Anti-Patterns To Reject

Reject or refactor these patterns when found:

- Controllers calling JPA repositories directly
- Domain objects annotated as persistence entities when persistence concerns dominate the model
- One giant `service` package acting as both use case and domain layer
- Application services returning persistence entities directly to controllers
- Repositories exposed as the primary business API
- Mappers hidden everywhere because boundaries were never clarified
- Shared `dto` packages crossing all modules and leaking transport concerns into the core
- Business validation only at the controller layer

## Output Expectations

When helping with a task, produce these artifacts when relevant:

- Proposed package/module layout
- Clear port and adapter naming
- A vertical slice implementation plan
- Sample code for the use case and one inbound and one outbound adapter
- Tests that prove the business rules and orchestration
- Brief explanation of architectural tradeoffs when the design is not obvious


