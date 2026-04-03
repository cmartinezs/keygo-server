package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.role.filter.AppRoleFilter;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import java.util.List;
import java.util.UUID;

/**
 * Use case: list and retrieve app roles within a client app.
 * <p>Validates that the tenant exists and is not suspended, and that the client app
 * belongs to the specified tenant before returning its roles.
 * <p>Caso de uso: listar y recuperar roles de app dentro de una app de cliente.
 * Valida que el tenant existe y no está suspendido, y que la app de cliente
 * pertenece al tenant indicado antes de retornar sus roles.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ListAppRolesUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final AppRoleRepositoryPort appRoleRepositoryPort;

  public ListAppRolesUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.appRoleRepositoryPort = appRoleRepositoryPort;
  }

  /**
   * List all roles for a given client app within a tenant with pagination and sorting.
   * <p>Lista todos los roles de una app de cliente dentro de un tenant con paginación y ordenamiento.
   *
   * @param tenantSlug the tenant slug
   * @param clientAppId the client app internal UUID
   * @param filter pagination, filtering, and sorting criteria
   * @return paginated result of roles
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws TenantSuspendedException if the tenant is suspended
   * @throws ClientAppNotFoundException if the app does not exist or does not belong to the tenant
   */
  public PagedResult<AppRole> execute(String tenantSlug, UUID clientAppId, AppRoleFilter filter) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(tenantSlug);
    }

    boolean appBelongsToTenant = clientAppRepositoryPort
        .findAllByTenantId(tenant.getId())
        .stream()
        .anyMatch(app -> app.getId().equals(ClientAppId.of(clientAppId)));

    if (!appBelongsToTenant) {
      throw new ClientAppNotFoundException(clientAppId.toString());
    }

    return appRoleRepositoryPort.findAllPaged(clientAppId, filter);
  }

  /**
   * List all roles for a given client app within a tenant.
   * <p>Lista todos los roles de una app de cliente dentro de un tenant.
   *
   * @param tenantSlug the tenant slug
   * @param clientAppId the client app internal UUID
   * @return list of roles
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws TenantSuspendedException if the tenant is suspended
   * @throws ClientAppNotFoundException if the app does not exist or does not belong to the tenant
   * @deprecated Use execute(tenantSlug, clientAppId, filter) instead
   */
  @Deprecated(forRemoval = true)
  public List<AppRole> execute(String tenantSlug, UUID clientAppId) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(tenantSlug);
    }

    boolean appBelongsToTenant = clientAppRepositoryPort
        .findAllByTenantId(tenant.getId())
        .stream()
        .anyMatch(app -> app.getId().equals(ClientAppId.of(clientAppId)));

    if (!appBelongsToTenant) {
      throw new ClientAppNotFoundException(clientAppId.toString());
    }

    return appRoleRepositoryPort.findByClientAppId(clientAppId);
  }
}
