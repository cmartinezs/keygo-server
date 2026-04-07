package io.cmartinezs.keygo.api.platform.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de petición para login de plataforma.
 *
 * @param emailOrUsername email o username del usuario
 * @param password        contraseña en texto plano
 */
public record PlatformLoginRequest(
    @NotBlank(message = "email_or_username is required") String emailOrUsername,
    @NotBlank(message = "password is required") String password) {}
