# Flujo de Autenticación — OAuth2/OIDC Completo

> **Descripción:** Diagramas de flujo del proceso completo de autenticación en KeyGo (Authorization Code + PKCE, Refresh Token, Logout, Reset Password).

**Fecha:** 2026-04-05

---

## 1. Authorization Code Flow + PKCE (Login)

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA/Mobile<br/>(keygo-ui)
    participant KeyGo as 🔐 KeyGo Server
    participant Email as 📧 Email Service

    User->>SPA: 1. Click "Login"
    
    SPA->>SPA: 2. Generate code_verifier (random 128 chars)
    SPA->>SPA: 3. Calculate code_challenge = SHA256(code_verifier)
    
    SPA->>KeyGo: 4. GET /oauth2/authorize<br/>?client_id=xxx<br/>&redirect_uri=https://app.com/callback<br/>&scope=openid profile email<br/>&response_type=code<br/>&code_challenge=xyz<br/>&code_challenge_method=S256<br/>&state=abc
    
    KeyGo->>KeyGo: 5. Validate:<br/>• client_id registered<br/>• redirect_uri whitelisted<br/>• scope valid
    
    KeyGo->>User: 6. Redirect to login form<br/>(or show if mobile native)
    
    User->>KeyGo: 7. POST /account/login<br/>body: { email, password }
    
    KeyGo->>KeyGo: 8. Validate email exists<br/>Verify password (BCrypt)
    KeyGo->>KeyGo: 9. Create session + generate<br/>authorization_code (TTL 10m)
    
    KeyGo->>SPA: 10. Redirect to redirect_uri<br/>?code=auth_code_xyz<br/>&state=abc
    
    SPA->>SPA: 11. Validate state matches<br/>Extract authorization_code
    
    SPA->>KeyGo: 12. POST /oauth2/token<br/>grant_type=authorization_code<br/>code + code_verifier + client credentials
    
    KeyGo->>KeyGo: 13. Validate:<br/>• code not expired<br/>• code_verifier matches challenge<br/>• client credentials valid
    
    KeyGo->>KeyGo: 14. Issue JWT tokens:<br/>• access_token (TTL 1h)<br/>• id_token (with user claims)<br/>• refresh_token (TTL 30d, hashed)
    
    KeyGo->>SPA: 15. Return tokens<br/>access_token + id_token + refresh_token<br/>expires_in=3600
    
    SPA->>SPA: 16. Store tokens securely:<br/>• access_token → memory/sessionStorage<br/>• refresh_token → secure httpOnly cookie
    
    SPA->>User: 17. Redirect to dashboard
```

---

## 2. Refresh Token Rotation (Token Expiration)

```mermaid
sequenceDiagram
    participant SPA as 🖥️ SPA/Mobile
    participant KeyGo as 🔐 KeyGo Server
    participant DB as 🗄️ Database

    SPA->>SPA: 1. access_token expiring (or 401 received)
    
    SPA->>KeyGo: 2. POST /oauth2/token<br/>grant_type=refresh_token<br/>old refresh_token + client credentials
    
    KeyGo->>DB: 3. Lookup refresh_token<br/>(hash validation: SHA256)
    
    KeyGo->>KeyGo: 4. Validate:<br/>• token not expired (TTL 30d)<br/>• status ≠ REVOKED<br/>• status ≠ USED (T-035: replay detect)<br/>  → if USED: revoke entire chain!<br/>  → reject with 401
    
    alt Replay Attack Detected (T-035)
        KeyGo->>DB: 4a. UPDATE refresh_tokens<br/>SET status=REVOKED<br/>WHERE session_id=X
        KeyGo->>SPA: Error: REPLAY_ATTACK_DETECTED<br/>(user must re-login)
    else Valid Refresh
        KeyGo->>DB: 5. Mark old refresh_token<br/>status=USED
        
        KeyGo->>KeyGo: 6. Generate NEW refresh_token<br/>Hash with SHA256<br/>Create RefreshTokenEntity
        
        KeyGo->>KeyGo: 7. Issue NEW access_token<br/>(TTL 1h, same user/scopes)
        
        KeyGo->>SPA: 8. Return new tokens<br/>new access_token + new refresh_token<br/>expires_in=3600
        
        SPA->>SPA: 9. Update stored tokens:<br/>• access_token ← new<br/>• refresh_token ← new<br/>(in secure cookie)
        
        SPA->>SPA: 10. Retry original request<br/>with new access_token
    end
