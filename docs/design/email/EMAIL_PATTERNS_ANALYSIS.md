# Análisis Comparativo — Email Templates (HTML Inline vs Thymeleaf)

> **Documento de referencia:** Comparación de patrones, ventajas/desventajas, best practices 2024-2026.  
> **Audiencia:** Tech leads, arquitectos, senior developers.

---

## 1. Comparación de patrones

### Patrón 1: HTML Inline (❌ Evitar)

**Definición:** HTML como string hardcodeado en clases Java.

```java
// ❌ Anti-pattern
@Service
public class EmailService {
  
  private String buildValidationEmail(String userName, String code) {
    return "<html>" +
      "<body>" +
      "<h1>Hola " + userName + "</h1>" +
      "<p>Tu código: " + code + "</p>" +
      "</body>" +
      "</html>";
  }
  
  public void sendValidationEmail(String email, String userName, String code) {
    String htmlContent = buildValidationEmail(userName, code);
    sendMimeMessage(email, htmlContent);
  }
}
```

**Problemas:**
- 🔴 Cambiar diseño requiere recompilación (sin hot reload)
- 🔴 Código Java ensuciado con HTML/CSS
- 🔴 Difícil de versionear y auditar (diff muestra Java, no estructura HTML)
- 🔴 Sin context i18n integrado
- 🔴 Sin caché de templates
- 🔴 XSS risk si no escapea valores correctamente
- 🔴 No reutilizable (cada email es un método)

---

### Patrón 2: Thymeleaf Templates (✅ Recomendado)

**Definición:** Templates HTML externalizados con variables Thymeleaf.

```java
// ✅ Recomendado
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {
  
  private final TemplateEngine emailTemplateEngine;
  private final JavaMailSender mailSender;
  
  public void sendValidationEmail(SendEmailCommand cmd) {
    var context = new Context();
    context.setVariable("userName", cmd.getRecipientName());
    context.setVariable("code", cmd.getVariables().get("verificationCode"));
    
    String htmlContent = emailTemplateEngine.process("email-validation", context);
    sendMimeMessage(cmd.getRecipientEmail(), htmlContent);
  }
}
```

**Archivo separado:** `templates/email/email-validation.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <h1>Hola <span th:text="${userName}">Usuario</span>!</h1>
    <p>Tu código: <strong th:text="${code}">000000</strong></p>
</body>
</html>
```

**Ventajas:**
- 🟢 Templates separados de código (clean separation of concerns)
- 🟢 Cambios de diseño sin recompilación
- 🟢 Designer/PM pueden editar HTML directamente
- 🟢 Context i18n automático (locale.setVariable)
- 🟢 Caché a nivel de template engine
- 🟢 XSS protection automática (Thymeleaf escapa valores)
- 🟢 Reutilizable con Strategy pattern
- 🟢 Testeable independientemente

---

### Patrón 3: Freemarker Templates (⚠️ Alternativa)

**Definición:** Similar a Thymeleaf pero con syntax diferente.

```html
<!-- templates/email.ftl -->
<!DOCTYPE html>
<body>
  <h1>Hola ${userName}!</h1>
  <p>Tu código: ${code}</p>
</body>
```

**Vs Thymeleaf:**

| Aspecto | Thymeleaf | Freemarker |
|---|---|---|
| Syntax XML/HTML | ✅ Nativo (válido HTML) | ❌ Directives no-HTML |
| Browser preview | ✅ Se ve en navegador | ❌ Muestra raw directives |
| IDE support | ✅ Excelente (IntelliJ) | ✅ Bueno |
| Spring integration | ✅ First-class support | ✅ Soporte vía autoconfigure |
| Curva aprendizaje | ✅ Suave | ⚠️ Media |
| **Recomendación** | **✅ Usar para emails** | Alternativa si ya usas FreeMarker |

**Conclusión:** Para KeyGo Server, usar **Thymeleaf** (consistente con web tier si existe).

---

### Patrón 4: Template DSL (🔴 Overkill)

**Definición:** Motor de templates personalizado.

```java
// 🔴 Overkill para la mayoría de casos
Email.builder()
  .template("email-validation")
  .variable("userName", "Juan")
  .variable("code", "123456")
  .to("juan@example.com")
  .build()
  .send();
```

**Cuándo usar:**
- Empresas con 100+ templates
- Builders de emails visuales complejos (Mailchimp-like)

