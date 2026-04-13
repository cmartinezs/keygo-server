# Endpoints — Billing

Gestión de planes, contratos, suscripciones y facturas.

Base paths:
- `/api/v1/billing` — billing de tenant/app
- `/api/v1/platform/billing` — billing de plataforma (KeyGo admin)

## Catálogo de planes

### GET /billing/catalog
Lista planes disponibles. **Público** (no requiere token).

```typescript
GET /api/v1/billing/catalog

// Response 200
{
  "data": [
    {
      "planCode": "STARTER",
      "name": "Starter",
      "description": "Para equipos pequeños",
      "billingOptions": [
        {
          "billingPeriod": "MONTHLY",
          "basePrice": 5.00,
          "currency": "USD",
          "isDefault": true
        }
      ]
    }
  ]
}
```

### GET /billing/catalog/{planCode}
Detalle de un plan específico. **Público**.

### POST /tenants/{tenantSlug}/apps/{clientId}/billing/plans
Crea un plan de billing con versión inicial y entitlements. Requiere token Bearer con rol
`ADMIN_TENANT` scoped al tenant del path.

```typescript
POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/plans
Authorization: Bearer {token}

{
  "code": "STARTER",
  "name": "Plan Starter",
  "description": "Plan de entrada",
  "isPublic": true,
  "sortOrder": 10,
  "version": "v1.0",
  "currency": "USD",
  "trialDays": 14,
  "effectiveFrom": "2026-04-01",
  "billingOptions": [
    {
      "billingPeriod": "MONTHLY",
      "basePrice": 9.99,
      "discountPct": 0,
      "isDefault": true
    }
  ],
  "entitlements": [
    {
      "metricCode": "MAX_USERS",
      "metricType": "QUOTA",
      "limitValue": 10.0000,
      "periodType": "NONE",
      "enforcementMode": "HARD",
      "isEnabled": true
    }
  ]
}
```

#### Nota de integración

- `entitlements[].limitValue` ahora debe tratarse como **decimal** (`NUMERIC(18,4)` en backend),
  no como entero estricto.
- Aunque ejemplos comunes usen `10.0000` o `25.0000`, UI no debe asumir que el valor siempre
  llegará sin escala decimal.

## Contratos

### POST /billing/contracts
Inicia el proceso de contratación (crea contrato en estado PENDING, envía email de verificación).

```typescript
POST /api/v1/billing/contracts

{
  "planCode": "STARTER",
  "billingPeriod": "MONTHLY",
  "email": "contractor@example.com",
  "firstName": "Carlos",
  "lastName": "Martínez",
  "companyName": "ACME Corp"
}

// Response 201
{
  "data": {
    "contractId": "uuid",
    "status": "PENDING_VERIFICATION",
    "verificationEmailSent": true
  }
}
```

### GET /billing/contracts/{contractId}/resume
Obtiene el estado actual de un contrato para reanudar el onboarding. **Público**.

```typescript
GET /api/v1/billing/contracts/{contractId}/resume

// Response 200
{
  "data": {
    "contractId": "uuid",
    "status": "PENDING_VERIFICATION",
    "email": "contractor@example.com",
    "planCode": "STARTER"
  }
}
```

### POST /billing/contracts/{contractId}/resend-verification
Reenvía el email de verificación. **Público**.

```typescript
POST /api/v1/billing/contracts/{contractId}/resend-verification

// Response 200: email reenviado
// Response 409: contrato ya verificado
```

### POST /billing/contracts/{contractId}/verify
Verifica el contrato con el código del email.

```typescript
POST /api/v1/billing/contracts/{contractId}/verify

{ "verificationCode": "123456" }

// Response 200: contrato activado, credenciales enviadas al email
```

## Suscripción

### GET /billing/subscription
Obtiene la suscripción activa del contratante autenticado.

```typescript
GET /api/v1/billing/subscription
Authorization: Bearer {token}

// Response 200
{
  "data": {
    "subscriptionId": "uuid",
    "planCode": "STARTER",
    "status": "ACTIVE",
    "currentPeriodStart": "2026-04-01",
    "currentPeriodEnd": "2026-05-01",
    "autoRenew": true
  }
}
```

### POST /billing/subscription/cancel
Cancela la suscripción activa.

```typescript
POST /api/v1/billing/subscription/cancel
Authorization: Bearer {token}

// Response 200: suscripción cancelada al fin del período
```

## Facturas

### GET /billing/invoices
Lista facturas del contratante con paginación.

```typescript
GET /api/v1/billing/invoices?page=0&size=10
Authorization: Bearer {token}

// Response 200 — PagedData<InvoiceData>
{
  "data": {
    "content": [
      {
        "invoiceId": "uuid",
        "invoiceNumber": "INV-2026-001",
        "amount": 5.00,
        "currency": "USD",
        "status": "PAID",
        "issuedAt": "2026-04-01",
        "paidAt": "2026-04-02"
      }
    ],
    ...
  }
}
```

---

## Platform Billing (solo KeyGo Admin)

Requieren `Authorization: Bearer <token>` con rol `KEYGO_ADMIN` o `KEYGO_TENANT_ADMIN`.

### GET /platform/billing/catalog
Catálogo de planes de la plataforma. **Público**.

### GET /platform/billing/catalog/{planCode}
Detalle de un plan de plataforma. **Público**.

### GET /platform/billing/subscription
Suscripción activa del contratante de plataforma autenticado.

### POST /platform/billing/subscription/cancel
Cancela la suscripción de plataforma.

### GET /platform/billing/invoices
Facturas del contratante de plataforma con paginación.
