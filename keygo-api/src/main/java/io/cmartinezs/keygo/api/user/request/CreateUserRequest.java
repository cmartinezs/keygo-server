package io.cmartinezs.keygo.api.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a new user within a tenant.
 * <p>Cuerpo de solicitud para crear un nuevo usuario dentro de un tenant.
 * @param username  desired username (3-100 chars, unique within tenant)
 * @param email     user email address (unique within tenant)
 * @param password  raw password (will be hashed server-side). Min 8 chars (HTTP validation);
 *                  full policy (12 chars + 4 classes) applied at use case layer
 * @param firstName optional first name
 * @param lastName  optional last name
 * @author cmartinezs
 * @version 1.0
 */
public record CreateUserRequest(

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
    String username,

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    String email,

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    String password,

    String firstName,

    String lastName
) {
}

