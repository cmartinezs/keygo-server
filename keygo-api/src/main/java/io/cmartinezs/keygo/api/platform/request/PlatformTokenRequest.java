package io.cmartinezs.keygo.api.platform.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de petición para intercambio de tokens de plataforma.
 *
 * @param grantType    tipo de grant (solo "refresh_token" soportado)
 * @param refreshToken token plano de refresco
 */
public record PlatformTokenRequest(
    @JsonProperty("grant_type") String grantType,
    @JsonProperty("refresh_token") String refreshToken) {}
