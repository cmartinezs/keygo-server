package io.cmartinezs.keygo.api.platform.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body para revocar un token de plataforma (RFC 7009).
 *
 * <p>A diferencia de {@code RevokeTokenRequest} (tenant-scoped), no requiere {@code client_id}
 * ya que los tokens de plataforma no pertenecen a una client app específica.
 *
 * @param token         valor del token a revocar (refresh_token o access_token)
 * @param tokenTypeHint hint del tipo de token ("refresh_token" o "access_token"); opcional
 * @author cmartinezs
 * @version 1.0
 */
public record PlatformRevokeTokenRequest(
    @NotBlank(message = "token is required") String token,
    String tokenTypeHint) {}
