package io.cmartinezs.keygo.api.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response: Código de autorización emitido.
 *
 * @param code valor del código
 * @param redirectUri URI donde redirigir
 */
public record AuthorizationCodeData(
    @JsonProperty("code") String code,
    @JsonProperty("redirect_uri") String redirectUri) {}

