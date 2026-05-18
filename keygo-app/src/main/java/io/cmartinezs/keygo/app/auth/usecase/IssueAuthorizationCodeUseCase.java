package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.IssueAuthorizationCodeCommand;
import io.cmartinezs.keygo.app.auth.exception.UnsupportedPkceMethodException;
import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.result.AuthorizationCodeIssuedResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.auth.exception.ScopeNotGrantedException;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCode;
import io.cmartinezs.keygo.domain.auth.model.CodeChallenge;
import io.cmartinezs.keygo.domain.auth.model.ScopeSet;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.membership.exception.MembershipInactiveException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipPendingException;
import io.cmartinezs.keygo.domain.membership.exception.NoMembershipException;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Caso de uso: Emitir un código de autorización.
 *
 * <p>Una vez que el usuario se ha autenticado y visto la pantalla de consentimiento, se emite un
 * código temporal que puede ser canjeado por tokens.
 *
 * <p>Valida que:
 * <ul>
 *   <li>El usuario tiene acceso a la app cliente (membership activa)
 *   <li>El código se guarda con expiración breve (ej. 10 minutos)
 * </ul>
 */
public class IssueAuthorizationCodeUseCase {
  private static final long DEFAULT_CODE_EXPIRATION_SECONDS = 600; // 10 minutos
  private static final int CODE_LENGTH_BYTES = 32; // 256 bits

  private final AuthorizationCodeRepositoryPort authorizationCodeRepository;
  private final ClientAppRepositoryPort clientAppRepository;
  private final UserRepositoryPort userRepository;
  private final MembershipRepositoryPort membershipRepository;
  private final TenantRepositoryPort tenantRepository;
  private final ClockPort clock;

  public IssueAuthorizationCodeUseCase(
      AuthorizationCodeRepositoryPort authorizationCodeRepository,
      ClientAppRepositoryPort clientAppRepository,
      UserRepositoryPort userRepository,
      MembershipRepositoryPort membershipRepository,
      TenantRepositoryPort tenantRepository,
      ClockPort clock) {
    this.authorizationCodeRepository = authorizationCodeRepository;
    this.clientAppRepository = clientAppRepository;
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
    this.tenantRepository = tenantRepository;
    this.clock = clock;
  }

  /**
   * Ejecuta la emisión del código de autorización.
   *
   * @param command parámetros del comando
   * @return resultado con el código y la URI de redirección
   * @throws UserNotFoundException si el usuario no existe
   * @throws ClientAppNotFoundException si la app cliente no existe
   * @throws MembershipInactiveException si el usuario no tiene acceso a la app
   * @throws ScopeNotGrantedException si algún scope no es válido
   */
  public AuthorizationCodeIssuedResult execute(IssueAuthorizationCodeCommand command) {
    // Obtener tenant ID
    TenantId tenantId = getTenantId(command.tenantSlug());

    // Validar usuario
    UserId userId = new UserId(java.util.UUID.fromString(command.userId()));
    User user =
        userRepository
            .findByIdAndTenantId(userId, tenantId)
            .orElseThrow(
                () ->
                    new UserNotFoundException("id", String.valueOf(userId)));

    // Validar app cliente
    ClientApp clientApp =
        clientAppRepository
            .findByClientIdAndTenantId(new ClientId(command.clientId()), tenantId)
            .orElseThrow(
                () ->
                    new ClientAppNotFoundException(
                        "Client app not found: " + command.clientId()));
    ClientAppId clientAppId = clientApp.getId();

    // Validar acceso a la app según política de acceso
    if (!clientApp.isOpenJoin()) {
      // CLOSED (default) y SELF_SIGNUP (no implementado) requieren membership activa
      Membership membership =
          membershipRepository
              .findByUserAndClientApp(userId.value(), clientAppId.value())
              .orElseThrow(
                  () ->
                      new NoMembershipException(
                          "User has no membership for this application"));

      if (membership.isSuspended()) {
        throw new MembershipInactiveException("User membership is suspended");
      }

      if (membership.isPending()) {
        throw new MembershipPendingException("User membership is pending approval");
      }
    }

    // Validar scopes
    ScopeSet scopes = ScopeSet.from(command.scopes());

    // Construir el code challenge
    CodeChallenge codeChallenge;
    if ("S256".equals(command.codeChallengeMethod())) {
      codeChallenge = CodeChallenge.s256(command.codeChallenge());
    } else if ("plain".equals(command.codeChallengeMethod())) {
      codeChallenge = CodeChallenge.plain(command.codeChallenge());
    } else {
      throw new UnsupportedPkceMethodException(command.codeChallengeMethod());
    }

    // Generar código aleatorio
    String code = generateSecureCode();

    // Crear el authorization code
    AuthorizationCode authorizationCode =
        AuthorizationCode.issued(
            code,
            tenantId,
            clientAppId,
            userId,
            command.redirectUri(),
            scopes,
            codeChallenge,
            clock.futureSeconds(DEFAULT_CODE_EXPIRATION_SECONDS));

    // Guardar
    authorizationCodeRepository.save(authorizationCode);

    return new AuthorizationCodeIssuedResult(code, command.redirectUri());
  }

  /** Obtiene el TenantId a partir del slug. */
  private TenantId getTenantId(String tenantSlug) {
    return tenantRepository
        .findBySlug(new io.cmartinezs.keygo.domain.tenant.model.TenantSlug(tenantSlug))
        .map(Tenant::getId)
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantSlug));
  }

  /** Genera un código seguro y aleatorio. */
  private String generateSecureCode() {
    byte[] buffer = new byte[CODE_LENGTH_BYTES];
    new SecureRandom().nextBytes(buffer);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
  }
}





