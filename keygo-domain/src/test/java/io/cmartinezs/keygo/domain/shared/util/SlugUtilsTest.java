package io.cmartinezs.keygo.domain.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link SlugUtils}.
 */
@DisplayName("SlugUtils")
class SlugUtilsTest {

  @ParameterizedTest(name = "''{0}'' → ''{1}''")
  @CsvSource({
      "My Tenant,           my-tenant",
      "Acme Corp,           acme-corp",
      "Hello World!,        hello-world",
      "  Leading Spaces ,   leading-spaces",
      "Under_score,         under-score",
      "UPPERCASE,           uppercase",
      "café,                cafe",
      "ñoño,                nono",
      "Héroe,               heroe",
      "tenant-name,         tenant-name",
      "Multiple   Spaces,   multiple-spaces",
      "mix3d_chars & stuff, mix3d-chars-stuff",
  })
  @DisplayName("should convert various inputs to expected slugs")
  void shouldConvertToSlug(String input, String expected) {
    assertThat(SlugUtils.toSlug(input.trim())).isEqualTo(expected.trim());
  }

  @Test
  @DisplayName("should throw when input is null")
  void shouldThrowOnNull() {
    assertThatThrownBy(() -> SlugUtils.toSlug(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("null or blank");
  }

  @Test
  @DisplayName("should throw when input is blank")
  void shouldThrowOnBlank() {
    assertThatThrownBy(() -> SlugUtils.toSlug("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("null or blank");
  }

  @Test
  @DisplayName("should throw when input produces an empty slug after normalization")
  void shouldThrowWhenResultIsEmpty() {
    // All characters are non-alphanumeric and non-ASCII → normalize to nothing
    assertThatThrownBy(() -> SlugUtils.toSlug("!@#$%^"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("should truncate slug to maxLength and not end with a hyphen")
  void shouldTruncateToMaxLength() {
    // 120-char name that becomes a long slug
    String longName = "a".repeat(60) + " " + "b".repeat(60);
    String result = SlugUtils.toSlug(longName);

    assertThat(result.length()).isLessThanOrEqualTo(100);
    assertThat(result).doesNotEndWith("-");
  }

  @Test
  @DisplayName("should truncate slug to custom maxLength")
  void shouldTruncateToCustomMaxLength() {
    String name = "this is a very descriptive tenant name";
    String result = SlugUtils.toSlug(name, 10);

    assertThat(result.length()).isLessThanOrEqualTo(10);
    assertThat(result).doesNotEndWith("-");
  }
}

