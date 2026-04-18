package io.cmartinezs.keygo.api.registration.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for email verification.
 * <p>Cuerpo de la solicitud para verificación de email.
 * @param email          the user's email address (optional if registration_id is provided)
 * @param registration_id the user's registration ID (optional if email is provided)
 * @param code         the 6-digit verification code received by email
 * @author cmartinezs
 * @version 1.0
 */
public record VerifyEmailRequest(
    @Email(message = "Email must be a valid email address")
    String email,

    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "Registration ID must be a valid UUID")
    String registration_id,

    @NotBlank(message = "Verification code is required")
    @Size(min = 6, max = 6, message = "Verification code must be exactly 6 digits")
    String code
) {
}