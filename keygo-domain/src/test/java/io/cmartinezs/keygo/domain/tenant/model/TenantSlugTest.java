package io.cmartinezs.keygo.domain.tenant.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TenantSlug value object.
 */
@DisplayName("TenantSlug")
class TenantSlugTest {

  @ParameterizedTest
  @ValueSource(strings = {"abc", "my-tenant", "keygo", "tenant-01", "a1b2c3", "my-org-name"})
  @DisplayName("should accept valid slugs")
  void shouldAcceptValidSlugs(String slug) {
    assertThatCode(() -> TenantSlug.of(slug)).doesNotThrowAnyException();
    assertThat(TenantSlug.of(slug).value()).isEqualTo(slug);
  }

  @ParameterizedTest
  @ValueSource(strings = {"-abc", "abc-", "ABC", "ab", "has space", "has_underscore", "Has-Upper"})
  @DisplayName("should reject invalid slugs")
  void shouldRejectInvalidSlugs(String slug) {
    assertThatThrownBy(() -> TenantSlug.of(slug))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("should reject null slug")
  void shouldRejectNull() {
    assertThatThrownBy(() -> TenantSlug.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null");
  }

  @Test
  @DisplayName("should reject blank slug")
  void shouldRejectBlank() {
    assertThatThrownBy(() -> TenantSlug.of("   "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("should reject slug shorter than 3 characters")
  void shouldRejectTooShort() {
    assertThatThrownBy(() -> TenantSlug.of("ab"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least 3");
  }

  @Test
  @DisplayName("toString returns the slug value")
  void shouldReturnValueOnToString() {
    assertThat(TenantSlug.of("my-tenant")).hasToString("my-tenant");
  }
}

