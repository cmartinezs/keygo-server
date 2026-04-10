# Email Templates — Resumen Visual (Quick Reference)

> **Acceso rápido:** Diagrama visual, checklist, y referencias cruzadas.

---

## 🏗️ Arquitectura (Visión de Alto Nivel)

```
┌─────────────────────────────────────────────────────────────────────┐
│  UseCase Layer (keygo-app)                                           │
│                                                                       │
│  RegisterUserUseCase → sendEmail(SendEmailCommand)                   │
│  VerifyEmailUseCase → sendEmail(SendEmailCommand)                    │
│  PasswordRecoveryUseCase → sendEmail(SendEmailCommand)               │
└────────────────────────────────┬────────────────────────────────────┘
                                  │
                                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│  Port (OUT) Interface — keygo-infra/port/notification/              │
│                                                                       │
│  EmailNotificationPort                                               │
│    ↳ void sendEmail(SendEmailCommand cmd)                            │
└────────────────────────────────┬────────────────────────────────────┘
                                  │
                                  ↓
┌─────────────────────────────────────────────────────────────────────┐
│  Adapter Implementation — keygo-infra/adapter/notification/          │
│                                                                       │
│  EmailNotificationAdapter implements EmailNotificationPort           │
│    1. Resolve Strategy (bytype: "email-validation", etc.)            │
│    2. Render Template (Thymeleaf)                                    │
│    3. Send MimeMessage (JavaMailSender)                              │
└────────────────────────────────┬────────────────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    ↓                           ↓
        ┌──────────────────────┐    ┌──────────────────────┐
        │  Strategy Pattern    │    │  Template Engine     │
        │                      │    │                      │
        │ EmailStrategy:       │    │ Thymeleaf            │
        │ - name              │    │ TemplateEngine       │
        │ - subject            │    │                      │
        │ - variables          │    │ @Bean(name =         │
        │ - dynamic links      │    │  "emailTemplateEngine")
        │                      │    │                      │
        │ Implementations:    │    │ Location:            │
        │ • EmailValidation   │    │ templates/email/     │
        │ • PasswordRecovery  │    │   ├─ email-*.html    │
        │ • etc.              │    │   └─ _layout.html    │
        └──────────────────────┘    └──────────────────────┘
```

---

## 📂 Estructura de ficheros

```
keygo-infra/src/main/java/io/cmartinezs/keygo/infra/
├── port/
│   └── notification/
│       ├── EmailNotificationPort.java              ← Interface (OUT)
│       ├── SendEmailCommand.java                   ← DTO (comando)
│       └── EmailNotificationException.java         ← Custom exception
│
└── adapter/
    └── notification/
        ├── EmailNotificationAdapter.java           ← Main adapter
        └── strategy/
            ├── EmailStrategy.java                  ← Base strategy
            ├── EmailValidationStrategy.java        ← Implementation 1
            ├── PasswordRecoveryStrategy.java       ← Implementation 2
            └── OtherTypeStrategy.java              ← Implementation N

keygo-run/src/main/java/io/cmartinezs/keygo/run/config/
├── ApplicationConfig.java                         ← Register @Bean
├── ThymeleafTemplateConfig.java                   ← Template engine config
└── KeyGoEmailProperties.java                      ← Configuration properties

keygo-run/src/main/resources/
├── application.yml                                ← Base config
├── application-local.yml                          ← Dev: Mailhog
├── application-desa.yml                           ← Staging: SendGrid
├── application-prod.yml                           ← Prod: SendGrid/SES
└── templates/
    └── email/
        ├── email-validation.html                  ← Template 1
        ├── password-recovery.html                 ← Template 2
        ├── email-verification-code-expired.html  ← Template 3
        └── _layout.html                           ← Shared layout (optional)
```

---

## ✅ Checklist de Implementación

### Fase 0: Setup (⏱️ 1 hora)

