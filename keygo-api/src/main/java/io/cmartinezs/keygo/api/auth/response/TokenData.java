package io.cmartinezs.keygo.api.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response: resultado del canje de authorization code o rotación de refresh token.
 *
 * <p>Campos estándar del token response (RFC 6749 + OIDC Core 1.0).
 *
 * @param accessToken         JWT firmado para acceder a recursos protegidos
 * @param idToken             JWT OIDC con claims de identidad del usuario
 * @param refreshToken        Refresh token plano (solo en la respuesta, nunca almacenar en client)
 * @param tokenType           tipo de token (siempre "Bearer")
 * @param expiresIn           segundos hasta la expiración del access_token
 * @param scope               scopes otorgados, separados por espacio
 * @param authorizationCodeId ID del código canjeado (referencia de auditoría; null en refresh_token grant)
 */
public record TokenData(
    @JsonProperty("access_token")          String accessToken,
    @JsonProperty("id_token")             String idToken,
    @JsonProperty("refresh_token")        String refreshToken,
    @JsonProperty("token_type")           String tokenType,
    @JsonProperty("expires_in")           long expiresIn,
    @JsonProperty("scope")                String scope,
    @JsonProperty("authorization_code_id") String authorizationCodeId) {}


