package io.cmartinezs.keygo.api.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body para solicitar recuperación de contraseña (self-service).
 *
 * @param email dirección de correo del usuario
 * @author cmartinezs
 * @version 1.0
 */
public record ForgotPasswordRequest(
    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    String email) {}
