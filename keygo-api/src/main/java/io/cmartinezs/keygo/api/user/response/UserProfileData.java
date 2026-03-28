package io.cmartinezs.keygo.api.user.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import lombok.Builder;
import lombok.Getter;


/**
 * Response DTO para el perfil propio del usuario autenticado (self-service).
 * <p>Incluye todos los campos OIDC extendidos del perfil, retornados al
 * propietario del token. Análogo a {@link UserData} pero sin el campo
 * {@code tenantId} expuesto por separado y con el perfil completo siempre visible.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class UserProfileData {

  /* UUID del usuario */
  private final String id;

  /* UUID del tenant */
  private final String tenantId;

  /* Nombre de usuario único en el tenant */
  private final String username;

  /* Email del usuario */
  private final String email;

  /* Nombre de pila */
  private final String firstName;

  /* Apellido */
  private final String lastName;

  /* Estado de la cuenta (ACTIVE, PENDING, SUSPENDED) */
  private final String status;

  /* OIDC phone_number */
  private final String phoneNumber;

  /* OIDC locale (BCP47, e.g. "es-MX") */
  private final String locale;

  /* OIDC zoneinfo (tz database, e.g. "America/Mexico_City") */
  private final String zoneinfo;

  /* OIDC picture — URL externa */
  private final String profilePictureUrl;

  /* OIDC birthdate — fecha ISO 8601 */
  private final String birthdate;

  /* OIDC website — URL */
  private final String website;

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<UserProfileData> {
  }
}
