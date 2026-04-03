package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para restablecer la contraseña usando un token de recuperación.
 *
 * <p>Utilizado por el endpoint público {@code POST /api/v1/tenants/{slug}/account/recover-password}.
 *
 * @param tenantSlug    slug del tenant
 * @param recoveryToken token hex de 32 caracteres recibido por email
 * @param newPassword   nueva contraseña (debe cumplir la política de seguridad)
 * @author cmartinezs
 * @version 1.0
 */
public record RecoverPasswordCommand(
    String tenantSlug,
    String recoveryToken,
    String newPassword) {}
