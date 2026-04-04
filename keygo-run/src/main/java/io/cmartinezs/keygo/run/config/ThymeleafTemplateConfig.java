package io.cmartinezs.keygo.run.config;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
 * <p>Esta configuración crea un TemplateEngine independiente para emails, sin afectar la
 * resolución de vistas web (si las hubiera).
 *
 * <p>Configuración:
 * - Prefix: `templates/email/`
 * - Suffix: `.html`
 * - TemplateMode: HTML
 * - Charset: UTF-8
 * - Caché: controlado por property `keygo.email.template-cache-enabled`
 */
@Configuration
public class ThymeleafTemplateConfig {

  /**
   * Bean TemplateEngine dedicado para email templates.
   *
   * <p>Solo se crea si no existe otro bean TemplateEngine (permite sobrescribir si es necesario).
   *
   * @param emailProperties Configuración de email (incluye flag de caché)
   * @return TemplateEngine configurado para Thymeleaf + Spring 6 (Spring Boot 4.x)
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

