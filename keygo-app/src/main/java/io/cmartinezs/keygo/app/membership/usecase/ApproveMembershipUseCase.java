package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.membership.result.ApproveMembershipResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.shared.util.EmailMasker;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.User;
import java.util.Map;

/**
 * Use case: approve a pending membership, transitioning it to ACTIVE.
 * <p>Caso de uso: aprobar una membresía pendiente, cambiando su estado a ACTIVE.
 * Validates that the membership exists, belongs to the specified tenant, and is in PENDING status.
 * After approval, sends a notification email to the user.
 * <p>Valida que la membresía existe, pertenece al tenant indicado, y está en estado PENDING.
 * Tras la aprobación, envía un email de notificación al usuario.
 *
 * @author cmartinezs
 * @version 1.2
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
  public ApproveMembershipResult execute(MembershipId membershipId, String tenantSlug) {
    Membership membership = membershipRepositoryPort.findByIdAndTenantSlug(membershipId, tenantSlug)
        .orElseThrow(() -> new MembershipNotFoundException("id", String.valueOf(membershipId.value())));

    membership.approve();

    var approved = membershipRepositoryPort.update(membership);

    String maskedEmail = sendApprovalNotification(approved, tenantSlug);

    return new ApproveMembershipResult(approved, maskedEmail);
  }

  private String sendApprovalNotification(Membership membership, String tenantSlug) {
    try {
      var tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug));
      if (tenant.isEmpty()) {
        return null;
      }

      var userOpt = userRepositoryPort.findByIdAndTenantId(membership.getUserId(), tenant.get().getId());
      if (userOpt.isEmpty()) {
        return null;
      }

      User user = userOpt.get();

      var appName = clientAppRepositoryPort.findById(membership.getClientAppId())
          .map(ClientApp::getName)
          .orElse("la aplicación");

      emailNotificationPort.sendEmail(
          EmailNotificationPort.TYPE_MEMBERSHIP_APPROVED,
          user.getEmail().value(),
          user.getUsername().value(),
          Map.of("userUsername", user.getUsername().value(),
              "userFirstName", user.getFirstName() != null ? user.getFirstName() : "",
              "userLastName", user.getLastName() != null ? user.getLastName() : "",
              "appName", appName));

      return EmailMasker.mask(user.getEmail().value());
    } catch (Exception e) {
      // Best-effort: el email de notificación no debe impedir la aprobación.
      return null;
    }
  }
}
