package io.cmartinezs.keygo.api.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request: Canjear código de autorización por token, rotar un refresh token o emitir token M2M.
 *
 * <p>Campos requeridos según el {@code grant_type}:
 * <ul>
 *   <li>{@code authorization_code}: clientId, code, redirectUri (codeVerifier para PKCE)</li>
 *   <li>{@code refresh_token}: clientId, refreshToken</li>
 *   <li>{@code client_credentials}: clientId, clientSecret</li>
 * </ul>
 *
 * <p>El mapeo snake_case ↔ camelCase (p. ej. {@code grant_type} → {@code grantType}) es gestionado
 * globalmente por {@code SnakeCaseAliasModule} en {@code ApplicationConfig}. No se requieren
 * anotaciones {@code @JsonProperty} en este DTO.
 *
 * @param grantType    tipo de grant (authorization_code | refresh_token | client_credentials)
 * @param clientId     client_id de la app
 * @param clientSecret client_secret en texto plano (solo en client_credentials grant)
 * @param code         código de autorización (solo en authorization_code grant)
 * @param codeVerifier verifier PKCE (solo en authorization_code grant con PKCE)
 * @param redirectUri  URI de redirección (solo en authorization_code grant)
 * @param refreshToken refresh token a rotar (solo en refresh_token grant)
 * @param scope        scopes solicitados (opcional)
 */
public record TokenRequest(
    String grantType,
    @NotBlank(message = "client_id is required") String clientId,
    String clientSecret,
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
