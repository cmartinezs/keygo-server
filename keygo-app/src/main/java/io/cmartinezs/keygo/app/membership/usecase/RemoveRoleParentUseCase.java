package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.exception.AppRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.AppRoleHierarchyPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import java.util.UUID;

/**
 * Use case: remove the parent assignment from a role within a client app.
 * <p>No-op if the role currently has no parent.
 */
public class RemoveRoleParentUseCase {

  private final TenantRepositoryPort tenantRepository;
  private final AppRoleRepositoryPort appRoleRepository;
  private final AppRoleHierarchyPort hierarchyRepository;

  public RemoveRoleParentUseCase(
      TenantRepositoryPort tenantRepository,
      AppRoleRepositoryPort appRoleRepository,
      AppRoleHierarchyPort hierarchyRepository) {
    this.tenantRepository = tenantRepository;
    this.appRoleRepository = appRoleRepository;
    this.hierarchyRepository = hierarchyRepository;
  }

  /**
   * Removes the parent of the role identified by {@code roleCode} in the given app.
   *
   * @param tenantSlug  slug of the owning tenant
   * @param clientAppId UUID of the client app
   * @param roleCode    code of the role whose parent link should be removed
   * @throws TenantNotFoundException  if the tenant does not exist
   * @throws AppRoleNotFoundException if the role does not exist in the app
   */
  public void execute(String tenantSlug, UUID clientAppId, String roleCode) {
    tenantRepository.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    AppRole role = appRoleRepository
        .findByClientAppAndCode(clientAppId, RoleCode.of(roleCode))
        .orElseThrow(() -> new AppRoleNotFoundException(clientAppId, RoleCode.of(roleCode)));

    hierarchyRepository.removeParent(role.getId().value());
  }
}
