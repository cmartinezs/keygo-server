# Guía de Implementación Rápida — Email Templates en KeyGo

> **Documento operativo:** Pasos concretos para implementar emails con Thymeleaf en KeyGo Server.  
> **Para:** Desarrolladores que quieren empezar YA.  
> **Tiempo estimado:** 1-2 horas (incluye tests).

---

## ⚡ Quick Start (5 pasos)

### 1️⃣ Agregar dependencia

```bash
# En keygo-run/pom.xml, buscar </dependencies> y agregar ANTES:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

Luego:
```bash
./mvnw clean install
```

### 2️⃣ Crear archivo de configuración Thymeleaf

**Archivo:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ThymeleafTemplateConfig.java`

```java
package io.cmartinezs.keygo.run.config;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafTemplateConfig {

  @Bean(name = "emailTemplateEngine")
  public TemplateEngine emailTemplateEngine() {
    var templateEngine = new SpringTemplateEngine();
    var resolver = new ClassLoaderTemplateResolver();
    resolver.setResolvablePatterns(Collections.singleton("html/*"));
    resolver.setPrefix("templates/email/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding(StandardCharsets.UTF_8.toString());
    resolver.setCacheable(true);
    templateEngine.addTemplateResolver(resolver);
    return templateEngine;
  }
}
```

### 3️⃣ Crear primer template HTML

**Archivo:** `keygo-run/src/main/resources/templates/email/test-email.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
    <meta charset="UTF-8" />
    <title>Email de prueba</title>
</head>
<body>
    <h1>Hola <span th:text="${userName}">Usuario</span>!</h1>
    <p>Tu código de verificación es: <strong th:text="${code}">123456</strong></p>
    <p>Expira en <span th:text="${minutes}">30</span> minutos.</p>
</body>
</html>
```

### 4️⃣ Inyectar TemplateEngine en tu servicio de email

```java
// En donde envíes emails (ej: SmtpEmailNotificationAdapter)

@RequiredArgsConstructor
public class SmtpEmailNotificationAdapter {

  private final TemplateEngine emailTemplateEngine;  // ← Inyectar bean "emailTemplateEngine"
  private final JavaMailSender mailSender;

  public void sendTestEmail(String to, String userName, String code) {
    // 1. Crear contexto Thymeleaf
    var context = new Context();
    context.setVariable("userName", userName);
    context.setVariable("code", code);
    context.setVariable("minutes", 30);

    // 2. Renderizar template
    String htmlContent = emailTemplateEngine.process("test-email", context);

    // 3. Enviar
    sendMimeMessage(to, htmlContent);
  }

  private void sendMimeMessage(String to, String htmlContent) throws Exception {
    var message = mailSender.createMimeMessage();
    var helper = new MimeMessageHelper(message, "UTF-8");
    helper.setTo(to);
    helper.setFrom("noreply@keygo.io", "KeyGo");
    helper.setSubject("Email de prueba");
    helper.setText(htmlContent, true);  // ← true = HTML mode
    mailSender.send(message);
  }
}
```

### 5️⃣ Configurar SMTP en application.yml

**Local (desarrollo con Mailhog/MailDev):**

```yaml
# keygo-run/src/main/resources/application-local.yml

spring:
  mail:
    host: localhost
    port: 1025
    properties:
      mail.smtp.auth: false
```

**Production (SendGrid, AWS SES, etc.):**

```yaml
# keygo-run/src/main/resources/application-prod.yml

spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

---

## 🧪 Verificar que funciona

```bash
# 1. Iniciar app
./mvnw spring-boot:run -pl keygo-run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# 2. En logs, buscar:
# ✅ "Started KeyGoServerApplication"
# ✅ "emailTemplateEngine"

# 3. Prueba unitaria rápida
```

**Archivo de test:** `keygo-run/src/test/java/io/cmartinezs/keygo/run/config/ThymeleafTemplateConfigTest.java`

```java
@SpringBootTest
class ThymeleafTemplateConfigTest {

