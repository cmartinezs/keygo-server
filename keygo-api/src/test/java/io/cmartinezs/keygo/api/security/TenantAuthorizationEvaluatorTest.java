package io.cmartinezs.keygo.api.security;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;

class TenantAuthorizationEvaluatorTest {

  private final TenantAuthorizationEvaluator evaluator = new TenantAuthorizationEvaluator();

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void hasTenantAccess_shouldAllowAdminRoleWithoutTenantClaim() {
    // Given
    setTenantPathVariable("acme");
    var authentication = new UsernamePasswordAuthenticationToken(
        Map.of("sub", "user-1"),
        "token",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    // When
    boolean result = evaluator.hasTenantAccess(authentication);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void hasTenantAccess_shouldAllowAdminTenantWhenTenantClaimMatchesPath() {
    // Given
    setTenantPathVariable("acme");
    var authentication = new UsernamePasswordAuthenticationToken(
        Map.of("sub", "user-1", "tenant_slug", "acme"),
        "token",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN_TENANT")));

    // When
    boolean result = evaluator.hasTenantAccess(authentication);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void hasTenantAccess_shouldDenyAdminTenantWhenTenantClaimDoesNotMatchPath() {
    // Given
    setTenantPathVariable("acme");
    var authentication = new UsernamePasswordAuthenticationToken(
        Map.of("sub", "user-1", "tenant_slug", "globex"),
        "token",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN_TENANT")));

    // When
    boolean result = evaluator.hasTenantAccess(authentication);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void hasTenantAccess_shouldResolveTenantFromIssuerFallback() {
    // Given
    setTenantPathVariable("acme");
    var authentication = new UsernamePasswordAuthenticationToken(
        Map.of("sub", "user-1", "iss", "http://localhost:8080/keygo-server/api/v1/tenants/acme"),
        "token",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN_TENANT")));

    // When
    boolean result = evaluator.hasTenantAccess(authentication);

    // Then
    assertThat(result).isTrue();
  }

  private void setTenantPathVariable(String tenantSlug) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(
        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
        Map.of("tenantSlug", tenantSlug));
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }
}

