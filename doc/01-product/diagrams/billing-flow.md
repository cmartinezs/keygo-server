# Flujo de Billing — Suscripciones y Facturación

> **Descripción:** Flujos de catálogo de planes, activación de suscripciones, generación de facturas y pagos.

**Fecha:** 2026-04-05

---

## 1. Flujo Completo: Catálogo → Suscripción → Pago → Factura

```mermaid
sequenceDiagram
    participant User as 👤 Usuario/Contractor
    participant SPA as 🖥️ SPA<br/>(Catálogo)
    participant KeyGo as 🔐 KeyGo API
    participant DB as 🗄️ Database
    participant Gateway as 💳 Payment Gateway<br/>(Stripe/MP)
    participant Email as 📧 Email

    User->>SPA: 1. Browse plans<br/>(no auth required)

    SPA->>KeyGo: 2. GET /api/v1/billing/catalog<br/>(public endpoint)

    KeyGo->>DB: 3. SELECT app_plans + versions + options<br/>WHERE status=ACTIVE

    KeyGo->>SPA: 4. Return catalog<br/>plans with billingOptions and entitlements

    User->>SPA: 5. Click "Subscribe to Basic Monthly"

    SPA->>KeyGo: 6. POST /api/v1/billing/contracts/{contractId}/activate<br/>(contractId viene del catálogo)<br/>body: { planVersionId, billingPeriod }

    KeyGo->>DB: 7. SELECT app_plan_version + billing_option<br/>Validate price

    KeyGo->>DB: 8. BEGIN TRANSACTION

    KeyGo->>DB: 9. INSERT app_subscriptions<br/>{ contract_id, currentPeriodStart,<br/>  currentPeriodEnd (30d from now),<br/>  autoRenew=true }

    KeyGo->>DB: 10. INSERT invoices<br/>{ subscription_id, status=PENDING,<br/>  total=9.99, periodStart, periodEnd,<br/>  created_at }

    KeyGo->>Email: 11. Send invoice email<br/>Subject: "Invoice #INV-001<br/>for Basic Monthly Plan"

    KeyGo->>DB: 12. COMMIT

    KeyGo->>SPA: 13. Return 201<br/>{ subscriptionId, invoiceId,<br/>  amountDue: 9.99, dueDate }

    SPA->>User: 14. Show payment form<br/>(card details)

    User->>SPA: 15. Enter card info

    SPA->>Gateway: 16. POST /payments/intents<br/>{ amount: 999, currency: USD,<br/>  metadata: { invoiceId } }

    Gateway->>Gateway: 17. Validate card (3D Secure, etc.)

    Gateway->>SPA: 18. Return paymentIntentId<br/>(status: PENDING)

    SPA->>User: 19. Confirm payment

    SPA->>Gateway: 20. POST /payments/confirm<br/>{ paymentIntentId }

    Gateway->>Gateway: 21. Process payment<br/>✅ Approve or ❌ Decline

    Gateway->>KeyGo: 22. Webhook: POST /billing/webhooks/payment.completed<br/>{ invoiceId, status: PAID, transactionId }

    KeyGo->>DB: 23. UPDATE invoices<br/>SET status=PAID

    KeyGo->>DB: 24. INSERT payment_transactions<br/>{ invoice_id, amount, transactionId,<br/>  status=COMPLETED }

    KeyGo->>Email: 25. Send receipt email<br/>"Payment received for Basic Monthly"

    KeyGo->>Gateway: 26. Return 200 OK (webhook ack)

    SPA->>User: 27. "Payment successful!<br/>Subscription active"
```

---

## 2. Estados de Suscripción

