package io.cmartinezs.keygo.api.platform.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de petición para verificar si un email de plataforma ya existe.
 *
 * @param email email a verificar
 */
public record CheckPlatformUserEmailRequest(
    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    String email) {}
