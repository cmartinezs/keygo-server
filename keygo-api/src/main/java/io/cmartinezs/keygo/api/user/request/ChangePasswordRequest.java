package io.cmartinezs.keygo.api.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body para cambiar la contraseña propia del usuario autenticado (self-service).
 *
 * <p>Campos mapeados automáticamente a snake_case en JSON por la configuración global de Jackson
 * ({@code PropertyNamingStrategies.SNAKE_CASE}):
 * <ul>
 *   <li>{@code currentPassword} → {@code current_password}</li>
 *   <li>{@code newPassword} → {@code new_password}</li>
 * </ul>
 *
 * @param currentPassword contraseña actual del usuario (requerida)
 * @param newPassword     nueva contraseña (requerida, debe cumplir política de seguridad)
 * @author cmartinezs
 * @version 1.0
 */
public record ChangePasswordRequest(
    @NotBlank(message = "currentPassword is required")
    String currentPassword,

    @NotBlank(message = "newPassword is required")
    @Size(min = 8, message = "newPassword must be at least 8 characters")
    String newPassword
) {
}
