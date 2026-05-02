package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.command.AssignRoleParentCommand;
import io.cmartinezs.keygo.app.membership.exception.AppRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.AppRoleHierarchyPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.exception.RoleHierarchyCycleException;
import io.cmartinezs.keygo.domain.membership.exception.RoleHierarchyDepthExceededException;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import java.util.List;
import java.util.UUID;

/**
 * Use case: assign a parent role to a child role within a client app.
 * <p>Validates cycle-free assignment and maximum depth constraint.
 */
public class AssignRoleParentUseCase {

  private final TenantRepositoryPort tenantRepository;
  private final ClientAppRepositoryPort clientAppRepository;
  private final AppRoleRepositoryPort appRoleRepository;
  private final AppRoleHierarchyPort hierarchyRepository;

  public AssignRoleParentUseCase(
      TenantRepositoryPort tenantRepository,
      ClientAppRepositoryPort clientAppRepository,
      AppRoleRepositoryPort appRoleRepository,
      AppRoleHierarchyPort hierarchyRepository) {
    this.tenantRepository = tenantRepository;
    this.clientAppRepository = clientAppRepository;
    this.appRoleRepository = appRoleRepository;
    this.hierarchyRepository = hierarchyRepository;
  }

  /**
   * Assigns {@code command.parentRoleCode()} as the direct parent of {@code command.childRoleCode()}.
   *
   * @param command assignment parameters
   * @throws TenantNotFoundException           if the tenant does not exist
   * @throws AppRoleNotFoundException          if either role does not exist in the app
   * @throws RoleHierarchyCycleException       if the assignment would create a cycle
   * @throws RoleHierarchyDepthExceededException if max hierarchy depth would be exceeded
   */
  public void execute(AssignRoleParentCommand command) {
    tenantRepository.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    AppRole childRole = appRoleRepository
        .findByClientAppAndCode(command.clientAppId(), RoleCode.of(command.childRoleCode()))
        .orElseThrow(() -> new AppRoleNotFoundException(command.clientAppId(), RoleCode.of(command.childRoleCode())));

    AppRole parentRole = appRoleRepository
        .findByClientAppAndCode(command.clientAppId(), RoleCode.of(command.parentRoleCode()))
        .orElseThrow(() -> new AppRoleNotFoundException(command.clientAppId(), RoleCode.of(command.parentRoleCode())));

    UUID childId = childRole.getId().value();
    UUID parentId = parentRole.getId().value();

    if (childId.equals(parentId)) {
      throw new RoleHierarchyCycleException(childId, parentId);
    }

    // Cycle check: if childId is already an ancestor of parentId, adding this link creates a cycle
    List<UUID> parentAncestors = hierarchyRepository.findAncestorIds(parentId);
    if (parentAncestors.contains(childId)) {
      throw new RoleHierarchyCycleException(childId, parentId);
    }

    // Depth check: parentId is already at level (parentAncestors.size() + 1).
    // childId would be at level (parentAncestors.size() + 2).
    // Maximum allowed depth is MAX_DEPTH = 5.
    if (parentAncestors.size() >= RoleHierarchyDepthExceededException.MAX_DEPTH - 1) {
      throw new RoleHierarchyDepthExceededException();
    }

    hierarchyRepository.assignParent(childId, parentId);
  }
}
