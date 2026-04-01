package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.command.CreateAppRoleCommand;
import io.cmartinezs.keygo.app.membership.exception.DuplicateAppRoleException;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: create a new role within a client app.
 * <p>Caso de uso: crear un nuevo rol dentro de una app de cliente.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateAppRoleUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final AppRoleRepositoryPort appRoleRepositoryPort;

  public CreateAppRoleUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.appRoleRepositoryPort = appRoleRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param command role creation input
   * @return persisted app role
   */
  public AppRole execute(CreateAppRoleCommand command) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    boolean appBelongsToTenant = clientAppRepositoryPort.findAllByTenantId(tenant.getId()).stream()
        .anyMatch(app -> app.getId().equals(ClientAppId.of(command.clientAppId())));
    if (!appBelongsToTenant) {
      throw new ClientAppNotFoundException(command.clientAppId().toString());
    }

    RoleCode roleCode = RoleCode.of(command.code());
    if (appRoleRepositoryPort.existsByClientAppAndCode(command.clientAppId(), roleCode)) {
      throw new DuplicateAppRoleException(command.code(), command.clientAppId());
    }

    AppRole role = AppRole.builder()
        .id(AppRoleId.generate())
        .clientAppId(ClientAppId.of(command.clientAppId()))
        .code(roleCode)
        .displayName(command.displayName())
        .description(command.description())
        .build();

    return appRoleRepositoryPort.save(role);
  }
}

