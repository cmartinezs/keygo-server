package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import java.util.List;
import java.util.UUID;

/**
 * Use case: list memberships for a given user or app, scoped to a tenant.
 * <p>Caso de uso: listar membresías de un usuario o app, acotadas a un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public class ListMembershipsUseCase {

  private final MembershipRepositoryPort membershipRepositoryPort;

  public ListMembershipsUseCase(MembershipRepositoryPort membershipRepositoryPort) {
    this.membershipRepositoryPort = membershipRepositoryPort;
  }

  /**
   * List memberships for a given user within a tenant.
   * <p>Lista membresías de un usuario dentro de un tenant.
   * @param userId the user ID
   * @param tenantSlug the tenant slug to scope the query
   * @return list of memberships
   */
  public List<Membership> listByUserId(UUID userId, String tenantSlug) {
    return membershipRepositoryPort.findByUserIdAndTenantSlug(userId, tenantSlug);
  }

  /**
   * List memberships for a given app within a tenant.
   * <p>Lista membresías de una app dentro de un tenant.
   * @param clientAppId the client app ID
   * @param tenantSlug the tenant slug to scope the query
   * @return list of memberships
   */
  public List<Membership> listByClientAppId(UUID clientAppId, String tenantSlug) {
    return membershipRepositoryPort.findByClientAppIdAndTenantSlug(clientAppId, tenantSlug);
  }
}
