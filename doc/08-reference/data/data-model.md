# Data Model

Este documento fue consolidado en [../../03-architecture/database-schema.md](../../03-architecture/database-schema.md).

## Uso

- Modelo de datos vigente: [../../03-architecture/database-schema.md](../../03-architecture/database-schema.md)
- Vista por dominios: [../../03-architecture/database-schema.md#canonical-model-by-domain](../../03-architecture/database-schema.md#canonical-model-by-domain)
- Invariantes transversales: [../../03-architecture/database-schema.md#cross-cutting-invariants](../../03-architecture/database-schema.md#cross-cutting-invariants)
- Relaciones principales: [entity-relationships.md](entity-relationships.md)
- Desde `V20`, `app_plans.client_app_id = NULL` representa planes publicos de plataforma para `GET /platform/billing/catalog`.
