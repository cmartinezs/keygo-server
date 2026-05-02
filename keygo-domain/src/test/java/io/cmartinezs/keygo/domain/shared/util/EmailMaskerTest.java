package io.cmartinezs.keygo.domain.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmailMaskerTest {

  @ParameterizedTest
  @CsvSource({
      "admin@keygo.local,       a********n@k****.local",
      "test@example.com.br,     t********t@e******.com.br",
      "ab@gmail.com,            a********b@g****.com",
      "u@d.com,                 u********@d****.com",
      "contractor@keygo.local,  c********r@k****.local",
      "user@demo.local,         u********r@d****.local",
      "a@b.co.uk,               a********@b****.co.uk",
      "john.doe@company.com,    j********e@c******.com",
  })
  void shouldMaskEmailCorrectly(String input, String expected) {
    assertThat(EmailMasker.mask(input)).isEqualTo(expected);
  }

  @Test
  void shouldReturnNullForNullInput() {
    assertThat(EmailMasker.mask(null)).isNull();
  }

  @Test
  void shouldReturnOriginalIfNoAtSign() {
    assertThat(EmailMasker.mask("notanemail")).isEqualTo("notanemail");
  }

  @Test
  void shouldHandleSingleCharLocal() {
    assertThat(EmailMasker.mask("x@example.com")).isEqualTo("x********@e******.com");
  }

  @Test
  void shouldHandleTwoCharLocal() {
    assertThat(EmailMasker.mask("ab@example.com")).isEqualTo("a********b@e******.com");
  }
}
