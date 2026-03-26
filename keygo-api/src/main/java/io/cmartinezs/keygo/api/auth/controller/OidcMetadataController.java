package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.app.auth.result.OidcConfigurationResult;
import io.cmartinezs.keygo.app.auth.usecase.GetOidcConfigurationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller: expone el OIDC Discovery Document (OpenID Connect Discovery 1.0).
 *
 * <p>Retorna JSON nativo (no {@code BaseResponse}) para garantizar interoperabilidad con
 * librerías OAuth2/OIDC de terceros que usan este documento para auto-configurarse.
 *
 * <p>Ruta pública: no requiere autenticación.
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
@Tag(name = "OIDC Discovery", description = "Public OIDC discovery and JWKS endpoints — no authentication required. "
    + "Returns raw JSON (not BaseResponse) for compatibility with third-party OAuth2/OIDC libraries.")
public class OidcMetadataController {

  private final GetOidcConfigurationUseCase getOidcConfigurationUseCase;

  public OidcMetadataController(GetOidcConfigurationUseCase getOidcConfigurationUseCase) {
    this.getOidcConfigurationUseCase = getOidcConfigurationUseCase;
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/.well-known/openid-configuration
   *
   * <p>Retorna el discovery document OIDC para el tenant indicado.
   *
   * @param tenantSlug slug del tenant
   * @return JSON con todos los campos obligatorios de OIDC Discovery 1.0
   */
  @GetMapping("/.well-known/openid-configuration")
  @Operation(
      summary = "Get OIDC discovery document",
      description = "Returns the OpenID Connect Discovery 1.0 document for the specified tenant. "
          + "This document contains all endpoints (authorization, token, userinfo, jwks_uri) and "
          + "supported capabilities required for automatic client configuration.")
  @ApiResponse(responseCode = "200", description = "OIDC discovery document — raw JSON per OIDC Discovery 1.0 spec",
      content = @Content(mediaType = "application/json"))
  @ApiResponse(responseCode = "404", description = "Tenant not found")
  public ResponseEntity<Map<String, Object>> getOidcConfiguration(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug) {
    OidcConfigurationResult result = getOidcConfigurationUseCase.execute(tenantSlug);
    return ResponseEntity.ok(toMap(result));
  }

  /** Convierte el record a un mapa con las claves snake_case del estándar OIDC. */
  private Map<String, Object> toMap(OidcConfigurationResult r) {
    Map<String, Object> doc = new LinkedHashMap<>();
    doc.put("issuer", r.issuer());
    doc.put("authorization_endpoint", r.authorizationEndpoint());
    doc.put("token_endpoint", r.tokenEndpoint());
    doc.put("jwks_uri", r.jwksUri());
    doc.put("userinfo_endpoint", r.userinfoEndpoint());
    doc.put("response_types_supported", r.responseTypesSupported());
    doc.put("subject_types_supported", r.subjectTypesSupported());
    doc.put("id_token_signing_alg_values_supported", r.idTokenSigningAlgSupported());
    doc.put("scopes_supported", r.scopesSupported());
    doc.put("token_endpoint_auth_methods_supported", r.tokenEndpointAuthMethodsSupported());
    doc.put("grant_types_supported", r.grantTypesSupported());
    doc.put("claims_supported", r.claimsSupported());
    return doc;
  }
}

