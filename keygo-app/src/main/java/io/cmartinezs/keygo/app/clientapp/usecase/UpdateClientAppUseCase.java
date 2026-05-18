package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.command.UpdateClientAppCommand;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.exception.InvalidClientAppConfigException;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedScope;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.clientapp.model.RedirectUri;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use case: update an existing client application.
 * <p>Caso de uso: actualizar una aplicación cliente existente.
 * @author cmartinezs
 * @version 1.0
 */
public class UpdateClientAppUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;

  public UpdateClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param command the update command
   * @return the updated client app
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws ClientAppNotFoundException if the client app does not exist
   */
  public ClientApp execute(UpdateClientAppCommand command) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    ClientApp clientApp = clientAppRepositoryPort
        .findByClientIdAndTenantId(ClientId.of(command.clientId()), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(command.clientId()));

    clientApp.updateInfo(command.name(), command.description());

    Set<RedirectUri> redirectUris = command.redirectUris() == null ? Set.of() :
        command.redirectUris().stream().map(RedirectUri::of).collect(Collectors.toSet());

    validateOAuthConfig(command.grants(), clientApp.getType(), command.redirectUris());

    clientApp.updateRedirectUris(redirectUris);

    Set<AllowedScope> scopes = command.scopes() == null ? Set.of() :
        command.scopes().stream().map(AllowedScope::of).collect(Collectors.toSet());
    clientApp.updateAccessPolicy(new AccessPolicy(command.grants(), scopes));

    return clientAppRepositoryPort.save(clientApp);
  }

  private void validateOAuthConfig(Set<AllowedGrant> grants, ClientType type, Set<String> redirectUris) {
    if (grants != null && grants.contains(AllowedGrant.AUTHORIZATION_CODE)) {
      if (redirectUris == null || redirectUris.isEmpty()) {
        throw new InvalidClientAppConfigException(
            "AUTHORIZATION_CODE grant requires at least one redirect URI");
      }
    }
    if (ClientType.PUBLIC.equals(type) && grants != null && grants.contains(AllowedGrant.CLIENT_CREDENTIALS)) {
      throw new InvalidClientAppConfigException(
          "CLIENT_CREDENTIALS grant is not allowed for PUBLIC client applications");
    }
  }
}