- [ ] Leer `docs/design/EMAIL_TEMPLATES_QUICKSTART.md` (5 min)
- [ ] Agregar `spring-boot-starter-thymeleaf` a `keygo-run/pom.xml`
- [ ] Crear `ThymeleafTemplateConfig.java` en `keygo-run/config/`
- [ ] Crear `KeyGoEmailProperties.java` en `keygo-run/config/`
- [ ] Crear carpeta `src/main/resources/templates/email/`
- [ ] Crear 1 template HTML de prueba (`test-email.html`)
- [ ] Actualizar `application.yml` con SMTP config
- [ ] Ejecutar `./mvnw clean install` — debe compilar sin errores

### Fase 1: Puertos y Adapter (⏱️ 1.5 horas)

- [ ] Crear `EmailNotificationPort.java` interfaz en `keygo-infra/port/notification/`
- [ ] Crear `SendEmailCommand.java` (command/DTO) builder
- [ ] Crear `EmailNotificationException.java` (custom exception)
- [ ] Crear `EmailNotificationAdapter.java` en `keygo-infra/adapter/notification/`
- [ ] Registrar `@Bean` adapter en `ApplicationConfig.java`
- [ ] Inyectar `EmailNotificationPort` en `keygo-infra/pom.xml`

### Fase 2: Estrategias (⏱️ 1.5 horas)

- [ ] Crear `EmailStrategy.java` (base interface)
- [ ] Crear `EmailValidationStrategy.java` (implementation 1)
- [ ] Crear `PasswordRecoveryEmailStrategy.java` (implementation 2)
- [ ] Implementar `resolveStrategy()` en adapter (switch/case)
- [ ] Crear templates correspondientes:
  - [ ] `email-validation.html` (variables: userName, code, verificationLink)
  - [ ] `password-recovery.html` (variables: userName, code, recoveryLink)

### Fase 3: Tests (⏱️ 1.5 horas)

- [ ] **Unit test Strategy:** `EmailValidationStrategyTest`
- [ ] **Integration test Template:** `ThymeleafTemplateConfigTest`
- [ ] **E2E test Adapter:** `EmailNotificationAdapterTest` (mock SMTP)
- [ ] Ejecutar `./mvnw test -pl keygo-infra` — 10+ tests passing
- [ ] Cobertura > 80% en adapter + strategies

### Fase 4: Documentación (⏱️ 30 min)

- [ ] Actualizar `AGENTS.md` § "Email notifications" (✅ ya hecho)
- [ ] Agregar entrada en `docs/ai/lecciones.md` (patrón, decisiones)
- [ ] Actualizar `docs/ai/propuestas.md` si hay mejoras futuras
- [ ] Documentar templates en Postman collection (si aplica)

**Total: ~5-6 horas** (dev experimentado)

---

## 🔄 Flujo de uso típico

### 1️⃣ Desde UseCase (keygo-app)

```java
@RequiredArgsConstructor
public class RegisterTenantUserUseCase {
  
  private final SaveTenantUserPort saveTenantUserPort;
  private final EmailNotificationPort emailNotificationPort;  // ← Injected
  
  public void execute(RegisterUserCommand cmd) {
    // 1. Validar
    // 2. Crear usuario
    var user = TenantUser.builder()
      .email(cmd.getEmail())
      .username(cmd.getUsername())
      .verificationCode(generateCode())
      .build();
    
    // 3. Guardar
    saveTenantUserPort.save(user);
    
    // 4. Enviar email ← HERE
    var emailCmd = SendEmailCommand.builder()
      .emailType("email-validation")           // ← Strategy resolver key
      .recipientEmail(user.getEmail())
      .recipientName(user.getUsername())
      .variables(Map.of(
        "verificationCode", user.getVerificationCode(),
        "expiresInMinutes", 30))
      .uriParams(Map.of(
        "verificationUrl", Map.of(
          "scheme", "https",
          "host", "app.keygo.io",
          "port", 443,
          "path", "/verify-email/{code}")))
      .locale(Locale.of("es"))
      .build();
    
    emailNotificationPort.sendEmail(emailCmd);  // ← Port call
  }
  
  private String generateCode() {
    return String.format("%06d", new Random().nextInt(1000000));
  }
}
```

