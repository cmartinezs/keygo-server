package io.cmartinezs.keygo.run.filter;

import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security filter that validates admin key for protected API endpoints.
 * Filtro de seguridad que valida la clave de administrador para endpoints de API protegidos.
 *
 * <p>Protection rules / Reglas de protección:
 * - /actuator/** - Public, no authentication required / Público, sin autenticación requerida
 * - /service/info** - Public, no authentication required / Público, sin autenticación requerida
 * - /api/** - Protected, requires X-KEYGO-ADMIN header / Protegido, requiere header X-KEYGO-ADMIN
 *
 * @author cmartinezs
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminKeyFilter extends OncePerRequestFilter {

  private static final String ADMIN_KEY_HEADER = "X-KEYGO-ADMIN";

  private final KeyGoBootstrapProperties bootstrapProperties;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String requestPath = request.getRequestURI();

    log.debug("BootstrapAdminKeyFilter processing request: {}", requestPath);

    // Check if bootstrap is enabled
    if (!bootstrapProperties.isEnabled()) {
      log.debug("Bootstrap is disabled, allowing request without authentication");
      filterChain.doFilter(request, response);
      return;
    }

    // Allow public paths without authentication
    if (isPublicPath(requestPath)) {
      log.debug("Public path detected, allowing request: {}", requestPath);
      filterChain.doFilter(request, response);
      return;
    }

    // Protected API paths require authentication
    if (requestPath.startsWith(bootstrapProperties.getApiPathPrefix())) {
      if (!validateAdminKey(request)) {
        log.warn("Invalid or missing admin key for path: {}", requestPath);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
        return;
      }
      log.debug("Admin key validated successfully for path: {}", requestPath);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Checks if the path is public and doesn't require authentication.
   * Verifica si la ruta es pública y no requiere autenticación.
   *
   * @param path the request path / la ruta de la solicitud
   * @return true if public / true si es pública
   */
  private boolean isPublicPath(String path) {
    return path.startsWith(bootstrapProperties.getActuatorPathPrefix())
        || path.startsWith(bootstrapProperties.getServiceInfoPathPrefix());
  }

  /**
   * Validates the admin key from the request header.
   * Valida la clave de administrador del header de la solicitud.
   *
   * @param request the HTTP request / la solicitud HTTP
   * @return true if valid / true si es válida
   */
  private boolean validateAdminKey(HttpServletRequest request) {
    String providedKey = request.getHeader(ADMIN_KEY_HEADER);

    if (providedKey == null || providedKey.isBlank()) {
      log.warn("Missing admin key header for path: {}", request.getRequestURI());
      return false;
    }

    String expectedKey = bootstrapProperties.getAdminKey();

    if (expectedKey == null || expectedKey.isBlank()) {
      log.error("Admin key not configured in properties");
      return false;
    }

    return providedKey.equals(expectedKey);
  }
}

