# Flujo de Account — Self-Service Usuario

> **Descripción:** Operaciones self-service del usuario autenticado: perfil, sesiones, cambio de contraseña, acceso/roles.

**Fecha:** 2026-04-05

---

## 1. Flujo Completo: Vista + Edición de Perfil

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA<br/>(Settings/Profile)
    participant API as 🔐 KeyGo API<br/>/account/profile
    participant DB as 🗄️ Database
    participant Email as 📧 Email

    User->>SPA: 1. Navigate to Settings

    SPA->>API: 2. GET /api/v1/tenants/{slug}/account/profile<br/>Header: Bearer {access_token}

    API->>API: 3. Extract JWT:<br/>• Validate signature<br/>• Check exp (not expired)

    API->>DB: 4. SELECT tenant_user<br/>WHERE tenant_id=(slug) AND id=(sub from JWT)

    API->>SPA: 5. Return 200<br/>profile data: sub, email, name, picture<br/>phoneNumber, birthdate, locale, updatedAt

    SPA->>User: 6. Show profile form (pre-filled)

    User->>SPA: 7. Edit name + locale<br/>Click "Save"

    SPA->>API: 8. PATCH /api/v1/tenants/{slug}/account/profile<br/>Header: Bearer {access_token}<br/>body: name + locale + picture

    API->>API: 9. Validate:<br/>• Bearer valid<br/>• locale in BCP 47 format<br/>• picture is HTTPS URL

    API->>DB: 10. BEGIN

    API->>DB: 11. UPDATE tenant_users<br/>SET name='Juan García',<br/>    locale='es-MX',<br/>    picture='...',<br/>    updated_at=NOW()

    API->>DB: 12. COMMIT

    API->>SPA: 13. Return 200:<br/>(updated user data)

    SPA->>User: 14. "Profile updated successfully"
```

---

## 2. Gestión de Sesiones (Devices)

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA<br/>(Security/Sessions)
    participant API as 🔐 KeyGo API<br/>/account/sessions
    participant DB as 🗄️ Database

    User->>SPA: 1. Click Settings → Security → Active Sessions

    SPA->>API: 2. GET /api/v1/tenants/{slug}/account/sessions<br/>Header: Bearer {access_token}

    API->>DB: 3. SELECT sessions<br/>WHERE tenant_id=(slug)<br/>  AND user_id=(sub)<br/>  AND status=ACTIVE<br/>ORDER BY created_at DESC

    API->>DB: 4. FOR EACH session:<br/>Determine is_current:<br/>• Extract current user_agent from Bearer<br/>• Compare with session.user_agent<br/>• is_current=(match)

    API->>SPA: 5. Return 200<br/>sessions[0]: Windows Chrome, isCurrent=true<br/>sessions[1]: iPhone Safari, isCurrent=false

    SPA->>User: 6. Show sessions list:<br/>This device [current]<br/>iPhone [2 days ago]

    User->>SPA: 7. Click "Sign out iPhone"<br/>(icon next to session)

    SPA->>API: 8. DELETE /api/v1/tenants/{slug}/account/sessions/{sessionId}<br/>Header: Bearer {access_token}

    API->>API: 9. Validate:<br/>• Bearer valid<br/>• sessionId belongs to user

    API->>DB: 10. UPDATE sessions<br/>SET terminated_at=NOW(),<br/>    status=TERMINATED

    API->>DB: 11. SELECT refresh_tokens<br/>WHERE session_id=UUID<br/>UPDATE refresh_tokens<br/>SET status=REVOKED

    API->>SPA: 12. Return 200 OK<br/>(idempotente - OK if already revoked)

    SPA->>User: 13. "Session signed out<br/>iPhone is no longer active"
```

---

## 3. Cambio de Contraseña (Autenticado)

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA<br/>(Security/Change Password)
    participant API as 🔐 KeyGo API<br/>/account/change-password
    participant DB as 🗄️ Database
    participant Crypto as 🔒 BCrypt

    User->>SPA: 1. Click Settings → Security → Change Password

    SPA->>SPA: 2. Show form:<br/>• Current password<br/>• New password<br/>• Confirm new password

    User->>SPA: 3. Enter passwords

    SPA->>API: 4. POST /api/v1/tenants/{slug}/account/change-password<br/>Header: Bearer {access_token}<br/>body: currentPassword + newPassword

    API->>API: 5. Validate Bearer

    API->>DB: 6. SELECT password_hash<br/>WHERE user_id=(sub)

    API->>Crypto: 7. Verify(currentPassword, password_hash)<br/>(BCrypt comparison)

    alt Current Password Incorrect
        Crypto->>API: ❌ Mismatch
        API->>SPA: 401 UNAUTHORIZED<br/>"Current password is incorrect"
    else Current Password Correct
        Crypto->>API: ✅ Match
        
        API->>API: 8. Validate newPassword:<br/>• Length ≥8<br/>• ≥1 uppercase letter<br/>• ≥1 digit<br/>• ≥1 special char<br/>• ≠ currentPassword

        API->>Crypto: 9. Hash(newPassword)<br/>with work_factor=12

        API->>DB: 10. BEGIN

        API->>DB: 11. UPDATE tenant_users<br/>SET password_hash=new_hash,<br/>    updated_at=NOW()

        API->>DB: 12. (Optional) Revoke all other sessions<br/>for security?<br/>(NO - sesión actual remains valid)

        API->>DB: 13. COMMIT

        API->>SPA: 14. Return 200 OK<br/>"Password changed successfully"

        SPA->>User: 15. "You can now login with new password<br/>on other devices"
    end
