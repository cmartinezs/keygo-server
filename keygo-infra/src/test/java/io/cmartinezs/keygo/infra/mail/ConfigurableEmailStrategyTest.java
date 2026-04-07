package io.cmartinezs.keygo.infra.mail;

import static org.assertj.core.api.Assertions.assertThat;

import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties;
import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties.EmailTypeConfig;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ConfigurableEmailStrategy")
class ConfigurableEmailStrategyTest {

  private KeyGoEmailProperties emailProperties;
  private EmailTypeConfig typeConfig;

  @BeforeEach
  void setUp() {
    emailProperties = new KeyGoEmailProperties();
    emailProperties.setDefaultFromAddress("default@keygo.local");
    emailProperties.setDefaultFromName("KeyGo Default");

    typeConfig = new EmailTypeConfig();
    typeConfig.setSubject("Verifica tu email");
    typeConfig.setTemplate("html/email-validation");
    typeConfig.setFromName("KeyGo - Verificacion");
    typeConfig.setFromAddress("verify@keygo.local");
    typeConfig.setDefaults(new HashMap<>(Map.of("expiresInMinutes", 30)));
  }

  private ConfigurableEmailStrategy createStrategy(Locale locale) {
    var cmd =
        SendEmailCommand.builder()
            .emailType("email-verification")
            .recipientEmail("user@test.com")
            .recipientName("Test User")
            .variables(new HashMap<>(Map.of("verificationCode", "123456")))
            .build();
    return new ConfigurableEmailStrategy(cmd, typeConfig, emailProperties, locale);
  }

  @Nested
  @DisplayName("Subject")
  class SubjectTests {

    @Test
    @DisplayName("usa subject del tipo por defecto")
    void usesDefaultSubject() {
      var strategy = createStrategy(Locale.forLanguageTag("es"));
      assertThat(strategy.getSubject()).isEqualTo("Verifica tu email");
    }

    @Test
    @DisplayName("usa subject i18n cuando el locale coincide")
    void usesI18nSubjectWhenLocaleMatches() {
      typeConfig.setSubjectsI18n(Map.of("en", "Verify your email"));
      var strategy = createStrategy(Locale.ENGLISH);
      assertThat(strategy.getSubject()).isEqualTo("Verify your email");
    }

    @Test
    @DisplayName("fallback a subject base si locale no tiene i18n")
    void fallsBackToDefaultSubjectWhenLocaleNotConfigured() {
      typeConfig.setSubjectsI18n(Map.of("en", "Verify your email"));
      var strategy = createStrategy(Locale.FRENCH);
      assertThat(strategy.getSubject()).isEqualTo("Verifica tu email");
    }

    @Test
    @DisplayName("fallback a subject base si mapa i18n esta vacio")
    void fallsBackToDefaultSubjectWhenI18nEmpty() {
      var strategy = createStrategy(Locale.ENGLISH);
      assertThat(strategy.getSubject()).isEqualTo("Verifica tu email");
    }
  }

  @Nested
  @DisplayName("Template")
  class TemplateTests {

    @Test
    @DisplayName("usa template del tipo por defecto")
    void usesDefaultTemplate() {
      var strategy = createStrategy(Locale.forLanguageTag("es"));
      assertThat(strategy.getTemplateName()).isEqualTo("html/email-validation");
    }

    @Test
    @DisplayName("usa template i18n cuando el locale coincide")
    void usesI18nTemplateWhenLocaleMatches() {
      typeConfig.setTemplatesI18n(Map.of("en", "html/email-validation_en"));
      var strategy = createStrategy(Locale.ENGLISH);
      assertThat(strategy.getTemplateName()).isEqualTo("html/email-validation_en");
    }

    @Test
    @DisplayName("fallback a template base si locale no tiene i18n")
    void fallsBackToDefaultTemplateWhenLocaleNotConfigured() {
      typeConfig.setTemplatesI18n(Map.of("en", "html/email-validation_en"));
      var strategy = createStrategy(Locale.FRENCH);
      assertThat(strategy.getTemplateName()).isEqualTo("html/email-validation");
    }
  }

  @Nested
  @DisplayName("From address / name")
  class FromTests {

    @Test
    @DisplayName("usa fromAddress del tipo si esta configurado")
    void usesTypeFromAddress() {
      var strategy = createStrategy(null);
      assertThat(strategy.getFromAddress()).isEqualTo("verify@keygo.local");
    }

    @Test
    @DisplayName("hereda defaultFromAddress si el tipo no tiene fromAddress")
    void inheritsDefaultFromAddress() {
      typeConfig.setFromAddress(null);
      var strategy = createStrategy(null);
      assertThat(strategy.getFromAddress()).isEqualTo("default@keygo.local");
    }

