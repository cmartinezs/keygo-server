package io.cmartinezs.keygo.app.auth.command;

/**
 * Comando: Autenticar usuario en el contexto de autorización.
 *
 * @param tenantSlug identificador del tenant
 * @param emailOrUsername email o username del usuario
 * @param password contraseña en texto plano (solo en esta capa; se hashea en domain)
 */
public record AuthenticateUserCommand(String tenantSlug, String emailOrUsername, String password) {}

