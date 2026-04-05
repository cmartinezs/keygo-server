# Diagrama de Casos de Uso — KeyGo Server

> **Descripción:** Mapa visual de actores y sus interacciones con el sistema, organizados por Bounded Context.

**Fecha:** 2026-04-05

---

## 1. Mapa General (Todos los Contextos)

```mermaid
graph TB
    subgraph "Actores Externos"
        User["👤 Usuario (no auth)"]
        Admin["👨‍💼 Admin KeyGo"]
        AdminTenant["👨‍💼 Admin Tenant"]
        Service["🔌 Servicio Backend<br/>(M2M)"]
        Gateway["💳 Payment Gateway<br/>(Stripe/MercadoPago)"]
    end

    subgraph "AUTH Context"
        UC_Auth["<b>UC-A: Authentication</b>"]
        AuthorizeCode["Authorize with Code"]
        ExchangeToken["Exchange Token"]
        RefreshToken["Refresh Token"]
        RevokeToken["Revoke Token"]
        UserInfo["Get UserInfo"]
        VerifyEmail["Verify Email"]
        ResetPassword["Reset Password"]
        LoginCreds["Validate Credentials"]
    end

    subgraph "TENANTS Context"
        UC_Tenant["<b>UC-T: Tenant Management</b>"]
        CreateTenant["Create Tenant"]
        ManageUsers["Manage Users"]
        ManageApps["Manage Apps"]
        ManageRoles["Manage Roles"]
        ManageMemberships["Manage Memberships"]
        ViewStats["View Stats"]
    end

    subgraph "BILLING Context"
        UC_Bill["<b>UC-B: Billing</b>"]
        ViewCatalog["View Catalog"]
        ActivateContract["Activate Subscription"]
        VerifyEmail_Bill["Verify Email (Contract)"]
        ViewInvoices["View Invoices"]
        ProcessPayment["Process Payment"]
        RenewSubscription["Renew Subscription"]
    end

    subgraph "ACCOUNT Context"
        UC_Acc["<b>UC-AC: Account</b>"]
        ViewProfile["View/Edit Profile"]
        ManageSessions["Manage Sessions"]
        ChangePassword["Change Password"]
        ViewAccess["View Access/Roles"]
    end

    %% User (no auth)
    User -->|"Authorize + Login"| AuthorizeCode
    User -->|"Forgot Password"| ResetPassword
    User -->|"Register"| VerifyEmail
    User -->|"Activate Plan"| ActivateContract
    User -->|"Verify Email"| VerifyEmail_Bill
    User -->|"Pay"| ProcessPayment

    %% User (authenticated)
    User -->|"Refresh Access"| RefreshToken
    User -->|"Logout"| RevokeToken
    User -->|"Get Info"| UserInfo
    User -->|"View/Edit Profile"| ViewProfile
    User -->|"Manage Sessions"| ManageSessions
    User -->|"Change Password"| ChangePassword
    User -->|"View Permissions"| ViewAccess
    User -->|"View Invoices"| ViewInvoices

    %% Admin
    Admin -->|"Create Tenant"| CreateTenant
    Admin -->|"Manage All Tenants"| ManageApps
    Admin -->|"View Platform Stats"| ViewStats

    %% Admin Tenant
    AdminTenant -->|"Manage Users"| ManageUsers
    AdminTenant -->|"Create Apps"| ManageApps
    AdminTenant -->|"Create Roles"| ManageRoles
    AdminTenant -->|"Assign Memberships"| ManageMemberships
    AdminTenant -->|"Create Plans"| ViewCatalog
    AdminTenant -->|"View Tenant Stats"| ViewStats

    %% Service (M2M)
    Service -->|"Get Platform Stats"| ViewStats
    Service -->|"Call APIs"| UserInfo

    %% Gateway
    Gateway -->|"Webhook: Payment Status"| ProcessPayment

    %% Styling
    classDef auth fill:#e1f5ff,stroke:#01579b
    classDef tenant fill:#f3e5f5,stroke:#4a148c
    classDef billing fill:#e8f5e9,stroke:#1b5e20
    classDef account fill:#fff3e0,stroke:#e65100
    
    class UC_Auth auth
    class AuthorizeCode,ExchangeToken,RefreshToken,RevokeToken,UserInfo,VerifyEmail,ResetPassword,LoginCreds auth
    class UC_Tenant tenant
    class CreateTenant,ManageUsers,ManageApps,ManageRoles,ManageMemberships,ViewStats tenant
    class UC_Bill billing
    class ViewCatalog,ActivateContract,VerifyEmail_Bill,ViewInvoices,ProcessPayment,RenewSubscription billing
    class UC_Acc account
    class ViewProfile,ManageSessions,ChangePassword,ViewAccess account
```

---

## 2. AUTH Context — Casos de Uso Detallados

