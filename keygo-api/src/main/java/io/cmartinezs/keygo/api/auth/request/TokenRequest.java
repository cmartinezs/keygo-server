package io.cmartinezs.keygo.api.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request: Canjear código de autorización por token o rotar un refresh token.
 *
 * <p>Campos requeridos según el {@code grant_type}:
 * <ul>
 *   <li>{@code authorization_code}: clientId, code, redirectUri (codeVerifier para PKCE)</li>
 *   <li>{@code refresh_token}: clientId, refreshToken</li>
 * </ul>
 *
 * @param grantType    tipo de grant (authorization_code | refresh_token)
 * @param clientId     client_id de la app
 * @param code         código de autorización (solo en authorization_code grant)
 * @param codeVerifier verifier PKCE (solo en authorization_code grant con PKCE)
 * @param redirectUri  URI de redirección (solo en authorization_code grant)
 * @param refreshToken refresh token a rotar (solo en refresh_token grant)
 * @param scope        scopes solicitados (opcional en refresh_token grant)
 */
public record TokenRequest(
    String grantType,
    @NotBlank(message = "client_id is required") String clientId,
    String code,
    String codeVerifier,
    String redirectUri,
    String refreshToken,
    String scope) {

  /** Retorna el grantType normalizado; por defecto "authorization_code". */
  public String resolvedGrantType() {
    return grantType != null && !grantType.isBlank() ? grantType : "authorization_code";
  }
}


