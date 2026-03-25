package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ScopeSetTest {

  @Test
  void from_withValidScopes_buildsScopeSet() {
    // Given / When
    ScopeSet scopeSet = ScopeSet.from("openid profile email");

    // Then
    assertThat(scopeSet.getScopes()).containsExactly("openid", "profile", "email");
    assertThat(scopeSet.asString()).isEqualTo("openid profile email");
  }

  @Test
  void from_withNullString_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> ScopeSet.from(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or empty");
  }

  @Test
  void from_withUnknownScope_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> ScopeSet.from("openid super_admin"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown or invalid scope");
  }

  @Test
  void of_withValidSet_buildsScopeSet() {
    // Given
    Set<String> input = new LinkedHashSet<>(Set.of("openid", "offline_access"));

    // When
    ScopeSet scopeSet = ScopeSet.of(input);

    // Then
    assertThat(scopeSet.contains("openid")).isTrue();
    assertThat(scopeSet.contains("email")).isFalse();
  }

  @Test
  void of_withEmptySet_throwsException() {
    // Given
    Set<String> emptyScopes = Set.of();

    // When / Then
    assertThatThrownBy(() -> ScopeSet.of(emptyScopes))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or empty");
  }

  @Test
  void of_withInvalidScope_throwsException() {
    // Given
    Set<String> invalidScopes = Set.of("openid", "admin");

    // When / Then
    assertThatThrownBy(() -> ScopeSet.of(invalidScopes))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown or invalid scope");
  }

  @Test
  void equals_andHashCode_withSameScopes_returnsTrue() {
    // Given
    ScopeSet first = ScopeSet.from("openid profile");
    ScopeSet second = ScopeSet.of(Set.of("openid", "profile"));

    // When / Then
    assertThat(first).isEqualTo(second).hasSameHashCodeAs(second).hasToString(first.asString());
  }
}
