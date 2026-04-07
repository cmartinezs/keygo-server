package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.PlatformUserSuspendedException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import io.cmartinezs.keygo.domain.user.exception.UserPendingVerificationException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.util.Optional;

/**
 * Use case: validate user credentials (email or username + password) within a tenant.
 * <p>Caso de uso: validar las credenciales de un usuario (email o username + contraseña) dentro de un tenant.
 * The {@code credential} parameter accepts either an email address or a username.
 * <p>El parámetro {@code credential} acepta tanto una dirección de email como un username.
 *
 * <p><strong>Cascade validation:</strong> if the tenant user is linked to a global platform user,
 * the platform user's status is checked first. A suspended platform user blocks all tenant logins.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ValidateUserCredentialsUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final CredentialEncoderPort credentialEncoderPort;
  private final PlatformUserRepositoryPort platformUserRepositoryPort;

  public ValidateUserCredentialsUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      CredentialEncoderPort credentialEncoderPort,
      PlatformUserRepositoryPort platformUserRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.credentialEncoderPort = credentialEncoderPort;
    this.platformUserRepositoryPort = platformUserRepositoryPort;
  }

  /**
   * Execute the use case.
   * @param tenantSlug  the tenant slug
   * @param credential  email address or username
   * @param rawPassword the raw password to verify
   * @return the authenticated User
   * @throws TenantNotFoundException              if the tenant does not exist
   * @throws UserNotFoundException               if no user matches the credential
   * @throws PlatformUserSuspendedException      if the linked platform user is suspended
   * @throws UserPendingVerificationException    if the user account is pending verification
   * @throws UserSuspendedException              if the user account is suspended
   * @throws UserPasswordResetRequiredException  if the user must reset their password before logging in
   * @throws InvalidCredentialsException         if the password does not match
   */
  public User execute(String tenantSlug, String credential, String rawPassword) {
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    // Try to find by email first, then fall back to username
    Optional<User> userOpt = tryFindByEmail(tenant, credential);
    if (userOpt.isEmpty()) {
      userOpt = tryFindByUsername(tenant, credential);
    }

    User user = userOpt.orElseThrow(() -> new UserNotFoundException("credential", credential));

    // Cascade: check linked platform user status before tenant-level checks
    validatePlatformUserStatus(user);

    if (user.isPending()) {
      throw new UserPendingVerificationException(user.getEmail().value());
    }

    if (user.isSuspended()) {
      throw new UserSuspendedException(user.getUsername().value());
    }

    if (!credentialEncoderPort.matches(rawPassword, user.getPasswordHash().value())) {
      throw new InvalidCredentialsException();
    }

    // Check after password validation to avoid revealing the account status
    // to an attacker who does not know the password.
    if (user.isResetPassword()) {
      throw new UserPasswordResetRequiredException(user.getUsername().value());
    }

    return user;
  }

  /**
   * If the tenant user is linked to a global platform user, verify the platform user is not suspended.
   */
  private void validatePlatformUserStatus(User user) {
    if (user.getPlatformUserId() == null) {
      return;
    }
    platformUserRepositoryPort.findById(new UserId(user.getPlatformUserId()))
        .filter(PlatformUser::isSuspended)
        .ifPresent(pu -> {
          throw new PlatformUserSuspendedException(pu.getUsername().value());
        });
  }

  private Optional<User> tryFindByEmail(Tenant tenant, String credential) {
    try {
      return userRepositoryPort.findByTenantIdAndEmail(tenant.getId(), EmailAddress.of(credential));
    } catch (IllegalArgumentException ex) {
      // credential is not a valid email format — try username next
      return Optional.empty();
    }
  }

  private Optional<User> tryFindByUsername(Tenant tenant, String credential) {
    try {
      return userRepositoryPort.findByTenantIdAndUsername(tenant.getId(), Username.of(credential));
    } catch (IllegalArgumentException ex) {
      // credential is not a valid username format either
      return Optional.empty();
    }
  }
}

