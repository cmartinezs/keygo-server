package io.cmartinezs.keygo.domain.clientapp.model;

import io.cmartinezs.keygo.domain.clientapp.exception.InvalidRedirectUriException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedirectUriTest {

  @Test
  void of_httpsUri_shouldBeValid() {
    // When
    RedirectUri uri = RedirectUri.of("https://example.com/callback");

    // Then
    assertThat(uri.value()).isEqualTo("https://example.com/callback");
  }

  @Test
  void of_httpLocalhostUri_shouldBeValid() {
    // When
    RedirectUri uri = RedirectUri.of("http://localhost:8080/callback");

    // Then
    assertThat(uri.value()).isEqualTo("http://localhost:8080/callback");
  }

  @Test
  void of_http127Uri_shouldBeValid() {
    // When
    RedirectUri uri = RedirectUri.of("http://127.0.0.1:3000/callback");

    // Then
    assertThat(uri.value()).isEqualTo("http://127.0.0.1:3000/callback");
  }

  @Test
  void of_ftpUri_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> RedirectUri.of("ftp://example.com/callback"))
        .isInstanceOf(InvalidRedirectUriException.class);
  }

  @Test
  void of_httpNonLocalUri_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> RedirectUri.of("http://example.com/callback"))
        .isInstanceOf(InvalidRedirectUriException.class);
  }

  @Test
  void of_nullUri_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> RedirectUri.of(null))
        .isInstanceOf(InvalidRedirectUriException.class);
  }

  @Test
  void of_blankUri_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> RedirectUri.of("  "))
        .isInstanceOf(InvalidRedirectUriException.class);
  }
}

