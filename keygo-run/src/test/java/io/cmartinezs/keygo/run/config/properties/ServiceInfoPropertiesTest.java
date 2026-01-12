package io.cmartinezs.keygo.run.config.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ServiceInfoProperties
 * Pruebas unitarias para ServiceInfoProperties
 *
 * @author cmartinezs
 * @version 1.0
 */
class ServiceInfoPropertiesTest {

    private ServiceInfoProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ServiceInfoProperties();
    }

    @Test
    void setTitle_shouldSetTitleCorrectly() {
        // Given
        String title = "KeyGo Server API";

        // When
        properties.setTitle(title);

        // Then
        assertThat(properties.getTitle()).isEqualTo(title);
    }

    @Test
    void setName_shouldSetNameCorrectly() {
        // Given
        String name = "keygo-server";

        // When
        properties.setName(name);

        // Then
        assertThat(properties.getName()).isEqualTo(name);
    }

    @Test
    void setVersion_shouldSetVersionCorrectly() {
        // Given
        String version = "1.0-SNAPSHOT";

        // When
        properties.setVersion(version);

        // Then
        assertThat(properties.getVersion()).isEqualTo(version);
    }

    @Test
    void properties_shouldImplementServiceInfoProvider() {
        // Given
        properties.setTitle("Test Title");
        properties.setName("test-name");
        properties.setVersion("1.0.0");

        // Then
        assertThat(properties.getTitle()).isEqualTo("Test Title");
        assertThat(properties.getName()).isEqualTo("test-name");
        assertThat(properties.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void getTitle_shouldReturnNullWhenNotSet() {
        // When / Then
        assertThat(properties.getTitle()).isNull();
    }

    @Test
    void getName_shouldReturnNullWhenNotSet() {
        // When / Then
        assertThat(properties.getName()).isNull();
    }

    @Test
    void getVersion_shouldReturnNullWhenNotSet() {
        // When / Then
        assertThat(properties.getVersion()).isNull();
    }

    @Test
    void properties_shouldAllowModification() {
        // Given
        properties.setTitle("Initial Title");
        properties.setName("initial-name");
        properties.setVersion("1.0");

        // When
        properties.setTitle("Updated Title");
        properties.setName("updated-name");
        properties.setVersion("2.0");

        // Then
        assertThat(properties.getTitle()).isEqualTo("Updated Title");
        assertThat(properties.getName()).isEqualTo("updated-name");
        assertThat(properties.getVersion()).isEqualTo("2.0");
    }
}

