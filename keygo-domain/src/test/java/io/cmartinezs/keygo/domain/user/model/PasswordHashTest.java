package io.cmartinezs.keygo.domain.user.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordHashTest {

  private static final String VALID_HASH = "$2a$10$somehashedvalue";

  @Test
  void validHashCreatedSuccessfully() {
    PasswordHash ph = PasswordHash.of(VALID_HASH);
    assertThat(ph.value()).isEqualTo(VALID_HASH);
  }

  @Test
  void nullHashThrows() {
    assertThatThrownBy(() -> PasswordHash.of(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void blankHashThrows() {
    assertThatThrownBy(() -> PasswordHash.of("  "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toStringIsRedacted() {
    // Ensure hash is never exposed in toString (security requirement)
    String str = PasswordHash.of(VALID_HASH).toString();
    assertThat(str).doesNotContain(VALID_HASH).containsIgnoringCase("REDACTED");
  }
}

