package io.cmartinezs.keygo.run.config.security;

import io.cmartinezs.keygo.run.config.properties.KeyGoCorsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CORS configuration in SecurityConfig.
 * <p>Verifies that the CorsConfigurationSource bean is wired correctly
 * with the values declared in KeyGoCorsProperties.
 */
class CorsConfigTest {

  private SecurityConfig securityConfig;
  private KeyGoCorsProperties corsProperties;

  @BeforeEach
  void setUp() {
    securityConfig = new SecurityConfig();

    corsProperties = new KeyGoCorsProperties();
    // Use explicit values so the test is not tied to defaults
    corsProperties.setAllowedOrigins(List.of("http://localhost:5173"));
    corsProperties.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    corsProperties.setAllowedHeaders(List.of("*"));
    corsProperties.setAllowCredentials(true);
    corsProperties.setMaxAge(3600L);
  }

  // ─── Allowed origins ───────────────────────────────────────────────────────

  @Test
  void corsConfigurationSource_shouldAllowConfiguredOrigin() {
    // Given
    CorsConfigurationSource source = securityConfig.corsConfigurationSource(corsProperties);
    MockHttpServletRequest request = new MockHttpServletRequest("GET",
        "/api/v1/tenants/keygo/oauth2/authorize");
    request.setServletPath("/api/v1/tenants/keygo/oauth2/authorize");

    // When
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Then
    assertThat(config).isNotNull();
    assertThat(config.getAllowedOrigins()).contains("http://localhost:5173");
  }

  @Test
  void corsConfigurationSource_shouldAllowMultipleOrigins() {
    // Given
    corsProperties.setAllowedOrigins(List.of("http://localhost:5173", "https://app.acme.com"));
    CorsConfigurationSource source = securityConfig.corsConfigurationSource(corsProperties);
    MockHttpServletRequest request = new MockHttpServletRequest();

    // When
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Then
    assertThat(config.getAllowedOrigins())
        .containsExactlyInAnyOrder("http://localhost:5173", "https://app.acme.com");
  }

  // ─── Allowed methods ───────────────────────────────────────────────────────

  @Test
  void corsConfigurationSource_shouldIncludeOptionsForPreflight() {
    // Given
    CorsConfigurationSource source = securityConfig.corsConfigurationSource(corsProperties);
    MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS",
        "/api/v1/tenants/keygo/oauth2/authorize");

    // When
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Then
    assertThat(config.getAllowedMethods()).contains("OPTIONS");
  }

  @Test
  void corsConfigurationSource_shouldAllowAllOAuth2FlowMethods() {
    // Given
    CorsConfigurationSource source = securityConfig.corsConfigurationSource(corsProperties);
    MockHttpServletRequest request = new MockHttpServletRequest();

    // When
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Then — GET (authorize), POST (login, token), OPTIONS (preflight) are all needed
    assertThat(config.getAllowedMethods()).contains("GET", "POST", "OPTIONS");
  }

  // ─── Credentials ───────────────────────────────────────────────────────────

  @Test
  void corsConfigurationSource_shouldAllowCredentials() {
    // Given — credentials required for JSESSIONID between /authorize → /login
    CorsConfigurationSource source = securityConfig.corsConfigurationSource(corsProperties);
    MockHttpServletRequest request = new MockHttpServletRequest();

    // When
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Then
    assertThat(config.getAllowCredentials()).isTrue();
  }

  // ─── Max age ───────────────────────────────────────────────────────────────

  @Test
  void corsConfigurationSource_shouldHavePositiveMaxAge() {
    // Given
    CorsConfigurationSource source = securityConfig.corsConfigurationSource(corsProperties);
    MockHttpServletRequest request = new MockHttpServletRequest();

    // When
    CorsConfiguration config = source.getCorsConfiguration(request);

    // Then
    assertThat(config.getMaxAge()).isGreaterThan(0L);
  }

  // ─── Path coverage ─────────────────────────────────────────────────────────

  @Test
  void corsConfigurationSource_shouldApplyToAllPaths() {
    // Given — wildcard "/**" must cover all API paths
    CorsConfigurationSource source = securityConfig.corsConfigurationSource(corsProperties);

    // When — check a few representative paths
    String[] paths = {
        "/api/v1/tenants/keygo/oauth2/authorize",
        "/api/v1/tenants/keygo/account/login",
        "/api/v1/tenants/keygo/oauth2/token",
        "/api/v1/tenants/keygo/userinfo",
        "/api/v1/tenants/keygo/.well-known/jwks.json",
        "/actuator/health"
    };

    for (String path : paths) {
      MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
      request.setServletPath(path);

      // Then — every path must resolve a CORS config
      CorsConfiguration config = source.getCorsConfiguration(request);
      assertThat(config)
          .as("Expected CORS config for path: %s", path)
          .isNotNull();
    }
  }
}

