package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.command.AuthenticatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import io.cmartinezs.keygo.domain.user.exception.UserPendingVerificationException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.Username;

/**
 * Use case: authenticate a global platform user by email/username and password.
 * <p>Caso de uso: autenticar un usuario global de la plataforma por email o username y contraseña.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class AuthenticatePlatformUserUseCase {

  private final PlatformUserRepositoryPort platformUserRepositoryPort;
  private final CredentialEncoderPort credentialEncoderPort;

  public AuthenticatePlatformUserUseCase(
      PlatformUserRepositoryPort platformUserRepositoryPort,
      CredentialEncoderPort credentialEncoderPort) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
    this.credentialEncoderPort = credentialEncoderPort;
  }

  /**
   * Authenticate a platform user.
   *
   * @param command the authentication command (emailOrUsername + rawPassword)
   * @return the authenticated PlatformUser
   * @throws UserNotFoundException            if no user exists with the given email or username
   * @throws InvalidCredentialsException      if the password does not match
   * @throws UserSuspendedException           if the user account is suspended
   * @throws UserPendingVerificationException if the user account is pending verification
   * @throws UserPasswordResetRequiredException if the user must reset their password
   */
  public PlatformUser execute(AuthenticatePlatformUserCommand command) {
    String input = command.emailOrUsername();
    boolean looksLikeEmail = input.contains("@");

    PlatformUser user = looksLikeEmail
        ? platformUserRepositoryPort.findByEmail(EmailAddress.of(input))
            .orElseThrow(() -> new UserNotFoundException("email", input))
        : platformUserRepositoryPort.findByUsername(Username.of(input))
            .orElseThrow(() -> new UserNotFoundException("username", input));

    if (!credentialEncoderPort.matches(command.rawPassword(), user.getPasswordHash().value())) {
      throw new InvalidCredentialsException();
    }

    if (user.isSuspended()) {
      throw new UserSuspendedException(user.getUsername().value());
    }

    if (user.isPending()) {
      throw new UserPendingVerificationException(input);
    }

    if (user.isResetPassword()) {
      throw new UserPasswordResetRequiredException(user.getUsername().value());
    }

    return user;
  }
}
