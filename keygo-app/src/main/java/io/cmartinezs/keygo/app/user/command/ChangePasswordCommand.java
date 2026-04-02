package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para cambiar la contraseña propia del usuario autenticado (self-service).
 *
 * <p>Utilizado por el endpoint {@code POST /api/v1/tenants/{slug}/account/change-password}.
 * El Bearer token se verifica para extraer el {@code sub} (UUID del usuario).
 *
 * @param tenantSlug      slug del tenant
 * @param bearerToken     access_token JWT extraído del header Authorization
 * @param currentPassword contraseña actual en texto plano (para verificación)
 * @param newPassword     nueva contraseña en texto plano (debe cumplir la política)
 * @author cmartinezs
 * @version 1.0
 */
public record ChangePasswordCommand(
    String tenantSlug,
    String bearerToken,
    String currentPassword,
    String newPassword) {}
