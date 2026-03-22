package io.cmartinezs.keygo.app.user.command;

/**
 * Command to create a new user within a tenant.
 * <p>Comando para crear un nuevo usuario dentro de un tenant.
 * @param tenantSlug  the slug of the tenant to which the user belongs
 * @param username    the desired username (unique within the tenant)
 * @param email       the user's email address (unique within the tenant)
 * @param rawPassword the raw password (will be hashed before storage)
 * @param firstName   optional first name
 * @param lastName    optional last name
 * @author cmartinezs
 * @version 1.0
 */
public record CreateUserCommand(
    String tenantSlug,
    String username,
    String email,
    String rawPassword,
    String firstName,
    String lastName
) {
}

