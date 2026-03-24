package io.cmartinezs.keygo.app.user.command;

/**
 * Command to update user profile information (admin endpoint).
 * <p>Comando para actualizar información del perfil de un usuario (endpoint de administración).
 * All profile fields are optional — null values are accepted to clear existing data.
 * <p>Todos los campos de perfil son opcionales — valores nulos son aceptados para limpiar datos.
 * @param tenantSlug        the slug of the tenant
 * @param userId            the UUID string of the user to update
 * @param firstName         new first name (may be null)
 * @param lastName          new last name (may be null)
 * @param phoneNumber       OIDC phone_number (may be null)
 * @param locale            BCP47 locale, e.g. "es-MX" (may be null)
 * @param zoneinfo          tz database zoneinfo, e.g. "America/Mexico_City" (may be null)
 * @param profilePictureUrl external picture URL (may be null)
 * @param birthdate         ISO 8601 date, e.g. "1990-01-15" (may be null)
 * @param website           website URL (may be null)
 * @author cmartinezs
 * @version 1.1
 */
public record UpdateUserCommand(
    String tenantSlug,
    String userId,
    String firstName,
    String lastName,
    String phoneNumber,
    String locale,
    String zoneinfo,
    String profilePictureUrl,
    String birthdate,
    String website
) {
}

