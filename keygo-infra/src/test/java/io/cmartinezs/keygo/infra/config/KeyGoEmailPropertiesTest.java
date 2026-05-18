package io.cmartinezs.keygo.infra.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties.EmailTypeConfig;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KeyGoEmailProperties")
class KeyGoEmailPropertiesTest {

  private KeyGoEmailProperties props;

  @BeforeEach
  void setUp() {
    props = new KeyGoEmailProperties();
    props.setDefaultFromAddress("noreply@keygo.local");
    props.setDefaultFromName("KeyGo");

    var verification = new EmailTypeConfig();
    verification.setSubject("Verifica tu email");
    verification.setTemplate("html/email-validation");

    var recovery = new EmailTypeConfig();
    recovery.setSubject("Restablecer contrasena");
    recovery.setTemplate("html/password-recovery");

    props.setTypes(Map.of("email-verification", verification, "password-recovery", recovery));
  }

  @Test
  @DisplayName("getType retorna config del tipo registrado")
  void getTypeReturnsRegisteredType() {
    var config = props.getType("email-verification");
    assertThat(config).isNotNull();
    assertThat(config.getSubject()).isEqualTo("Verifica tu email");
    assertThat(config.getTemplate()).isEqualTo("html/email-validation");
  }

  @Test
  @DisplayName("getType retorna null para tipo no registrado")
  void getTypeReturnsNullForUnknownType() {
    assertThat(props.getType("unknown-type")).isNull();
  }

  @Test
  @DisplayName("types map contiene todos los tipos registrados")
  void typesMapContainsAllRegistered() {
    assertThat(props.getTypes()).hasSize(2).containsKeys("email-verification", "password-recovery");
  }

  @Test
  @DisplayName("defaults se inicializan correctamente")
  void defaultsAreInitialized() {
    assertThat(props.getDefaultFromAddress()).isEqualTo("noreply@keygo.local");
    assertThat(props.getDefaultFromName()).isEqualTo("KeyGo");
assertThat(props.isTemplateCacheEnabled()).isTrue();
  }

  @Test
  @DisplayName("EmailTypeConfig tiene defaults vacios por defecto")
  void emailTypeConfigHasEmptyDefaults() {
    var config = new EmailTypeConfig();
    assertThat(config.getDefaults()).isEmpty();
    assertThat(config.getLinks()).isEmpty();
    assertThat(config.getSubjectsI18n()).isEmpty();
    assertThat(config.getTemplatesI18n()).isEmpty();
  }
}
