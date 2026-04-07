package io.cmartinezs.keygo.api.platform.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de petición para intercambio de tokens de plataforma.
 *
 * <p>Soporta dos grant types:
 * <ul>
 *   <li>{@code authorization_code} — intercambio de código PKCE por tokens
 *   <li>{@code refresh_token} — rotación de refresh token
 * </ul>
 *
 * @param grantType    tipo de grant ("authorization_code" o "refresh_token")
 * @param refreshToken token plano de refresco (para grant_type=refresh_token)
 * @param code         authorization code (para grant_type=authorization_code)
 * @param redirectUri  URI de redirección (para grant_type=authorization_code)
 * @param codeVerifier verificador PKCE (para grant_type=authorization_code)
 */
public record PlatformTokenRequest(
    @NotBlank(message = "grant_type is required")
    @JsonProperty("grant_type") String grantType,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("code") String code,
    @JsonProperty("redirect_uri") String redirectUri,
    @JsonProperty("code_verifier") String codeVerifier) {}
