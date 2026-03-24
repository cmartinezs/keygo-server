package io.cmartinezs.keygo.app.user.result;

/**
 * Resultado de la consulta/actualización del perfil de usuario (self-service).
 *
 * <p>Contiene todos los campos del perfil OIDC extendido retornados al usuario
 * propietario del token. A diferencia de {@link io.cmartinezs.keygo.app.auth.result.UserInfoResult},
 * este resultado siempre incluye todos los campos sin filtrado por scope.
 *
 * @param id                UUID del usuario
 * @param tenantId          UUID del tenant
 * @param username          nombre de usuario único en el tenant
 * @param email             email del usuario
 * @param firstName         nombre de pila
 * @param lastName          apellido
 * @param status            estado de la cuenta (ACTIVE, PENDING, SUSPENDED)
 * @param phoneNumber       OIDC phone_number
 * @param locale            OIDC locale (BCP47)
 * @param zoneinfo          OIDC zoneinfo (tz database)
 * @param profilePictureUrl OIDC picture (URL externa)
 * @param birthdate         OIDC birthdate (ISO 8601 date)
 * @param website           OIDC website (URL)
 * @author cmartinezs
 * @version 1.0
 */
public record UserProfileResult(
    String id,
    String tenantId,
    String username,
    String email,
    String firstName,
    String lastName,
    String status,
    String phoneNumber,
    String locale,
    String zoneinfo,
    String profilePictureUrl,
    String birthdate,
    String website) {}

