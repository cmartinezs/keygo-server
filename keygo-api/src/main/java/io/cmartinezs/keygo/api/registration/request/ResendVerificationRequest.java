package io.cmartinezs.keygo.api.registration.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for resending a verification email.
 * <p>Cuerpo de la solicitud para reenviar el email de verificación.
 * @param email the user's email address
 * @author cmartinezs
 * @version 1.0
 */
public record ResendVerificationRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email
) {
}

