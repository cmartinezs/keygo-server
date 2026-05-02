package io.cmartinezs.keygo.api.user.request;

/**
 * Request body for updating a user's profile information (admin endpoint).
 * <p>Cuerpo de solicitud para actualizar la información de perfil de un usuario (endpoint de admin).
 * All fields are optional — null values mean "no change".
 * <p>Todos los campos son opcionales — valores nulos significan "sin cambio".
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
public record UpdateUserRequest(
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

