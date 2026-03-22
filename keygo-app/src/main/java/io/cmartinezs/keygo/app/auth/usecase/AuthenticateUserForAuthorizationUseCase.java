package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.AuthenticateUserCommand;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.Optional;

/**
 * Caso de uso: Autenticar usuario en el contexto de autorización OAuth 2.0.
 *
 * <p>Valida que:
 * <ul>
 *   <li>El usuario existe en el tenant
 *   <li>El usuario está activo (no suspendido)
 *   <li>Las credenciales son válidas (email/username + password)
 * </ul>
 *
 * <p>Resultado: objeto User auténtico que puede proceder a autorización.
 */
public class AuthenticateUserForAuthorizationUseCase {
  private final TenantRepositoryPort tenantRepository;
  private final UserRepositoryPort userRepository;
  private final PasswordHasherPort passwordHasher;

  public AuthenticateUserForAuthorizationUseCase(
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      PasswordHasherPort passwordHasher) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
  }

  /**
   * Ejecuta la autenticación del usuario.
   *
   * @param tenantSlug slug del tenant
   * @param command parámetros del comando
   * @return usuario autenticado
   * @throws TenantNotFoundException si el tenant no existe
   * @throws InvalidCredentialsException si el usuario no existe o credenciales no son válidas
   * @throws UserSuspendedException si el usuario está suspendido
   */
  public User execute(String tenantSlug, AuthenticateUserCommand command) {
    // Resolver TenantId desde el slug
    TenantId tenantId =
        tenantRepository
            .findBySlug(new TenantSlug(tenantSlug))
            .map(Tenant::getId)
            .orElseThrow(
                () -> new TenantNotFoundException("Tenant not found: " + tenantSlug));

    // Buscar usuario por email primero
    Optional<User> userOpt = tryFindByEmail(tenantId, command.emailOrUsername());
    if (userOpt.isEmpty()) {
      userOpt = tryFindByUsername(tenantId, command.emailOrUsername());
    }

    User user =
        userOpt.orElseThrow(InvalidCredentialsException::new);

    // Validar que el usuario está activo
    if (user.isSuspended()) {
      throw new UserSuspendedException("User is suspended: " + user.getId());
    }

    // Validar contraseña
    if (!passwordHasher.matches(command.password(), user.getPasswordHash().value())) {
      throw new InvalidCredentialsException();
    }

    return user;
  }

  private Optional<User> tryFindByEmail(TenantId tenantId, String credential) {
    try {
      return userRepository.findByTenantIdAndEmail(tenantId, EmailAddress.of(credential));
    } catch (IllegalArgumentException ex) {
      // No es un email válido, intentar con username
      return Optional.empty();
    }
  }

  private Optional<User> tryFindByUsername(TenantId tenantId, String credential) {
    try {
      return userRepository.findByTenantIdAndUsername(tenantId, Username.of(credential));
    } catch (IllegalArgumentException ex) {
      // No es un username válido
      return Optional.empty();
    }
  }
}





