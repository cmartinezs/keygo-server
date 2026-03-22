package io.cmartinezs.keygo.domain.auth.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SigningKeyIdTest {

  @Test
  void givenValidValue_whenCreating_thenSucceeds() {
    // Given / When
    var id = new SigningKeyId("abc-123");

    // Then
    assertThat(id.value()).isEqualTo("abc-123");
  }

  @Test
  void givenNullValue_whenCreating_thenThrowsIllegalArgument() {
    // Given / When / Then
    assertThatThrownBy(() -> new SigningKeyId(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenBlankValue_whenCreating_thenThrowsIllegalArgument() {
    // Given / When / Then
    assertThatThrownBy(() -> new SigningKeyId("   "))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

