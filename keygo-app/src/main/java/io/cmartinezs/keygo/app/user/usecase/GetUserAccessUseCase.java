package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetUserAccessCommand;
import io.cmartinezs.keygo.app.user.result.UserAccessResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.UserAccessEntry;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: obtener las apps y roles del usuario autenticado (vista self-service).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Verificar el access_token JWT y extraer el {@code sub} (UUID del usuario).</li>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Obtener todas las membresías del usuario en el tenant.</li>
 *   <li>Para cada membresía, obtener el nombre de la app y los roles directos.</li>
 * </ol>
 *
 * <p>Retorna lista vacía si el usuario no tiene membresías (nunca un 404).
 *
 * <p>Usado por: {@code GET /api/v1/tenants/{slug}/account/access}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetUserAccessUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final AccessTokenVerifierPort accessTokenVerifier;
  private final TenantRepositoryPort tenantRepository;
  private final MembershipRepositoryPort membershipRepository;
  private final ClientAppRepositoryPort clientAppRepository;

  public GetUserAccessUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      AccessTokenVerifierPort accessTokenVerifier,
      TenantRepositoryPort tenantRepository,
      MembershipRepositoryPort membershipRepository,
      ClientAppRepositoryPort clientAppRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.accessTokenVerifier = accessTokenVerifier;
    this.tenantRepository = tenantRepository;
    this.membershipRepository = membershipRepository;
    this.clientAppRepository = clientAppRepository;
  }

  /**
   * Ejecuta la consulta de acceso self-service.
   *
   * @param command parámetros del comando
   * @return resultado con la lista de entradas de acceso (puede estar vacía)
   * @throws InvalidRefreshTokenException si el token es inválido
   * @throws TenantNotFoundException      si el tenant no existe
   */
  public UserAccessResult execute(GetUserAccessCommand command) {
    // 1. Verificar token y extraer sub
    var publicKeys = signingKeyRepository.findPublishableKeys();
    if (publicKeys.isEmpty()) {
      throw new InvalidRefreshTokenException("No public signing keys available for token verification");
    }

    Map<String, Object> claims;
    try {
      claims = accessTokenVerifier.verify(command.bearerToken(), publicKeys);
    } catch (InvalidRefreshTokenException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidRefreshTokenException("Invalid or expired access token: " + e.getMessage(), e);
    }

    String sub = (String) claims.get("sub");
    if (sub == null) {
      throw new InvalidRefreshTokenException("Access token missing 'sub' claim");
    }

    // 2. Resolver tenant (para validar que existe)
    tenantRepository.findBySlug(new TenantSlug(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 3. Parsear userId
    UUID userId;
    try {
      userId = UUID.fromString(sub);
    } catch (IllegalArgumentException e) {
      throw new InvalidRefreshTokenException("Access token 'sub' is not a valid UUID: " + sub);
    }

    // 4. Obtener membresías del usuario en el tenant
    List<Membership> memberships = membershipRepository.findByUserIdAndTenantSlug(
        userId, command.tenantSlug());

    // 5. Mapear a UserAccessEntry enriqueciendo con nombre de app y roles directos
    List<UserAccessEntry> entries = memberships.stream()
        .map(membership -> toAccessEntry(membership, userId))
        .toList();

    return new UserAccessResult(entries);
  }

  private UserAccessEntry toAccessEntry(Membership membership, UUID userId) {
    UUID clientAppUuid = membership.getClientAppId().value();

    String appName = clientAppRepository.findById(ClientAppId.of(clientAppUuid))
        .map(app -> app.getName())
        .orElse("Unknown");

    List<String> roles = membershipRepository.findRoleCodesByUserAndClientApp(
        userId, clientAppUuid);

    return new UserAccessEntry(
        clientAppUuid,
        appName,
        membership.getId().value(),
        membership.getStatus().name(),
        roles);
  }
}
