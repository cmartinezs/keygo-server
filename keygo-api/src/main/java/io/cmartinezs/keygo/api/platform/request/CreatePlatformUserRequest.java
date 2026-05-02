package io.cmartinezs.keygo.api.platform.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new global platform user.
 * <p>DTO de solicitud para crear un nuevo usuario global de la plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
public record CreatePlatformUserRequest(

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    String password,

    @Size(max = 255, message = "First name must not exceed 255 characters")
    String firstName,

    @Size(max = 255, message = "Last name must not exceed 255 characters")
    String lastName
) {}
