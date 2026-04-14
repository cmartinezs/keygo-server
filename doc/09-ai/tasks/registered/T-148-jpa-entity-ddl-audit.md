# T-148 — Auditoría JPA entity vs DDL Flyway

**Estado:** ✅ APLICADO  
**Módulos afectados:** `keygo-supabase`

---

## Ejecución completada (2026-04-14)

Se aplicaron todas las correcciones identificadas en el análisis:

1. **AppContractEntity** — `selectedPlanVersion`: revertir `@JoinColumn` a `app_plan_version_id` (fuente de verdad DDL V14)
2. **V22 Flyway** — `app_contracts.client_app_id DROP NOT NULL` para soportar contratos de plataforma
3. **V23 Flyway** — `app_subscriptions.client_app_id DROP NOT NULL` (alineado con V22)
4. **V24 Flyway** — `invoices`, `payment_transactions`, `usage_counters`: `client_app_id DROP NOT NULL`
5. **InvoiceEntity** — `currency` default: `"MXN"` → `"USD"`; `clientApp` nullable
6. **PaymentTransactionEntity** — `provider` length: `50` → `20`; `clientApp` nullable
7. **UsageCounterEntity** — `clientApp` nullable
8. **AppSubscriptionEntity** — `clientApp` nullable
9. **PaymentMethodEntity** (3 campos):
   - `provider`: `50` → `20`
   - `methodType`: `20` → `30`
   - `displayLabel`: `100` + nullable → `255` + `nullable = false`

Docs actualizadas: `migrations.md` (V1–V24, next V25), `CLAUDE.md`.

---

## Problema / Requisito

Durante el desarrollo del flujo de onboarding de billing se detectaron desalineamientos entre
los atributos JPA (`@JoinColumn`, `@Column`) y las columnas reales definidas en las migraciones
Flyway. La fuente de verdad es el DDL Flyway. Se requiere auditar todas las entidades de
`keygo-supabase` y homologar nombres, longitudes, tipos y nullabilidad.

---

## Resultado de la auditoría (2026-04-14)

Se revisaron las 44 entidades JPA del baseline V1–V21. La mayoría está correctamente alineada.
Se detectaron **5 desalineamientos reales** que requieren corrección; el resto son MATCH o
DEFAULT de bajo impacto ya correctamente modelados.

### Desalineamientos verificados

| # | Entidad | Campo Java | Anotación actual | DDL (fuente de verdad) | Severidad |
|---|---|---|---|---|---|
| 1 | `InvoiceEntity` | `currency` | `@Builder.Default "MXN"` | `VARCHAR(3) NOT NULL DEFAULT 'USD'` | **ALTA** |
| 2 | `PaymentMethodEntity` | `provider` | `length = 50` | `VARCHAR(20) NOT NULL` | MEDIA |
| 3 | `PaymentMethodEntity` | `methodType` | `length = 20` | `VARCHAR(30) NOT NULL` | MEDIA |
| 4 | `PaymentMethodEntity` | `displayLabel` | `length = 100`, sin `nullable = false` | `VARCHAR(255) NOT NULL` | **ALTA** |
| 5 | `PaymentTransactionEntity` | `provider` | `length = 50` | `VARCHAR(20) NOT NULL` | MEDIA |

### Ya resueltos durante la sesión 2026-04-14

| Entidad | Campo | Corrección aplicada |
|---|---|---|
| `AppContractEntity` | `clientApp` | `optional = false` / `nullable = false` → removidos (V22 lo hizo nullable) |
| `AppContractEntity` | `contractor` | `optional = false` / `nullable = false` → removidos (V18 lo hizo nullable) |
| `AppContractEntity` | `selectedPlanVersion` | `@JoinColumn(name = "app_plan_version_id")` → pendiente migración DDL |

### Entidades sin desalineamientos confirmados

`PlatformUserEntity`, `TenantEntity`, `TenantUserEntity`, `PlatformRoleEntity`, `TenantRoleEntity`,
`AppRoleEntity`, `ClientAppEntity`, `SessionEntity`, `PlatformSessionEntity`, `RefreshTokenEntity`,
`AuthorizationCodeEntity`, `SigningKeyEntity`, `MembershipEntity`, `ContractorEntity`,
`ContractorUserEntity`, `AppPlanEntity`, `AppPlanVersionEntity`, `AppPlanBillingOptionEntity`,
`AppPlanEntitlementEntity`, `AppSubscriptionEntity`, `ContractEmailVerificationEntity`,
`InvoiceEntity`(salvo #1), `UsageCounterEntity`, `TenantBillingProfileEntity`, entidades de audit,
entidades de jerarquía RBAC y composite keys.

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | `InvoiceEntity.currency`: cambiar `@Builder.Default "MXN"` → `"USD"` | `InvoiceEntity.java` | ✅ APPLIED |
| 2 | `PaymentMethodEntity.provider`: cambiar `length = 50` → `length = 20` | `PaymentMethodEntity.java` | ✅ APPLIED |
| 3 | `PaymentMethodEntity.methodType`: cambiar `length = 20` → `length = 30` | `PaymentMethodEntity.java` | ✅ APPLIED |
| 4 | `PaymentMethodEntity.displayLabel`: cambiar `length = 100` → `length = 255` + añadir `nullable = false` | `PaymentMethodEntity.java` | ✅ APPLIED |
| 5 | `PaymentTransactionEntity.provider`: cambiar `length = 50` → `length = 20` | `PaymentTransactionEntity.java` | ✅ APPLIED |
| 6 | `V22` + entidades afectadas: `app_contracts.client_app_id` nullable | Flyway + `AppContractEntity.java` | ✅ APPLIED |
| 7 | `V23` + entidades afectadas: `app_subscriptions.client_app_id` nullable | Flyway + `AppSubscriptionEntity.java` | ✅ APPLIED |
| 8 | `V24` + entidades afectadas: `invoices/payment_transactions/usage_counters.client_app_id` nullable | Flyway + 3 entidades | ✅ APPLIED |

No se requieren migraciones Flyway: todos los cambios son ajustes de metadatos JPA para que
reflejen correctamente la DDL existente. Las columnas ya tienen las definiciones correctas en la BD.

---

## Verificación

```bash
./mvnw -pl keygo-supabase test
./mvnw checkstyle:check -pl keygo-supabase
```

Con `hibernate.ddl-auto=validate`, si los metadatos JPA son inconsistentes con el schema, el
arranque falla. Una vez aplicados los cambios, el arranque local con H2 confirma la corrección.
