package io.cmartinezs.keygo.run.filter;
import tools.jackson.databind.json.JsonMapper;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
/**
 * Security filter that validates admin key or admin JWT Bearer token for protected API endpoints.
 *
 * <p>Uses {@code request.getServletPath()} to strip the context-path from path comparisons.
 *
 * <p>Authentication options for protected paths:
 * <ol>
 *   <li>{@code X-KEYGO-ADMIN: <key>} header — service-to-service / CLI usage</li>
 *   <li>{@code Authorization: Bearer <jwt>} with a {@code roles} claim containing any value
 *       from {@code keygo.bootstrap.admin-roles} — browser / frontend usage</li>
 * </ol>
 *
 * <p>Public paths (no authentication required):
 * <ul>
 *   <li>/actuator/**, /service/info**, /swagger-ui**, /v3/api-docs**, /.well-known</li>
 *   <li>Suffixes: /userinfo, /oauth2/revoke, /register, /verify-email, /resend-verification,
 *       /account/profile, /oauth2/authorize, /account/login, /oauth2/token</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminKeyFilter extends OncePerRequestFilter {
  private static final String ADMIN_KEY_HEADER = "X-KEYGO-ADMIN";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private final KeyGoBootstrapProperties bootstrapProperties;
  private final JsonMapper jsonMapper;
  /**
   * Optional: available only when the 'supabase' profile is active.
   * Used to verify Bearer JWT tokens for admin access.
   */
  @Autowired(required = false)
  AccessTokenVerifierPort accessTokenVerifier;
  @Autowired(required = false)
  SigningKeyRepositoryPort signingKeyRepository;
  @Override
  @SuppressWarnings("NullableProblems")
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String requestPath = request.getServletPath();
    log.debug("BootstrapAdminKeyFilter processing request: {} (URI: {})", requestPath, request.getRequestURI());
    if (!bootstrapProperties.isEnabled()) {
      log.debug("Bootstrap is disabled, allowing request without authentication");
      filterChain.doFilter(request, response);
      return;
    }
    if (isPublicPath(requestPath)) {
      log.debug("Public path detected, allowing request: {}", requestPath);
      filterChain.doFilter(request, response);
      return;
    }
    if (requestPath.startsWith(bootstrapProperties.getApiPathPrefix())) {
      if (!validateAuthentication(request)) {
        log.warn("Invalid or missing authentication for path: {}", requestPath);
        writeErrorResponse(response);
        return;
      }
      log.debug("Authentication validated successfully for path: {}", requestPath);
    }
    filterChain.doFilter(request, response);
  }
  // ─── Public path detection ────────────────────────────────────────────────
  private boolean isPublicPath(String path) {
    return isPublicByPrefix(path) || isPublicBySegment(path) || isPublicBySuffix(path);
  }
  private boolean isPublicByPrefix(String path) {
    return hasPrefix(path, bootstrapProperties.getActuatorPathPrefix())
        || hasPrefix(path, bootstrapProperties.getServiceInfoPathPrefix())
        || hasPrefix(path, bootstrapProperties.getSwaggerUiPathPrefix())
        || hasPrefix(path, bootstrapProperties.getApiDocsPathPrefix());
  }
  private boolean isPublicBySegment(String path) {
    return hasSegment(path, bootstrapProperties.getWellKnownPathPrefix());
  }
  private boolean isPublicBySuffix(String path) {
    return hasSuffix(path, bootstrapProperties.getUserInfoPathSuffix())
        || hasSuffix(path, bootstrapProperties.getRevocationPathSuffix())
        || hasSuffix(path, bootstrapProperties.getRegisterPathSuffix())
        || hasSuffix(path, bootstrapProperties.getVerifyEmailPathSuffix())
        || hasSuffix(path, bootstrapProperties.getResendVerificationPathSuffix())
        || hasSuffix(path, bootstrapProperties.getAccountProfilePathSuffix())
        || hasSuffix(path, bootstrapProperties.getAuthorizePathSuffix())
        || hasSuffix(path, bootstrapProperties.getLoginPathSuffix())
        || hasSuffix(path, bootstrapProperties.getTokenPathSuffix());
  }
  private static boolean hasPrefix(String path, String prefix) {
    return prefix != null && path.startsWith(prefix);
  }
  private static boolean hasSegment(String path, String segment) {
    return segment != null && path.contains(segment);
  }
  private static boolean hasSuffix(String path, String suffix) {
    return suffix != null && path.endsWith(suffix);
  }
  // ─── Authentication ───────────────────────────────────────────────────────
  /**
   * Validates the request via X-KEYGO-ADMIN header (primary) or Bearer JWT (secondary).
   */
  private boolean validateAuthentication(HttpServletRequest request) {
    String adminKeyHeader = request.getHeader(ADMIN_KEY_HEADER);
    if (adminKeyHeader != null && !adminKeyHeader.isBlank()) {
      return validateAdminKey(request);
    }
    String authHeader = request.getHeader(AUTHORIZATION_HEADER);
    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)
        && accessTokenVerifier != null && signingKeyRepository != null) {
      return validateBearerAdminToken(authHeader.substring(BEARER_PREFIX.length()), request.getServletPath());
    }
    log.warn("No valid authentication provided for path: {}", request.getServletPath());
    return false;
  }
  private boolean validateAdminKey(HttpServletRequest request) {
    String providedKey = request.getHeader(ADMIN_KEY_HEADER);
    if (providedKey == null || providedKey.isBlank()) {
      log.warn("Missing admin key header for path: {}", request.getServletPath());
      return false;
    }
    String expectedKey = bootstrapProperties.getAdminKey();
    if (expectedKey == null || expectedKey.isBlank()) {
      log.error("Admin key not configured in properties");
      return false;
    }
    return providedKey.equals(expectedKey);
  }
  /**
   * Validates a Bearer JWT and checks that its {@code roles} claim contains at least one
   * of the configured {@code adminRoles}.
   */
  private boolean validateBearerAdminToken(String token, String path) {
    try {
      var publicKeys = signingKeyRepository.findPublishableKeys();
      Map<String, Object> claims = accessTokenVerifier.verify(token, publicKeys);
      @SuppressWarnings("unchecked")
      List<String> roles = (List<String>) claims.get("roles");
      if (roles == null || roles.isEmpty()) {
        log.warn("JWT has no 'roles' claim — access denied for path: {}", path);
        return false;
      }
      List<String> requiredAdminRoles = bootstrapProperties.getAdminRoles();
      boolean hasAdminRole = roles.stream().anyMatch(requiredAdminRoles::contains);
      if (!hasAdminRole) {
        log.warn("JWT roles {} do not include any admin role {} for path: {}", roles, requiredAdminRoles, path);
        return false;
      }
      log.debug("Bearer JWT validated with admin role for path: {}", path);
      return true;
    } catch (Exception e) {
      log.warn("Bearer JWT validation failed for path {}: {}", path, e.getMessage());
      return false;
    }
  }
  // ─── Error response ───────────────────────────────────────────────────────
  private void writeErrorResponse(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    BaseResponse<Void> errorResponse = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.AUTHENTICATION_REQUIRED))
        .build();
    jsonMapper.writeValue(response.getWriter(), errorResponse);
  }
}
