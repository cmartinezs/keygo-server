package io.cmartinezs.keygo.api.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request: Canjear código de autorización por token.
 *
 * @param clientId client_id de la app
 * @param code código de autorización
 * @param codeVerifier verifier PKCE
 * @param redirectUri URI de redirección (debe coincidir)
 */
public record TokenRequest(
    @NotBlank(message = "client_id is required") String clientId,
    @NotBlank(message = "code is required") String code,
    String codeVerifier,
    @NotBlank(message = "redirect_uri is required") String redirectUri) {}

