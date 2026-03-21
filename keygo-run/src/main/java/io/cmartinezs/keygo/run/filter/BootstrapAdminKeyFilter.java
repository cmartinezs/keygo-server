package io.cmartinezs.keygo.run.filter;

import tools.jackson.databind.json.JsonMapper;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
  private final JsonMapper jsonMapper;

  @Override
  @SuppressWarnings("NullableProblems") // Parameters are guaranteed non-null by Spring Framework
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
        writeErrorResponse(response, HttpStatus.UNAUTHORIZED, ResponseCode.AUTHENTICATION_REQUIRED);
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
        || path.startsWith(bootstrapProperties.getServiceInfoPathPrefix())
        || (bootstrapProperties.getSwaggerUiPathPrefix() != null
            && path.startsWith(bootstrapProperties.getSwaggerUiPathPrefix()))
        || (bootstrapProperties.getApiDocsPathPrefix() != null
            && path.startsWith(bootstrapProperties.getApiDocsPathPrefix()));
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

  /**
   * Writes an error response directly to the HTTP response as JSON.
   * Escribe una respuesta de error directamente al HTTP response como JSON.
   *
   * @param response the HTTP response / la respuesta HTTP
   * @param status the HTTP status / el estado HTTP
   * @param responseCode the response code / el código de respuesta
   * @throws IOException if writing fails / si falla la escritura
   */
  private void writeErrorResponse(
      HttpServletResponse response,
      HttpStatus status,
      ResponseCode responseCode) throws IOException {

    BaseResponse<Void> errorResponse = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(responseCode))
        .build();

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    jsonMapper.writeValue(response.getWriter(), errorResponse);
  }
}

