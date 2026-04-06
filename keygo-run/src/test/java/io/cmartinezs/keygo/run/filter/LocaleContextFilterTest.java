package io.cmartinezs.keygo.run.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import io.cmartinezs.keygo.api.shared.KeyGoLocaleResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link LocaleContextFilter}.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>The resolved locale is set in {@code LocaleContextHolder} during filter execution.</li>
 *   <li>{@code en_US} (underscore notation) resolves to {@code en-US}.</li>
 *   <li>{@code es-CL} resolves to {@code es-CL}.</li>
 *   <li>Absent header falls back to {@code en-US} (default).</li>
 *   <li>{@code LocaleContextHolder} is cleared after request completion (no thread-local leak).</li>
 *   <li>{@code LocaleContextHolder} is cleared even when the filter chain throws.</li>
 *   <li>The filter chain is always called.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocaleContextFilter")
class LocaleContextFilterTest {

  @Mock
  private FilterChain filterChain;

  private LocaleContextFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    KeyGoLocaleResolver resolver = new KeyGoLocaleResolver();
    resolver.setDefaultLocale(Locale.US);
    resolver.setSupportedLocales(List.of(
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("es-CL"),
        Locale.forLanguageTag("en"),
        Locale.forLanguageTag("en-US")));
    filter = new LocaleContextFilter(resolver);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    LocaleContextHolder.resetLocaleContext();
  }

  @AfterEach
  void tearDown() {
    LocaleContextHolder.resetLocaleContext();
  }

  // ─── Locale resolution during filter execution ────────────────────────────

  @Test
  @DisplayName("should set en-US locale in LocaleContextHolder when Accept-Language is en_US (underscore)")
  void shouldSetEnUsLocaleForUnderscoreHeader() throws ServletException, IOException {
    // Given — the reported bug: en_US with underscore must resolve to en-US, not es-CL
    request.addHeader("Accept-Language", "en_US");
    AtomicReference<Locale> capturedLocale = new AtomicReference<>();
    doAnswer(inv -> {
      capturedLocale.set(LocaleContextHolder.getLocale());
      return null;
    }).when(filterChain).doFilter(any(), any());

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    assertThat(capturedLocale.get()).isEqualTo(Locale.forLanguageTag("en-US"));
  }

  @Test
  @DisplayName("should set es-CL locale when Accept-Language is es-CL")
  void shouldSetEsCLLocaleForSpanishHeader() throws ServletException, IOException {
    // Given
    request.addHeader("Accept-Language", "es-CL");
    AtomicReference<Locale> capturedLocale = new AtomicReference<>();
    doAnswer(inv -> {
      capturedLocale.set(LocaleContextHolder.getLocale());
      return null;
    }).when(filterChain).doFilter(any(), any());

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    assertThat(capturedLocale.get()).isEqualTo(Locale.forLanguageTag("es-CL"));
  }

  @Test
  @DisplayName("should set en-US (default) locale when Accept-Language header is absent")
  void shouldSetDefaultLocaleWhenHeaderAbsent() throws ServletException, IOException {
    // Given — no Accept-Language header
    AtomicReference<Locale> capturedLocale = new AtomicReference<>();
    doAnswer(inv -> {
      capturedLocale.set(LocaleContextHolder.getLocale());
      return null;
    }).when(filterChain).doFilter(any(), any());

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    assertThat(capturedLocale.get()).isEqualTo(Locale.US);
  }

  @Test
  @DisplayName("should set en-US (default) locale when Accept-Language is unsupported (e.g. ja-JP)")
  void shouldSetDefaultLocaleWhenUnsupportedLocale() throws ServletException, IOException {
    // Given
    request.addHeader("Accept-Language", "ja-JP");
    AtomicReference<Locale> capturedLocale = new AtomicReference<>();
    doAnswer(inv -> {
      capturedLocale.set(LocaleContextHolder.getLocale());
      return null;
    }).when(filterChain).doFilter(any(), any());

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    assertThat(capturedLocale.get()).isEqualTo(Locale.US);
  }

  // ─── LocaleContextHolder cleanup ──────────────────────────────────────────

  @Test
  @DisplayName("should clear LocaleContextHolder after successful request")
  void shouldClearLocaleContextAfterSuccessfulRequest() throws ServletException, IOException {
    // Given
    request.addHeader("Accept-Language", "es-CL");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then — locale context is reset; getLocale() returns the JVM default (not es-CL)
    // We verify the filter chain was called and no es-CL is pinned to the thread.
    verify(filterChain).doFilter(request, response);
    // LocaleContextHolder after reset returns the JVM default (not what the filter set)
    assertThat(LocaleContextHolder.getLocale()).isNotEqualTo(Locale.forLanguageTag("es-CL"));
  }

  @Test
  @DisplayName("should clear LocaleContextHolder even when filter chain throws ServletException")
  void shouldClearLocaleContextOnServletException() throws ServletException, IOException {
    // Given
    request.addHeader("Accept-Language", "es-CL");
    doThrow(new ServletException("simulated failure")).when(filterChain).doFilter(any(), any());

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(ServletException.class);
    // Locale context must be cleared despite the exception
    assertThat(LocaleContextHolder.getLocale()).isNotEqualTo(Locale.forLanguageTag("es-CL"));
  }

  @Test
  @DisplayName("should clear LocaleContextHolder even when filter chain throws IOException")
  void shouldClearLocaleContextOnIOException() throws ServletException, IOException {
    // Given — use es-CL so that after cleanup the assertion holds
    // (after resetLocaleContext(), getLocale() returns Locale.getDefault() which is en-US, not es-CL)
    request.addHeader("Accept-Language", "es-CL");
    doThrow(new IOException("simulated IO failure")).when(filterChain).doFilter(any(), any());

    // When / Then
    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(IOException.class);
    // After reset, locale returns JVM default (en-US), NOT the request's es-CL
    assertThat(LocaleContextHolder.getLocale()).isNotEqualTo(Locale.forLanguageTag("es-CL"));
  }

  // ─── Filter chain delegation ───────────────────────────────────────────────

  @Test
  @DisplayName("should always call the filter chain")
  void shouldAlwaysCallFilterChain() throws ServletException, IOException {
    // Given
    request.addHeader("Accept-Language", "en-US");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
  }
}




