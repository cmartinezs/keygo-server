package io.cmartinezs.keygo.api.auth.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request: Envío de credenciales de login.
 *
 * @param emailOrUsername email o username del usuario
 * @param password contraseña en texto plano
 */
public record LoginRequest(
    @NotBlank(message = "emailOrUsername is required") String emailOrUsername,
    @NotBlank(message = "password is required") String password) {}

