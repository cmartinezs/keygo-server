package io.cmartinezs.keygo.api.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response: resultado del canje de authorization code por token.
 *
 * <p>En Fase 6 se agregarán {@code access_token}, {@code token_type}, {@code expires_in} y
 * {@code refresh_token}. Por ahora solo se confirma el ID del código canjeado (auditoría).
 *
 * @param authorizationCodeId ID del código que fue consumido (referencia de auditoría)
 */
public record TokenData(
    @JsonProperty("authorization_code_id") String authorizationCodeId) {}

