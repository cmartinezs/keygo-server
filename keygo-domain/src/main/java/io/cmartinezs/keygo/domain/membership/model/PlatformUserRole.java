package io.cmartinezs.keygo.domain.membership.model;

import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

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
  private final String scopeType;
  @Getter(AccessLevel.NONE)
  private final UUID contractorId;
  @Getter(AccessLevel.NONE)
  private final UUID tenantId;
  private final Instant assignedAt;

  @Builder
  private PlatformUserRole(
      PlatformUserRoleId id,
      UserId userId,
      PlatformRoleId platformRoleId,
      String scopeType,
      UUID contractorId,
      UUID tenantId,
      Instant assignedAt) {
    if (id == null) throw new IllegalArgumentException("PlatformUserRole id cannot be null");
    if (userId == null) throw new IllegalArgumentException("PlatformUserRole userId cannot be null");
    if (platformRoleId == null) throw new IllegalArgumentException("PlatformUserRole platformRoleId cannot be null");

    this.id = id;
    this.userId = userId;
    this.platformRoleId = platformRoleId;
    this.scopeType = scopeType != null && !scopeType.isBlank() ? scopeType : "GLOBAL";
    this.contractorId = contractorId;
    this.tenantId = tenantId;
    this.assignedAt = assignedAt != null ? assignedAt : Instant.now();
  }

  public Optional<UUID> getContractorId() {
    return Optional.ofNullable(contractorId);
  }

  public Optional<UUID> getTenantId() {
    return Optional.ofNullable(tenantId);
  }

  @Override
  public String toString() {
    return "PlatformUserRole[userId=" + userId + ", platformRoleId=" + platformRoleId + "]";
  }
}