```

---

## 3. Token Revocation (Logout)

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA/Mobile
    participant KeyGo as 🔐 KeyGo Server
    participant DB as 🗄️ Database

    User->>SPA: 1. Click "Logout"
    
    SPA->>KeyGo: 2. POST /oauth2/revoke<br/>Header: Authorization: Bearer {access_token}<br/>body: token=refresh_token
    
    KeyGo->>KeyGo: 3. Validate access_token<br/>Extract user_id
    
    KeyGo->>DB: 4. UPDATE refresh_tokens<br/>SET status=REVOKED<br/>WHERE token_hash = SHA256(refresh_token)<br/>  AND session_id matches
    
    KeyGo->>DB: 5. UPDATE sessions<br/>SET terminated_at=NOW()<br/>WHERE id=session_id
    
    KeyGo->>SPA: 6. Return 200 OK<br/>(idempotent - OK even if already revoked)
    
    SPA->>SPA: 7. Delete stored tokens:<br/>• access_token ← null<br/>• refresh_token ← null<br/>• Clear cookies
    
    SPA->>User: 8. Redirect to login page
    
    alt Future Refresh Attempt
        SPA->>KeyGo: 9. POST /oauth2/token<br/>with revoked refresh_token
        KeyGo->>KeyGo: 10. Check status = REVOKED
        KeyGo->>SPA: 11. Reject: 401 UNAUTHORIZED<br/>(user must re-login)
    end
```

---

## 4. Password Reset Flow

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant SPA as 🖥️ SPA/Mobile
    participant KeyGo as 🔐 KeyGo Server
    participant Email as 📧 Email Service
    participant DB as 🗄️ Database

    User->>SPA: 1. Click "Forgot Password"
    
    SPA->>SPA: 2. Show email input form
    
    User->>SPA: 3. Enter email
    
    SPA->>KeyGo: 4. POST /account/forgot-password<br/>body: { email }
    
    KeyGo->>DB: 5. Query user by email
    
    alt Email Not Found
        KeyGo->>KeyGo: 5a. Anti-enumeration:<br/>Log attempt but don't reveal
        KeyGo->>SPA: 6a. Return 200 OK<br/>"Check your email"
    else Email Found
        KeyGo->>KeyGo: 6. Generate PasswordRecoveryToken<br/>(32-char random, TTL 30m)
        
        KeyGo->>DB: 7. INSERT password_recovery_tokens<br/>{ token_hash, user_id, expires_at }
        
        KeyGo->>Email: 8. Send recovery email:<br/>Click: https://app.com/recovery?token=xyz
        
        KeyGo->>SPA: 9. Return 200 OK<br/>"Check your email"
    end
    
    User->>Email: 10. Click recovery link in email
    
    SPA->>SPA: 11. Extract token from URL<br/>POST /account/recover-password<br/>body: { token, newPassword }
    
    KeyGo->>DB: 12. Query recovery token<br/>Validate: exists, not expired, not used
    
    KeyGo->>KeyGo: 13. Validate newPassword:<br/>• Meets complexity (8 chars, 1 upper, 1 digit, 1 special)<br/>• ≠ current password
    
    KeyGo->>KeyGo: 14. Generate PasswordResetCode<br/>(6-digit random, TTL 24h)
    
    KeyGo->>DB: 15. INSERT password_reset_codes<br/>{ code_hash, user_id, expires_at }
    
    KeyGo->>Email: 16. Send reset code:<br/>Your code: 123456
    
    KeyGo->>SPA: 17. Return { resetCodeId }<br/>(UUID for security, not email)
    
    User->>SPA: 18. Receive code from email<br/>Enter in form
    
    SPA->>KeyGo: 19. POST /account/reset-password<br/>body: resetCodeId + resetCode + newPassword
    
    KeyGo->>DB: 20. Query reset code<br/>Validate: exists, not expired, matches code
    
    KeyGo->>KeyGo: 21. Hash newPassword with BCrypt
    
    KeyGo->>DB: 22. UPDATE tenant_users<br/>SET password_hash=new_hash,<br/>    status=ACTIVE,<br/>    updated_at=NOW()
    
    KeyGo->>DB: 23. Mark code as used
    
    KeyGo->>SPA: 24. Return 200 OK<br/>"Password reset successfully"
    
    SPA->>User: 25. Redirect to login
    
    User->>SPA: 26. Login con nueva contraseña<br/>(nuevo authorization code flow)