```mermaid
stateDiagram-v2
    [*] --> PENDING: Contrato creado
    
    PENDING --> ACTIVE: Pago recibido
    
    ACTIVE --> ACTIVE: Suscripción vigente
    
    ACTIVE --> RENEWAL: Fin de período alcanzado
    
    RENEWAL --> ACTIVE: Renovación automática
    
    ACTIVE --> FAILED: Pago fallido
    
    FAILED --> ACTIVE: Reintento exitoso
    
    FAILED --> SUSPENDED: Reintentos agotados
    
    SUSPENDED --> CANCELLED: Cancelación o término admin
    
    CANCELLED --> [*]: Fin de suscripción
    
    ACTIVE --> CANCELLED: Usuario cancela
    
    note right of PENDING
        Contrato activado
        Invoice: PENDING
        Awaiting payment
    end note
    
    note right of ACTIVE
        Subscription válida
        User puede usar features
        según entitlements
    end note
    
    note right of RENEWAL
        Auto-renew job detectó<br/>period end
        Nueva invoice creada
    end note
    
    note right of FAILED
        Invoice payment fallido
        Dunning job reintentará
    end note
    
    note right of SUSPENDED
        Dunning reintentos agotados
        Subscription suspendida
        Features bloqueadas
    end note
```

---

## 3. Flujo de Renovación Automática (T-085)

```mermaid
sequenceDiagram
    participant Cron as ⏰ Scheduled Job<br/>(@Scheduled)
    participant UseCase as 🎯 RenewSubscriptionUseCase
    participant DB as 🗄️ Database
    participant Email as 📧 Email

    Cron->>UseCase: 1. RenewSubscriptionUseCase.execute()<br/>(runs every 1h)

    UseCase->>DB: 2. SELECT subscriptions<br/>WHERE currentPeriodEnd < NOW()<br/>AND status=ACTIVE<br/>AND autoRenew=true

    UseCase->>DB: 3. FOR EACH subscription:

    UseCase->>DB: 3a. BEGIN TRANSACTION

    UseCase->>DB: 3b. INSERT app_subscriptions (NEW)<br/>{ contract_id, currentPeriodStart=NOW(),<br/>  currentPeriodEnd=NOW()+30days,<br/>  autoRenew=true }

    UseCase->>DB: 3c. Lookup billing_option price<br/>SELECT base_price FROM app_plan_billing_options

    UseCase->>DB: 3d. INSERT invoices (NEW)<br/>{ subscription_id, status=PENDING,<br/>  total=base_price, periodStart=NOW(),<br/>  periodEnd=NOW()+30days }

    UseCase->>Email: 3e. Send email<br/>"Your subscription renewed<br/>Invoice: #INV-002"

    UseCase->>DB: 3f. COMMIT

    UseCase->>DB: 4. Log metrics<br/>{ renewed_count: N,<br/>  total_new_invoices: N,<br/>  timestamp }

    Cron->>Cron: 5. Complete (next run in 1h)
```

---

## 4. Flujo de Dunning (Reintentos de Pago)

```mermaid
sequenceDiagram
    participant Invoice as 📄 Failed Invoice<br/>(status=FAILED)
    participant Cron as ⏰ Dunning Job<br/>(@Scheduled daily)
    participant Gateway as 💳 Payment Gateway
    participant Email as 📧 Email
    participant DB as 🗄️ Database

    Invoice->>DB: 1. Payment failed (webhook)<br/>INSERT dunning_events<br/>{ invoice_id, attempt=1,<br/>  nextRetryAt=NOW()+1day }

    Cron->>Cron: 2. Daily dunning job<br/>Queries: nextRetryAt < NOW()

    Cron->>DB: 3. SELECT dunning_events<br/>WHERE nextRetryAt < NOW()<br/>AND attempt < 3<br/>ORDER BY nextRetryAt ASC

    Cron->>DB: 4. FOR EACH event:

    Cron->>Email: 4a. Send reminder email<br/>"Payment failed - retrying in 24h"

    Cron->>DB: 4b. (Espera 24h local)

    Cron->>Gateway: 4c. POST /charge<br/>{ customerId, invoiceId, amount }

    Gateway->>Gateway: 4d. Process charge

    alt Payment Success
        Gateway->>Cron: ✅ Charge successful
        Cron->>DB: 4e. UPDATE invoices<br/>SET status=PAID
        Cron->>DB: 4f. DELETE dunning_event
        Cron->>Email: 4g. Send receipt email
    else Payment Failure
        Gateway->>Cron: ❌ Charge declined
        Cron->>DB: 4e. UPDATE dunning_events<br/>SET attempt=2,<br/>    nextRetryAt=NOW()+3days
        Cron->>Email: 4f. Send retry email<br/>"Retry on day 3"
    end

    Note over Cron: Retry schedule:<br/>Attempt 1: D+1<br/>Attempt 2: D+3<br/>Attempt 3: D+7<br/>After 3: SUSPEND subscription
```

