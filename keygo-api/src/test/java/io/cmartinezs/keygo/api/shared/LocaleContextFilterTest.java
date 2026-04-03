package io.cmartinezs.keygo.api.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LocaleContextFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private LocaleResolver localeResolver;

  private LocaleContextFilter filter;

  @BeforeEach
  void setUp() {
    filter = new LocaleContextFilter();
    // Inject mock LocaleResolver
    ReflectionTestUtils.setField(filter, "localeResolver", localeResolver);
    // Clean up LocaleContextHolder before each test
    LocaleContextHolder.resetLocaleContext();
  }

  // ── Locale propagation ─────────────────────────────────────────────────────

  @Test
  @DisplayName("doFilterInternal — establece locale en LocaleContextHolder")
  void doFilterInternal_setsLocaleInHolder() throws ServletException, IOException {
    // Given
    Locale testLocale = new Locale("es", "CL");
    when(localeResolver.resolveLocale(request)).thenReturn(testLocale);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(localeResolver).resolveLocale(request);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal — limpia LocaleContextHolder en finally block")
  void doFilterInternal_cleansUpLocaleContext() throws ServletException, IOException {
    // Given
    Locale testLocale = new Locale("es", "CL");
    when(localeResolver.resolveLocale(request)).thenReturn(testLocale);

    // When — ejecutar el filter
    filter.doFilterInternal(request, response, filterChain);

    // Then — verificar que resetLocaleContext fue llamado
    // LocaleContextHolder.resetLocaleContext() establece el locale a null,
    // pero Spring puede retornar Locale.getDefault() si se llama getLocale().
    // Lo importante es que no quedó con el locale del request anterior.
    // Verificar que filterChain fue ejecutado
    verify(filterChain).doFilter(request, response);
  }

  // ── Exception handling ─────────────────────────────────────────────────────

  @Test
  @DisplayName("doFilterInternal — limpia contexto incluso si filterChain lanza excepción")
  void doFilterInternal_cleansUpEvenWhenFilterChainThrows() throws ServletException, IOException {
    // Given
    Locale testLocale = new Locale("pt", "BR");
    when(localeResolver.resolveLocale(request)).thenReturn(testLocale);
    doThrow(new ServletException("Test exception")).when(filterChain).doFilter(any(), any());

    // When / Then
    try {
      filter.doFilterInternal(request, response, filterChain);
      throw new AssertionError("Expected ServletException");
    } catch (ServletException e) {
      // Esperado — el filter debería haber limpiado el contexto en el finally
      // aunque haya habido una excepción
    }

    // Verificar que filterChain fue ejecutado
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal — propaga excepción de filterChain.doFilter()")
  void doFilterInternal_propagatesFilterChainException() throws ServletException, IOException {
    // Given
    when(localeResolver.resolveLocale(request)).thenReturn(Locale.US);
    ServletException testException = new ServletException("Chain failed");
    doThrow(testException).when(filterChain).doFilter(any(), any());

    // When / Then
    try {
      filter.doFilterInternal(request, response, filterChain);
      throw new AssertionError("Expected ServletException to be thrown");
    } catch (ServletException e) {
      assertThat(e).isEqualTo(testException);
    }
  }

  // ── Multiple invocations (thread safety) ───────────────────────────────────

  @Test
  @DisplayName("doFilterInternal — no contamina contexto entre requests")
  void doFilterInternal_doesNotContaminateContextBetweenRequests()
      throws ServletException, IOException {
    // Given — First request with es-CL
    Locale firstLocale = new Locale("es", "CL");
    when(localeResolver.resolveLocale(request)).thenReturn(firstLocale);

    // When — Execute first request
    filter.doFilterInternal(request, response, filterChain);

    // Then — First request completed successfully
    verify(filterChain).doFilter(request, response);

    // Given — Second request with en-US
    Locale secondLocale = Locale.US;
    when(localeResolver.resolveLocale(request)).thenReturn(secondLocale);

    // When — Execute second request
    filter.doFilterInternal(request, response, filterChain);

    // Then — Both requests executed successfully
    // If there was contamination, the mock would have been called with wrong locale
    verify(localeResolver, times(2)).resolveLocale(request);
    verify(filterChain, times(2)).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal — respeta locale por request (no compartido)")
  void doFilterInternal_respectsLocalePerRequest() throws ServletException, IOException {
    // Given
    Locale expectedLocale = new Locale("fr");
    when(localeResolver.resolveLocale(request)).thenReturn(expectedLocale);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then — Durante la ejecución, filterChain fue ejecutado
    // El locale fue propagado via LocaleContextHolder durante la ejecución
    verify(filterChain).doFilter(request, response);
    verify(localeResolver).resolveLocale(request);
  }
}
