package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.filter.MembershipFilter;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import java.util.List;
import java.util.UUID;

/**
 * Use case: list memberships with pagination, filtering, and sorting.
 * <p>Caso de uso: listar membresías con paginación, filtrado y ordenamiento.
 * @author cmartinezs
 * @version 1.0
 */
public class ListMembershipsUseCase {

  private final MembershipRepositoryPort membershipRepositoryPort;

  public ListMembershipsUseCase(MembershipRepositoryPort membershipRepositoryPort) {
    this.membershipRepositoryPort = membershipRepositoryPort;
  }

  /**
   * List memberships with pagination, filtering, and sorting.
   * @param tenantSlug the tenant slug to scope the query
   * @param filter filter criteria (userId, clientAppId, pagination, sorting)
   * @return paginated result of memberships
   */
  public PagedResult<Membership> execute(String tenantSlug, MembershipFilter filter) {
    return membershipRepositoryPort.findAllPaged(tenantSlug, filter);
  }

  /**
   * List memberships for a given user within a tenant.
   * <p>Lista membresías de un usuario dentro de un tenant.
   * @param userId the user ID
   * @param tenantSlug the tenant slug to scope the query
   * @return list of memberships
   * @deprecated Use execute(tenantSlug, filter) instead
   */
  @Deprecated(forRemoval = true)
  public List<Membership> listByUserId(UUID userId, String tenantSlug) {
    return membershipRepositoryPort.findByUserIdAndTenantSlug(userId, tenantSlug);
  }

  /**
   * List memberships for a given app within a tenant.
   * <p>Lista membresías de una app dentro de un tenant.
   * @param clientAppId the client app ID
   * @param tenantSlug the tenant slug to scope the query
   * @return list of memberships
   * @deprecated Use execute(tenantSlug, filter) instead
   */
  @Deprecated(forRemoval = true)
  public List<Membership> listByClientAppId(UUID clientAppId, String tenantSlug) {
    return membershipRepositoryPort.findByClientAppIdAndTenantSlug(clientAppId, tenantSlug);
  }
}
