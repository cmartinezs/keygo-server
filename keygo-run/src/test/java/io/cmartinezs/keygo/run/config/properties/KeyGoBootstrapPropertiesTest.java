package io.cmartinezs.keygo.run.config.properties;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

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
    private Validator validator;

    @BeforeEach
    void setUp() {
        properties = new KeyGoBootstrapProperties();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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
    void setAdminKey_shouldSetAdminKeyCorrectly() {
        // Given
        String adminKey = "mySecretKey123";

        // When
        properties.setAdminKey(adminKey);

        // Then
        assertThat(properties.getAdminKey()).isEqualTo(adminKey);
    }

    @Test
    void getAdminKey_shouldReturnNullWhenNotSet() {
        // When / Then
        assertThat(properties.getAdminKey()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "validKey123"})
    void validation_shouldPassWhenEnabledIsFalse(String adminKey) {
        // Given
        properties.setEnabled(false);
        properties.setAdminKey(adminKey);

        // When
        Set<ConstraintViolation<KeyGoBootstrapProperties>> violations = validator.validate(properties);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void validation_shouldPassWhenEnabledIsTrueAndAdminKeyIsValid() {
        // Given
        properties.setEnabled(true);
        properties.setAdminKey("validKey123");

        // When
        Set<ConstraintViolation<KeyGoBootstrapProperties>> violations = validator.validate(properties);

        // Then
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "  "})
    void validation_shouldFailWhenEnabledIsTrueAndAdminKeyIsInvalid(String adminKey) {
        // Given
        properties.setEnabled(true);
        properties.setAdminKey(adminKey);

        // When
        Set<ConstraintViolation<KeyGoBootstrapProperties>> violations = validator.validate(properties);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("adminKey must not be blank when bootstrap is enabled");
    }

    @Test
    void properties_shouldAllowModification() {
        // Given
        properties.setEnabled(false);
        properties.setAdminKey("initialKey");

        // When
        properties.setEnabled(true);
        properties.setAdminKey("updatedKey");

        // Then
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getAdminKey()).isEqualTo("updatedKey");
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
}


