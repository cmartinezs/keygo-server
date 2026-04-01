package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.command.CreateTenantCommand;
import io.cmartinezs.keygo.app.tenant.exception.DuplicateTenantException;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;

/**
 * Use case: create a new tenant.
 * Validates slug uniqueness and persists the tenant with ACTIVE status.
 * <p>Caso de uso: crear un nuevo tenant.
 * Valida la unicidad del slug y persiste el tenant con estado ACTIVE.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateTenantUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  public CreateTenantUseCase(TenantRepositoryPort tenantRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param command the creation command
   * @return the created and persisted Tenant
   * @throws IllegalArgumentException if the slug derived from the name is already in use
   */
  public Tenant execute(CreateTenantCommand command) {
    TenantSlug slug = TenantSlug.fromName(command.name());

    if (tenantRepositoryPort.existsBySlug(slug)) {
      throw new DuplicateTenantException(slug.value());
    }

    Tenant tenant = Tenant.builder()
        .id(TenantId.generate())
        .slug(slug)
        .name(command.name())
        .ownerEmail(command.ownerEmail())
        .status(TenantStatus.ACTIVE)
        .build();

    return tenantRepositoryPort.save(tenant);
  }
}

