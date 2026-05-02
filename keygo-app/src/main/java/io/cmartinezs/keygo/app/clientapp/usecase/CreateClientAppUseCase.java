package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.command.CreateClientAppCommand;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedScope;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.clientapp.model.RedirectUri;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use case: create a new client application under a tenant.
 * <p>Caso de uso: crear una nueva aplicación cliente bajo un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateClientAppUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final ClientCredentialGeneratorPort credentialGenerator;
  private final CredentialEncoderPort credentialEncoder;

  public CreateClientAppUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      ClientCredentialGeneratorPort credentialGenerator,
      CredentialEncoderPort credentialEncoder) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.credentialGenerator = credentialGenerator;
    this.credentialEncoder = credentialEncoder;
  }

  /**
   * Execute the use case.
   * @param command the creation command
   * @return a result containing the created client app and its raw secret (if CONFIDENTIAL)
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws TenantSuspendedException if the tenant is suspended
   * @throws IllegalArgumentException if a clientId collision occurs after retries
   */
  public CreateClientAppResult execute(CreateClientAppCommand command) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    String rawClientId = credentialGenerator.generateClientId();
    ClientId clientId = ClientId.of(rawClientId);

    if (clientAppRepositoryPort.existsByClientId(clientId)) {
      rawClientId = credentialGenerator.generateClientId();
      clientId = ClientId.of(rawClientId);
    }

    String rawSecret = null;
    String hashedSecret = null;
    if (ClientType.CONFIDENTIAL.equals(command.type())) {
      rawSecret = credentialGenerator.generateClientSecret();
      hashedSecret = credentialEncoder.encode(rawSecret);
    }

    Set<RedirectUri> redirectUris = command.redirectUris() == null ? Set.of() :
        command.redirectUris().stream().map(RedirectUri::of).collect(Collectors.toSet());

    Set<AllowedScope> scopes = command.scopes() == null ? Set.of() :
        command.scopes().stream().map(AllowedScope::of).collect(Collectors.toSet());

    AccessPolicy accessPolicy = new AccessPolicy(command.grants(), scopes);

    ClientApp clientApp = ClientApp.builder()
        .tenantId(tenant.getId())
        .clientId(clientId)
        .name(command.name())
        .description(command.description())
        .type(command.type())
        .hashedSecret(hashedSecret)
        .redirectUris(redirectUris)
        .accessPolicy(accessPolicy)
        .status(ClientAppStatus.ACTIVE)
        .build();

    ClientApp saved = clientAppRepositoryPort.save(clientApp);
    return new CreateClientAppResult(saved, rawSecret);
  }
}