  @Autowired private TemplateEngine emailTemplateEngine;

  @Test
  void shouldRenderTestEmailTemplate() {
    // Given
    var context = new Context();
    context.setVariable("userName", "Juan");
    context.setVariable("code", "ABC123");
    context.setVariable("minutes", 30);

    // When
    String html = emailTemplateEngine.process("test-email", context);

    // Then
    assertThat(html)
        .contains("Hola Juan!")
        .contains("ABC123")
        .contains("30 minutos");
  }
}
```

```bash
./mvnw test -pl keygo-run -Dtest=ThymeleafTemplateConfigTest
```

---

## 🏗️ Estructura final

```
keygo-run/
├── src/main/
│   ├── java/io/cmartinezs/keygo/run/config/
│   │   ├── ApplicationConfig.java
│   │   └── ThymeleafTemplateConfig.java  ← NUEVO
│   └── resources/
│       ├── application.yml
│       ├── application-local.yml
│       └── templates/
│           └── email/  ← NUEVA CARPETA
│               ├── test-email.html
│               ├── email-validation.html
│               └── password-recovery.html
└── pom.xml  ← ACTUALIZADO
```

---

## 📝 Agregar más templates

Para cada tipo de email, crear un archivo `.html`:

**`templates/email/email-validation.html`:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <h2>Verifica tu email</h2>
    <p>Código: <code th:text="${verificationCode}">123456</code></p>
    <p><a th:href="${verificationLink}">O haz clic aquí</a></p>
</body>
</html>
```

**En tu adapter, renderizar así:**

```java
String html = emailTemplateEngine.process("email-validation", context);
```

---

## ✅ Checklist

- [ ] `spring-boot-starter-thymeleaf` agregado en pom.xml
- [ ] `ThymeleafTemplateConfig.java` creado
- [ ] Carpeta `templates/email/` existe
- [ ] Al menos 1 template `.html` creado
- [ ] Test pass: `./mvnw test -pl keygo-run`
- [ ] App inicia sin errores: `./mvnw spring-boot:run -pl keygo-run`

---

## 🚀 Próximo: Strategy Pattern (opcional pero recomendado)

Cuando tengas 3+ tipos de emails, organiza con strategy:

```java
interface EmailStrategy {
  String getTemplateName();  // "email-validation"
  String getSubject();       // "Verifica tu email"
  Map<String, Object> getVariables();  // variables para template
}

class EmailValidationStrategy implements EmailStrategy {
  public String getTemplateName() { return "email-validation"; }
  public String getSubject() { return "Verifica tu email"; }
  // ...
}

// Uso:
EmailStrategy strategy = new EmailValidationStrategy(cmd);
String html = emailTemplateEngine.process(strategy.getTemplateName(), context);
```

Ver documento completo en `docs/design/EMAIL_TEMPLATES_THYMELEAF.md` para detalles.

---

## 🆘 Troubleshooting

| Problema | Solución |
|---|---|
| `Template not found: test-email` | Verificar archivo existe en `src/main/resources/templates/email/test-email.html` |
| `${userName}` no se renderiza | Agregar `xmlns:th="http://www.thymeleaf.org"` en `<html>` tag |
| SMTP connection timeout | Verificar `host` / `port` en `application.yml` (usar Mailhog en local) |
| `@Bean named 'emailTemplateEngine' not found` | Verificar `@Bean(name = "emailTemplateEngine")` en `ThymeleafTemplateConfig` |

---

## 📚 Recursos

- [Thymeleaf official docs](https://www.thymeleaf.org/documentation.html)
- [Spring Boot Mail](https://spring.io/guides/gs/sending-email/)
- [Mailhog para testing local](https://github.com/mailhog/MailHog)
- Documento completo: `docs/design/EMAIL_TEMPLATES_THYMELEAF.md`


