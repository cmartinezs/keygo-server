package io.cmartinezs.keygo.domain.clientapp.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AllowedScopeTest {

  @Test
  void of_withValidValue_createsAllowedScope() {
    // Given / When
    AllowedScope scope = AllowedScope.of("openid");

    // Then
    assertThat(scope.value()).isEqualTo("openid");
    assertThat(scope).hasToString("openid");
  }

  @Test
  void of_withNullValue_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> AllowedScope.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or blank");
  }

  @Test
  void of_withBlankValue_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> AllowedScope.of("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or blank");
  }
}

