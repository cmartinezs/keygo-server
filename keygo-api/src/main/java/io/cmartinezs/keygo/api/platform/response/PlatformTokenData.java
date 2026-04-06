package io.cmartinezs.keygo.api.platform.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta con tokens de plataforma.
 *
 * @param accessToken  JWT firmado (access_token)
 * @param refreshToken token plano para rotación
 * @param tokenType    tipo de token (siempre "Bearer")
 * @param expiresIn    segundos hasta la expiración del access_token
 */
public record PlatformTokenData(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") long expiresIn) {}
