package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;

/**
 * Use case: lookup registration info by registration_id at platform level.
 * <p>Caso de uso: buscar info del registration por registration_id a nivel plataforma.
 * Returns tenant + app info needed for verification flow.
 */
public class GetPlatformRegistrationInfoUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;

  public GetPlatformRegistrationInfoUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
  }

  public record RegistrationInfo(
      UserId id,
      String email,
      String tenantSlug,
      String tenantName,
      String clientAppId,
      String clientAppName,
      String clientAppDescription,
      String firstName,
      String lastName,
      String username,
      io.cmartinezs.keygo.domain.user.model.UserStatus status
  ) {}

  public RegistrationInfo execute(String registrationId, String clientAppId) {
    User user = userRepositoryPort.findById(UserId.of(registrationId))
        .orElseThrow(() -> new UserNotFoundException("id", registrationId));

    var tenant = tenantRepositoryPort.findById(user.getTenantId())
        .orElseThrow(() -> new IllegalStateException("Tenant not found for user"));

    var clientApp = clientAppRepositoryPort.findByClientIdAndTenantId(
            ClientId.of(clientAppId), tenant.getId())
        .orElseThrow(() -> new IllegalArgumentException("Client app not found: " + clientAppId));

    return new RegistrationInfo(
        user.getId(),
        user.getEmail().value(),
        tenant.getSlug().value(),
        tenant.getName(),
        clientApp.getClientId().value(),
        clientApp.getName(),
        clientApp.getDescription(),
        user.getFirstName(),
        user.getLastName(),
        user.getUsername().value(),
        user.getStatus()
    );
  }
}