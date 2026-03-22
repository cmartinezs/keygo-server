package io.cmartinezs.keygo.api.user.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO for user data.
 * <p>DTO de respuesta con datos del usuario.
 * Note: passwordHash is intentionally excluded from this DTO.
 * <p>Nota: passwordHash se excluye intencionalmente de este DTO.
 * @author cmartinezs
 * @version 1.0
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
}

