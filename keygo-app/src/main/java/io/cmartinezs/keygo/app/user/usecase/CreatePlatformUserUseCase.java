package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.user.command.CreatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleCode;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordValidationHelper;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.List;

/**
 * Use case: create a new global platform user.
 * <p>Caso de uso: crear un nuevo usuario global de la plataforma.
 * Validates email/username uniqueness globally (not per tenant).
 * Auto-assigns the KEYGO_USER platform role.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class CreatePlatformUserUseCase {

  private static final List<String> DEFAULT_ROLES = List.of(PlatformRoleCode.KEYGO_USER.code());

  private final PlatformUserRepositoryPort platformUserRepositoryPort;
  private final CredentialEncoderPort credentialEncoderPort;
  private final AssignPlatformRoleUseCase assignPlatformRoleUseCase;

  public CreatePlatformUserUseCase(
      PlatformUserRepositoryPort platformUserRepositoryPort,
      CredentialEncoderPort credentialEncoderPort,
      AssignPlatformRoleUseCase assignPlatformRoleUseCase) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
    this.credentialEncoderPort = credentialEncoderPort;
    this.assignPlatformRoleUseCase = assignPlatformRoleUseCase;
  }

  /**
   * Execute the use case.
   *
   * @param command the creation command
   * @return the created and persisted PlatformUser
   * @throws DuplicateUserException if email or username already exists globally
   */
  public PlatformUser execute(CreatePlatformUserCommand command) {
    EmailAddress email = EmailAddress.of(command.email());
    Username username = Username.of(command.username());

    if (platformUserRepositoryPort.existsByEmail(email)) {
      throw new DuplicateUserException("email", command.email());
    }

    if (platformUserRepositoryPort.existsByUsername(username)) {
      throw new DuplicateUserException("username", command.username());
    }

    PasswordValidationHelper.validate(command.rawPassword(), false);

    String hashedPassword = credentialEncoderPort.encode(command.rawPassword());

    PlatformUser user = PlatformUser.builder()
        .username(username)
        .email(email)
        .passwordHash(PasswordHash.of(hashedPassword))
        .firstName(command.firstName())
        .lastName(command.lastName())
        .status(UserStatus.ACTIVE)
        .build();

    PlatformUser saved = platformUserRepositoryPort.save(user);

    assignPlatformRoleUseCase.execute(new AssignPlatformRoleCommand(saved.getId().value(), DEFAULT_ROLES));

    return saved;
  }
}
