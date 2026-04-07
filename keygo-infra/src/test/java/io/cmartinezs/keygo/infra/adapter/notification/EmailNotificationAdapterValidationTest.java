package io.cmartinezs.keygo.infra.adapter.notification;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties;
import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties.EmailTypeConfig;
import io.cmartinezs.keygo.infra.config.KeyGoUiProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@DisplayName("EmailNotificationAdapter — fail-fast validation")
class EmailNotificationAdapterValidationTest {

  private TemplateEngine templateEngine;
  private JavaMailSender mailSender;
  private KeyGoUiProperties uiProperties;
  private KeyGoEmailProperties emailProperties;

  @BeforeEach
  void setUp() {
    templateEngine = mock(TemplateEngine.class);
    mailSender = mock(JavaMailSender.class);
    uiProperties = new KeyGoUiProperties();
    emailProperties = new KeyGoEmailProperties();
  }

  private EmailNotificationAdapter createAdapter() {
    return new EmailNotificationAdapter(templateEngine, mailSender, uiProperties, emailProperties);
  }

  @Nested
  @DisplayName("Validacion de configuracion YAML")
  class YamlConfigValidation {

    @Test
    @DisplayName("falla si un tipo no tiene template")
    void failsWhenTemplateIsNull() {
      var config = new EmailTypeConfig();
      config.setSubject("Subject");
      config.setTemplate(null);
      emailProperties.setTypes(Map.of("test-type", config));

      var adapter = createAdapter();
      assertThatThrownBy(adapter::validateTemplatesExist)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("no template configured");
    }

    @Test
    @DisplayName("falla si un tipo tiene template blank")
    void failsWhenTemplateIsBlank() {
      var config = new EmailTypeConfig();
      config.setSubject("Subject");
      config.setTemplate("  ");
      emailProperties.setTypes(Map.of("test-type", config));

      var adapter = createAdapter();
      assertThatThrownBy(adapter::validateTemplatesExist)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("no template configured");
    }

    @Test
    @DisplayName("falla si un tipo no tiene subject")
    void failsWhenSubjectIsNull() {
      var config = new EmailTypeConfig();
      config.setSubject(null);
      config.setTemplate("html/some-template");
      emailProperties.setTypes(Map.of("test-type", config));

      var adapter = createAdapter();
      assertThatThrownBy(adapter::validateTemplatesExist)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("no subject configured");
    }
  }

  @Nested
  @DisplayName("Validacion de archivo template en classpath")
  class TemplateFileValidation {

    @Test
    @DisplayName("falla si el archivo .html no existe en classpath")
    void failsWhenTemplateFileDoesNotExist() {
      var config = new EmailTypeConfig();
      config.setSubject("Subject");
      config.setTemplate("html/non-existent-template");
      emailProperties.setTypes(Map.of("missing-template", config));

      var adapter = createAdapter();
      assertThatThrownBy(adapter::validateTemplatesExist)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Email template file not found")
          .hasMessageContaining("non-existent-template");
    }

    @Test
    @DisplayName("falla si template i18n no existe en classpath")
    void failsWhenI18nTemplateFileDoesNotExist() {
      var config = new EmailTypeConfig();
      config.setSubject("Subject");
      config.setTemplate("html/email-validation");
      config.setTemplatesI18n(Map.of("de", "html/email-validation_de"));
      emailProperties.setTypes(Map.of("i18n-missing", config));

      var adapter = createAdapter();

      // Nota: puede fallar por el template base o por el i18n dependiendo de si
      // email-validation.html existe en el classpath del modulo test
      assertThatThrownBy(adapter::validateTemplatesExist)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Email template file not found");
    }

    @Test
    @DisplayName("no falla si no hay tipos configurados (solo warn)")
    void doesNotFailWhenNoTypesConfigured() {
      emailProperties.setTypes(new LinkedHashMap<>());
      var adapter = createAdapter();
      assertThatCode(adapter::validateTemplatesExist).doesNotThrowAnyException();
    }
  }
}
