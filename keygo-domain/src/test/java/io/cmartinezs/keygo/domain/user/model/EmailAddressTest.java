package io.cmartinezs.keygo.domain.user.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAddressTest {

  @Test
  void validEmailCreatedSuccessfully() {
    EmailAddress email = EmailAddress.of("user@example.com");
    assertThat(email.value()).isEqualTo("user@example.com");
  }

  @Test
  void emailWithSubdomainIsValid() {
    EmailAddress email = EmailAddress.of("user@mail.example.co.uk");
    assertThat(email.value()).isNotBlank();
  }

  @Test
  void nullEmailThrows() {
    assertThatThrownBy(() -> EmailAddress.of(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void blankEmailThrows() {
    assertThatThrownBy(() -> EmailAddress.of("  "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void invalidFormatThrows() {
    assertThatThrownBy(() -> EmailAddress.of("not-an-email"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invalid format");
  }

  @Test
  void missingAtThrows() {
    assertThatThrownBy(() -> EmailAddress.of("userdomain.com"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toStringReturnsValue() {
    assertThat(EmailAddress.of("a@b.com")).hasToString("a@b.com");
  }
}

