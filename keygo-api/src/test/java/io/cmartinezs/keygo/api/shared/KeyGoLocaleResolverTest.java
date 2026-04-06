package io.cmartinezs.keygo.api.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link KeyGoLocaleResolver}.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>Standard BCP 47 {@code Accept-Language} values (hyphen) resolve correctly.</li>
 *   <li>Non-standard values with underscores (e.g. {@code en_US}) are normalised and resolved.</li>
 *   <li>Unsupported locales fall back to the configured default ({@code en-US}).</li>
 *   <li>Absent or blank headers fall back to the default.</li>
 *   <li>Malformed headers fall back gracefully without throwing.</li>
 *   <li>{@link HttpServletRequest} integration delegates to {@link KeyGoLocaleResolver#resolveFromHeader}.</li>
 * </ul>
 */
@DisplayName("KeyGoLocaleResolver")
class KeyGoLocaleResolverTest {

  private KeyGoLocaleResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new KeyGoLocaleResolver();
    resolver.setDefaultLocale(Locale.US);
    resolver.setSupportedLocales(List.of(
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("es-CL"),
        Locale.forLanguageTag("en"),
        Locale.forLanguageTag("en-US")));
  }

  // ─── Standard BCP 47 headers (hyphen) ─────────────────────────────────────

  @Test
  @DisplayName("should resolve en-US when Accept-Language is en-US (exact match)")
  void shouldResolveEnUsForStandardHeader() {
    // Given / When
    Locale result = resolver.resolveFromHeader("en-US");
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("en-US"));
  }

  @Test
  @DisplayName("should resolve es-CL when Accept-Language is es-CL (exact match)")
  void shouldResolveEsCLForStandardHeader() {
    // Given / When
    Locale result = resolver.resolveFromHeader("es-CL");
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("es-CL"));
  }

  @Test
  @DisplayName("should resolve es when Accept-Language is es (language-only)")
  void shouldResolveEsForLanguageOnlyHeader() {
    // Given / When
    Locale result = resolver.resolveFromHeader("es");
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("es"));
  }

  @Test
  @DisplayName("should resolve en when Accept-Language is en (language-only)")
  void shouldResolveEnForLanguageOnlyHeader() {
    // Given / When
    Locale result = resolver.resolveFromHeader("en");
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("en"));
  }

  // ─── Non-standard headers with underscores (the main bug scenario) ─────────

  @Test
  @DisplayName("should resolve en-US when Accept-Language is en_US (underscore — non-standard)")
  void shouldResolveEnUsForUnderscoreHeader() {
    // Given / When — the reported bug: en_US with underscore was returning es-CL
    Locale result = resolver.resolveFromHeader("en_US");
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("en-US"));
  }

  @Test
  @DisplayName("should resolve es-CL when Accept-Language is es_CL (underscore — non-standard)")
  void shouldResolveEsCLForUnderscoreHeader() {
    // Given / When
    Locale result = resolver.resolveFromHeader("es_CL");
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("es-CL"));
  }

  // ─── q-value / multiple locales ───────────────────────────────────────────

  @Test
  @DisplayName("should resolve best match from multi-locale header with q-values")
  void shouldResolveBestMatchFromMultiLocaleHeader() {
    // Given / When — client prefers fr (not supported) then es
    Locale result = resolver.resolveFromHeader("fr;q=0.9,es;q=0.8");
    // Then — fr is not supported, es is → es wins
    assertThat(result).isEqualTo(Locale.forLanguageTag("es"));
  }

  @Test
  @DisplayName("should resolve en-US when first preference is unsupported but en-US is second")
  void shouldResolveSecondPreferenceWhenFirstUnsupported() {
    // Given / When
    Locale result = resolver.resolveFromHeader("ja,en-US;q=0.9");
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("en-US"));
  }

  // ─── Fallback to default ───────────────────────────────────────────────────

  @ParameterizedTest
  @CsvSource({"''", "' '"})
  @DisplayName("should return default locale (en-US) when Accept-Language is absent or blank")
  void shouldFallbackToDefaultWhenHeaderBlank(String header) {
    // Given / When
    Locale result = resolver.resolveFromHeader(header.isBlank() ? header : null);
    // Then
    assertThat(result).isEqualTo(Locale.US);
  }

  @Test
  @DisplayName("should return default locale (en-US) when header is null")
  void shouldFallbackToDefaultWhenHeaderNull() {
    // Given / When
    Locale result = resolver.resolveFromHeader(null);
    // Then
    assertThat(result).isEqualTo(Locale.US);
  }

  @Test
  @DisplayName("should return default locale (en-US) when locale is not in supported list")
  void shouldFallbackToDefaultForUnsupportedLocale() {
    // Given / When — Japanese is not in the supported list
    Locale result = resolver.resolveFromHeader("ja-JP");
    // Then
    assertThat(result).isEqualTo(Locale.US);
  }

  @Test
  @DisplayName("should return default locale (en-US) when Accept-Language header is malformed")
  void shouldFallbackToDefaultForMalformedHeader() {
    // Given / When
    Locale result = resolver.resolveFromHeader("!!!invalid-header!!!");
    // Then — no exception, graceful fallback
    assertThat(result).isEqualTo(Locale.US);
  }

  // ─── resolveLocale(HttpServletRequest) integration ──────────────────────

  @Test
  @DisplayName("resolveLocale(request) should read Accept-Language header from request")
  void shouldReadAcceptLanguageFromRequest() {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Accept-Language")).thenReturn("es-CL");
    // When
    Locale result = resolver.resolveLocale(request);
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("es-CL"));
  }

  @Test
  @DisplayName("resolveLocale(request) should resolve en_US (underscore) from request header")
  void shouldResolveUnderscoreLocaleFromRequest() {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Accept-Language")).thenReturn("en_US");
    // When
    Locale result = resolver.resolveLocale(request);
    // Then
    assertThat(result).isEqualTo(Locale.forLanguageTag("en-US"));
  }

  // ─── setLocale must throw ──────────────────────────────────────────────────

  @Test
  @DisplayName("setLocale should throw UnsupportedOperationException")
  void shouldThrowOnSetLocale() {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    // When / Then
    org.junit.jupiter.api.Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> resolver.setLocale(request, null, Locale.FRENCH));
  }
}

