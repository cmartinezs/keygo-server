package io.cmartinezs.keygo.api.user.response;

import io.cmartinezs.keygo.domain.membership.model.UserAccessEntry;

import java.util.List;
import java.util.UUID;

/**
 * DTO de presentación: entrada de acceso del usuario a una aplicación.
 *
 * @param clientAppId   UUID de la aplicación cliente
 * @param clientAppName nombre legible de la aplicación
 * @param membershipId  UUID de la membresía
 * @param status        estado de la membresía
 * @param roles         roles directos asignados
 */
public record UserAccessData(
    UUID clientAppId,
    String clientAppName,
    UUID membershipId,
    String status,
    List<String> roles) {

  public static UserAccessData from(UserAccessEntry entry) {
    return new UserAccessData(
        entry.clientAppId(),
        entry.clientAppName(),
        entry.membershipId(),
        entry.status(),
        entry.roles());
  }
}
