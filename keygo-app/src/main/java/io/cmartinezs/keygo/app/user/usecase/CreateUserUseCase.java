package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.CreateUserCommand;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordValidationHelper;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

/**
 * Use case: create a new user within an active tenant.
 * <p>Caso de uso: crear un nuevo usuario dentro de un tenant activo.
 * Validates uniqueness of email and username within the tenant scope.
 * <p>Valida la unicidad de email y username dentro del ámbito del tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class CreateUserUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final CredentialEncoderPort credentialEncoderPort;

  public CreateUserUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.credentialEncoderPort = credentialEncoderPort;
  }

  /**
   * Execute the use case.
   * @param command the creation command
   * @return the created and persisted User
   * @throws TenantNotFoundException  if the tenant does not exist
   * @throws TenantSuspendedException if the tenant is suspended
   * @throws DuplicateUserException   if email or username already exists in the tenant
   */
  public User execute(CreateUserCommand command) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    EmailAddress email = EmailAddress.of(command.email());
    Username username = Username.of(command.username());

    if (userRepositoryPort.existsByTenantIdAndEmail(tenant.getId(), email)) {
      throw new DuplicateUserException("email", command.email());
    }

     if (userRepositoryPort.existsByTenantIdAndUsername(tenant.getId(), username)) {
       throw new DuplicateUserException("username", command.username());
     }

      // Validar política de la contraseña (permanente, proporcionada por admin)
      PasswordValidationHelper.validate(command.rawPassword(), false);

      String hashedPassword = credentialEncoderPort.encode(command.rawPassword());

     User user = User.builder()
        .tenantId(tenant.getId())
        .username(username)
        .email(email)
        .passwordHash(PasswordHash.of(hashedPassword))
        .firstName(command.firstName())
        .lastName(command.lastName())
        .status(UserStatus.ACTIVE)
        .build();

    return userRepositoryPort.save(user);
  }
}

