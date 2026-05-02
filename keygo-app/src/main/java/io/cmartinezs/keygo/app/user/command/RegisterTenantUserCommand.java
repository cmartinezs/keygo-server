package io.cmartinezs.keygo.app.user.command;

/**
 * Command to register a new user within a client app of a tenant.
 * <p>Comando para registrar un nuevo usuario en una app de un tenant.
 * The user is created with PENDING status and must verify their email before logging in.
 * <p>El usuario se crea con estado PENDING y debe verificar su email antes de hacer login.
 * @param tenantSlug  the slug of the tenant
 * @param clientId    the OAuth2 client_id of the app
 * @param username    the desired username (unique within the tenant)
 * @param email       the user's email address (unique within the tenant)
 * @param rawPassword the raw password (will be hashed before storage)
 * @param firstName   optional first name
 * @param lastName    optional last name
 * @author cmartinezs
 * @version 1.0
 */
public record RegisterTenantUserCommand(
    String tenantSlug,
    String clientId,
    String username,
    String email,
    String rawPassword,
    String firstName,
    String lastName
) {
}

