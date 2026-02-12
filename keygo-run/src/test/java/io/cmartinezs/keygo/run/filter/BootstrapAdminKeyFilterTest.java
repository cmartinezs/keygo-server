package io.cmartinezs.keygo.run.filter;

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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for BootstrapAdminKeyFilter
 * Pruebas unitarias para BootstrapAdminKeyFilter
 *
 * @author cmartinezs
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class BootstrapAdminKeyFilterTest {

  @Mock
  private KeyGoBootstrapProperties bootstrapProperties;

  @Mock
  private FilterChain filterChain;

  private BootstrapAdminKeyFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new BootstrapAdminKeyFilter(bootstrapProperties);
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
    request.setRequestURI("/api/v1/test");

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
    request.setRequestURI(publicPath);

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
    request.setRequestURI("/api/v1/test");
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
    request.setRequestURI("/api/v1/test");
    if (headerAdminKey != null) {
      request.addHeader("X-KEYGO-ADMIN", headerAdminKey);
    }

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @ParameterizedTest
  @ValueSource(strings = {"   ", "  "})
  void doFilterInternal_shouldRejectApiPathWithBlankAdminKeyHeader(String blankKey) throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    // Not setting adminKey stub because validation fails on blank header before checking properties
    request.setRequestURI("/api/v1/test");
    request.addHeader("X-KEYGO-ADMIN", blankKey);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void doFilterInternal_shouldAllowNonApiPathWithoutAuth() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    request.setRequestURI("/other/path");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }
}



