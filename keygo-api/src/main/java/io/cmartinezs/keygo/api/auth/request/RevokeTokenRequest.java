package io.cmartinezs.keygo.api.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request: revocar un token (RFC 7009).
 *
 * <p>El mapeo snake_case ↔ camelCase ({@code token_type_hint} → {@code tokenTypeHint},
 * {@code client_id} → {@code clientId}) es gestionado globalmente por {@code SnakeCaseAliasModule}.
 *
 * @param token         valor del token a revocar (refresh_token o access_token)
 * @param tokenTypeHint hint del tipo de token ("refresh_token" o "access_token"); opcional
 * @param clientId      client_id de la app que posee el token
 */
public record RevokeTokenRequest(
    @NotBlank(message = "token is required")     String token,
    String tokenTypeHint,
    @NotBlank(message = "client_id is required") String clientId) {}