    @Test
    @DisplayName("hereda defaultFromAddress si fromAddress es blank")
    void inheritsDefaultFromAddressWhenBlank() {
      typeConfig.setFromAddress("  ");
      var strategy = createStrategy(null);
      assertThat(strategy.getFromAddress()).isEqualTo("default@keygo.local");
    }

    @Test
    @DisplayName("usa fromName del tipo si esta configurado")
    void usesTypeFromName() {
      var strategy = createStrategy(null);
      assertThat(strategy.getFromName()).isEqualTo("KeyGo - Verificacion");
    }

    @Test
    @DisplayName("hereda defaultFromName si el tipo no tiene fromName")
    void inheritsDefaultFromName() {
      typeConfig.setFromName(null);
      var strategy = createStrategy(null);
      assertThat(strategy.getFromName()).isEqualTo("KeyGo Default");
    }
  }

  @Nested
  @DisplayName("Template variables")
  class VariablesTests {

    @Test
    @DisplayName("defaults del tipo se aplican si el caller no los sobreescribe")
    void defaultsAppliedFromConfig() {
      var strategy = createStrategy(null);
      var vars = strategy.getTemplateVariables();
      assertThat(vars).containsEntry("expiresInMinutes", 30);
    }

    @Test
    @DisplayName("variables del caller sobreescriben los defaults")
    void callerVariablesOverrideDefaults() {
      var cmd =
          SendEmailCommand.builder()
              .emailType("email-verification")
              .recipientEmail("user@test.com")
              .recipientName("Test User")
              .variables(new HashMap<>(Map.of("expiresInMinutes", 60, "verificationCode", "999")))
              .build();
      var strategy = new ConfigurableEmailStrategy(cmd, typeConfig, emailProperties, null);

      var vars = strategy.getTemplateVariables();
      assertThat(vars).containsEntry("expiresInMinutes", 60);
      assertThat(vars).containsEntry("verificationCode", "999");
    }

    @Test
    @DisplayName("agrega userUsername y recipientEmail automáticamente")
    void addsStandardVariables() {
      var strategy = createStrategy(null);
      var vars = strategy.getTemplateVariables();
      assertThat(vars).containsEntry("userUsername", "Test User");
      assertThat(vars).containsEntry("recipientEmail", "user@test.com");
    }

    @Test
    @DisplayName("userUsername fallback a 'usuario' si recipientName es null")
    void userUsernameFallsBackToDefault() {
      var cmd =
          SendEmailCommand.builder()
              .emailType("email-verification")
              .recipientEmail("user@test.com")
              .recipientName(null)
              .variables(new HashMap<>())
              .build();
      var strategy = new ConfigurableEmailStrategy(cmd, typeConfig, emailProperties, null);

      assertThat(strategy.getTemplateVariables()).containsEntry("userUsername", "usuario");
    }

    @Test
    @DisplayName("appName tiene fallback a 'la aplicación'")
    void appNameFallsBackToDefault() {
      var strategy = createStrategy(null);
      var vars = strategy.getTemplateVariables();
      assertThat(vars).containsEntry("appName", "la aplicación");
    }

    @Test
    @DisplayName("appName del caller tiene prioridad sobre el fallback")
    void callerAppNameOverridesDefault() {
      var cmd =
          SendEmailCommand.builder()
              .emailType("email-verification")
              .recipientEmail("user@test.com")
              .recipientName("Test User")
              .variables(new HashMap<>(Map.of("appName", "Mi App Genial")))
              .build();
      var strategy = new ConfigurableEmailStrategy(cmd, typeConfig, emailProperties, null);

      assertThat(strategy.getTemplateVariables()).containsEntry("appName", "Mi App Genial");
    }

    @Test
    @DisplayName("null defaults no causa NPE")
    void nullDefaultsDoesNotCauseNpe() {
      typeConfig.setDefaults(null);
      var strategy = createStrategy(null);
      var vars = strategy.getTemplateVariables();
      assertThat(vars).containsEntry("recipientEmail", "user@test.com");
      assertThat(vars).doesNotContainKey("expiresInMinutes");
    }

    @Test
    @DisplayName("variables del caller tienen prioridad sobre estándar")
    void callerVariablesTakePrecedenceOverStandard() {
      var cmd =
          SendEmailCommand.builder()
              .emailType("email-verification")
              .recipientEmail("user@test.com")
              .recipientName("Test User")
              .variables(new HashMap<>(Map.of("userUsername", "custom_username", "verificationCode", "123")))
              .build();
      var strategy = new ConfigurableEmailStrategy(cmd, typeConfig, emailProperties, null);

      assertThat(strategy.getTemplateVariables()).containsEntry("userUsername", "custom_username");
    }
  }

  @Test
  @DisplayName("locale null no causa error")
  void nullLocaleDoesNotCauseError() {
    var strategy = createStrategy(null);
    assertThat(strategy.getSubject()).isEqualTo("Verifica tu email");
    assertThat(strategy.getTemplateName()).isEqualTo("html/email-validation");
  }
}
