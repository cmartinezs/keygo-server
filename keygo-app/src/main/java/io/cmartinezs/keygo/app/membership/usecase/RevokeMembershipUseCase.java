package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;

/**
 * Use case: revoke user access to an application.
 * <p>Caso de uso: revocar acceso de usuario a una aplicación.
 * Removes the membership entirely.
 * <p>Remueve la membresía completamente.
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
   * @param membershipId the ID of the membership to revoke
   * @throws MembershipNotFoundException if the membership does not exist
   */
  public void execute(MembershipId membershipId) {
    membershipRepositoryPort.findById(membershipId)
        .orElseThrow(() -> new MembershipNotFoundException("Membership not found: " + membershipId));
    membershipRepositoryPort.deleteById(membershipId);
  }
}

