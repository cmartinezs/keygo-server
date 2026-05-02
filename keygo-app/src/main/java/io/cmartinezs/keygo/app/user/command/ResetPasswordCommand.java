package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para que un usuario en estado {@code RESET_PASSWORD} establezca su contraseña definitiva.
 *
 * <p>Utilizado por el endpoint público {@code POST /api/v1/tenants/{slug}/account/reset-password}.
 * El usuario se identifica por el {@code requestId} (UUID de la fila en {@code password_reset_codes})
 * devuelto en el 401 del login bloqueado. No requiere email para evitar enumeración de usuarios.
 *
 * @param tenantSlug         slug del tenant
 * @param requestId          UUID de la solicitud de reset (ID de la fila en password_reset_codes)
 * @param temporaryPassword  contraseña temporal asignada por el administrador
 * @param newPassword        nueva contraseña definitiva (debe cumplir la política de seguridad)
 * @param confirmNewPassword confirmación de la nueva contraseña (debe coincidir con newPassword)
 * @param verificationCode   código de 6 dígitos enviado al email al intentar el login bloqueado
 * @author cmartinezs
 * @version 3.0
 */
public record ResetPasswordCommand(
    String tenantSlug,
    String requestId,
    String temporaryPassword,
    String newPassword,
    String confirmNewPassword,
    String verificationCode) {}
