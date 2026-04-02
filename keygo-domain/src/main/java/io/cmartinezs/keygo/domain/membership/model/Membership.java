package io.cmartinezs.keygo.domain.membership.model;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.exception.InvalidRoleAssignmentException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipAlreadySuspendedException;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

/**
 * Membership domain entity — represents a user's access to a single application.
 * <p>Entidad de dominio Membership — representa el acceso de un usuario a una única aplicación.
 * One membership = one user + one app. Roles are managed separately via MembershipRole.
 * <p>Una membresía = un usuario + una app. Los roles se manejan por separado vía MembershipRole.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class Membership {

  private final MembershipId id;
  private final UserId userId;
  private final ClientAppId clientAppId;
  private MembershipStatus status;
  private final Set<MembershipRole> roles = new HashSet<>();

  @Builder
  private Membership(MembershipId id, UserId userId, ClientAppId clientAppId, MembershipStatus status) {
    if (id == null) throw new IllegalArgumentException("Membership id cannot be null");
    if (userId == null) throw new IllegalArgumentException("Membership userId cannot be null");
    if (clientAppId == null) throw new IllegalArgumentException("Membership clientAppId cannot be null");
    if (status == null) throw new IllegalArgumentException("Membership status cannot be null");

    this.id = id;
    this.userId = userId;
    this.clientAppId = clientAppId;
    this.status = status;
  }

  /* Domain behaviour */

  /**
   * Returns true if the membership is active and user can access the app.
   * <p>Retorna true si la membresía está activa y el usuario puede acceder a la app.
   */
  public boolean isActive() {
    return MembershipStatus.ACTIVE.equals(this.status);
  }

  /**
   * Returns true if the membership is suspended.
   * <p>Retorna true si la membresía está suspendida.
   */
  public boolean isSuspended() {
    return MembershipStatus.SUSPENDED.equals(this.status);
  }

  /**
   * Suspend this membership. Throws if already suspended.
   * <p>Suspende esta membresía. Lanza excepción si ya estaba suspendida.
   * @throws IllegalStateException if already suspended
   */
  public void suspend() {
    if (MembershipStatus.SUSPENDED.equals(this.status)) {
      throw new MembershipAlreadySuspendedException(userId.value(), clientAppId.value());
    }
    this.status = MembershipStatus.SUSPENDED;
  }

  /**
   * Reactivate a previously suspended membership.
   * <p>Reactiva una membresía previamente suspendida.
   */
  public void activate() {
    this.status = MembershipStatus.ACTIVE;
  }

  /**
   * Assign a role to this membership.
   * <p>Asigna un rol a esta membresía.
   * @param role the role to assign
   * @throws IllegalArgumentException if role is null
   */
  public void assignRole(AppRole role) {
    if (role == null) {
      throw new InvalidRoleAssignmentException("role cannot be null");
    }
    if (!role.getClientAppId().equals(this.clientAppId)) {
      throw new InvalidRoleAssignmentException("role must belong to the same app as the membership");
    }
    this.roles.add(MembershipRole.of(role.getId()));
  }

  /**
   * Remove a role from this membership.
   * <p>Remueve un rol de esta membresía.
   * @param roleId the ID of the role to remove
   */
  public void removeRole(AppRoleId roleId) {
    this.roles.removeIf(mr -> mr.roleId().equals(roleId));
  }

  /**
   * Check if this membership has a specific role.
   * <p>Verifica si esta membresía tiene un rol específico.
   * @param roleId the role ID to check
   * @return true if the membership has the role
   */
  public boolean hasRole(AppRoleId roleId) {
    return roles.stream().anyMatch(mr -> mr.roleId().equals(roleId));
  }

  @Override
  public String toString() {
    return "Membership[user=" + userId + ", app=" + clientAppId + ", status=" + status + "]";
  }
}

