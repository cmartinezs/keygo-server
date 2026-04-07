package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;

/**
 * Use case: approve a pending membership, transitioning it to ACTIVE.
 * <p>Caso de uso: aprobar una membresía pendiente, cambiando su estado a ACTIVE.
 * Validates that the membership exists, belongs to the specified tenant, and is in PENDING status.
 * After approval, sends a notification email to the user.
 * <p>Valida que la membresía existe, pertenece al tenant indicado, y está en estado PENDING.
 * Tras la aprobación, envía un email de notificación al usuario.
 *
 * @author cmartinezs
 * @version 1.1
 */
public class ApproveMembershipUseCase {

  private final MembershipRepositoryPort membershipRepositoryPort;
  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final EmailNotificationPort emailNotificationPort;

  public ApproveMembershipUseCase(
      MembershipRepositoryPort membershipRepositoryPort,
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      ClientAppRepositoryPort clientAppRepositoryPort,
      EmailNotificationPort emailNotificationPort) {
    this.membershipRepositoryPort = membershipRepositoryPort;
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.emailNotificationPort = emailNotificationPort;
  }

  /**
   * Approve a pending membership.
   *
   * @param membershipId the ID of the membership to approve
   * @param tenantSlug   the slug of the tenant that must own the membership
   * @return the approved membership with ACTIVE status
   * @throws MembershipNotFoundException if the membership does not exist or does not belong to the tenant
   * @throws io.cmartinezs.keygo.domain.membership.exception.MembershipAlreadyActiveException if already active
   * @throws io.cmartinezs.keygo.domain.membership.exception.MembershipAlreadySuspendedException if suspended
   */
  public Membership execute(MembershipId membershipId, String tenantSlug) {
    Membership membership = membershipRepositoryPort.findByIdAndTenantSlug(membershipId, tenantSlug)
        .orElseThrow(() -> new MembershipNotFoundException("id", String.valueOf(membershipId.value())));

    membership.approve();

    var approved = membershipRepositoryPort.update(membership);

    sendApprovalNotification(approved, tenantSlug);

    return approved;
  }

  private void sendApprovalNotification(Membership membership, String tenantSlug) {
    try {
      var tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug));
      if (tenant.isEmpty()) {
        return;
      }

      var user = userRepositoryPort.findByIdAndTenantId(membership.getUserId(), tenant.get().getId());
      if (user.isEmpty()) {
        return;
      }

      var appName = clientAppRepositoryPort.findById(membership.getClientAppId())
          .map(app -> app.getName())
          .orElse("la aplicación");

      emailNotificationPort.sendMembershipApprovedEmail(
          user.get().getEmail().value(),
          user.get().getUsername().value(),
          appName);
    } catch (Exception e) {
      // Email failure must not prevent the approval from completing.
      // KeyGoTracingAspect will log the error via AOP if tracing is enabled.
    }
  }
}
