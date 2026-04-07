# Email Templates con Thymeleaf — Spring Boot 4.x

> **Documento técnico:** Implementación de emails HTML usando templates Thymeleaf en lugar de HTML inline.
> 
> **Versión:** 1.0  
> **Fecha:** 2026-04-03  
> **Módulos:** `keygo-infra`, `keygo-run`

---

## Índice

1. [Análisis del patrón actual](#análisis-del-patrón-actual)
2. [Spring Boot 4.x + Jackson 3 considerations](#spring-boot-4x--jackson-3-considerations)
3. [Arquitectura del patrón Thymeleaf](#arquitectura-del-patrón-thymeleaf)
4. [Estructura de directorios](#estructura-de-directorios)
5. [Implementación paso a paso](#implementación-paso-a-paso)
6. [Ejemplos prácticos](#ejemplos-prácticos)
7. [Best practices industria 2024-2026](#best-practices-industria-2024-2026)
8. [Testing](#testing)
9. [Migración desde HTML inline](#migración-desde-html-inline)

---

## Análisis del patrón actual

### Fortalezas del ejemplo rescatado (Spring Boot 3.x)

El código analizado en `examples/java-mail/` implementa un patrón altamente profesional y mantenible:

| Aspecto | Implementación |
|---|---|
| **Separación de responsabilidades** | ✅ Templates en archivos `.html` (no strings Java) |
| **Configuración externalizada** | ✅ `EmailServiceProperties` (prefix `email`) vía `@ConfigurationProperties` |
| **Strategy pattern** | ✅ `EmailStrategy` interface + implementations (`EmailValidationStrategy`, `PasswordRecoveryEmailStrategy`) |
| **Generación dinámica de URLs** | ✅ `UriComponentsBuilder` (con path variables y query params) |
| **Localización (i18n)** | ✅ `LocaleContextHolder.getLocale()` integrado |
| **Validación de configuración** | ✅ `@Validated` + Jakarta Bean Validation (`@NotNull`, `@Size`, `@Min`/`@Max`) |
| **Encoding UTF-8** | ✅ Explícito en Thymeleaf + `MimeMessageHelper` |
| **Multipart MIME** | ✅ `MULTIPART_MODE_MIXED_RELATED` para embebidas (imágenes, etc.) |

### Limitaciones y oportunidades de mejora

| Limitación | Oportunidad |
|---|---|
| Configuración anidada compleja (3 niveles: `EmailServiceProperties` → `EmailDataData` → `EmailTemplateData`/`EmailSenderData`) | Simplificar YAML con alias YAML (`&anchors`, `*refs`) o usar `@Value` combinado |
| No hay manejo de errores explícito en sendEmail | Agregar Circuit Breaker (Resilience4j) y retry logic |
| Test coverage no visible | Implementar integration tests + unit tests con Thymeleaf mock |
| Caché de templates no controlado | Configurar `setCacheable()` según profile (`true` prod, `false` dev) |
| No hay soporte para templates multilengua en URL | Extender `EmailLinkData` con `locale` |

---

## Spring Boot 4.x + Jackson 3 considerations

### Cambios en versiones

| Aspecto | Spring Boot 3.x | Spring Boot 4.x |
|---|---|---|
| **Java mínimo** | 17 | 21 |
| **Thymeleaf versión** | 3.1.x | 3.2.x (compatible, sin cambios de API) |
| **Jakarta Mail** | ✅ jakarta.mail | ✅ jakarta.mail (sin cambios) |
| **Jackson namespace** | `com.fasterxml.jackson.*` | `tools.jackson.*` (databind/datatype) |
| **Spring Security** | 6.2.x | 7.x (SecurityFilterChain, sin WebSecurityConfigurerAdapter) |
| **Validation API** | Jakarta 3.0 | Jakarta 3.1 |

### Imports correctos para KeyGo Server (Spring Boot 4.x)

```java
// ✅ Correcto para Jackson 3.x (Spring Boot 4)
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

// ✅ Annotations: namespace NO cambió
import com.fasterxml.jackson.annotation.JsonInclude;

// ✅ Thymeleaf: sin cambios
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

// ✅ Jakarta (correcto desde SB 3.x)
import jakarta.mail.internet.InternetAddress;
import jakarta.validation.Valid;
```

---

## Arquitectura del patrón Thymeleaf

### Flujo end-to-end

```
┌─────────────────────────────────────────────────────────────┐
│ 1. UseCase llama port: SendEmailUseCase                     │
│    Input: SendEmailCommand (email, type, variables)         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Port interface (OUT): EmailNotificationPort              │
│    Método: void sendEmail(SendEmailCommand cmd)             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Adapter: EmailNotificationAdapter                        │
│    • Resuelve tipo → EmailStrategy                          │
│    • Lee configuración → EmailServiceProperties             │
│    • Renderiza template                                     │
│    • Envía vía JavaMailSender                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. EmailStrategy interface                                  │
│    Implementaciones: EmailValidationStrategy,               │
│                     PasswordRecoveryStrategy, etc.           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. ThymeleafTemplateEngine                                  │
│    Ubicación: src/main/resources/templates/email/           │
│    Cada template es un .html con variables Thymeleaf        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. Context (variables + links generadas)                    │
│    URIs renderizadas vía UriComponentsBuilder               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. MimeMessage con HTML renderizado                         │
│    Envío vía JavaMailSender (SMTP)                          │
└─────────────────────────────────────────────────────────────┘
```

### Componentes clave

| Componente | Paquete | Responsabilidad |
|---|---|---|
| `EmailNotificationPort` | `keygo-app/.../port/notification/` | Interface puerto OUT |
| `EmailNotificationAdapter` | `keygo-infra/.../adapter/notification/` | Implementa puerto; orquesta flujo |
| `EmailStrategy` | `keygo-infra/.../strategy/email/` | Estrategia por tipo de email |
| `ThymeleafTemplateConfig` | `keygo-run/.../config/` | Bean `TemplateEngine` para emails |
| `EmailServiceProperties` | `keygo-run/.../properties/email/` | Configuración YAML |
| Templates HTML | `keygo-run/src/main/resources/templates/email/` | Archivos `.html` con variables Thymeleaf |

---

## Estructura de directorios

```
keygo-infra/
├── src/main/java/io/cmartinezs/keygo/infra/
│   ├── adapter/
│   │   └── notification/
│   │       ├── EmailNotificationAdapter.java
│   │       └── strategy/
│   │           ├── EmailStrategy.java
│   │           ├── EmailValidationStrategy.java
│   │           ├── PasswordRecoveryEmailStrategy.java
│   │           └── EmailVerificationCodeExpiredStrategy.java
│   └── port/
│       └── notification/
│           └── EmailNotificationPort.java

keygo-run/
├── src/main/java/io/cmartinezs/keygo/run/
│   └── config/
│       ├── ApplicationConfig.java (actualizar: agregar @Bean para EmailNotificationAdapter)
│       └── ThymeleafTemplateConfig.java (nuevo)
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml
│   ├── application-desa.yml
│   ├── application-prod.yml
│   └── templates/
│       └── email/
│           ├── email-validation.html
│           ├── password-recovery.html
│           ├── email-verification-code-expired.html
│           └── _layout.html (layout base compartido)
```

---

## Implementación paso a paso

### Paso 1: Agregar Thymeleaf a dependencias

**Archivo:** `keygo-run/pom.xml`

```xml
<!-- En <dependencies>, agregar: -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<!-- Para emails, no necesitamos spring-boot-starter-web -->
<!-- Thymeleaf viene ligero sin resolver vistas web -->
```

**Notas:**
- `spring-boot-starter-thymeleaf` incluye `org.thymeleaf:thymeleaf` + `org.thymeleaf.extras:thymeleaf-extras-java8time` (timezone handling).
- Spring Boot configura automáticamente `ThymeleafAutoConfiguration`, pero nosotros sobrescribimos con `ThymeleafTemplateConfig` para emails.

### Paso 2: Crear interfaz puerto OUT

**Archivo:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/port/notification/EmailNotificationPort.java`

```java
package io.cmartinezs.keygo.infra.port.notification;

public interface EmailNotificationPort {

  /**
   * Envía un email basado en template Thymeleaf.
   *
   * @param cmd Comando con tipo de email, destinatario, variables
   * @throws EmailNotificationException si falla el envío
   */
  void sendEmail(SendEmailCommand cmd) throws EmailNotificationException;
}
```

**Archivo:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/port/notification/SendEmailCommand.java`

```java
package io.cmartinezs.keygo.infra.port.notification;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendEmailCommand {

  /**
   * Identificador del tipo de email (usado para resolver EmailStrategy).
   * Ejemplos: "email-validation", "password-recovery"
   */
  private String emailType;

  /** Email del destinatario */
  private String recipientEmail;

  /** Nombre del destinatario (para Subject y Template) */
  private String recipientName;

  /** Variables a pasar al template Thymeleaf */
  private Map<String, Object> variables;

  /** (Opcional) Mapa de links dinámicas: link-id → URI params */
  private Map<String, Map<String, Object>> uriParams;

  /** (Opcional) Locale para internacionalización */
  private java.util.Locale locale;
}
```

**Archivo:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/port/notification/EmailNotificationException.java`

```java
package io.cmartinezs.keygo.infra.port.notification;

public class EmailNotificationException extends RuntimeException {
  public EmailNotificationException(String message) {
    super(message);
  }

  public EmailNotificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
```

### Paso 3: Crear clase de configuración Thymeleaf

**Archivo:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ThymeleafTemplateConfig.java`

```java
package io.cmartinezs.keygo.run.config;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Configuración de Thymeleaf especializada para templates de email.
 *
 * <p>Nota: Esta config NO afecta la resolución de vistas web (si las hubiera). Crea un
 * TemplateEngine independiente para emails solamente.
 *
 * <p>Configuración:
 * - Prefix: `templates/email/`
 * - Suffix: `.html`
 * - TemplateMode: HTML
 * - Charset: UTF-8
 * - Caché: controlado por property `keygo.email.template-cache-enabled` (default: true en prod, false en dev)
 */
@Configuration
public class ThymeleafTemplateConfig {

  /**
   * Bean TemplateEngine dedicado para email templates.
   * 
   * Solo se crea si no existe otro bean TemplateEngine (lo que permite sobrescribir si es necesario).
   *
   * @param emailTemplateResolver Resolver configurado en método privado abajo
   * @return TemplateEngine con soporte Thymeleaf Spring 6 (Spring Boot 4.x)
   */
  @Bean(name = "emailTemplateEngine")
  @ConditionalOnMissingBean(name = "emailTemplateEngine")
  public TemplateEngine emailTemplateEngine(KeyGoEmailProperties emailProperties) {
    final var templateEngine = new SpringTemplateEngine();
    templateEngine.addTemplateResolver(emailTemplateResolver(emailProperties));
    return templateEngine;
  }

  /**
   * Resolver de templates para ubicación de email templates.
   *
   * @param emailProperties Configuración de email (incluye flag de caché)
   * @return ITemplateResolver configurado
   */
  private ITemplateResolver emailTemplateResolver(KeyGoEmailProperties emailProperties) {
    final var templateResolver = new ClassLoaderTemplateResolver();
    
    // Patrón de resolución: solo archivos en templates/email/
    templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
    
    // Ubicación base de templates
    templateResolver.setPrefix("templates/email/");
    templateResolver.setSuffix(".html");
    
    // Modo de template: HTML (no XML, no RAW)
    templateResolver.setTemplateMode(TemplateMode.HTML);
    
    // Encoding explícito
    templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.toString());
    
    // Caché controlado por configuración (false en dev, true en prod)
    templateResolver.setCacheable(emailProperties.isTemplateCacheEnabled());
    
    return templateResolver;
  }
}
```

**Archivo:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/KeyGoEmailProperties.java`

```java
package io.cmartinezs.keygo.run.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para email en KeyGo.
 *
 * En application.yml:
 * ```yaml
 * keygo:
 *   email:
 *     smtp-host: smtp.example.com
 *     smtp-port: 587
 *     template-cache-enabled: true  # ← controla caché Thymeleaf
 * ```
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "keygo.email")
public class KeyGoEmailProperties {

  @NotBlank
  private String smtpHost;

  private int smtpPort = 587;

  private boolean templateCacheEnabled = true;

  // Otras propiedades específicas de tu setup SMTP (user, password, timeout, etc.)
}
```

### Paso 4: Crear Strategy base y implementaciones

**Archivo:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/adapter/notification/strategy/EmailStrategy.java`

```java
package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.port.notification.SendEmailCommand;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Strategy base para tipos específicos de email.
 *
 * Cada estrategia:
 * 1. Define qué template usar
 * 2. Extrae/valida variables del comando
 * 3. Genera URIs dinámicas (si aplica)
 * 4. Proporciona sender info (from, subject)
 */
public abstract class EmailStrategy {

  protected final SendEmailCommand cmd;

  public EmailStrategy(SendEmailCommand cmd) {
    this.cmd = cmd;
  }

  /**
   * Nombre del template (sin extensión .html). Se resuelve como
   * `templates/email/{templateName}.html`.
   *
   * @return nombre template, ej: "email-validation"
   */
  public abstract String getTemplateName();

  /**
   * Subject del email. Puede incluir placeholders ${name}, ${code}, etc.
   *
   * @return subject, ej: "Valida tu email en ${appName}"
   */
  public abstract String getSubject();

  /**
   * Email From address.
   *
   * @return ej: "noreply@keygo.io"
   */
  public abstract String getFromAddress();

  /**
   * Nombre personal en From header.
   *
   * @return ej: "KeyGo Support"
   */
  public abstract String getFromName();

  /**
   * Variables Thymeleaf para renderizar template.
   * Todas las variables que aparecen en el .html deben estar aquí.
   *
   * @return mapa key → valor
   */
  public abstract Map<String, Object> getTemplateVariables();

  /**
   * URIs dinámicas que se insertan como variables en el template.
   * Ejemplo: `verificationLink` → construida con UriComponentsBuilder.
   *
   * @return mapa de nombre-variable → URI completa
   */
  public Map<String, String> getDynamicLinks() {
    return Map.of(); // Default: sin links dinámicas
  }

  /**
   * Helper: construir URI con protocolo, host, puerto y path.
   *
   * @param scheme ej: "https"
   * @param host ej: "app.keygo.io"
   * @param port ej: 443
   * @param path ej: "/verify-email/{code}"
   * @param pathVariables ej: {"code" → "ABC123"}
   * @return URI completa renderizada
   */
  protected String buildUri(
      String scheme,
      String host,
      int port,
      String path,
      Map<String, String> pathVariables) {
    var builder =
        UriComponentsBuilder.newInstance()
            .scheme(scheme)
            .host(host)
            .port(port)
            .path(path);

    if (pathVariables != null && !pathVariables.isEmpty()) {
      return builder.buildAndExpand(pathVariables).toUriString();
    }
    return builder.build().toUriString();
  }
}
```

**Archivo:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/adapter/notification/strategy/EmailValidationStrategy.java`

```java
package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.port.notification.SendEmailCommand;
import java.util.Map;

public class EmailValidationStrategy extends EmailStrategy {

  public EmailValidationStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "email-validation";
  }

  @Override
  public String getSubject() {
    return "Verifica tu email en KeyGo";
  }

  @Override
  public String getFromAddress() {
    return "noreply@keygo.io";
  }

  @Override
  public String getFromName() {
    return "KeyGo - Verificación";
  }

  @Override
  public Map<String, Object> getTemplateVariables() {
    return Map.of(
        "userName", cmd.getRecipientName(),
        "recipientEmail", cmd.getRecipientEmail(),
        "verificationCode", cmd.getVariables().get("verificationCode"),
        "expiresInMinutes", cmd.getVariables().getOrDefault("expiresInMinutes", 30));
  }

  @Override
  public Map<String, String> getDynamicLinks() {
    // Variables del comando: expectedKeys = {
    //   "verificationUrl" → {scheme, host, port, path, code}
    // }
    if (cmd.getUriParams() == null || cmd.getUriParams().isEmpty()) {
      return Map.of();
    }

    var urlParams = cmd.getUriParams().get("verificationUrl");
    if (urlParams == null) {
      return Map.of();
    }

    String verificationLink =
        buildUri(
            (String) urlParams.get("scheme"),
            (String) urlParams.get("host"),
            ((Number) urlParams.getOrDefault("port", 443)).intValue(),
            (String) urlParams.get("path"),
            Map.of("code", (String) cmd.getVariables().get("verificationCode")));

    return Map.of("verificationLink", verificationLink);
  }
}
```

### Paso 5: Crear Adapter de notificación

**Archivo:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/adapter/notification/EmailNotificationAdapter.java`

```java
package io.cmartinezs.keygo.infra.adapter.notification;

import io.cmartinezs.keygo.infra.adapter.notification.strategy.*;
import io.cmartinezs.keygo.infra.port.notification.*;
import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Adapter que implementa puerto EmailNotificationPort.
 *
 * Responsabilidades:
 * 1. Resolver EmailStrategy por tipo
 * 2. Renderizar template Thymeleaf
 * 3. Construir MimeMessage con HTML renderizado
 * 4. Enviar vía JavaMailSender
 * 5. Logging y manejo de errores
 *
 * Inyectado como @Bean en ApplicationConfig.
 */
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {

  private final JavaMailSender mailSender;

  /** TemplateEngine dedicado para emails (bean name = "emailTemplateEngine") */
  private final TemplateEngine emailTemplateEngine;

  @Override
  public void sendEmail(SendEmailCommand cmd) throws EmailNotificationException {
    try {
      // Paso 1: Resolver strategy según tipo
      var strategy = resolveStrategy(cmd);

      // Paso 2: Renderizar template
      String htmlContent = renderTemplate(strategy, cmd);

      // Paso 3: Preparar email y enviar
      sendMimeMessage(strategy, cmd, htmlContent);

      log.info("Email sent successfully: type={}, to={}", cmd.getEmailType(), cmd.getRecipientEmail());
    } catch (Exception ex) {
      log.error(
          "Failed to send email: type={}, to={}",
          cmd.getEmailType(),
          cmd.getRecipientEmail(),
          ex);
      throw new EmailNotificationException(
          "Failed to send email of type " + cmd.getEmailType(), ex);
    }
  }

  /**
   * Resuelve la estrategia según emailType.
   *
   * @param cmd comando con emailType
   * @return EmailStrategy apropiada
   * @throws EmailNotificationException si tipo no es reconocido
   */
  private EmailStrategy resolveStrategy(SendEmailCommand cmd) throws EmailNotificationException {
    return switch (cmd.getEmailType()) {
      case "email-validation" -> new EmailValidationStrategy(cmd);
      case "password-recovery" -> new PasswordRecoveryEmailStrategy(cmd);
      case "email-verification-code-expired" -> new EmailVerificationCodeExpiredStrategy(cmd);
      // Agregar más casos según necesidad
      default -> throw new EmailNotificationException(
          "Unknown email type: " + cmd.getEmailType());
    };
  }

  /**
   * Renderiza template Thymeleaf con variables de la estrategia.
   *
   * @param strategy EmailStrategy
   * @param cmd comando
   * @return HTML renderizado
   */
  private String renderTemplate(EmailStrategy strategy, SendEmailCommand cmd) {
    // Crear contexto Thymeleaf
    Locale locale = cmd.getLocale() != null ? cmd.getLocale() : Locale.getDefault();
    var context = new Context(locale);

    // Agregar variables del template
    strategy.getTemplateVariables().forEach(context::setVariable);

    // Agregar links dinámicas
    strategy.getDynamicLinks().forEach(context::setVariable);

    // Renderizar template
    return emailTemplateEngine.process(strategy.getTemplateName(), context);
  }

  /**
   * Construye y envía MimeMessage con contenido HTML.
   *
   * @param strategy EmailStrategy
   * @param cmd comando
   * @param htmlContent HTML renderizado
   * @throws Exception si falla el envío
   */
  @SneakyThrows
  private void sendMimeMessage(EmailStrategy strategy, SendEmailCommand cmd, String htmlContent) {
    var message = mailSender.createMimeMessage();
    var messageHelper =
        new MimeMessageHelper(
            message,
            MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
            StandardCharsets.UTF_8.name());

    // Headers
    messageHelper.setTo(cmd.getRecipientEmail());
    messageHelper.setFrom(new InternetAddress(strategy.getFromAddress(), strategy.getFromName()));
    messageHelper.setSubject(strategy.getSubject());

    // Contenido: HTML renderizado (isHtml=true)
    messageHelper.setText(htmlContent, true);

    // Envío
    mailSender.send(message);
  }
}
```

### Paso 6: Registrar Bean Adapter en ApplicationConfig

**Archivo:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ApplicationConfig.java`

Agregar en clase `ApplicationConfig`:

```java
// ...existing imports...
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import io.cmartinezs.keygo.infra.adapter.notification.EmailNotificationAdapter;
import io.cmartinezs.keygo.infra.port.notification.EmailNotificationPort;

@Configuration
public class ApplicationConfig {

  // ...existing beans...

  /**
   * Adapter para notificaciones vía email.
   * Depende de: JavaMailSender (autoconfigured vía spring-boot-starter-mail),
   *             TemplateEngine (bean "emailTemplateEngine" from ThymeleafTemplateConfig)
   */
  @Bean
  public EmailNotificationPort emailNotificationPort(
      JavaMailSender mailSender,
      TemplateEngine emailTemplateEngine) {
    return new EmailNotificationAdapter(mailSender, emailTemplateEngine);
  }
}
```

### Paso 7: Crear templates HTML

**Archivo:** `keygo-run/src/main/resources/templates/email/email-validation.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Verifica tu email</title>
    <style>
      body {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        line-height: 1.6;
        color: #333;
        background-color: #f4f4f4;
        margin: 0;
        padding: 0;
      }
      .container {
        max-width: 600px;
        margin: 20px auto;
        background-color: white;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      .header {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        padding: 30px;
        text-align: center;
      }
      .header h1 {
        margin: 0;
        font-size: 24px;
      }
      .content {
        padding: 30px;
      }
      .greeting {
        margin-bottom: 20px;
      }
      .code-box {
        background-color: #f9f9f9;
        border-left: 4px solid #667eea;
        padding: 15px;
        margin: 20px 0;
        font-size: 18px;
        font-weight: bold;
        font-family: 'Courier New', monospace;
        letter-spacing: 2px;
      }
      .button {
        display: inline-block;
        background-color: #667eea;
        color: white;
        padding: 12px 30px;
        text-decoration: none;
        border-radius: 4px;
        margin: 20px 0;
        font-weight: bold;
      }
      .button:hover {
        background-color: #764ba2;
      }
      .footer {
        background-color: #f4f4f4;
        padding: 20px;
        text-align: center;
        font-size: 12px;
        color: #666;
        border-top: 1px solid #ddd;
      }
      .expires {
        color: #e74c3c;
        font-size: 14px;
        margin-top: 15px;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1>Verifica tu email</h1>
        <p>¡Bienvenido a KeyGo!</p>
      </div>

      <!-- Contenido -->
      <div class="content">
        <div class="greeting">
          <p>Hola <strong th:text="${userName}">Usuario</strong>,</p>
          <p>
            Gracias por registrarte en KeyGo. Para completar tu cuenta, necesitamos verificar tu
            email.
          </p>
        </div>

        <p>Usa este código de verificación:</p>

        <!-- Código de verificación -->
        <div class="code-box" th:text="${verificationCode}">123456</div>

        <!-- O enlace directo (si disponible) -->
        <p th:if="${verificationLink != null}">
          O haz clic en el siguiente enlace para verificar directamente:
        </p>
        <p th:if="${verificationLink != null}">
          <a class="button" th:href="${verificationLink}">Verificar Email</a>
        </p>

        <!-- Información de expiración -->
        <div class="expires">
          ⏱️ Este código expira en
          <strong th:text="${expiresInMinutes}">30</strong>
          minutos.
        </div>

        <!-- Disclaimer -->
        <p style="margin-top: 30px; color: #999; font-size: 12px;">
          Si no solicitaste este email, simplemente ignóralo.
        </p>
      </div>

      <!-- Footer -->
      <div class="footer">
        <p>© 2026 KeyGo. Todos los derechos reservados.</p>
        <p th:text="'Email: ' + ${recipientEmail}">Email: user@example.com</p>
      </div>
    </div>
  </body>
</html>
```

**Archivo:** `keygo-run/src/main/resources/templates/email/password-recovery.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Restablecer contraseña</title>
    <style>
      /* Estilos idénticos a email-validation.html, solo cambiar colores si es necesario */
      body {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        line-height: 1.6;
        color: #333;
        background-color: #f4f4f4;
        margin: 0;
        padding: 0;
      }
      .container {
        max-width: 600px;
        margin: 20px auto;
        background-color: white;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      .header {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
        padding: 30px;
        text-align: center;
      }
      .header h1 {
        margin: 0;
        font-size: 24px;
      }
      .content {
        padding: 30px;
      }
      .code-box {
        background-color: #f9f9f9;
        border-left: 4px solid #f5576c;
        padding: 15px;
        margin: 20px 0;
        font-size: 18px;
        font-weight: bold;
        font-family: 'Courier New', monospace;
        letter-spacing: 2px;
      }
      .button {
        display: inline-block;
        background-color: #f5576c;
        color: white;
        padding: 12px 30px;
        text-decoration: none;
        border-radius: 4px;
        margin: 20px 0;
        font-weight: bold;
      }
      .button:hover {
        background-color: #e74c3c;
      }
      .footer {
        background-color: #f4f4f4;
        padding: 20px;
        text-align: center;
        font-size: 12px;
        color: #666;
        border-top: 1px solid #ddd;
      }
      .warning {
        background-color: #fff3cd;
        border-left: 4px solid #ffc107;
        padding: 15px;
        margin: 20px 0;
        color: #856404;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <!-- Header -->
      <div class="header">
        <h1>Restablecer contraseña</h1>
      </div>

      <!-- Contenido -->
      <div class="content">
        <p>Hola <strong th:text="${userName}">Usuario</strong>,</p>
        <p>
          Recibimos una solicitud para restablecer la contraseña de tu cuenta KeyGo. Si no solicitaste
          esto, puedes ignorar este email.
        </p>

        <p>Para continuar con el restablecimiento, usa este código:</p>

        <!-- Código -->
        <div class="code-box" th:text="${verificationCode}">123456</div>

        <!-- Enlace directo -->
        <p th:if="${recoveryLink != null}">
          O haz clic aquí para restablecer directamente:
        </p>
        <p th:if="${recoveryLink != null}">
          <a class="button" th:href="${recoveryLink}">Restablecer Contraseña</a>
        </p>

        <!-- Warning -->
        <div class="warning">
          <strong>⚠️ Por seguridad:</strong> Este enlace expira en <strong>1 hora</strong>. Si no
          completaste esta solicitud, tu cuenta es segura — simplemente ignora este email.
        </div>

        <p style="margin-top: 30px; color: #999; font-size: 12px;">
          Este email no contiene tu contraseña. Nunca compartas códigos o enlaces con terceros.
        </p>
      </div>

      <!-- Footer -->
      <div class="footer">
        <p>© 2026 KeyGo. Todos los derechos reservados.</p>
      </div>
    </div>
  </body>
</html>
```

### Paso 8: Configuración application.yml

**Archivo:** `keygo-run/src/main/resources/application.yml`

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
      mail.smtp.connectiontimeout: 5000
      mail.smtp.timeout: 5000
      mail.smtp.writetimeout: 5000

keygo:
  email:
    smtp-host: ${MAIL_HOST:smtp.gmail.com}
    smtp-port: ${MAIL_PORT:587}
    template-cache-enabled: true  # ← false en dev, true en prod
```

**Archivo:** `keygo-run/src/main/resources/application-local.yml` (development)

```yaml
spring:
  mail:
    host: localhost
    port: 1025  # ← Mailhog/MailDev en local
    # Sin usuario/password en local

keygo:
  email:
    smtp-host: localhost
    smtp-port: 1025
    template-cache-enabled: false  # No cachear en dev
```

**Archivo:** `keygo-run/src/main/resources/application-desa.yml` (staging)

```yaml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}

keygo:
  email:
    template-cache-enabled: true
```

---

## Ejemplos prácticos

### Ejemplo 1: Enviar email de validación desde UseCase

```java
// En keygo-app/.../usecase/user/RegisterUserUseCase.java

package io.cmartinezs.keygo.app.usecase.user;

import io.cmartinezs.keygo.app.port.user.SaveTenantUserPort;
import io.cmartinezs.keygo.infra.port.notification.EmailNotificationPort;
import io.cmartinezs.keygo.infra.port.notification.SendEmailCommand;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterUserUseCase {

  private final SaveTenantUserPort saveTenantUserPort;
  private final EmailNotificationPort emailNotificationPort;
  private final UserConfigPort userConfigPort;

  public void execute(RegisterUserCommand cmd) {
    // 1. Validar input
    // 2. Crear usuario (status PENDING)
    var user = TenantUser.builder()
        .email(cmd.getEmail())
        .username(cmd.getUsername())
        .status(UserStatus.PENDING)
        .verificationCode(generateCode())
        .build();

    saveTenantUserPort.save(user);

    // 3. Enviar email de verificación
    var emailCmd = SendEmailCommand.builder()
        .emailType("email-validation")
        .recipientEmail(user.getEmail())
        .recipientName(user.getUsername())
        .variables(Map.of(
            "verificationCode", user.getVerificationCode(),
            "expiresInMinutes", 30))
        .uriParams(Map.of(
            "verificationUrl", Map.of(
                "scheme", "https",
                "host", userConfigPort.getAppHost(),
                "port", 443,
                "path", "/verify-email/{code}")))
        .locale(cmd.getLocale())
        .build();

    emailNotificationPort.sendEmail(emailCmd);
  }

  private String generateCode() {
    return String.format("%06d", new Random().nextInt(1000000));
  }
}
```

### Ejemplo 2: Agregar nueva estrategia (email de confirmación de pago)

```java
// keygo-infra/.../strategy/PaymentConfirmationEmailStrategy.java

package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.port.notification.SendEmailCommand;
import java.util.Map;

public class PaymentConfirmationEmailStrategy extends EmailStrategy {

  public PaymentConfirmationEmailStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "payment-confirmation";
  }

  @Override
  public String getSubject() {
    return "Confirmación de pago - KeyGo";
  }

  @Override
  public String getFromAddress() {
    return "billing@keygo.io";
  }

  @Override
  public String getFromName() {
    return "KeyGo Billing";
  }

  @Override
  public Map<String, Object> getTemplateVariables() {
    return Map.of(
        "userName", cmd.getRecipientName(),
        "amount", cmd.getVariables().get("amount"),
        "currency", cmd.getVariables().getOrDefault("currency", "USD"),
        "transactionId", cmd.getVariables().get("transactionId"),
        "date", cmd.getVariables().get("date"));
  }
}
```

Luego **registrar** en `EmailNotificationAdapter.resolveStrategy()`:

```java
case "payment-confirmation" -> new PaymentConfirmationEmailStrategy(cmd);
```

---

## Best practices industria 2024-2026

### 1. **AMP para Email (Google AMP for Email)**

Soporte opcional para elementos interactivos en Gmail:

```html
<!-- Agregar en template (si destino soporta AMP4Email): -->
<script type="text/x-amp-component">
  <!-- Componentes AMP: amp-list, amp-form, etc. -->
</script>
```

**Ventaja:** Usuarios pueden interactuar sin salir del email (ej: confirmar acción).

**Riesgo:** Solo soportado en Gmail/Yahoo. Usar como enhancement, no requerimiento.

### 2. **Email Preview Text**

Agregar texto de preview (primeras líneas visibles en bandeja):

```html
<body>
  <div style="display: none; max-height: 0; overflow: hidden;">
    Este es el preview text que ven los usuarios en la bandeja de entrada...
  </div>
  <!-- Contenido real -->
</body>
```

### 3. **CSS Inline en Producción**

Algunos clientes de email (Outlook, etc.) no soportan `<style>` tags. Alternativa: **Inliner CSS automático**

```java
// Opción 1: Usar librería Premailer en template preprocessing
// Opción 2: Hacer inline CSS manualmente en templates complejos
<table width="100%" cellpadding="0" cellspacing="0" style="border: 1px solid #ddd;">
  <!-- content -->
</table>
```

**Recomendación:** Templates simples con colores, márgenes inline. Tests en múltiples clientes (Litmus, Email on Acid).

### 4. **Gestión de plantillas multilengua**

Extender framework para soportar i18n automático:

```java
// Opción 1: Usar resolver multiple por locale
getTemplateName() {
  String locale = cmd.getLocale().getLanguage(); // "es", "en", "fr"
  return "email-validation_" + locale; // → email-validation_es.html
}

// Opción 2: Usar message bundles en template
// En template: <p th:text="#{email.greeting}">Hola</p>
// En properties: email.greeting=Hola (es), email.greeting=Hello (en)
```

### 5. **Retry Logic + Circuit Breaker**

Agregar resiliencia para fallos SMTP transitorios:

```java
// Agregar dependencia: resilience4j-spring-boot3
@CircuitBreaker(name = "email", fallbackMethod = "emailFallback")
@Retry(name = "email")
@Override
public void sendEmail(SendEmailCommand cmd) {
  // ... sendMimeMessage ...
}

private void emailFallback(SendEmailCommand cmd, Exception ex) {
  log.warn("Email delivery failed, queuing for retry: {}", cmd.getEmailType());
  // Guardar en base de datos para retry asíncrono
}
```

### 6. **Tracking y Analytics**

Agregar tracking pixels + link rewrites para métricas:

```html
<!-- Pixel tracking (Gmail rechaza, pero otros clientes lo soportan) -->
<img src="https://track.keygo.io/open?id=ABC123&email=user%40example.com" 
     width="1" height="1" alt="" style="display: none;" />

<!-- Click tracking (reescribir links) -->
<!-- Original: <a href="https://app.keygo.io/verify">Verificar</a> -->
<!-- Reescrito: <a href="https://track.keygo.io/click?id=ABC123&url=...">Verificar</a> -->
```

### 7. **Validación de templates antes de envío**

Test unitario para cada template:

```java
@Test
void emailValidationTemplateRendersWithoutErrors() {
  // Given
  var cmd = SendEmailCommand.builder()
      .emailType("email-validation")
      .recipientEmail("test@example.com")
      .recipientName("Test User")
      .variables(Map.of(
          "verificationCode", "123456",
          "expiresInMinutes", 30))
      .build();

  // When
  var strategy = new EmailValidationStrategy(cmd);
  String html = emailTemplateEngine.process(strategy.getTemplateName(), context);

  // Then
  assertThat(html).contains("123456"); // Código presente
  assertThat(html).contains("30 minutos"); // Variables renderizadas
  assertThat(html).doesNotContain("${"); // Sin variables sin renderizar
}
```

---

## Testing

### Test 1: Test unitario de Strategy

```java
@Test
void emailValidationStrategyExtractsVariablesCorrectly() {
  // Given
  var cmd = SendEmailCommand.builder()
      .emailType("email-validation")
      .recipientEmail("user@example.com")
      .recipientName("John Doe")
      .variables(Map.of(
          "verificationCode", "ABC123",
          "expiresInMinutes", 30))
      .build();

  // When
  var strategy = new EmailValidationStrategy(cmd);

  // Then
  assertThat(strategy.getTemplateName()).isEqualTo("email-validation");
  assertThat(strategy.getSubject()).isEqualTo("Verifica tu email en KeyGo");
  assertThat(strategy.getTemplateVariables())
      .containsEntry("userName", "John Doe")
      .containsEntry("verificationCode", "ABC123");
}
```

### Test 2: Test de rendering con Thymeleaf

```java
@WebMvcTest
class EmailNotificationAdapterTest {

  @Autowired private TemplateEngine emailTemplateEngine;

  @Test
  void shouldRenderEmailValidationTemplate() {
    // Given
    var context = new Context(Locale.of("es"));
    context.setVariable("userName", "María García");
    context.setVariable("verificationCode", "456789");
    context.setVariable("expiresInMinutes", 30);

    // When
    String html = emailTemplateEngine.process("email-validation", context);

    // Then
    assertThat(html)
        .contains("María García")
        .contains("456789")
        .contains("30 minutos")
        .contains("<html")
        .contains("</html>");
  }
}
```

### Test 3: Integration test con MockMail

```java
@SpringBootTest
@DirtiesContext
class EmailNotificationIntegrationTest {

  @Autowired private EmailNotificationPort emailNotificationPort;
  @Autowired private SimpleMailMessage[] sentMessages; // Mock

  @Test
  void shouldSendEmailWithRenderedTemplate() throws Exception {
    // Given
    var cmd = SendEmailCommand.builder()
        .emailType("email-validation")
        .recipientEmail("test@keygo.io")
        .recipientName("Test User")
        .variables(Map.of("verificationCode", "999888"))
        .build();

    // When
    emailNotificationPort.sendEmail(cmd);

    // Then
    assertThat(sentMessages).hasSize(1);
    SimpleMailMessage msg = sentMessages[0];
    assertThat(msg.getTo()).contains("test@keygo.io");
    assertThat(msg.getSubject()).contains("Verifica tu email");
  }
}
```

---

## Migración desde HTML inline

Si actualmente el proyecto tiene HTML hardcodeado en clases Java:

### Antes (no recomendado):

```java
// ❌ Anti-pattern: HTML como string
private String buildEmailHtml(String name, String code) {
  return "<html><body>" +
    "<h1>Hola " + name + "</h1>" +
    "<p>Tu código: " + code + "</p>" +
    "</body></html>";
}
```

### Después (recomendado):

1. **Crear template** → `templates/email/my-email.html` con variables Thymeleaf
2. **Crear Strategy** → `MyEmailStrategy extends EmailStrategy`
3. **Registrar en adapter** → Agregar case en `resolveStrategy()`
4. **Eliminar método buildEmailHtml()**

---

## Checklist de implementación

- [ ] Agregar `spring-boot-starter-thymeleaf` en `keygo-run/pom.xml`
- [ ] Crear `ThymeleafTemplateConfig.java` (bean `emailTemplateEngine`)
- [ ] Crear `KeyGoEmailProperties.java` (configuración)
- [ ] Crear interfaz `EmailNotificationPort.java`
- [ ] Crear clases `SendEmailCommand` + `EmailNotificationException`
- [ ] Crear `EmailStrategy` interface + implementations
- [ ] Crear `EmailNotificationAdapter.java`
- [ ] Registrar bean adapter en `ApplicationConfig.java`
- [ ] Crear templates HTML en `src/main/resources/templates/email/`
- [ ] Configurar SMTP en `application.yml`, `application-local.yml`, etc.
- [ ] Escribir tests: strategy, rendering, integration
- [ ] Documentar en `AGENTS.md` § nuevos patrones
- [ ] Crear entry en `docs/ai/lecciones.md`

---

## Referencias

| Recurso | Enlace | Notas |
|---|---|---|
| Thymeleaf Spring 6 | https://www.thymeleaf.org/documentation.html | Official documentation |
| Spring Boot 4.x Mail | https://spring.io/guides/gs/sending-email/ | Starter guide (actualizar a SB4) |
| Best practices email | https://www.campaignmonitor.com/css/ | CSS support en clientes email |
| Email previewing | https://www.litmus.com / https://www.emailonacid.com | Test en múltiples clientes |
| MIME multipart | https://www.rfc-editor.org/rfc/rfc2112 | Attachments y resources embebidas |

---

## Próximas mejoras (roadmap)

| Horizonte | Propuesta | Esfuerzo |
|---|---|---|
| Corto plazo | Agregar templates multilengua (es/en/fr) | 🟢 Bajo |
| Corto plazo | Circuit Breaker + Retry con Resilience4j | 🟢 Bajo |
| Mediano plazo | Async email sending con @Async + ThreadPoolTaskExecutor | 🟡 Medio |
| Mediano plazo | Email preview text generator automático | 🟡 Medio |
| Largo plazo | AMP for Email (Google AMP4Email) support | 🔴 Alto |
| Largo plazo | Email analytics + tracking system | 🔴 Alto |
| Largo plazo | Template builder UI / WYSIWYG editor | 🔴 Alto |

---

## Autores y changelog

| Fecha | Versión | Cambios |
|---|---|---|
| 2026-04-03 | 1.0 | Documento inicial: arquitectura, implementación paso a paso, best practices SB4.x |


