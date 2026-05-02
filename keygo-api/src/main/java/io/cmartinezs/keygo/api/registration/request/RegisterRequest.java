package io.cmartinezs.keygo.api.registration.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for user self-registration within a tenant's client app.
 * <p>Cuerpo de la solicitud para auto-registro de usuario en la app de un tenant.
 * @param username  desired username (unique within the tenant)
 * @param email     user's email address (unique within the tenant)
 * @param password  raw password (min 8 chars HTTP validation; full policy applied at use case)
 * @param firstName optional first name
 * @param lastName  optional last name
 * @author cmartinezs
 * @version 1.0
 */
public record RegisterRequest(
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,

    String firstName,
    String lastName
) {
}

