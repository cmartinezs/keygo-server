# Template Email — Mapeo Rápido

> Referencia rápida de todos los templates de email y sus variables.

## 📋 Tabla resumen

| Nombre de archivo | Tipo de email (`emailType`) | Método port | Variables requeridas | Propósito |
|---|---|---|---|---|
| `email-validation.html` | `email-verification` | `sendVerificationEmail()` | `userName`, `verificationCode`, `expiresInMinutes` | Registro de usuario — verificación de email |
| `contract-verification.html` | `contract-verification` | `sendContractVerificationEmail()` | `userName`, `verificationCode`, `contractId`, `expiresInMinutes` | Onboarding de contrato de billing — verificación de email |
| `temporary-password.html` | `temporary-password` | `sendTemporaryPasswordEmail()` | `userName`, `temporaryPassword` | Contraseña inicial para nuevos usuarios creados por admin |
| `password-recovery.html` | `password-recovery` | `sendPasswordRecoveryEmail()` | `userName`, `recoveryToken`, `tenantSlug` | Recuperación de contraseña — flujo self-service |

## 🚀 Cómo se usan

### 1️⃣ Email de verificación (Registro de usuario)

**Cuándo se envía:** Cuando un usuario se registra en una app del tenant.

**Ubicación del código:** `RegisterTenantUserUseCase.execute()`

**Invocación:**
```java
emailNotificationPort.sendVerificationEmail(
    email.value(),           // toEmail
    username.value(),        // username (persona)
    code                     // verificationCode (6 dígitos)
);
```

**Variables en template:**
- `userName` → nombre del usuario para personalización
- `verificationCode` → código de 6 dígitos
- `expiresInMinutes` → 30 (automático)

---

### 2️⃣ Email de verificación de contrato (Billing)

**Cuándo se envía:** Cuando se inicia un nuevo contrato de suscripción (onboarding de billing).

**Ubicación del código:** `CreateAppContractUseCase.execute()`

**Invocación:**
```java
emailNotificationPort.sendContractVerificationEmail(
    toEmail,                 // contractor's email
    recipientName,           // contractor's full name
    verificationCode,        // 6 dígitos
    contractId               // UUID del contrato
);
```

**Variables en template:**
- `userName` → nombre del contratista
- `verificationCode` → código de 6 dígitos
- `contractId` → UUID del contrato (para poder reanudar onboarding si es necesario)
- `expiresInMinutes` → 30 (automático)

---

### 3️⃣ Email de contraseña temporal

**Cuándo se envía:** Cuando un admin crea un nuevo usuario en un tenant (y el usuario no se registra automáticamente).

**Ubicación del código:** (pendiente de implementación — futuro)

**Invocación:**
```java
emailNotificationPort.sendTemporaryPasswordEmail(
    toEmail,                 // usuario's email
    username,                // usuario's username
    rawPassword              // contraseña plana generada
);
```

**Variables en template:**
- `userName` → nombre del usuario
- `temporaryPassword` → contraseña plana (debe ser cambiada en primer login)

---

### 4️⃣ Email de recuperación de contraseña

**Cuándo se envía:** Cuando un usuario solicita recuperar su contraseña (forgot password flow).

**Ubicación del código:** (pendiente de implementación — futuro)

**Invocación:**
```java
emailNotificationPort.sendPasswordRecoveryEmail(
    toEmail,                 // usuario's email
    username,                // usuario's username
    recoveryToken,           // token de recuperación (32 chars hex)
    tenantSlug               // slug del tenant (para construir URL si es necesario)
);
```

**Variables en template:**
- `userName` → nombre del usuario
- `recoveryToken` → token de recuperación (válido 24 horas)
- `tenantSlug` → slug del tenant (opcional, para construir links si el frontend lo necesita)

---

## 🎨 Características comunes de diseño

Todos los templates incluyen:

✅ **Header gradient** — Color único por tipo de email (morado para verificación, rojo para recuperación)  
✅ **Código/Token en caja destacada** — Fácil de copiar y leer  
✅ **Responsive design** — Se adapta a móvil, tablet, desktop  
✅ **Advertencia de seguridad** — Recordatorio de no compartir códigos  
✅ **Instrucciones paso a paso** — Guía clara de qué hacer  
✅ **Descargo de responsabilidad** — Indica qué hacer si no lo solicitaron  
✅ **Footer con links** — Política de privacidad, soporte, etc.

---

## 📦 Variables automáticas en contexto Thymeleaf

El adaptador `EmailNotificationAdapter` en `keygo-infra` también inyecta automáticamente:

| Variable | Fuente | Notas |
|---|---|---|
| `recipientEmail` | `SendEmailCommand.recipientEmail` | Disponible en template (no usado actualmente) |
| `recipientName` | `SendEmailCommand.recipientName` | Disponible en template (no usado actualmente) |

**Nota:** Actualmente solo usamos `userName` que viene en el map `templateVariables` del command.

---

## 🔄 Flujo end-to-end

```
1. UseCase llama a emailNotificationPort.sendXXXEmail(...)
   ↓
2. EmailNotificationPort (interface) mapea a sendEmail(..., emailType, templateVariables)
   ↓
3. SmtpEmailNotificationAdapter.sendEmail() crea SendEmailCommand
   ↓
4. EmailNotificationAdapter (nueva versión) en keygo-infra:
   - Carga template Thymeleaf correspondiente
   - Renderiza con variables
   - Construye MimeMessage con JavaMailSender
   - Envía por SMTP
```

---

## 🧪 Testing

Para probar los templates localmente:

```bash
# 1. Activar perfil supabase + local
export SPRING_PROFILES_ACTIVE=supabase,local

# 2. Usar MailHog para capturar emails
docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog

# 3. Configurar SMTP en .env
export SMTP_HOST=localhost
export SMTP_PORT=1025

# 4. Ejecutar la app
./mvnw spring-boot:run -pl keygo-run

# 5. Ver emails en MailHog
# Abre http://localhost:8025
```

---

## 📝 Changelog

**2026-04-04**
- ✅ Creados 4 templates profesionales con diseño responsive
- ✅ Variables alineadas con `EmailNotificationPort`
- ✅ Estilos CSS inline (compatibilidad con clientes email antiguos)
- ✅ Gradientes de color únicos por tipo de email
- ✅ Instrucciones paso a paso en cada template

---

## 🔗 Referencias

- **Port:** `keygo-app/src/main/java/.../user/port/EmailNotificationPort.java`
- **Adapter:** `keygo-infra/src/main/java/.../email/EmailNotificationAdapter.java`
- **SMTP Wrapper:** `keygo-infra/src/main/java/.../email/SmtpEmailNotificationAdapter.java`
- **Ubicación de templates:** `keygo-run/src/main/resources/templates/email/`