```mermaid
graph TD
    subgraph "Actores"
        User["👤 Usuario"]
        Client["🖥️ Cliente Web/Mobile"]
        Admin["👨‍💼 Admin"]
    end

    subgraph "AUTH Use Cases"
        UC1["<b>UC-A1:</b><br/>Authorize (Code + PKCE)"]
        UC2["<b>UC-A2:</b><br/>Exchange Code → Token"]
        UC3["<b>UC-A3:</b><br/>Refresh Token"]
        UC4["<b>UC-A4:</b><br/>Revoke Token"]
        UC5["<b>UC-A5:</b><br/>Get UserInfo"]
        UC6["<b>UC-A8:</b><br/>Verify Email"]
        UC7["<b>UC-A9:</b><br/>Forgot Password"]
        UC8["<b>UC-A10:</b><br/>Reset Password"]
        UC9["<b>UC-A12:</b><br/>Change Password"]
        UC10["<b>UC-A6:</b><br/>Validate Credentials"]
    end

    subgraph "Systems"
        EmailSys["📧 Email System"]
        TokenSys["🔐 Token Signer<br/>(RSA)"]
        PasswordSys["🔒 Password Hasher<br/>(BCrypt)"]
        SessionSys["📝 Session Manager"]
    end

    %% User flows
    User -->|"Open App"| UC1
    User -->|"Login with Code"| UC2
    User -->|"Token Expired"| UC3
    User -->|"Logout"| UC4
    User -->|"Get Profile"| UC5
    User -->|"Verify Email"| UC6
    User -->|"Forgot Password"| UC7
    User -->|"Use Recovery Link"| UC8
    User -->|"Logged In"| UC9

    %% Client flows
    Client -->|"Query Token"| UC3
    Client -->|"Revoke on Logout"| UC4

    %% Admin flows
    Admin -->|"Validate User"| UC10

    %% System interactions
    UC1 --> TokenSys
    UC2 --> TokenSys
    UC2 --> SessionSys
    UC3 --> TokenSys
    UC5 --> TokenSys
    UC6 --> EmailSys
    UC7 --> EmailSys
    UC8 --> PasswordSys
    UC9 --> PasswordSys
    UC10 --> PasswordSys

    classDef auth fill:#e1f5ff,stroke:#01579b
    classDef system fill:#f0f0f0,stroke:#666
    class UC1,UC2,UC3,UC4,UC5,UC6,UC7,UC8,UC9,UC10 auth
    class EmailSys,TokenSys,PasswordSys,SessionSys system
```

---

## 3. TENANTS Context — Casos de Uso Detallados

```mermaid
graph TD
    subgraph "Actores"
        Admin["👨‍💼 Admin KeyGo"]
        AdminTenant["👨‍💼 Admin Tenant"]
        User["👤 Usuario Tenant"]
    end

    subgraph "TENANTS Use Cases"
        UC_T1["<b>UC-T1/T6:</b><br/>Create/Suspend Tenant"]
        UC_T2["<b>UC-T2/T3:</b><br/>List/Get Tenants"]
        UC_T7["<b>UC-T7/T10:</b><br/>Create/Manage Users"]
        UC_T13["<b>UC-T13:</b><br/>Create Apps"]
        UC_T16["<b>UC-T16:</b><br/>Create Roles"]
        UC_T17["<b>UC-T17:</b><br/>Assign Role Parent<br/>(Hierarchy)"]
        UC_T18["<b>UC-T18/T19:</b><br/>Create/List Memberships"]
        UC_T21["<b>UC-T21:</b><br/>View Tenant Stats"]
        UC_T22["<b>UC-T22:</b><br/>View Tenant Dashboard"]
    end

    subgraph "Systems"
        AuthSys["🔐 Auth Context<br/>(Roles in JWT)"]
        DBSys["🗄️ Database<br/>(Isolation by tenant)"]
    end

    %% Admin KeyGo
    Admin -->|"Manage Platform"| UC_T1
    Admin -->|"Monitor Tenants"| UC_T2

    %% Admin Tenant
    AdminTenant -->|"Setup Tenant"| UC_T7
    AdminTenant -->|"Register App"| UC_T13
    AdminTenant -->|"Define Roles"| UC_T16
    AdminTenant -->|"Setup Hierarchy"| UC_T17
    AdminTenant -->|"Assign Roles"| UC_T18
    AdminTenant -->|"Monitor Tenant"| UC_T21
    AdminTenant -->|"Dashboard"| UC_T22

    %% User
    User -->|"View Own Roles"| UC_T21

    %% System interactions
    UC_T1 --> DBSys
    UC_T7 --> AuthSys
    UC_T16 --> AuthSys
    UC_T17 --> DBSys
    UC_T18 --> AuthSys
    UC_T21 --> DBSys
    UC_T22 --> DBSys

    classDef tenant fill:#f3e5f5,stroke:#4a148c
    classDef system fill:#f0f0f0,stroke:#666
    class UC_T1,UC_T2,UC_T7,UC_T13,UC_T16,UC_T17,UC_T18,UC_T21,UC_T22 tenant
    class AuthSys,DBSys system
```

---

## 4. BILLING Context — Casos de Uso Detallados