---

## 5. Flujo: Admin Crea Plan

```mermaid
sequenceDiagram
    participant Admin as 👨‍💼 Admin Tenant
    participant API as 🔐 KeyGo API<br/>{slug}/apps/{appId}/plans
    participant DB as 🗄️ Database

    Admin->>API: 1. POST /api/v1/tenants/{slug}/apps/{appId}/billing/plans<br/>Header: Bearer {admin_token}<br/>body: code + name + billingOptions + entitlements

    API->>API: 2. Validate:<br/>• @PreAuthorize ADMIN_TENANT<br/>• ≥1 billingOption (T-095)<br/>• isDefault: exactly 1

    API->>DB: 3. BEGIN

    API->>DB: 4. INSERT app_plans<br/>{ client_app_id, tenant_id, code,<br/>  name, status=DRAFT }

    API->>DB: 5. INSERT app_plan_versions<br/>{ app_plan_id, version=1, isActive=true }

    API->>DB: 6. FOR EACH billingOption:<br/>INSERT app_plan_billing_options<br/>{ app_plan_version_id, billingPeriod,<br/>  basePrice, isDefault }

    API->>DB: 7. FOR EACH entitlement:<br/>INSERT app_plan_entitlements<br/>{ app_plan_version_id, featureName,<br/>  featureLimit }

    API->>DB: 8. COMMIT

    API->>Admin: 9. Return 201<br/>{ planId, code, status: DRAFT,<br/>  billingOptions, entitlements }

    Admin->>API: 10. (Optional) PUT .../plans/{code}/activate<br/>(change status DRAFT → ACTIVE)

    API->>DB: 11. UPDATE app_plans<br/>SET status=ACTIVE

    API->>Admin: 12. Plan is now public in catalog
```

---

## 6. Matriz de Estados: Invoice

| Estado | Significado | Acción | Próximo |
|---|---|---|---|
| **PENDING** | Generada, awaiting payment | Mostrar en dashboard | PAID o FAILED |
| **PAID** | Pago recibido | Usar acceso a features | EXPIRED (30+ días) |
| **FAILED** | Pago rechazado | Iniciar dunning (T-090) | PAID (retry) o SUSPENDED |
| **EXPIRED** | Vencida (30+ días sin pagar) | Bloquear features | CANCELLED |
| **CANCELLED** | Cancelada | Histórico solo | (terminal) |

---

## 7. Flujo: Usuario Cancela Suscripción

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA
    participant API as 🔐 KeyGo API
    participant DB as 🗄️ Database
    participant Email as 📧 Email

    User->>SPA: 1. Settings → Subscriptions<br/>Click "Cancel"

    SPA->>SPA: 2. Confirm popup<br/>"Sure? Will lose access."

    User->>SPA: 3. Confirm

    SPA->>API: 4. POST /api/v1/tenants/{slug}/apps/{appId}/billing/subscription/cancel<br/>Header: Bearer {user_token}<br/>(idempotente)

    API->>API: 5. Validate:<br/>• Bearer token<br/>• Subscription ownership

    API->>DB: 6. BEGIN

    API->>DB: 7. UPDATE app_subscriptions<br/>SET status=CANCELLED,<br/>    terminatedAt=NOW()

    API->>DB: 8. UPDATE invoices<br/>WHERE subscription_id<br/>SET status=CANCELLED

    API->>DB: 9. COMMIT

    API->>Email: 10. Send confirmation email<br/>"Subscription cancelled<br/>Access ends: {currentPeriodEnd}"

    API->>SPA: 11. Return 200 OK

    SPA->>User: 12. "Subscription cancelled<br/>Access until {date}"
```

---

**Última actualización:** 2026-04-05  
**Próximo:** FLUJO_ACCOUNT.md (self-service usuario)