```

---

## 4. Ver Acceso / Permisos (Roles en Apps)

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA<br/>(Access/Permissions)
    participant API as 🔐 KeyGo API<br/>/account/access
    participant DB as 🗄️ Database

    User->>SPA: 1. Click Settings → Access & Permissions

    SPA->>API: 2. GET /api/v1/tenants/{slug}/account/access<br/>Header: Bearer {access_token}

    API->>DB: 3. SELECT memberships<br/>WHERE tenant_id=(slug) AND user_id=(sub)

    API->>DB: 4. FOR EACH membership:<br/>SELECT membership_roles → app_roles

    API->>DB: 5. FOR EACH app_role:<br/>Expand hierarchy (CTE) to get parent roles

    API->>SPA: 6. Return 200<br/>roles by app + scopes<br/>globalAccess=USER_TENANT

    SPA->>User: 7. Show access matrix:<br/>MobileApp: Admin, Viewer<br/>Dashboard: Editor<br/>Scopes: openid, profile, email
```

---

## 5. Vista de Preferencias de Notificación

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA<br/>(Notifications)
    participant API as 🔐 KeyGo API<br/>/account/notification-preferences
    participant DB as 🗄️ Database

    User->>SPA: 1. Click Settings → Notifications

    SPA->>API: 2. GET /api/v1/tenants/{slug}/account/notification-preferences<br/>Header: Bearer {access_token}

    API->>DB: 3. SELECT user_notification_preferences<br/>WHERE user_id=(sub)

    API->>SPA: 4. Return 200<br/>emailAlertsEnabled + emailInvoices<br/>emailSecurityAlerts + smsEnabled

    SPA->>User: 5. Show toggles<br/>☑ Email Alerts<br/>☑ Invoice Notifications<br/>☑ Security Alerts<br/>☐ SMS Notifications

    User->>SPA: 6. Toggle: disable SMS<br/>(already disabled)

    User->>SPA: 7. Disable: Email Alerts

    SPA->>API: 8. PATCH (future)<br/>body: { emailAlertsEnabled: false }<br/>(NOT YET IMPLEMENTED - future enhancement)

    API->>DB: 9. UPDATE user_notification_preferences<br/>SET emailAlertsEnabled=false

    SPA->>User: 10. "Preferences saved"
```

---

## 6. Estados de Cambio de Contraseña

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: Usuario logueado<br/>(password válida)
    
    ACTIVE --> ENTERING_OLD: Click<br/>"Change Password"
    
    ENTERING_OLD --> VALIDATING: Ingresa contraseña actual
    
    VALIDATING --> ERROR: ❌ Incorrect<br/>(BCrypt mismatch)
    
    ERROR --> ENTERING_OLD: Retry
    
    VALIDATING --> ENTERING_NEW: ✅ Correct
    
    ENTERING_NEW --> VALIDATING_NEW: Ingresa nueva contraseña<br/>+ confirma
    
    VALIDATING_NEW --> REQUIREMENTS: Valida complejidad
    
    REQUIREMENTS --> ERROR_REQ: ❌ No cumple<br/>8+ chars, 1 upper, 1 digit,<br/>1 special
    
    ERROR_REQ --> ENTERING_NEW: Retry
    
    REQUIREMENTS --> HASHING: ✅ OK
    
    HASHING --> DB_UPDATE: Hash con BCrypt<br/>(work_factor=12)
    
    DB_UPDATE --> SUCCESS: UPDATE DB
    
    SUCCESS --> ACTIVE: Contraseña cambiada<br/>Sesión actual sigue válida
    
    note right of VALIDATING
        BCrypt.verify(
          currentPassword,
          storedHash
        )
    end note
    
    note right of ENTERING_NEW
        Validar:<br/>• Length ≥8<br/>• ≠ currentPassword<br/>• 1 Upper + 1 digit + 1 special
    end note
    
    note right of HASHING
        BCrypt with
        work_factor=12
        Salt generated
    end note
```

---

## 7. Tabla Resumida: Operaciones Account

| Operación | Endpoint | Método | Auth | Mutante |
|---|---|---|---|---|
| Ver perfil | `/account/profile` | GET | Bearer | No |
| Editar perfil | `/account/profile` | PATCH | Bearer | Sí |
| Listar sesiones | `/account/sessions` | GET | Bearer | No |
| Revocar sesión | `/account/sessions/{id}` | DELETE | Bearer | Sí |
| Ver acceso/roles | `/account/access` | GET | Bearer | No |
| Ver notificaciones | `/account/notification-preferences` | GET | Bearer | No |
| Cambiar contraseña | `/account/change-password` | POST | Bearer | Sí |
| Forgot password | `/account/forgot-password` | POST | Público | Sí |
| Recover password | `/account/recover-password` | POST | Público | Sí |
| Reset password | `/account/reset-password` | POST | Público | Sí |

---

**Última actualización:** 2026-04-05  
**Próximos:** Diagramas de Secuencia (DIAGRAMAS/SECUENCIAS/) y Máquinas de Estado (DIAGRAMAS/ESTADOS/)
