package io.cmartinezs.keygo.api.user.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for validating user credentials.
 * <p>Cuerpo de solicitud para validar las credenciales de un usuario.
 * @param credential email address or username
 * @param password   the raw password to verify
 * @author cmartinezs
 * @version 1.0
 */
public record ValidateCredentialsRequest(

    @NotBlank(message = "credential is required")
    String credential,

    @NotBlank(message = "password is required")
    String password
) {
}