### 2️⃣ Dentro de Adapter (keygo-infra)

```
sendEmail(SendEmailCommand cmd)
  ↓
resolveStrategy(cmd)              // "email-validation" → EmailValidationStrategy
  ↓
renderTemplate(strategy, cmd)      // Thymeleaf: process("email-validation", context)
  ↓
sendMimeMessage(strategy, html)    // JavaMailSender: send(mimeMessage)
```

### 3️⃣ Resultado

```
EMAIL SENT ✅
  To: user@example.com
  From: noreply@keygo.io ("KeyGo - Verificación")
  Subject: Verifica tu email en KeyGo
  Body: <HTML renderizado con variables>
```

---

## 🧪 Testing por nivel

### Level 1: Unit Test — Strategy

```java
@Test
void emailValidationStrategyExtractsVariablesCorrectly() {
  var cmd = SendEmailCommand.builder()
    .emailType("email-validation")
    .recipientName("Alice")
    .variables(Map.of("verificationCode", "123456"))
    .build();
  
  var strategy = new EmailValidationStrategy(cmd);
  
  assertThat(strategy.getTemplateName()).isEqualTo("email-validation");
  assertThat(strategy.getTemplateVariables()).containsEntry("code", "123456");
}
```

### Level 2: Integration Test — Template Rendering

```java
@SpringBootTest
class ThymeleafTemplateRenderingTest {
  
  @Autowired private TemplateEngine emailTemplateEngine;
  
  @Test
  void shouldRenderEmailValidationTemplate() {
    var context = new Context();
    context.setVariable("userName", "Bob");
    context.setVariable("code", "XYZ789");
    
    String html = emailTemplateEngine.process("email-validation", context);
    
    assertThat(html)
      .contains("Bob")
      .contains("XYZ789")
      .doesNotContain("${");  // Variables renderizadas
  }
}
```

### Level 3: E2E Test — Full Flow

```java
@SpringBootTest
class EmailNotificationE2ETest {
  
  @Autowired private EmailNotificationPort emailPort;
  @Autowired private WiserServer smtpServer;  // Mock SMTP
  
  @Test
  void shouldSendValidationEmailEndToEnd() {
    var cmd = SendEmailCommand.builder()
      .emailType("email-validation")
      .recipientEmail("test@example.com")
      .recipientName("Charlie")
      .variables(Map.of("verificationCode", "111222"))
      .build();
    
    emailPort.sendEmail(cmd);
    
    assertThat(smtpServer.getMessages())
      .hasSize(1)
      .anySatisfy(msg -> {
        assertThat(msg.getEnvelopeReceiver()).contains("test@example.com");
        assertThat(msg.getMimeMessage().getContent().toString())
          .contains("111222");
      });
  }
}
```

---

## 📚 Referencias cruzadas

| Documento | Contenido | Para quién |
|---|---|---|
| [`EMAIL_TEMPLATES_QUICKSTART.md`](EMAIL_TEMPLATES_QUICKSTART.md) | 5 pasos quick-start | Devs que quieren empezar YA |
| [`EMAIL_TEMPLATES_THYMELEAF.md`](EMAIL_TEMPLATES_THYMELEAF.md) | Arquitectura completa + paso a paso | Devs, Tech Leads |
| [`EMAIL_PATTERNS_ANALYSIS.md`](EMAIL_PATTERNS_ANALYSIS.md) | Comparativa patterns, best practices industry | Architects, Senior Devs |
| `AGENTS.md` § Email notifications | Referencias en quick-start | AI agents, team |
| `docs/ai/lecciones.md` | Lecciones aplicadas al proyecto | Future developers |

