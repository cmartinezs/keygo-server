package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.command.CreateTenantRoleCommand;
import io.cmartinezs.keygo.app.membership.exception.DuplicateTenantRoleException;
import io.cmartinezs.keygo.app.membership.port.TenantRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.TenantRole;
import io.cmartinezs.keygo.domain.membership.model.TenantRoleId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;

/**
 * Use case: create a new role within a tenant.
 * <p>Caso de uso: crear un nuevo rol dentro de un tenant.
 * Validates code uniqueness within the tenant before creation.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateTenantRoleUseCase {

  private final TenantRoleRepositoryPort tenantRoleRepositoryPort;

  public CreateTenantRoleUseCase(TenantRoleRepositoryPort tenantRoleRepositoryPort) {
    this.tenantRoleRepositoryPort = tenantRoleRepositoryPort;
  }

  /**
   * Create a new tenant role.
   * @param command role creation input (tenantId, code, name, description)
   * @return the persisted TenantRole
   * @throws DuplicateTenantRoleException if a role with the same code already exists in the tenant
   */
  public TenantRole execute(CreateTenantRoleCommand command) {
    if (tenantRoleRepositoryPort.existsByTenantAndCode(command.tenantId(), command.code())) {
      throw new DuplicateTenantRoleException(command.code(), command.tenantId());
    }

    TenantRole tenantRole = TenantRole.builder()
        .id(TenantRoleId.generate())
        .tenantId(new TenantId(command.tenantId()))
        .code(command.code())
        .name(command.name())
        .description(command.description())
        .active(true)
        .build();

    return tenantRoleRepositoryPort.create(tenantRole);
  }
}
