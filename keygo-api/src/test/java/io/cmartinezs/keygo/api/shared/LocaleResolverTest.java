package io.cmartinezs.keygo.api.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocaleResolverTest {

  @Mock
  private HttpServletRequest request;

  private final LocaleResolver resolver = new LocaleResolver();

  // ── Parsing and resolution ─────────────────────────────────────────────────

  @Test
  @DisplayName("resolveLocale — resuelve es-CL desde Accept-Language header")
  void resolveLocale_parsesEsCl() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("es-CL,es;q=0.9");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(new Locale("es", "CL"));
  }

  @Test
  @DisplayName("resolveLocale — resuelve en-US desde Accept-Language header")
  void resolveLocale_parsesEnUs() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.9");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(Locale.US);
  }

  @Test
  @DisplayName("resolveLocale — resuelve pt-BR desde Accept-Language header")
  void resolveLocale_parsesPtBr() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("pt-BR,pt;q=0.9");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(new Locale("pt", "BR"));
  }

  @Test
  @DisplayName("resolveLocale — fallback a en-US si header está ausente")
  void resolveLocale_fallbacksToEnUsWhenHeaderMissing() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn(null);

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(Locale.US);
  }

  @Test
  @DisplayName("resolveLocale — fallback a en-US si header está vacío")
  void resolveLocale_fallbacksToEnUsWhenHeaderBlank() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(Locale.US);
  }

  @Test
  @DisplayName("resolveLocale — fallback a en-US si idioma no está soportado")
  void resolveLocale_fallbacksToEnUsWhenLanguageUnsupported() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("de-DE,de;q=0.9");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(Locale.US);
  }

  // ── Language support ───────────────────────────────────────────────────────

  @Test
  @DisplayName("resolveLocale — acepta genérico 'es' sin región")
  void resolveLocale_acceptsGenericEs() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("es");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(new Locale("es"));
  }

  @Test
  @DisplayName("resolveLocale — acepta genérico 'en' sin región")
  void resolveLocale_acceptsGenericEn() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("en");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(new Locale("en"));
  }

  @Test
  @DisplayName("resolveLocale — acepta fr (francés)")
  void resolveLocale_acceptsFrench() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("fr-FR,fr;q=0.9");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(new Locale("fr", "FR"));
  }

  @Test
  @DisplayName("resolveLocale — case-insensitive: transforma 'EN-us' a 'en-US'")
  void resolveLocale_caseInsensitive() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("EN-us");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale.getLanguage()).isEqualTo("en");
    assertThat(locale.getCountry()).isEqualTo("US");
  }

  // ── Multiple preferences (qvalue) ──────────────────────────────────────────

  @Test
  @DisplayName("resolveLocale — toma la primera preferencia sin importar qvalue")
  void resolveLocale_takesFirstPreferenceIgnoringQvalue() {
    // Given
    // fr with q=0.5 is listed first; de with q=0.9 is second but unsupported
    when(request.getHeader("Accept-Language")).thenReturn("fr;q=0.5,de;q=0.9");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(new Locale("fr"));
  }

  // ── Edge cases ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("resolveLocale — maneja header con espacios")
  void resolveLocale_handlesHeaderWithSpaces() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("  es-CL  ,  en-US  ;q=0.9");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(new Locale("es", "CL"));
  }

  @Test
  @DisplayName("resolveLocale — maneja header con múltiples hífenes (ignora extras)")
  void resolveLocale_ignoresExtraHyphens() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("en-US-extra");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    // Toma "en" como language, "US" como región; ignora "-extra"
    assertThat(locale.getLanguage()).isEqualTo("en");
    assertThat(locale.getCountry()).isEqualTo("US");
  }

  @Test
  @DisplayName("resolveLocale — rechaza malformed header tipo '*'")
  void resolveLocale_rejectsWildcardOnly() {
    // Given
    when(request.getHeader("Accept-Language")).thenReturn("*");

    // When
    Locale locale = resolver.resolveLocale(request);

    // Then
    assertThat(locale).isEqualTo(Locale.US); // Fallback
  }
}
