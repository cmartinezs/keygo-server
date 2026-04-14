package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para actualizar el perfil del usuario de plataforma autenticado (self-service).
 *
 * <p>Utilizado por el endpoint {@code PATCH /api/v1/platform/account/profile}.
 * El Bearer token se verifica para extraer el {@code sub} (UUID del platform user).
 * Solo se actualizan los campos no-nulos — null significa "no cambiar".
 *
 * <p>Nota: {@code PlatformUser} no tiene {@code birthdate} ni {@code website},
 * por lo que esos campos no forman parte de este comando.
 *
 * @param bearerToken       access_token JWT extraído del header Authorization
 * @param firstName         nuevo nombre (null = no cambiar)
 * @param lastName          nuevo apellido (null = no cambiar)
 * @param phoneNumber       OIDC phone_number (null = no cambiar)
 * @param locale            BCP47 locale, e.g. "es-MX" (null = no cambiar)
 * @param zoneinfo          tz database, e.g. "America/Mexico_City" (null = no cambiar)
 * @param profilePictureUrl URL externa de foto de perfil (null = no cambiar)
 * @author cmartinezs
 * @version 1.0
 */
public record UpdatePlatformUserProfileCommand(
    String bearerToken,
    String firstName,
    String lastName,
    String phoneNumber,
    String locale,
    String zoneinfo,
    String profilePictureUrl) {}
