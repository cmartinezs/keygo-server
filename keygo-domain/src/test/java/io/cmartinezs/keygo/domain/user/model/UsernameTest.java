package io.cmartinezs.keygo.domain.user.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsernameTest {

  @Test
  void validUsernameCreatedSuccessfully() {
    Username u = Username.of("johndoe");
    assertThat(u.value()).isEqualTo("johndoe");
  }

  @Test
  void usernameWithSpecialCharsIsValid() {
    Username u = Username.of("john.doe_123-x");
    assertThat(u.value()).isNotBlank();
  }

  @Test
  void nullUsernameThrows() {
    assertThatThrownBy(() -> Username.of(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void blankUsernameThrows() {
    assertThatThrownBy(() -> Username.of("   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void tooShortUsernameThrows() {
    assertThatThrownBy(() -> Username.of("ab"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("3-100");
  }

  @Test
  void invalidCharactersThrow() {
    assertThatThrownBy(() -> Username.of("user name!"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toStringReturnsValue() {
    assertThat(Username.of("alice")).hasToString("alice");
  }
}

