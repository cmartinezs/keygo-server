package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResetUserPasswordCommand;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;

/**
 * Use case: reset a user's password (admin-initiated).
 * <p>Caso de uso: resetear la contraseña de un usuario (iniciado por el administrador).
 * @author cmartinezs
 * @version 1.0
 */
public class ResetUserPasswordUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final PasswordHasherPort passwordHasherPort;

  public ResetUserPasswordUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      PasswordHasherPort passwordHasherPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.passwordHasherPort = passwordHasherPort;
  }

  /**
   * Execute the use case.
   * @param command the reset password command
   * @return the user with the updated password
   * @throws TenantNotFoundException if the tenant does not exist
   * @throws UserNotFoundException   if the user does not exist within the tenant
   */
  public User execute(ResetUserPasswordCommand command) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    User user = userRepositoryPort.findByIdAndTenantId(UserId.of(command.userId()), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException(command.userId()));

    String hashedPassword = passwordHasherPort.hash(command.newRawPassword());
    user.updatePassword(PasswordHash.of(hashedPassword));

    return userRepositoryPort.save(user);
  }
}

