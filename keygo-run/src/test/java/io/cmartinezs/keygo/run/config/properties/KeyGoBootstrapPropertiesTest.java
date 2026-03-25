package io.cmartinezs.keygo.run.config.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for KeyGoBootstrapProperties
 * Pruebas unitarias para KeyGoBootstrapProperties
 *
 * @author cmartinezs
 * @version 1.0
 */
class KeyGoBootstrapPropertiesTest {

    private KeyGoBootstrapProperties properties;

    @BeforeEach
    void setUp() {
        properties = new KeyGoBootstrapProperties();
    }

    @Test
    void enabled_shouldDefaultToTrue() {
        // When / Then
        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void setEnabled_shouldSetEnabledCorrectly() {
        // Given
        boolean enabled = false;

        // When
        properties.setEnabled(enabled);

        // Then
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void properties_shouldAllowModification() {
        // Given
        properties.setEnabled(false);

        // When
        properties.setEnabled(true);

        // Then
        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void apiPathPrefix_shouldBeNullByDefault() {
        // When / Then - Must be configured in application.yml
        assertThat(properties.getApiPathPrefix()).isNull();
    }

    @Test
    void actuatorPathPrefix_shouldBeNullByDefault() {
        // When / Then - Must be configured in application.yml
        assertThat(properties.getActuatorPathPrefix()).isNull();
    }

    @Test
    void serviceInfoPathPrefix_shouldBeNullByDefault() {
        // When / Then - Must be configured in application.yml
        assertThat(properties.getServiceInfoPathPrefix()).isNull();
    }

    @Test
    void pathPrefixes_shouldBeConfigurable() {
        // Given
        String customApiPath = "/custom-api/";
        String customActuatorPath = "/custom-actuator/";
        String customServiceInfoPath = "/custom-service-info";

        // When
        properties.setApiPathPrefix(customApiPath);
        properties.setActuatorPathPrefix(customActuatorPath);
        properties.setServiceInfoPathPrefix(customServiceInfoPath);

        // Then
        assertThat(properties.getApiPathPrefix()).isEqualTo(customApiPath);
        assertThat(properties.getActuatorPathPrefix()).isEqualTo(customActuatorPath);
        assertThat(properties.getServiceInfoPathPrefix()).isEqualTo(customServiceInfoPath);
    }


    @Test
    void oauth2PublicSuffixes_shouldBeConfigurable() {
        // Given / When
        properties.setAuthorizePathSuffix("/oauth2/authorize");
        properties.setLoginPathSuffix("/account/login");
        properties.setTokenPathSuffix("/oauth2/token");

        // Then
        assertThat(properties.getAuthorizePathSuffix()).isEqualTo("/oauth2/authorize");
        assertThat(properties.getLoginPathSuffix()).isEqualTo("/account/login");
        assertThat(properties.getTokenPathSuffix()).isEqualTo("/oauth2/token");
    }
}
