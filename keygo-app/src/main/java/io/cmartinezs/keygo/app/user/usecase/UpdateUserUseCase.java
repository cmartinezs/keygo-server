package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.UpdateUserCommand;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;

/**
 * Use case: update a user's profile information (firstName, lastName).
 * <p>Caso de uso: actualizar la información de perfil de un usuario.
 * @author cmartinezs
 * @version 1.0
 */
public class UpdateUserUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;

  public UpdateUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param command the update command
   * @return the updated and persisted User
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws UserNotFoundException   if the user does not exist within the tenant
   */
  public User execute(UpdateUserCommand command) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    User user = userRepositoryPort.findByIdAndTenantId(UserId.of(command.userId()), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", String.valueOf(command.userId())));

    user.updateProfile(
        command.firstName(),
        command.lastName(),
        command.phoneNumber(),
        command.locale(),
        command.zoneinfo(),
        command.profilePictureUrl(),
        command.birthdate(),
        command.website());

    return userRepositoryPort.save(user);
  }
}

