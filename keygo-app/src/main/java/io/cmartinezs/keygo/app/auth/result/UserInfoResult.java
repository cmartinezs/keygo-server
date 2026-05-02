package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado del endpoint userinfo (OIDC §5.3).
 * <p>Campos condicionados al scope solicitado:
 * <ul>
 *   <li>Siempre presentes: {@code sub}, {@code email}, {@code name}, {@code preferredUsername}</li>
 *   <li>scope "profile": {@code givenName}, {@code familyName}, {@code picture}, {@code locale},
 *       {@code zoneinfo}, {@code birthdate}, {@code website}</li>
 *   <li>scope "phone": {@code phoneNumber}</li>
 * </ul>
 *
 * @param sub               identificador único del usuario (UUID)
 * @param email             email del usuario
 * @param name              nombre completo
 * @param preferredUsername username preferido
 * @param givenName         OIDC given_name (first name)
 * @param familyName        OIDC family_name (last name)
 * @param picture           OIDC picture (profile picture URL)
 * @param locale            OIDC locale (BCP47, e.g. "es-MX")
 * @param zoneinfo          OIDC zoneinfo (tz database, e.g. "America/Mexico_City")
 * @param birthdate         OIDC birthdate (ISO 8601 date)
 * @param website           OIDC website (URL)
 * @param phoneNumber       OIDC phone_number
 */
public record UserInfoResult(
    String sub,
    String email,
    String name,
    String preferredUsername,
    String givenName,
    String familyName,
    String picture,
    String locale,
    String zoneinfo,
    String birthdate,
    String website,
    String phoneNumber) {}

