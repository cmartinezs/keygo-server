# Package Layout

Use this reference when you need a concrete starting point for a Spring Boot 4.x hexagonal service.

## Recommended structure

```text
com.example.orders
|-- bootstrap
|   |-- OrderServiceApplication.java
|   `-- configuration
|       `-- OrderModuleConfiguration.java
|-- domain
|   |-- model
|   |   |-- Order.java
|   |   |-- OrderId.java
|   |   `-- OrderStatus.java
|   |-- service
|   |   `-- PricingPolicy.java
|   `-- event
|       `-- OrderCreated.java
|-- application
|   |-- port
|   |   |-- in
|   |   |   `-- CreateOrderUseCase.java
|   |   `-- out
|   |       |-- LoadCustomerPort.java
|   |       |-- SaveOrderPort.java
|   |       `-- PublishOrderCreatedPort.java
|   |-- usecase
|   |   `-- CreateOrderService.java
|   `-- dto
|       |-- CreateOrderCommand.java
|       `-- OrderResult.java
`-- infrastructure
    |-- adapter
    |   |-- in
    |   |   `-- web
    |   |       |-- CreateOrderController.java
    |   |       |-- CreateOrderRequest.java
    |   |       `-- OrderResponse.java
    |   `-- out
    |       |-- persistence
    |       |   |-- JpaOrderEntity.java
    |       |   |-- SpringDataOrderRepository.java
    |       |   `-- OrderPersistenceAdapter.java
    |       `-- messaging
    |           `-- OrderCreatedKafkaAdapter.java
    `-- mapper
        `-- OrderPersistenceMapper.java
```

## Notes

- Keep DTOs owned by the layer that needs them. Do not reuse HTTP request bodies as application commands or persistence entities.
- Use `application/dto` for use-case inputs and outputs when they are shared by multiple adapters.
- Keep transport DTOs under inbound adapters and persistence entities under outbound persistence adapters.
- If the module grows, split by feature first and keep the same hexagonal shape inside each feature.

## Feature-first variant

For a larger bounded context, prefer:

```text
com.example.billing
|-- invoice
|   |-- domain
|   |-- application
|   `-- infrastructure
|-- payment
|   |-- domain
|   |-- application
|   `-- infrastructure
`-- bootstrap
```

Use this variant when `orders`, `payments`, `invoices`, or similar features evolve at different speeds.

## Bean wiring guideline

Create explicit bean factories when the dependency graph is meaningful:

```java
@Configuration
class OrderModuleConfiguration {

    @Bean
    CreateOrderUseCase createOrderUseCase(
            LoadCustomerPort loadCustomerPort,
            SaveOrderPort saveOrderPort,
            PublishOrderCreatedPort publishOrderCreatedPort) {
        return new CreateOrderService(loadCustomerPort, saveOrderPort, publishOrderCreatedPort);
    }
}
```

Prefer this over hiding the central use-case wiring behind broad scanning when architectural clarity matters.
