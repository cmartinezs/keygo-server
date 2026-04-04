package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para que un usuario en estado {@code RESET_PASSWORD} establezca su contraseña definitiva.
 *
 * <p>Utilizado por el endpoint público {@code POST /api/v1/tenants/{slug}/account/reset-password}.
 * El usuario se identifica por email y verifica la contraseña temporal recibida por el admin.
 * Adicionalmente requiere el código de verificación de 6 dígitos enviado al email en el momento
 * del intento de login bloqueado.
 *
 * @param tenantSlug         slug del tenant
 * @param email              dirección de correo del usuario
 * @param temporaryPassword  contraseña temporal asignada por el administrador
 * @param newPassword        nueva contraseña definitiva (debe cumplir la política de seguridad)
 * @param confirmNewPassword confirmación de la nueva contraseña (debe coincidir con newPassword)
 * @param verificationCode   código de 6 dígitos enviado al email al intentar el login bloqueado
 * @author cmartinezs
 * @version 2.0
 */
public record ResetPasswordCommand(
    String tenantSlug,
    String email,
    String temporaryPassword,
    String newPassword,
    String confirmNewPassword,
    String verificationCode) {}