```

---

## 5. Estado de Transiciones (Sesión y Tokens)

```mermaid
stateDiagram-v2
    [*] --> INIT: Usuario inicia sesión<br/>(POST /account/login)
    
    INIT --> AUTHORIZED: Validación OK<br/>Authorization Code generado<br/>(TTL 10m)
    
    AUTHORIZED --> TOKEN_ISSUED: POST /oauth2/token<br/>Code intercambiado<br/>Tokens emitidos
    
    TOKEN_ISSUED --> ACTIVE: Tokens válidos<br/>Access Token: 1h<br/>Refresh Token: 30d
    
    ACTIVE --> ACTIVE: GET /userinfo<br/>API calls con Bearer
    
    ACTIVE --> REFRESHING: Access token expiring<br/>POST /oauth2/token<br/>(grant_type=refresh_token)
    
    REFRESHING --> ACTIVE: Nuevo access_token emitido<br/>Nuevo refresh_token (rotación)
    
    ACTIVE --> REVOKED: POST /oauth2/revoke<br/>o Logout
    
    REVOKED --> [*]: Session terminada<br/>Tokens inválidos
    
    ACTIVE --> EXPIRED: Refresh token vence<br/>(después 30d)
    
    EXPIRED --> [*]: Usuario debe re-login
    
    note right of AUTHORIZED
        Authorization Code
        Status: VALID
        TTL: 10m
        Next: intercambiar en /token
    end note
    
    note right of TOKEN_ISSUED
        3 Tokens emitidos:
        • access_token (1h)
        • id_token (1h, claims)
        • refresh_token (30d, hashed)
    end note
    
    note right of ACTIVE
        Usuario autenticado
        Puede:
        • Llamar APIs con Bearer
        • Refrescar tokens
        • Logout (revoke)
    end note
    
    note right of REVOKED
        Logout exitoso
        • refresh_token status=REVOKED
        • Session terminada
        • Próximo acceso: 401
    end note
```

---

## 6. Claims en JWT (Access Token vs ID Token)

### **Access Token (autoriza API calls)**

```json
{
  "sub": "user-uuid",
  "iss": "https://keygo.local/tenants/acme",
  "aud": "client-id-mobile-app",
  "exp": 1712416800,
  "iat": 1712413200,
  "scope": "openid profile email",
  "roles": ["ADMIN_TENANT"],
  "oid": "acme"  // tenant_slug
}
```

### **ID Token (identifica usuario)**

```json
{
  "sub": "user-uuid",
  "iss": "https://keygo.local/tenants/acme",
  "aud": "client-id-mobile-app",
  "exp": 1712416800,
  "iat": 1712413200,
  "nonce": "random-nonce-from-authorize",
  "email": "user@acme.com",
  "name": "John Doe",
  "picture": "https://...",
  "locale": "es-MX"
}
```

---

## 7. Flujo de Verificación de Email (Registro)

```mermaid
sequenceDiagram
    participant User as 👤 Usuario
    participant App as 🖥️ App (Registro)
    participant KeyGo as 🔐 KeyGo Server
    participant Email as 📧 Email Service

    User->>App: 1. Formulario: nombre, email, password
    
    App->>KeyGo: 2. POST /api/v1/tenants/{slug}/apps/{clientId}/register<br/>body: { email, password, name }
    
    KeyGo->>KeyGo: 3. Validar:<br/>• Email no existe en tenant<br/>• Password cumple complejidad<br/>• Email formato válido
    
    KeyGo->>KeyGo: 4. Hash password (BCrypt)
    
    KeyGo->>DB: 5. CREATE tenant_user<br/>status=UNVERIFIED<br/>email_verified=false
    
    KeyGo->>KeyGo: 6. Generar EmailVerificationCode<br/>(6-digit random, TTL 30m)
    
    KeyGo->>Email: 7. Enviar email:<br/>Subject: "Verifica tu email"<br/>Código: 123456
    
    KeyGo->>App: 8. Return { userId, requiresVerification }
    
    App->>User: 9. "Verifica tu email - código enviado"
    
    User->>Email: 10. Lee código de email
    
    User->>App: 11. Ingresa código en formulario
    
    App->>KeyGo: 12. POST /api/v1/tenants/{slug}/apps/{clientId}/verify-email<br/>body: { email, code }
    
    KeyGo->>DB: 13. Lookup EmailVerificationCode<br/>Validate: no expired, matches code
    
    KeyGo->>DB: 14. UPDATE tenant_users<br/>SET status=ACTIVE,<br/>    email_verified=true
    
    KeyGo->>DB: 15. DELETE email_verification_code<br/>(mark as used)
    
    KeyGo->>App: 16. Return 200 OK
    
    App->>User: 17. "Email verificado - puedes loguearte"
    
    User->>App: 18. Login (authorization code flow)
```

---

## 8. Resumen: Flujos Críticos

| Flujo | Duración | Tokens Emitidos | Seguridad Clave |
|---|---|---|---|
| **Authorization Code + PKCE** | ~2-3 min | access + id + refresh | PKCE previene code interception |
| **Refresh Token Rotation** | ~100 ms | access + refresh (nuevo) | SHA256 hash, replay detection (T-035) |
| **Logout/Revoke** | ~50 ms | ninguno | Marca refresh como REVOKED |
| **Password Reset** | ~5-10 min | ninguno | Recovery token (30m) + Reset code (24h) |
| **Email Verification** | ~5 min | ninguno | 6-digit code (30m TTL) |

---

**Última actualización:** 2026-04-05  
**Próximo:** FLUJO_TENANT_MANAGEMENT.md (creación y gestión de tenants)