---

## 🚀 Extensión: Agregar nuevo tipo de email

### Paso 1: Crear Strategy

```java
// keygo-infra/.../strategy/NewEmailTypeStrategy.java

public class NewEmailTypeStrategy extends EmailStrategy {
  @Override public String getTemplateName() { return "new-email-type"; }
  @Override public String getSubject() { return "Subject"; }
  @Override public String getFromAddress() { return "noreply@keygo.io"; }
  @Override public String getFromName() { return "KeyGo"; }
  @Override public Map<String, Object> getTemplateVariables() { /* ... */ }
}
```

### Paso 2: Registrar en Adapter

```java
// EmailNotificationAdapter.resolveStrategy()

case "new-email-type" -> new NewEmailTypeStrategy(cmd);
```

### Paso 3: Crear Template

```html
<!-- templates/email/new-email-type.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
  <!-- Your HTML -->
</body>
</html>
```

### Paso 4: Usar desde UseCase

```java
var emailCmd = SendEmailCommand.builder()
  .emailType("new-email-type")
  .recipientEmail(...)
  .variables(...)
  .build();

emailNotificationPort.sendEmail(emailCmd);
```

✅ **Done!** No necesita cambios en Framework, Config, o Beans.

---

## ⚙️ Configuración SMTP por ambiente

### Local (Desarrollo con Mailhog)

```yaml
# application-local.yml
spring:
  mail:
    host: localhost
    port: 1025
    # Sin credenciales

keygo:
  email:
    template-cache-enabled: false  # Hot reload
```

**Setup:**
```bash
# Terminal 1: Iniciar Mailhog
docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Terminal 2: Ver emails en http://localhost:8025
```

### Staging (SendGrid)

```yaml
# application-desa.yml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

keygo:
  email:
    template-cache-enabled: true
```

### Production (SendGrid o SES)

```yaml
# application-prod.yml
spring:
  mail:
    host: smtp.sendgrid.net  # o: email-smtp.REGION.amazonaws.com (SES)
    port: 587
    username: apikey  # o: AWS SES user
    password: ${MAIL_PASSWORD}  # o: AWS SES password
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.timeout: 10000

keygo:
  email:
    template-cache-enabled: true
```

---

## 🔗 Próximos pasos (Roadmap)

| Horizonte | Feature | Esfuerzo |
|---|---|---|
| **Corto plazo** | Agregar 5+ templates (confirmation, billing, alerts) | 🟢 Bajo |
| **Corto plazo** | Multilingual templates (es/en/fr) | 🟢 Bajo |
| **Mediano plazo** | Async email sending (@Async) | 🟡 Medio |
| **Mediano plazo** | Retry logic + Circuit breaker (Resilience4j) | 🟡 Medio |
| **Largo plazo** | Email tracking + analytics (pixel, clicks) | 🔴 Alto |
| **Largo plazo** | Email template builder UI (WYSIWYG) | 🔴 Alto |

---

## 🆘 Troubleshooting

| Problema | Solución |
|---|---|
| `Template not found: email-validation` | Verificar archivo existe: `src/main/resources/templates/email/email-validation.html` |
| `${userName}` no se renderiza | Agregar `xmlns:th="http://www.thymeleaf.org"` en `<html>` tag |
| `@Bean named 'emailTemplateEngine' not found` | Verificar `@Bean(name = "emailTemplateEngine")` en `ThymeleafTemplateConfig` |
| SMTP connection timeout | Verificar `host`/`port` en `application.yml` (usar Mailhog `localhost:1025` en dev) |
| Email no se envía (silencioso) | Habilitar logging: `logging.level.org.springframework.mail: DEBUG` |
| XSS en template | NO usar `th:utext` — usar `th:text` (escapa HTML) |

---

**Última actualización:** 2026-04-03  
**Versión:** 1.0


