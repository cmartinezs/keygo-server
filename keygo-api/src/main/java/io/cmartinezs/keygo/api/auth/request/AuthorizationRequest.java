package io.cmartinezs.keygo.api.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request: Iniciar flujo de autorización OAuth 2.0.
 *
 * <p>Encapsula los parámetros estándar de OAuth 2.0 Authorization endpoint.
 */
public record AuthorizationRequest(
    @NotBlank(message = "client_id is required") String clientId,
    @NotBlank(message = "redirect_uri is required") String redirectUri,
    @NotBlank(message = "scope is required") String scope,
    @NotBlank(message = "response_type is required") String responseType,
    String state,
    String codeChallengeMethod,
    String codeChallenge) {

  public AuthorizationRequest {
    if (responseType == null || !responseType.equals("code")) {
      throw new IllegalArgumentException("response_type must be 'code' for this implementation");
    }
  }
}

