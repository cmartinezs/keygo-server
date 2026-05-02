package io.cmartinezs.keygo.api.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class TenantAuthorizationEvaluator {

  public boolean hasTenantAccess(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    if (hasAdminRole(authentication)) {
      return true;
    }

    String requestedTenantSlug = resolveTenantSlugFromPath();
    if (requestedTenantSlug == null || requestedTenantSlug.isBlank()) {
      return false;
    }

    String tokenTenantSlug = resolveTenantSlugFromClaims(authentication);
    if (tokenTenantSlug == null || tokenTenantSlug.isBlank()) {
      return false;
    }

    return requestedTenantSlug.equalsIgnoreCase(tokenTenantSlug);
  }

  private boolean hasAdminRole(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(a -> "ROLE_ADMIN".equals(a) || "ROLE_KEYGO_ADMIN".equals(a));
  }

  private String resolveTenantSlugFromPath() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      return null;
    }

    HttpServletRequest request = attributes.getRequest();
    Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (!(pathVariables instanceof Map<?, ?> vars)) {
      return null;
    }

    Object tenantSlug = vars.get("tenantSlug");
    return tenantSlug instanceof String value ? value : null;
  }

  private String resolveTenantSlugFromClaims(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Map<?, ?> claims)) {
      return null;
    }

    Object explicitTenant = claims.get("tenant_slug");
    if (explicitTenant instanceof String value && !value.isBlank()) {
      return value;
    }

    Object issuerClaim = claims.get("iss");
    if (!(issuerClaim instanceof String issuer) || issuer.isBlank()) {
      return null;
    }

    String marker = "/api/v1/tenants/";
    int markerIndex = issuer.indexOf(marker);
    if (markerIndex < 0) {
      return null;
    }

    String tail = issuer.substring(markerIndex + marker.length());
    int slashIndex = tail.indexOf('/');
    return slashIndex >= 0 ? tail.substring(0, slashIndex) : tail;
  }
}


