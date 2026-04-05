package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: rotate the client secret of a confidential client application.
 * <p>Caso de uso: rotar el secret de cliente de una aplicación cliente confidencial.
 * @author cmartinezs
 * @version 1.0
 */
public class RotateClientSecretUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final ClientCredentialGeneratorPort credentialGenerator;
  private final CredentialEncoderPort credentialEncoder;

  public RotateClientSecretUseCase(
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
   * @param tenantSlug the tenant slug
   * @param clientId the OAuth2 client_id
   * @return a result containing the updated app and the new raw secret
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws ClientAppNotFoundException if the client app does not exist
   * @throws IllegalStateException if the client app is PUBLIC (no secret to rotate)
   */
  public RotateSecretResult execute(String tenantSlug, String clientId) {
    Tenant tenant = tenantRepositoryPort
        .findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    ClientApp clientApp = clientAppRepositoryPort
        .findByClientIdAndTenantId(ClientId.of(clientId), tenant.getId())
        .orElseThrow(() -> new ClientAppNotFoundException(clientId));

    String newRawSecret = credentialGenerator.generateClientSecret();
    String newHashedSecret = credentialEncoder.encode(newRawSecret);

    clientApp.rotateSecret(newHashedSecret);
    ClientApp saved = clientAppRepositoryPort.save(clientApp);

    return new RotateSecretResult(saved, newRawSecret);
  }
}

