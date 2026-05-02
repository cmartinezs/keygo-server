package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para solicitar un token de recuperación de contraseña vía email.
 *
 * <p>Utilizado por el endpoint público {@code POST /api/v1/tenants/{slug}/account/forgot-password}.
 *
 * @param tenantSlug slug del tenant
 * @param email      dirección de correo del usuario
 * @author cmartinezs
 * @version 1.0
 */
public record ForgotPasswordCommand(String tenantSlug, String email) {}
