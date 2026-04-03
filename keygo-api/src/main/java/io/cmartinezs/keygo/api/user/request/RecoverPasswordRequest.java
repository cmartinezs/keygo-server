package io.cmartinezs.keygo.api.user.request;

/**
 * Request body para restablecer la contraseña con token de recuperación (self-service).
 *
 * <p>Campos mapeados automáticamente a snake_case en JSON:
 * <ul>
 *   <li>{@code recoveryToken} → {@code recovery_token}</li>
 *   <li>{@code newPassword} → {@code new_password}</li>
 * </ul>
 *
 * @param recoveryToken token hex de 32 caracteres recibido por email
 * @param newPassword   nueva contraseña (debe cumplir política de seguridad)
 * @author cmartinezs
 * @version 1.0
 */
public record RecoverPasswordRequest(
    String recoveryToken,
    String newPassword) {}
