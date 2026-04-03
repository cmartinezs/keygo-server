package io.cmartinezs.keygo.api.user.request;

/**
 * Request body para restablecer la contraseña con contraseña temporal (self-service).
 *
 * <p>Campos mapeados automáticamente a snake_case en JSON por la configuración global de Jackson
 * ({@code PropertyNamingStrategies.SNAKE_CASE}):
 * <ul>
 *   <li>{@code email} → {@code email}</li>
 *   <li>{@code temporaryPassword} → {@code temporary_password}</li>
 *   <li>{@code newPassword} → {@code new_password}</li>
 * </ul>
 *
 * @param email             dirección de correo del usuario
 * @param temporaryPassword contraseña temporal asignada por el administrador
 * @param newPassword       nueva contraseña definitiva (debe cumplir política de seguridad)
 * @author cmartinezs
 * @version 1.0
 */
public record AccountResetPasswordRequest(
    String email,
    String temporaryPassword,
    String newPassword) {}