```mermaid
graph TD
    subgraph "Actores"
        User["👤 Usuario/Contractor"]
        Admin["👨‍💼 Admin Tenant"]
        Gateway["💳 Payment Gateway"]
        Cron["⏰ Scheduled Job"]
    end

    subgraph "BILLING Use Cases"
        UC_B1["<b>UC-B1:</b><br/>List Plans (Public)"]
        UC_B3["<b>UC-B3:</b><br/>Create Plan"]
        UC_B5["<b>UC-B5:</b><br/>Create Contract"]
        UC_B6["<b>UC-B6:</b><br/>Activate Subscription"]
        UC_B7["<b>UC-B7:</b><br/>Generate Invoice"]
        UC_B8["<b>UC-B8:</b><br/>Process Payment"]
        UC_B9["<b>UC-B9:</b><br/>Renew Subscription"]
        UC_B12["<b>UC-B12:</b><br/>View Invoices"]
    end

    subgraph "Systems"
        PlanSys["📋 Plan Versioning<br/>(Immutable)"]
        InvoiceSys["📄 Invoice System"]
        EmailSys["📧 Email Notifications"]
    end

    %% User flows (Public)
    User -->|"Browse Plans"| UC_B1
    User -->|"Subscribe"| UC_B5
    User -->|"Activate Plan"| UC_B6
    User -->|"View Invoices"| UC_B12
    User -->|"Pay Invoice"| UC_B8

    %% Admin flows
    Admin -->|"Create Plan"| UC_B3

    %% System flows (Cron)
    Cron -->|"Auto-Generate"| UC_B7
    Cron -->|"Auto-Renew"| UC_B9

    %% Gateway flows
    Gateway -->|"Webhook: Status"| UC_B8

    %% System interactions
    UC_B3 --> PlanSys
    UC_B6 --> InvoiceSys
    UC_B7 --> InvoiceSys
    UC_B6 --> EmailSys
    UC_B8 --> EmailSys
    UC_B9 --> InvoiceSys

    classDef billing fill:#e8f5e9,stroke:#1b5e20
    classDef system fill:#f0f0f0,stroke:#666
    class UC_B1,UC_B3,UC_B5,UC_B6,UC_B7,UC_B8,UC_B9,UC_B12 billing
    class PlanSys,InvoiceSys,EmailSys system
```

---

## 5. ACCOUNT Context — Casos de Uso Detallados

```mermaid
graph TD
    subgraph "Actores"
        User["👤 Usuario Autenticado"]
        AuthSys["🔐 Auth System<br/>(Password Change)"]
    end

    subgraph "ACCOUNT Use Cases"
        UC_AC1["<b>UC-AC1/AC2:</b><br/>View/Edit Profile"]
        UC_AC3["<b>UC-AC3:</b><br/>List Sessions"]
        UC_AC4["<b>UC-AC4:</b><br/>Revoke Session"]
        UC_AC5["<b>UC-AC5:</b><br/>View Access/Roles"]
        UC_AC6["<b>UC-AC6:</b><br/>View Notifications"]
    end

    subgraph "Systems"
        ProfileSys["👤 Profile Service<br/>(OIDC Claims)"]
        SessionSys["📝 Session Manager"]
        PermSys["🔒 Permission Engine"]
    end

    %% User flows
    User -->|"Manage Profile"| UC_AC1
    User -->|"Monitor Sessions"| UC_AC3
    User -->|"Sign Out Device"| UC_AC4
    User -->|"Check Permissions"| UC_AC5
    User -->|"Configure Alerts"| UC_AC6

    %% System interactions
    UC_AC1 --> ProfileSys
    UC_AC3 --> SessionSys
    UC_AC4 --> SessionSys
    UC_AC5 --> PermSys

    classDef account fill:#fff3e0,stroke:#e65100
    classDef system fill:#f0f0f0,stroke:#666
    class UC_AC1,UC_AC3,UC_AC4,UC_AC5,UC_AC6 account
    class ProfileSys,SessionSys,PermSys system
```

---

## 6. Matriz de Actores vs. Contextos

| Actor | Auth | Tenants | Billing | Account |
|---|---|---|---|---|
| **Usuario (no auth)** | ✅ Login, Reset Password, Verify Email | ❌ | ✅ View Catalog, Activate Plan, Pay | ❌ |
| **Usuario (authenticated)** | ✅ Logout, Refresh, UserInfo | ❌ (view only) | ✅ View Invoices | ✅ Full self-service |
| **Admin KeyGo** | ✅ (system admin) | ✅ Create/Suspend Tenants | ✅ (system level) | ❌ |
| **Admin Tenant** | ❌ (pero gestiona usuarios) | ✅ Users, Apps, Roles, Memberships | ✅ Create Plans | ❌ (pero ve usuarios) |
| **Servicio Backend (M2M)** | ✅ Client Credentials | ✅ System access | ✅ Webhooks | ❌ |
| **Payment Gateway** | ❌ | ❌ | ✅ Payment Status | ❌ |
| **Scheduled Jobs** | ❌ | ❌ | ✅ Renewal, Cleanup | ❌ |

---

**Última actualización:** 2026-04-05  
**Próximo:** FLUJO_AUTENTICACION.md (flujo OAuth2/OIDC detallado)
