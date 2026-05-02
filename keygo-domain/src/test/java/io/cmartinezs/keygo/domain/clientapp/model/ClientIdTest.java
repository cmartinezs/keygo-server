package io.cmartinezs.keygo.domain.clientapp.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientIdTest {

  @Test
  void of_validValue_shouldCreateClientId() {
    // When
    ClientId clientId = ClientId.of("my-client-123");

    // Then
    assertThat(clientId.value()).isEqualTo("my-client-123");
  }

  @Test
  void of_nullValue_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> ClientId.of(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void of_blankValue_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> ClientId.of("   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void of_valueLongerThan255_shouldThrow() {
    // Given
    String longValue = "a".repeat(256);

    // When / Then
    assertThatThrownBy(() -> ClientId.of(longValue))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

