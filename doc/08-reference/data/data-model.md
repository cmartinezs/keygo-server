# Data Model

Este documento fue consolidado en [../../03-architecture/database-schema.md](../../03-architecture/database-schema.md).

## Uso

- Modelo de datos vigente: [../../03-architecture/database-schema.md](../../03-architecture/database-schema.md)
- Vista por dominios: [../../03-architecture/database-schema.md#canonical-model-by-domain](../../03-architecture/database-schema.md#canonical-model-by-domain)
- Invariantes transversales: [../../03-architecture/database-schema.md#cross-cutting-invariants](../../03-architecture/database-schema.md#cross-cutting-invariants)
- Relaciones principales: [entity-relationships.md](entity-relationships.md)
- Desde `V20`, `app_plans.client_app_id = NULL` representa planes publicos de plataforma para `GET /platform/billing/catalog`.
- Desde `V35`, la tabla `app_plan_entitlements` contiene la matriz completa de métricas para los 6 planes de plataforma (73 filas). Los `metric_code` reconocidos son:

| metric_code | metric_type | Descripción | Planes |
|---|---|---|---|
| `MAX_TENANTS` | QUOTA | Número máximo de tenants gestionables | Todos |
| `MAX_CLIENT_APPS` | QUOTA | Número máximo de client apps registrables | Todos |
| `MAX_USERS` | QUOTA | Número máximo de usuarios (platform users) | Todos |
| `MAX_ADMINS` | QUOTA | Número máximo de administradores por contractor | Todos excepto FLEX |
| `MAX_MONTHLY_TOKENS` | QUOTA | Tokens de autenticación máximos por mes | Todos |
| `SOCIAL_LOGIN` | BOOLEAN | Habilita login social (OAuth2 providers externos) | Todos |
| `CUSTOM_DOMAIN` | BOOLEAN | Permite dominio personalizado por tenant | Todos |
| `SLA_UPTIME_PCT` | QUOTA | Porcentaje de uptime garantizado (centi-%, 999=99.9%) | Todos |
| `AUDIT_LOG_DAYS` | QUOTA | Días de retención de audit log; NULL = ilimitado | Todos |
| `PRIORITY_SUPPORT` | BOOLEAN | Soporte prioritario dedicado | ENTERPRISE |
| `CUSTOM_SLA` | BOOLEAN | SLA negociado contractualmente | ENTERPRISE |
| `DEDICATED_SUCCESS_MGR` | BOOLEAN | Customer success manager asignado | ENTERPRISE |
| `FLEX_TENANT_T1_MAX` | QUOTA | Tope del tramo 1 de tenants (FLEX) | FLEX |
| `FLEX_TENANT_RATE_T1` | RATE | Tarifa por tenant en tramo 1 en centavos USD (FLEX) | FLEX |
| `FLEX_TENANT_T2_MAX` | QUOTA | Tope del tramo 2 de tenants (FLEX) | FLEX |
| `FLEX_TENANT_RATE_T2` | RATE | Tarifa por tenant en tramo 2 en centavos USD (FLEX) | FLEX |
| `FLEX_TENANT_RATE_T3` | RATE | Tarifa por tenant en tramo 3+ en centavos USD (FLEX) | FLEX |
| `FLEX_APP_T1_MAX` | QUOTA | Tope del tramo 1 de apps (FLEX) | FLEX |
| `FLEX_APP_RATE_T1` | RATE | Tarifa por app en tramo 1 en centavos USD (FLEX) | FLEX |
| `FLEX_APP_T2_MAX` | QUOTA | Tope del tramo 2 de apps (FLEX) | FLEX |
| `FLEX_APP_RATE_T2` | RATE | Tarifa por app en tramo 2 en centavos USD (FLEX) | FLEX |
| `FLEX_APP_RATE_T3` | RATE | Tarifa por app en tramo 3+ en centavos USD (FLEX) | FLEX |
| `FLEX_IDENTITY_T1_MAX` | QUOTA | Tope del tramo 1 de identidades (FLEX) | FLEX |
| `FLEX_IDENTITY_RATE_T1` | RATE | Tarifa por identidad en tramo 1 en centavos USD (FLEX) | FLEX |
| `FLEX_IDENTITY_T2_MAX` | QUOTA | Tope del tramo 2 de identidades (FLEX) | FLEX |
| `FLEX_IDENTITY_RATE_T2` | RATE | Tarifa por identidad en tramo 2 en centavos USD (FLEX) | FLEX |
| `FLEX_IDENTITY_RATE_T3` | RATE | Tarifa por identidad en tramo 3+ en centavos USD (FLEX) | FLEX |
| `FLEX_ADMIN_INCLUDED_PER_TENANT` | QUOTA | Admins incluidos sin cargo por tenant (FLEX) | FLEX |
| `FLEX_ADMIN_RATE` | RATE | Tarifa por admin adicional en centavos USD (FLEX) | FLEX |
