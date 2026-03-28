package io.cmartinezs.keygo.api.user.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Response DTO for user data.
 * <p>DTO de respuesta con datos del usuario.
 * Note: passwordHash is intentionally excluded from this DTO.
 * <p>Nota: passwordHash se excluye intencionalmente de este DTO.
 * @author cmartinezs
 * @version 1.1
 */
@Getter
@Builder
public class UserData {

  /* Unique user identifier (UUID) */
  private final String id;

  /* Tenant the user belongs to */
  private final String tenantId;

  /* Unique username within the tenant */
  private final String username;

  /* Email address of the user */
  private final String email;

  /* Optional first name */
  private final String firstName;

  /* Optional last name */
  private final String lastName;

  /* Account status: ACTIVE, SUSPENDED, PENDING */
  private final String status;

  // ─── OIDC extended profile fields (V13) ───────────────────────────────────

  /* OIDC phone_number */
  private final String phoneNumber;

  /* OIDC locale (BCP47, e.g. "es-MX") */
  private final String locale;

  /* OIDC zoneinfo (tz database, e.g. "America/Mexico_City") */
  private final String zoneinfo;

  /* OIDC picture — external URL */
  private final String profilePictureUrl;

  /* OIDC birthdate — ISO 8601 date string */
  private final String birthdate;

  /* OIDC website — URL */
  private final String website;

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<UserData> {
  }

  /** Solo para referencia de schema OpenAPI (lista). */
  public static final class ListResponse extends BaseResponse<List<UserData>> {
  }
}
