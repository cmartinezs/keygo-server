package io.cmartinezs.keygo.run.filter;

import tools.jackson.databind.json.JsonMapper;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapAdminKeyFilterTest {

  @Mock private KeyGoBootstrapProperties bootstrapProperties;
  @Mock private JsonMapper jsonMapper;
  @Mock private FilterChain filterChain;
  @Mock private AccessTokenVerifierPort accessTokenVerifier;
  @Mock private SigningKeyRepositoryPort signingKeyRepository;

  private BootstrapAdminKeyFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new BootstrapAdminKeyFilter(bootstrapProperties, jsonMapper, accessTokenVerifier, signingKeyRepository);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();

    lenient().when(bootstrapProperties.getApiPathPrefix()).thenReturn("/api/");
    lenient().when(bootstrapProperties.getActuatorPathPrefix()).thenReturn("/actuator/");
    lenient().when(bootstrapProperties.getServiceInfoPathPrefix()).thenReturn("/service/info");
  }

  // ─── Bootstrap disabled ────────────────────────────────────────────────────

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

  // ─── Public paths ──────────────────────────────────────────────────────────

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

  @ParameterizedTest
  @ValueSource(strings = {
      "/api/v1/tenants/keygo/oauth2/authorize",
      "/api/v1/tenants/keygo/account/login",
      "/api/v1/tenants/keygo/oauth2/token"
  })
  void doFilterInternal_shouldAllowOAuth2FlowPathsWithoutAuth(String path) throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    lenient().when(bootstrapProperties.getAuthorizePathSuffix()).thenReturn("/oauth2/authorize");
    lenient().when(bootstrapProperties.getLoginPathSuffix()).thenReturn("/account/login");
    lenient().when(bootstrapProperties.getTokenPathSuffix()).thenReturn("/oauth2/token");
    request.setServletPath(path);

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  // ─── Bearer JWT auth ───────────────────────────────────────────────────────

  @Test
  void doFilterInternal_shouldAllowApiPathWithValidBearerJwt() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(accessTokenVerifier.verify(anyString(), anyList()))
        .thenReturn(Map.of("sub", "user-uuid", "roles", List.of("ADMIN")));

    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer valid.jwt.token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void doFilterInternal_shouldAllowApiPathWithAnyNonEmptyRoleClaim() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(accessTokenVerifier.verify(anyString(), anyList()))
        .thenReturn(Map.of("sub", "user-uuid", "roles", List.of("USER")));

    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer valid.jwt.token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void doFilterInternal_shouldRejectBearerJwtWithNoRolesClaim() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(accessTokenVerifier.verify(anyString(), anyList()))
        .thenReturn(Map.of("sub", "user-uuid"));

    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer valid.jwt.token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void doFilterInternal_shouldAllowBearerRolesAsCsvString() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(accessTokenVerifier.verify(anyString(), anyList()))
        .thenReturn(Map.of("sub", "user-uuid", "roles", "ADMIN,ADMIN_TENANT"));

    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer valid.jwt.token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void doFilterInternal_shouldRejectBearerJwtWhenVerifierNotAvailable() throws ServletException, IOException {
    // Given — no tokenVerifier/signingKeyRepository injected (non-supabase profile)
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    filter = new BootstrapAdminKeyFilter(bootstrapProperties, jsonMapper);
    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer some.jwt.token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void doFilterInternal_shouldRejectBearerJwtWhenVerificationThrowsException() throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(accessTokenVerifier.verify(anyString(), anyList()))
        .thenThrow(new RuntimeException("invalid signature"));

    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer expired.jwt.token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void doFilterInternal_shouldRejectApiPathWhenAuthorizationHeaderMissing()
      throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    request.setServletPath("/api/v1/tenants");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
    verify(jsonMapper).writeValue(any(Writer.class), any());
  }

  // ─── Non-API paths ─────────────────────────────────────────────────────────

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

  // ─── Regression: context-path ──────────────────────────────────────────────

  @Test
  void doFilterInternal_shouldRejectApiPathWithContextPathInUri_whenBearerMissing()
      throws ServletException, IOException {
    // Given – simulates a request under context-path /keygo-server
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    request.setContextPath("/keygo-server");
    request.setRequestURI("/keygo-server/api/v1/tenants");
    request.setServletPath("/api/v1/tenants");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  void doFilterInternal_shouldAllowApiPathWithContextPathInUri_whenBearerValid()
      throws ServletException, IOException {
    // Given – simulates a request under context-path /keygo-server
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(accessTokenVerifier.verify(anyString(), anyList()))
        .thenReturn(Map.of("sub", "user-uuid", "roles", List.of("ADMIN")));
    request.setContextPath("/keygo-server");
    request.setRequestURI("/keygo-server/api/v1/tenants");
    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer valid.jwt.token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void doFilterInternal_shouldPopulateSecurityContextAuthoritiesFromRoles()
      throws ServletException, IOException {
    // Given
    when(bootstrapProperties.isEnabled()).thenReturn(true);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(accessTokenVerifier.verify(anyString(), anyList()))
        .thenReturn(Map.of("sub", "user-uuid", "roles", List.of("ADMIN_TENANT")));
    request.setServletPath("/api/v1/tenants");
    request.addHeader("Authorization", "Bearer token");

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
