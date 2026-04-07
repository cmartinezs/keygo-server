package io.cmartinezs.keygo.api.user.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body para restablecer la contraseña con token de recuperación (self-service).
 *
 * <p>Campos mapeados automáticamente a snake_case en JSON:
 * <ul>
 *   <li>{@code recoveryToken} → {@code recovery_token}</li>
 *   <li>{@code newPassword} → {@code new_password}</li>
 *   <li>{@code confirmPassword} → {@code confirm_password}</li>
 * </ul>
 *
 * @param recoveryToken   token hex de 32 caracteres recibido por email
 * @param newPassword     nueva contraseña (debe cumplir política de seguridad)
 * @param confirmPassword confirmación de la nueva contraseña (debe coincidir con newPassword)
 * @author cmartinezs
 * @version 1.1
 */
public record RecoverPasswordRequest(
    @NotBlank(message = "recovery_token is required") String recoveryToken,
    @NotBlank(message = "new_password is required")
    @Size(min = 8, max = 128, message = "new_password must be between 8 and 128 characters")
    String newPassword,
    @NotBlank(message = "confirm_password is required")
    String confirmPassword) {

  @JsonIgnore
  @AssertTrue(message = "new_password and confirm_password must match")
  public boolean isPasswordMatch() {
    return newPassword != null && newPassword.equals(confirmPassword);
  }
}
