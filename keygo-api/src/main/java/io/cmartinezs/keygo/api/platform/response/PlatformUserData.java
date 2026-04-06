package io.cmartinezs.keygo.api.platform.response;

import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO representing a global platform user in API responses.
 * <p>DTO de respuesta que representa un usuario global de la plataforma.
 * Note: passwordHash is intentionally excluded from this DTO.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class PlatformUserData {

  private final String id;
  private final String email;
  private final String username;
  private final String firstName;
  private final String lastName;
  private final String status;

  /**
   * Map a PlatformUser domain model to a PlatformUserData response DTO.
   *
   * @param user the domain model
   * @return the response DTO
   */
  public static PlatformUserData from(PlatformUser user) {
    return PlatformUserData.builder()
        .id(user.getId().value().toString())
        .email(user.getEmail().value())
        .username(user.getUsername().value())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .status(user.getStatus().name())
        .build();
  }
}
