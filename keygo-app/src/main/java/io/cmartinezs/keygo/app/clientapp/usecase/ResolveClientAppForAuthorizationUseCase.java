package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.exception.ClientAppInactiveException;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: resolve and validate a client application for an OAuth2 authorization request.
 * Validates that the client exists, is active, and that the redirect URI and grant are allowed.
 * <p>Caso de uso: resolver y validar una aplicación cliente para una solicitud de autorización OAuth2.
 * Valida que el cliente exista, esté activo, y que el redirect URI y grant sean válidos.
 * @author cmartinezs
 * @version 1.0
 */
public class ResolveClientAppForAuthorizationUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;

  public ResolveClientAppForAuthorizationUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param tenantSlug the tenant slug
   * @param clientId the OAuth2 client_id
   * @param redirectUri the redirect URI to validate (may be null to skip validation)
   * @param grant the grant type to validate (may be null to skip validation)
   * @return the resolved and validated client app
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws TenantSuspendedException if the tenant is suspended
   * @throws ClientAppNotFoundException if the client app does not exist
   * @throws IllegalStateException if the client app is not active
   */
  public ClientApp execute(String tenantSlug, String clientId, String redirectUri, AllowedGrant grant) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(tenantSlug);
    }

    ClientApp clientApp = clientAppRepositoryPort
        .findByClientIdAndTenantId(ClientId.of(clientId), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));

    if (!clientApp.isActive()) {
      throw new ClientAppInactiveException(clientId);
    }

    if (redirectUri != null) {
      clientApp.validateRedirectUri(redirectUri);
    }

    if (grant != null) {
      clientApp.validateGrant(grant);
    }

    return clientApp;
  }
}

