package io.cmartinezs.keygo.api.membership.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import java.util.UUID;

/**
 * Response DTO for a role assigned to a membership.
 * <p>DTO de response para un rol asignado a una membresía.
 * @author cmartinezs
 * @version 1.0
 */
public record MembershipRoleData(
    UUID id,
    String code,
    @JsonProperty("display_name") String displayName
) {

  public static MembershipRoleData from(AppRole role) {
    return new MembershipRoleData(
        role.getId().value(),
        role.getCode().value(),
        role.getDisplayName());
  }
}
