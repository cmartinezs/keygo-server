package io.cmartinezs.keygo.api.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body para restablecer la contraseña con contraseña temporal (self-service).
 *
 * <p>El usuario se identifica por {@code requestId} (UUID de la solicitud de reset devuelto
 * en el 401 del login bloqueado) en lugar de email, evitando revelar direcciones de correo
 * en la URL o el body.
 *
 * <p>Campos mapeados automáticamente a snake_case en JSON por la configuración global de Jackson
 * ({@code PropertyNamingStrategies.SNAKE_CASE}):
 * <ul>
 *   <li>{@code requestId} → {@code request_id}</li>
 *   <li>{@code temporaryPassword} → {@code temporary_password}</li>
 *   <li>{@code newPassword} → {@code new_password}</li>
 *   <li>{@code confirmNewPassword} → {@code confirm_new_password}</li>
 *   <li>{@code verificationCode} → {@code verification_code}</li>
 * </ul>
 *
 * @param requestId          UUID de la solicitud de reset (devuelto en el 401 del login bloqueado)
 * @param temporaryPassword  contraseña temporal asignada por el administrador
 * @param newPassword        nueva contraseña definitiva (debe cumplir política de seguridad)
 * @param confirmNewPassword confirmación de la nueva contraseña (debe coincidir con newPassword)
 * @param verificationCode   código de 6 dígitos enviado al email al intentar el login bloqueado
 * @author cmartinezs
 * @version 3.0
 */
public record AccountResetPasswordRequest(
    @NotBlank String requestId,
    @NotBlank String temporaryPassword,
    @NotBlank @Size(min = 8, message = "newPassword must be at least 8 characters") String newPassword,
    @NotBlank String confirmNewPassword,
    @NotBlank @Size(min = 6, max = 6) @Pattern(regexp = "\\d{6}") String verificationCode) {}