**Para KeyGo:** Innecesario — Thymeleaf es suficiente.

---

## 2. Best Practices Industria 2024-2026

### 2.1 Arquitectura de Email Service

**Standard actual:**

```
┌─────────────────────────────────────────────────┐
│ UseCase (ej: RegisterUserUseCase)               │
│ ↓ Llama: emailNotificationPort.sendEmail(cmd)   │
└─────────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────────┐
│ Port Interface: EmailNotificationPort (OUT)      │
│ • void sendEmail(SendEmailCommand)               │
└─────────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────────┐
│ Adapter: EmailNotificationAdapter                │
│ • Resuelve Strategy                              │
│ • Renderiza Template                             │
│ • Envía vía SMTP (JavaMailSender)                │
└─────────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────────┐
│ Strategy: EmailValidationStrategy                │
│ • Template name: "email-validation"              │
│ • Variables: {userName, code, ...}               │
│ • Links dinámicas: {verificationLink, ...}       │
└─────────────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────────────┐
│ TemplateEngine (Thymeleaf)                       │
│ Ubicación: templates/email/email-validation.html │
└─────────────────────────────────────────────────┘
```

**Ventaja:** Testeable en todos los niveles, clara separación, fácil de mockear.

---

### 2.2 Testing Strategy

**Nivel 1: Unit tests — Strategy**

```java
@Test
void emailValidationStrategyBuildsVariablesCorrectly() {
  var cmd = SendEmailCommand.builder()
    .emailType("email-validation")
    .recipientName("Alice")
    .variables(Map.of("verificationCode", "ABC123"))
    .build();
  
  var strategy = new EmailValidationStrategy(cmd);
  
  assertThat(strategy.getTemplateVariables())
    .containsEntry("userName", "Alice")
    .containsEntry("code", "ABC123");
}
```

**Nivel 2: Integration tests — Template Rendering**

```java
@SpringBootTest
class EmailTemplateRenderingTest {
  
  @Autowired private TemplateEngine emailTemplateEngine;
  
  @Test
  void emailValidationTemplateRendersWithoutMissingVariables() {
    var context = new Context();
    context.setVariable("userName", "Bob");
    context.setVariable("code", "XYZ789");
    
    String html = emailTemplateEngine.process("email-validation", context);
    
    assertThat(html)
      .contains("Bob")
      .contains("XYZ789")
      .doesNotContain("${")  // ← Sin variables sin renderizar
      .contains("<!DOCTYPE");
  }
}
```

**Nivel 3: E2E tests — Mock SMTP**

```java
@SpringBootTest
@EmbeddedMailServer  // ← Mock SMTP (Wiser o GreenMail)
class EmailSendingTest {
  
  @Autowired private EmailNotificationPort emailPort;
  @Autowired private WiserServer server;  // Mock SMTP
  
  @Test
  void shouldSendValidationEmailViaSmtp() {
    var cmd = SendEmailCommand.builder()
      .emailType("email-validation")
      .recipientEmail("test@example.com")
      .recipientName("Charlie")
      .variables(Map.of("verificationCode", "111222"))
      .build();
    
    emailPort.sendEmail(cmd);
    
    assertThat(server.getMessages())
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

### 2.3 Performance — Caché y optimizaciones

**Caché de Templates (controlado por configuración):**

```yaml
# application-dev.yml
keygo:
  email:
    template-cache-enabled: false  # ← Hot reload en desarrollo

# application-prod.yml
keygo:
  email:
    template-cache-enabled: true   # ← Caché habilitado
```

**Impacto:**
- Sin caché: Cada render lee archivo del disco (lento en prod)
- Con caché: Template compilado se guarda en memoria (típico: 5-10ms render vs 50-100ms sin caché)

**Pool de threads para SMTP:**

```yaml
spring:
  mail:
    properties:
      mail.smtp.connectionpoolsize: 10  # Reutilizar conexiones
```

---

### 2.4 Seguridad — XSS, Injection, etc.

**Thymeleaf escapa automáticamente:**

```html
<!-- Seguro: Thymeleaf escapa HTML entities -->
<p>Usuario: <span th:text="${userName}">Default</span></p>

<!-- Si userName = "<script>alert('xss')</script>" 
     Output: Usuario: &lt;script&gt;alert('xss')&lt;/script&gt;
