package io.cmartinezs.keygo.api.user.request;

/**
 * Request body para actualizar el perfil propio del usuario autenticado (self-service).
 * <p>Todos los campos son opcionales — null = "no cambiar" (PATCH semántica).
 *
 * @param firstName         nuevo nombre de pila (null = no cambiar)
 * @param lastName          nuevo apellido (null = no cambiar)
 * @param phoneNumber       OIDC phone_number (null = no cambiar)
 * @param locale            BCP47 locale, e.g. "es-MX" (null = no cambiar)
 * @param zoneinfo          tz database, e.g. "America/Mexico_City" (null = no cambiar)
 * @param profilePictureUrl URL externa de foto de perfil (null = no cambiar)
 * @param birthdate         fecha ISO 8601, e.g. "1990-01-15" (null = no cambiar)
 * @param website           URL del sitio web (null = no cambiar)
 * @author cmartinezs
 * @version 1.0
 */
public record UpdateUserProfileRequest(
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

