package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para actualizar el perfil del usuario autenticado (self-service).
 *
 * <p>Utilizado por el endpoint {@code PATCH /api/v1/tenants/{slug}/account/profile}.
 * El {@code sub} (UUID del usuario) es extraído del SecurityContext por el controller
 * antes de construir este comando — el filtro ya verificó el token.
 * Solo se actualizan los campos no-nulos — null significa "no cambiar".
 *
 * @param tenantSlug        slug del tenant
 * @param userId            UUID del usuario autenticado (claim {@code sub} del JWT)
 * @param firstName         nuevo nombre (null = no cambiar)
 * @param lastName          nuevo apellido (null = no cambiar)
 * @param phoneNumber       OIDC phone_number (null = no cambiar)
 * @param locale            BCP47 locale, e.g. "es-MX" (null = no cambiar)
 * @param zoneinfo          tz database, e.g. "America/Mexico_City" (null = no cambiar)
 * @param profilePictureUrl URL externa de foto de perfil (null = no cambiar)
 * @param birthdate         fecha de nacimiento ISO 8601, e.g. "1990-01-15" (null = no cambiar)
 * @param website           URL del sitio web (null = no cambiar)
 * @author cmartinezs
 * @version 1.0
 */
public record UpdateUserProfileCommand(
    String tenantSlug,
    String userId,
    String firstName,
    String lastName,
    String phoneNumber,
    String locale,
    String zoneinfo,
    String profilePictureUrl,
    String birthdate,
    String website) {}