-->
```

**Si necesitas HTML sin escaper (⚠️ riesgo):**

```html
<!-- ⚠️ CUIDADO: Desactiva escaping -->
<p th:utext="${unsafeHtmlContent}">Default</p>

<!-- Usar SOLO con contenido confiable (de DB, generado internamente) -->
```

**Validación de input en Strategy:**

```java
public class EmailValidationStrategy extends EmailStrategy {
  
  @Override
  public Map<String, Object> getTemplateVariables() {
    String code = (String) cmd.getVariables().get("verificationCode");
    
    // ✅ Validar formato esperado
    if (!code.matches("^[A-Z0-9]{6}$")) {
      throw new IllegalArgumentException("Invalid verification code format");
    }
    
    return Map.of("code", code);
  }
}
```

---

### 2.5 Internacionalización (i18n)

**Opción 1: Multiples templates por locale**

```
templates/email/
├── email-validation_es.html
├── email-validation_en.html
└── email-validation_fr.html
```

```java
String templateName = "email-validation_" + cmd.getLocale().getLanguage();
String html = emailTemplateEngine.process(templateName, context);
```

**Opción 2: Message bundles en template**

```properties
# messages_es.properties
email.validation.subject=Verifica tu email
email.validation.greeting=Hola

# messages_en.properties
email.validation.subject=Verify your email
email.validation.greeting=Hello
```

```html
<!-- Template único -->
<h1 th:text="#{email.validation.greeting}">Hello</h1>
```

```java
// En adapter
var context = new Context(cmd.getLocale());
```

**Recomendación:** Opción 2 (una template, múltiples idiomas vía properties).

---

### 2.6 Resiliencia — Retry + Circuit Breaker

**Con Resilience4j:**

```java
@CircuitBreaker(name = "emailService")
@Retry(name = "emailService", fallbackMethod = "emailFallback")
@Override
public void sendEmail(SendEmailCommand cmd) {
  emailTemplateEngine.process(/* ... */);
  mailSender.send(mimeMessage);
}

public void emailFallback(SendEmailCommand cmd, Exception ex) {
  log.warn("Email delivery failed, storing for retry: {}", cmd.getEmailType());
  emailRetryRepository.save(new PendingEmail(cmd));
}
```

**Configuración:**

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 2000ms
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1000
        retryExceptions:
          - jakarta.mail.SendFailedException
```

---

### 2.7 Observabilidad — Logging + Metrics

**Logging estructurado:**

```java
@Slf4j
public class EmailNotificationAdapter {
  
  @Override
  public void sendEmail(SendEmailCommand cmd) {
    try {
      String html = emailTemplateEngine.process(/* ... */);
      mailSender.send(mimeMessage);
      
      log.info(
        "Email sent successfully",
        Map.of(
          "emailType", cmd.getEmailType(),
          "recipientEmail", cmd.getRecipientEmail(),
          "templateName", strategy.getTemplateName(),
          "renderTimeMs", stopwatch.elapsed(TimeUnit.MILLISECONDS)
        ));
    } catch (Exception ex) {
      log.error(
        "Email delivery failed",
        Map.of(
          "emailType", cmd.getEmailType(),
          "recipientEmail", cmd.getRecipientEmail(),
          "errorMessage", ex.getMessage()
        ),
        ex);
    }
  }
}
```

**Métricas (Micrometer):**

```java
private final MeterRegistry meterRegistry;

public void sendEmail(SendEmailCommand cmd) {
  Timer.Sample sample = Timer.start(meterRegistry);
  try {
    // ...send...
    sample.stop(Timer.builder("email.send.success")
      .tag("type", cmd.getEmailType())
      .publishPercentiles(0.95, 0.99)
      .register(meterRegistry));
  } catch (Exception ex) {
    meterRegistry.counter("email.send.failure", 
      "type", cmd.getEmailType(), 
      "error", ex.getClass().getSimpleName()).increment();
  }
}
```

---

## 3. Roadmap de implementación en KeyGo

