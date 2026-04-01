package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.app.auth.usecase.GetJwksUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller: expone el JWKS endpoint (RFC 7517).
 *
 * <p>Retorna JSON nativo (no {@code BaseResponse}) para garantizar interoperabilidad con
 * librerías OAuth2 de terceros que consumen este endpoint directamente.
 *
 * <p>Ruta pública: no requiere autenticación.
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
@Tag(name = "OIDC Discovery", description = "Public OIDC discovery and JWKS endpoints — no authentication required. "
    + "Returns raw JSON (not BaseResponse) for compatibility with third-party OAuth2/OIDC libraries.")
public class JwksController {

  private final GetJwksUseCase getJwksUseCase;

  public JwksController(GetJwksUseCase getJwksUseCase) {
    this.getJwksUseCase = getJwksUseCase;
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/.well-known/jwks.json
   *
   * <p>Retorna el JWK Set con las claves públicas RSA activas y retiradas.
   *
   * @param tenantSlug slug del tenant
   * @return JSON con estructura {@code {"keys": [...]}}
   */
  @GetMapping("/.well-known/jwks.json")
  @Operation(
      summary = "Get JWK Set",
      description = "Returns the JSON Web Key Set (RFC 7517) containing the active and retired RSA "
          + "public keys used to sign JWT tokens for the specified tenant. "
          + "Used by relying parties to verify token signatures.")
  @ApiResponse(responseCode = "200", description = "JWK Set retrieved — raw JSON `{\"keys\": [...]}`",
      content = @Content(mediaType = "application/json"))
  @ApiResponse(responseCode = "404", description = "Tenant not found (code: RESOURCE_NOT_FOUND)")
  public ResponseEntity<Map<String, Object>> getJwks(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug) {
    return ResponseEntity.ok(getJwksUseCase.execute());
  }
}
