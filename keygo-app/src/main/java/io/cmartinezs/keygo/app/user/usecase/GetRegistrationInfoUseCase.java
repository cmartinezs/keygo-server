package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

/**
 * Use case: retrieve registration info by registration_id.
 * <p>Caso de uso: obtener info del registration por registration_id.
 * Used by UI to show "Verifica tu email: user@example.com" before user enters OTP.
 * Only returns info for PENDING users (not active/verified).
 */
public class GetRegistrationInfoUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;

  public GetRegistrationInfoUseCase(
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
      String firstName,
      String lastName,
      String username,
      UserStatus status
  ) {}

  public RegistrationInfo execute(String tenantSlug, String clientId, String registrationId) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    clientAppRepositoryPort.findByClientIdAndTenantId(ClientId.of(clientId), tenant.getId())
        .orElseThrow(() -> new IllegalArgumentException("Client app not found: " + clientId));

    User user = userRepositoryPort.findByIdAndTenantId(UserId.of(registrationId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", registrationId));

    if (user.getStatus() != UserStatus.PENDING) {
      throw new IllegalArgumentException("User is not pending verification");
    }

    return new RegistrationInfo(
        user.getId(),
        user.getEmail().value(),
        user.getFirstName(),
        user.getLastName(),
        user.getUsername().value(),
        user.getStatus()
    );
  }
}