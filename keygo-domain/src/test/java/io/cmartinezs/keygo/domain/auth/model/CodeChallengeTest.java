package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CodeChallengeTest {

  @Test
  void s256_withValidChallenge_createsValueObject() {
    // Given / When
    CodeChallenge challenge = CodeChallenge.s256(" challenge-value ");

    // Then
    assertThat(challenge.getChallenge()).isEqualTo("challenge-value");
    assertThat(challenge.getMethod()).isEqualTo("S256");
  }

  @Test
  void s256_withBlankChallenge_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> CodeChallenge.s256("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or empty");
  }

  @Test
  void plain_withValidChallenge_createsValueObject() {
    // Given / When
    CodeChallenge challenge = CodeChallenge.plain(" verifier ");

    // Then
    assertThat(challenge.getChallenge()).isEqualTo("verifier");
    assertThat(challenge.getMethod()).isEqualTo("plain");
  }

  @Test
  void plain_withNullChallenge_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> CodeChallenge.plain(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or empty");
  }

  @Test
  void equals_withSameData_returnsTrue() {
    // Given
    CodeChallenge first = CodeChallenge.s256("abc");
    CodeChallenge second = CodeChallenge.s256("abc");

    // When / Then
    assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
  }

  @Test
  void equals_withDifferentMethod_returnsFalse() {
    // Given
    CodeChallenge first = CodeChallenge.s256("abc");
    CodeChallenge second = CodeChallenge.plain("abc");

    // When / Then
    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void toString_hidesChallengeValue() {
    // Given
    CodeChallenge challenge = CodeChallenge.s256("secret-value");

    // When
    String printed = challenge.toString();

    // Then
    assertThat(printed).contains("method='S256'").doesNotContain("secret-value");
  }
}

