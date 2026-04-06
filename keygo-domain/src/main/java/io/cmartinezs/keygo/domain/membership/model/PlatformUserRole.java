package io.cmartinezs.keygo.domain.membership.model;

import io.cmartinezs.keygo.domain.user.model.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * PlatformUserRole domain entity — represents a platform role assignment to a global user.
 * <p>Entidad de dominio PlatformUserRole — representa la asignación de un rol de plataforma a un usuario global.
 * This is the N:N link between {@link io.cmartinezs.keygo.domain.user.model.User} and {@link PlatformRole}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class PlatformUserRole {

  private final PlatformUserRoleId id;
  private final UserId userId;
  private final PlatformRoleId platformRoleId;
  private final Instant assignedAt;

  @Builder
  private PlatformUserRole(PlatformUserRoleId id, UserId userId, PlatformRoleId platformRoleId, Instant assignedAt) {
    if (id == null) throw new IllegalArgumentException("PlatformUserRole id cannot be null");
    if (userId == null) throw new IllegalArgumentException("PlatformUserRole userId cannot be null");
    if (platformRoleId == null) throw new IllegalArgumentException("PlatformUserRole platformRoleId cannot be null");

    this.id = id;
    this.userId = userId;
    this.platformRoleId = platformRoleId;
    this.assignedAt = assignedAt != null ? assignedAt : Instant.now();
  }

  @Override
  public String toString() {
    return "PlatformUserRole[userId=" + userId + ", platformRoleId=" + platformRoleId + "]";
  }
}
