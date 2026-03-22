package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import java.util.List;
import java.util.UUID;

/**
 * Use case: list memberships for a given user or app.
 * <p>Caso de uso: listar membresías de un usuario o app.
 * @author cmartinezs
 * @version 1.0
 */
public class ListMembershipsUseCase {

  private final MembershipRepositoryPort membershipRepositoryPort;

  public ListMembershipsUseCase(MembershipRepositoryPort membershipRepositoryPort) {
    this.membershipRepositoryPort = membershipRepositoryPort;
  }

  /**
   * List memberships for a given user.
   * <p>Lista membresías de un usuario.
   * @param userId the user ID
   * @return list of memberships
   */
  public List<Membership> listByUserId(UUID userId) {
    return membershipRepositoryPort.findByUserId(userId);
  }

  /**
   * List memberships for a given app.
   * <p>Lista membresías de una app.
   * @param clientAppId the client app ID
   * @return list of memberships
   */
  public List<Membership> listByClientAppId(UUID clientAppId) {
    return membershipRepositoryPort.findByClientAppId(clientAppId);
  }
}

