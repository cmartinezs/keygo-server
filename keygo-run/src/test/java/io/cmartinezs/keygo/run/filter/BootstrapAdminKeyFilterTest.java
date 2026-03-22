package io.cmartinezs.keygo.run.filter;

import tools.jackson.databind.json.JsonMapper;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for BootstrapAdminKeyFilter
 * Pruebas unitarias para BootstrapAdminKeyFilter
 *
 * <p>Tests use {@code request.setServletPath()} (not {@code setRequestURI()}) to match
 * the fix applied in the filter: the filter now reads {@code getServletPath()} so that
 * the context-path ({@code /keygo-server}) is excluded and prefixes like {@code /api/} match.
 *
 * @author cmartinezs
 * @version 1.1
 */
@ExtendWith(MockitoExtension.class)
class BootstrapAdminKeyFilterTest {

  @Mock
  private KeyGoBootstrapProperties bootstrapProperties;

  @Mock
  private JsonMapper jsonMapper;

  @Mock
  private FilterChain filterChain;

  private BootstrapAdminKeyFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new BootstrapAdminKeyFilter(bootstrapProperties, jsonMapper);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();

    // Mock default path prefixes with lenient() to avoid UnnecessaryStubbingException
    lenient().when(bootstrapProperties.getApiPathPrefix()).thenReturn("/api/");
    lenient().when(bootstrapProperties.getActuatorPathPrefix()).thenReturn("/actuator/");
    lenient().when(bootstrapProperties.getServiceInfoPathPrefix()).thenReturn("/service/info");
  }

  @Test
  void doFilterInternal_shouldAllowRequestWhenBootstrapDisabled() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(false);
    request.setServletPath("/api/v1/test");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/actuator/health", "/actuator/metrics", "/service/info", "/service/info/details"})
  void doFilterInternal_shouldAllowPublicPathsWithoutAuth(String publicPath) throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    request.setServletPath(publicPath);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void doFilterInternal_shouldAllowApiPathWithValidAdminKey() throws ServletException, IOException {
    // Given
    String adminKey = "valid-admin-key";
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(bootstrapProperties.getAdminKey()).thenReturn(adminKey);
    request.setServletPath("/api/v1/test");
    request.addHeader("X-KEYGO-ADMIN", adminKey);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  /**
   * Provides test cases for authentication rejection scenarios
   * Proporciona casos de prueba para escenarios de rechazo de autenticación
   */
  static Stream<Arguments> authenticationRejectionScenarios() {
    return Stream.of(
        // Scenario: Invalid admin key in header
        Arguments.of("invalid-key", "valid-admin-key", "Invalid admin key"),
        // Scenario: Missing admin key header
        Arguments.of(null, "valid-admin-key", "Missing admin key header"),
        // Scenario: Null admin key in properties
        Arguments.of("some-key", null, "Null admin key in properties"),
        // Scenario: Blank admin key in properties
        Arguments.of("some-key", "   ", "Blank admin key in properties")
    );
  }

  @ParameterizedTest(name = "{2}: headerKey={0}, propertyKey={1}")
  @MethodSource("authenticationRejectionScenarios")
  void doFilterInternal_shouldRejectApiPathWithInvalidAuthentication(
      String headerAdminKey,
      String propertyAdminKey,
      String scenario) throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    lenient().when(bootstrapProperties.getAdminKey()).thenReturn(propertyAdminKey);
    request.setServletPath("/api/v1/test");
    if (headerAdminKey != null) {
      request.addHeader("X-KEYGO-ADMIN", headerAdminKey);
    }

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).startsWith("application/json");
    verify(jsonMapper).writeValue(any(Writer.class), any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"   ", "  "})
  void doFilterInternal_shouldRejectApiPathWithBlankAdminKeyHeader(String blankKey) throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    // Not setting adminKey stub because validation fails on blank header before checking properties
    request.setServletPath("/api/v1/test");
    request.addHeader("X-KEYGO-ADMIN", blankKey);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).startsWith("application/json");
    verify(jsonMapper).writeValue(any(Writer.class), any());
  }

  @Test
  void doFilterInternal_shouldAllowNonApiPathWithoutAuth() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    request.setServletPath("/other/path");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  /**
   * Regression test for the bug where getRequestURI() was used instead of getServletPath().
   * When a context-path is active (e.g. /keygo-server), getRequestURI() returns
   * /keygo-server/api/v1/... which does NOT start with /api/ and the filter would skip auth.
   * Using getServletPath() (which strips the context-path) fixes this.
   */
  @Test
  void doFilterInternal_shouldRejectApiPathWithContextPathInUri_whenAdminKeyMissing()
      throws ServletException, IOException {
    // Given – simulates a request under context-path /keygo-server
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    request.setContextPath("/keygo-server");
    request.setRequestURI("/keygo-server/api/v1/tenants");
    // servletPath is the path WITHOUT the context-path — this is what the fixed filter reads
    request.setServletPath("/api/v1/tenants");
    // No X-KEYGO-ADMIN header provided

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then – filter must block the request, not let it through
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void doFilterInternal_shouldAllowApiPathWithContextPathInUri_whenAdminKeyValid()
      throws ServletException, IOException {
    // Given – simulates a request under context-path /keygo-server
    String adminKey = "valid-admin-key";
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(bootstrapProperties.getAdminKey()).thenReturn(adminKey);
    request.setContextPath("/keygo-server");
    request.setRequestURI("/keygo-server/api/v1/tenants");
    request.setServletPath("/api/v1/tenants");
    request.addHeader("X-KEYGO-ADMIN", adminKey);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }
}



