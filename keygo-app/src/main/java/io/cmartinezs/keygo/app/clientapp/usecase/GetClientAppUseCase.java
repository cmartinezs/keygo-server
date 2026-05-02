package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: retrieve a specific client application by clientId and tenant.
 * <p>Caso de uso: obtener una aplicación cliente específica por clientId y tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class GetClientAppUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;

  public GetClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param tenantSlug the tenant slug
   * @param clientId the OAuth2 client_id string
   * @return the found client app
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws ClientAppNotFoundException if the client app does not exist
   */
  public ClientApp execute(String tenantSlug, String clientId) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    return clientAppRepositoryPort
        .findByClientIdAndTenantId(ClientId.of(clientId), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));
  }
}

