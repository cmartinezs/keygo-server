package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.app.auth.usecase.GetJwksUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller: expone JWKS y OIDC discovery para la plataforma.
 *
 * <p>Retorna JSON nativo (no {@code BaseResponse}) para interoperabilidad con librerías
 * OAuth2/OIDC de terceros. Las claves expuestas son las globales (tenant_id IS NULL).
 *
 * <p>Ruta pública: no requiere autenticación.
 */
@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform OIDC", description = "Public OIDC discovery and JWKS for platform authentication — no auth required.")
public class PlatformOidcMetadataController {

  private final GetJwksUseCase getJwksUseCase;

  public PlatformOidcMetadataController(GetJwksUseCase getJwksUseCase) {
    this.getJwksUseCase = getJwksUseCase;
  }

  /**
   * GET /api/v1/platform/.well-known/jwks.json
   *
   * <p>Retorna el JWK Set con las claves públicas RSA globales (usadas para firmar tokens
   * de plataforma).
   */
  @GetMapping("/.well-known/jwks.json")
  @Operation(
      summary = "Get platform JWK Set",
      description = "Returns the JSON Web Key Set (RFC 7517) containing the global RSA public keys "
          + "used to sign platform JWT tokens. Used by the UI to verify id_token signatures.")
  @ApiResponse(responseCode = "200", description = "JWK Set — raw JSON `{\"keys\": [...]}`",
      content = @Content(mediaType = "application/json"))
  public ResponseEntity<Map<String, Object>> getJwks() {
    return ResponseEntity.ok(getJwksUseCase.execute());
  }
}