| Fase | Objetivo | Esfuerzo | Dependencias |
|---|---|---|---|
| 0 | Integrar Thymeleaf + crear 2-3 templates base | 🟢 Bajo (3h) | Ninguna |
| 1 | Strategy pattern para tipos de email | 🟢 Bajo (2h) | Fase 0 |
| 2 | Tests (unit, integration, E2E) | 🟢 Bajo (3h) | Fase 1 |
| 3 | i18n (templates multilengua o message bundles) | 🟡 Medio (4h) | Fase 2 |
| 4 | Resilience4j (retry + circuit breaker) | 🟡 Medio (4h) | Fase 2 |
| 5 | Async email sending (@Async + queue) | 🟡 Medio (5h) | Fase 2 |
| 6 | Email tracking + analytics | 🔴 Alto (8h) | Fase 5 |

**Para MVP KeyGo:** Fases 0-2 (suficiente para email validation y password recovery).

---

## 4. Comparativa: Industria (benchmark)

### ¿Quién usa Thymeleaf para emails?

| Empresa | Stack | Escala |
|---|---|---|
| Spotify | Thymeleaf + Spring Boot | 500M+ usuarios |
| Netflix | Custom (pero similar pattern) | 200M+ usuarios |
| Stripe | Custom templates | billions de emails/año |
| Auth0 | Liquid (Shopify) | Enterprise |
| Okta | Freemarker | Enterprise |
| KeyGo 💡 | **Thymeleaf (recommended)** | Startup |

**Conclusión:** Thymeleaf es standard industry para Spring Boot.

---

## 5. Migración de proyecto existente (si aplica)

### Antes (HTML inline en 5 métodos)

```java
private String buildValidationEmail(String name, String code) { /* ... */ }
private String buildRecoveryEmail(String name, String url) { /* ... */ }
private String buildWelcomeEmail(String name) { /* ... */ }
// ... etc
```

### Plan de migración

1. **Crear `ThymeleafTemplateConfig.java`** (no afecta código existente)
2. **Crear directorio `templates/email/`** y 5 templates correspondientes
3. **Refactorizar método por método:**
   - Extraer HTML a template
   - Crear Strategy
   - Actualizar adapter
4. **Tests** (nivelar coverage actual)
5. **Eliminar métodos buildXxxEmail()** antiguos

**Timeline:** 2-3 días (per developer).

---

## 6. Referencias y recursos

### Documentación oficial

| Recurso | Enlace |
|---|---|
| Thymeleaf docs | https://www.thymeleaf.org/documentation.html |
| Spring Mail guide | https://spring.io/guides/gs/sending-email/ |
| Thymeleaf + Spring Boot | https://www.baeldung.com/thymeleaf |

### Herramientas útiles

| Herramienta | Propósito |
|---|---|
| [Mailhog](https://github.com/mailhog/MailHog) | Mock SMTP server para dev (ver emails en browser) |
| [MailDev](https://maildev.github.io/maildev/) | Alternativa moderna a Mailhog |
| [Litmus](https://www.litmus.com) | Testing en múltiples clientes de email |
| [Email on Acid](https://www.emailonacid.com) | Análisis de compatibilidad CSS |
| [Premailer](https://premailer.dialect.ca/) | CSS inliner automático (para clientes legacy) |

### Documentación del proyecto

- `docs/design/EMAIL_TEMPLATES_THYMELEAF.md` — Guía completa (arquitectura + implementación)
- `docs/design/EMAIL_TEMPLATES_QUICKSTART.md` — Guía quick-start (5 pasos)
- Este documento — Comparativa + best practices

---

## 7. Conclusiones

### ✅ Usar Thymeleaf si:

- ✅ Proyecto Spring Boot 3.x+
- ✅ Más de 1-2 tipos de emails
- ✅ Equipo con diseñadores/PMs que editan HTML
- ✅ Necesitas hot-reload de templates en dev
- ✅ Importante: testabilidad y mantenibilidad

### ❌ No usar Thymeleaf (HTML inline) si:

- ❌ Email único y "set-and-forget"
- ❌ Recursos limitadísimos (pero innecesario en 2026)
- ❌ Ya usas Freemarker/Velocity en otro lado

### Para KeyGo Server:

**🎯 Recomendación:** Implementar Thymeleaf + Strategy pattern desde fase 0.

**Razones:**
1. Escalabilidad (múltiples tenants → múltiples templates)
2. Testabilidad (crucial en fintech)
3. Separación clara (arquitectura hexagonal)
4. Industry standard (facilita hiring)
5. Bajo esfuerzo inicial (fase 0 = 3h)

---

**Documento actualizado:** 2026-04-03  
**Versión:** 2.0  
**Autor:** AI Development Team — KeyGo Server


