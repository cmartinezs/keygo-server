package io.cmartinezs.keygo.infra.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("KeyGoUiProperties")
class KeyGoUiPropertiesTest {

  private KeyGoUiProperties properties;

  @BeforeEach
  void setUp() {
    properties = new KeyGoUiProperties();
  }

  @Nested
  @DisplayName("Default values")
  class DefaultValuesTest {

    @Test
    @DisplayName("should have default base-url")
    void shouldHaveDefaultBaseUrl() {
      assertThat(properties.getBaseUrl()).isEqualTo("http://localhost:5173");
    }

    @Test
    @DisplayName("should have empty paths map by default")
    void shouldHaveEmptyPathsMap() {
      assertThat(properties.getPaths()).isEmpty();
    }
  }

  @Nested
  @DisplayName("UiPath properties")
  class UiPathPropertiesTest {

    private KeyGoUiProperties.UiPath uiPath;

    @BeforeEach
    void setUp() {
      uiPath = new KeyGoUiProperties.UiPath();
    }

    @Test
    @DisplayName("should allow setting route")
    void shouldAllowSettingRoute() {
      uiPath.setRoute("/reset-password");
      assertThat(uiPath.getRoute()).isEqualTo("/reset-password");
    }

    @Test
    @DisplayName("should allow setting query params")
    void shouldAllowSettingQueryParams() {
      uiPath.setQueryParams(List.of("request-id", "token"));
      assertThat(uiPath.getQueryParams()).containsExactly("request-id", "token");
    }

    @Test
    @DisplayName("should have query params by default")
    void shouldHaveQueryParamsByDefault() {
      assertThat(uiPath.getQueryParams()).isNotNull();
    }
  }

  @Nested
  @DisplayName("Configuration integration")
  class ConfigurationIntegrationTest {

    @Test
    @DisplayName("should allow setting base-url")
    void shouldAllowSettingBaseUrl() {
      properties.setBaseUrl("https://app.keygo.io");
      assertThat(properties.getBaseUrl()).isEqualTo("https://app.keygo.io");
    }

    @Test
    @DisplayName("should allow adding paths")
    void shouldAllowAddingPaths() {
      KeyGoUiProperties.UiPath resetPasswordPath = new KeyGoUiProperties.UiPath();
      resetPasswordPath.setRoute("/reset-password");
      resetPasswordPath.setQueryParams(List.of("request-id"));

      properties.getPaths().put("reset-password", resetPasswordPath);

      assertThat(properties.getPaths()).hasSize(1);
      assertThat(properties.getPaths().get("reset-password").getRoute())
          .isEqualTo("/reset-password");
      assertThat(properties.getPaths().get("reset-password").getQueryParams())
          .containsExactly("request-id");
    }
  }
}





