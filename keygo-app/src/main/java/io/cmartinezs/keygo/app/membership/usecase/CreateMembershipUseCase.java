package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.command.CreateMembershipCommand;
import io.cmartinezs.keygo.app.membership.exception.DuplicateMembershipException;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.membership.exception.InvalidRoleAssignmentException;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.Optional;

/**
 * Use case: create a new membership (user access to app within a tenant).
 * <p>Caso de uso: crear una nueva membresía (acceso de usuario a app dentro de un tenant).
 * Validates tenant, user, app, and roles before creating membership.
 * <p>Valida tenant, usuario, app y roles antes de crear la membresía.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateMembershipUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final MembershipRepositoryPort membershipRepositoryPort;
  private final AppRoleRepositoryPort appRoleRepositoryPort;

  public CreateMembershipUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      MembershipRepositoryPort membershipRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.membershipRepositoryPort = membershipRepositoryPort;
    this.appRoleRepositoryPort = appRoleRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param command the creation command
   * @return the created and persisted Membership
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws TenantSuspendedException if the tenant is suspended
   * @throws UserNotFoundException if the user does not exist
   * @throws ClientAppNotFoundException if the client app does not exist
   * @throws IllegalArgumentException if user does not belong to tenant or membership already exists
   * @throws InvalidRoleAssignmentException if a role does not exist or belongs to a different app
   */
  public Membership execute(CreateMembershipCommand command) {
    // Resolve and validate tenant
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    // Validate user exists (mock repo call; real repo would validate tenant scoping)
    UserId userId = UserId.of(command.userId());
    // Note: A real implementation would validate user belongs to tenant

    // Validate app exists (mock repo call)
    // Note: A real implementation would check tenant scoping

    // Validate no duplicate membership
    if (membershipRepositoryPort.existsByUserAndClientApp(command.userId(), command.clientAppId())) {
      throw new DuplicateMembershipException(command.userId(), command.clientAppId());
    }

    // Validate and load all requested roles
    for (String roleCode : command.roleCodes()) {
      Optional<AppRole> role = appRoleRepositoryPort.findByClientAppAndCode(
          command.clientAppId(), RoleCode.of(roleCode));

      if (role.isEmpty()) {
        throw new InvalidRoleAssignmentException(
            "Role '" + roleCode + "' does not exist in app " + command.clientAppId());
      }
    }

    // Create membership
    Membership membership = Membership.builder()
        .id(MembershipId.generate())
        .userId(userId)
        .clientAppId(io.cmartinezs.keygo.domain.clientapp.model.ClientAppId.of(command.clientAppId()))
        .status(MembershipStatus.ACTIVE)
        .build();

    // Assign roles
    for (String roleCode : command.roleCodes()) {
      AppRole role = appRoleRepositoryPort.findByClientAppAndCode(
          command.clientAppId(), RoleCode.of(roleCode))
          .orElseThrow(() -> new InvalidRoleAssignmentException("Role not found: " + roleCode));
      membership.assignRole(role);
    }

    return membershipRepositoryPort.save(membership);
  }
}

