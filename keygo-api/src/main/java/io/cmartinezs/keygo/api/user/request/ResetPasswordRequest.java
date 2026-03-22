package io.cmartinezs.keygo.api.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for resetting a user's password (admin-initiated).
 * <p>Cuerpo de solicitud para resetear la contraseña de un usuario (iniciado por el administrador).
 * @param newPassword the new raw password
 * @author cmartinezs
 * @version 1.0
 */
public record ResetPasswordRequest(

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "newPassword must be at least 8 characters")
    String newPassword
) {
}

