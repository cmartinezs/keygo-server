package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;

/**
 * Use case: revoke user access to an application.
 * <p>Caso de uso: revocar acceso de usuario a una aplicación.
 * Validates that the membership belongs to the specified tenant before removing it.
 * <p>Valida que la membresía pertenece al tenant indicado antes de eliminarla.
 * @author cmartinezs
 * @version 1.0
 */
public class RevokeMembershipUseCase {

  private final MembershipRepositoryPort membershipRepositoryPort;

  public RevokeMembershipUseCase(MembershipRepositoryPort membershipRepositoryPort) {
    this.membershipRepositoryPort = membershipRepositoryPort;
  }

  /**
   * Execute the use case.
   * Verifies the membership exists and belongs to the given tenant before deleting it.
   * @param membershipId the ID of the membership to revoke
   * @param tenantSlug the slug of the tenant that must own the membership
   * @throws MembershipNotFoundException if the membership does not exist or does not belong to the tenant
   */
  public void execute(MembershipId membershipId, String tenantSlug) {
    membershipRepositoryPort.findByIdAndTenantSlug(membershipId, tenantSlug)
        .orElseThrow(() -> new MembershipNotFoundException(
            "Membership not found or does not belong to tenant: " + membershipId));
    membershipRepositoryPort.deleteById(membershipId);
  }
}
