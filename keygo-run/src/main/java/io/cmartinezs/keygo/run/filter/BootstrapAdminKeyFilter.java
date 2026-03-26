package io.cmartinezs.keygo.run.filter;
import tools.jackson.databind.json.JsonMapper;
import io.cmartinezs.keygo.api.error.ApiErrorDataFactory;
import io.cmartinezs.keygo.api.error.ErrorData;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
/**
 * Security filter that validates Bearer JWT token for protected API endpoints.
 *
 * <p>Uses {@code request.getServletPath()} to strip the context-path from path comparisons.
 *
 * <p>Authentication option for protected paths:
 * {@code Authorization: Bearer <jwt>}.
 *
 * <p>Public paths (no authentication required):
 * <ul>
 *   <li>/actuator/**, /service/info**, /swagger-ui**, /v3/api-docs**, /.well-known</li>
 *   <li>Suffixes: /userinfo, /oauth2/revoke, /register, /verify-email, /resend-verification,
 *       /account/profile, /oauth2/authorize, /account/login, /oauth2/token</li>
 * </ul>
 */
@Slf4j
public class BootstrapAdminKeyFilter extends OncePerRequestFilter {
  private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
  private static final String BEARER_PREFIX = "Bearer ";
  private final KeyGoBootstrapProperties bootstrapProperties;
  private final JsonMapper jsonMapper;
  private final boolean includeTechnicalDetails;
  /**
   * Optional: available only when the 'supabase' profile is active.
   * Used to verify Bearer JWT tokens for admin access.
   */
  AccessTokenVerifierPort accessTokenVerifier;
  SigningKeyRepositoryPort signingKeyRepository;

  public BootstrapAdminKeyFilter(
      KeyGoBootstrapProperties bootstrapProperties,
      JsonMapper jsonMapper) {
    this(bootstrapProperties, jsonMapper, null, null, false);
  }

  public BootstrapAdminKeyFilter(
      KeyGoBootstrapProperties bootstrapProperties,
      JsonMapper jsonMapper,
      AccessTokenVerifierPort accessTokenVerifier,
      SigningKeyRepositoryPort signingKeyRepository) {
    this(bootstrapProperties, jsonMapper, accessTokenVerifier, signingKeyRepository, false);
  }

  public BootstrapAdminKeyFilter(
      KeyGoBootstrapProperties bootstrapProperties,
      JsonMapper jsonMapper,
      AccessTokenVerifierPort accessTokenVerifier,
      SigningKeyRepositoryPort signingKeyRepository,
      boolean includeTechnicalDetails) {
    this.bootstrapProperties = bootstrapProperties;
    this.jsonMapper = jsonMapper;
    this.accessTokenVerifier = accessTokenVerifier;
    this.signingKeyRepository = signingKeyRepository;
    this.includeTechnicalDetails = includeTechnicalDetails;
  }

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
      if (!authenticateBearer(request)) {
        log.warn("Invalid or missing authentication for path: {}", requestPath);
        writeErrorResponse(response, "Missing or invalid bearer token for protected API path");
        return;
      }
      log.debug("Authentication validated successfully for path: {}", requestPath);
    }
    try {
      filterChain.doFilter(request, response);
    } finally {
      SecurityContextHolder.clearContext();
    }
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
  private boolean authenticateBearer(HttpServletRequest request) {
    String authHeader = request.getHeader(AUTHORIZATION_HEADER);
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.warn("Missing or invalid Authorization header for path: {}", request.getServletPath());
      return false;
    }

    if (accessTokenVerifier == null || signingKeyRepository == null) {
      log.warn("Bearer verifier dependencies are unavailable for path: {}", request.getServletPath());
      return false;
    }

    String token = authHeader.substring(BEARER_PREFIX.length()).trim();
    if (token.isBlank()) {
      log.warn("Bearer token is blank for path: {}", request.getServletPath());
      return false;
    }

    try {
      var publicKeys = signingKeyRepository.findPublishableKeys();
      Map<String, Object> claims = accessTokenVerifier.verify(token, publicKeys);
      List<String> roles = extractRoles(claims.get("roles"));
      if (roles.isEmpty()) {
        log.warn("JWT has no usable 'roles' claim for path: {}", request.getServletPath());
        return false;
      }

      Collection<GrantedAuthority> authorities = roles.stream()
          .map(String::trim)
          .filter(role -> !role.isBlank())
          .map(role -> "ROLE_" + role)
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toSet());

      if (authorities.isEmpty()) {
        log.warn("JWT produced no authorities for path: {}", request.getServletPath());
        return false;
      }

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(claims, token, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      return true;
    } catch (Exception e) {
      log.warn("Bearer JWT validation failed for path {}: {}", request.getServletPath(), e.getMessage());
      return false;
    }
  }

  private List<String> extractRoles(Object rolesClaim) {
    if (rolesClaim instanceof Collection<?> collection) {
      return collection.stream()
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .toList();
    }
    if (rolesClaim instanceof String roleString) {
      return List.of(roleString.split(","));
    }
    return List.of();
  }

  // ─── Error response ───────────────────────────────────────────────────────
  private void writeErrorResponse(HttpServletResponse response, String detail) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    BaseResponse<ErrorData> errorResponse = BaseResponse.<ErrorData>builder()
        .failure(ResponseHelper.message(ResponseCode.AUTHENTICATION_REQUIRED))
        .data(ApiErrorDataFactory.fromDetail(
            ResponseCode.AUTHENTICATION_REQUIRED,
            detail,
            "BootstrapAdminKeyFilter",
            includeTechnicalDetails))
        .build();
    jsonMapper.writeValue(response.getWriter(), errorResponse);
  }
}
