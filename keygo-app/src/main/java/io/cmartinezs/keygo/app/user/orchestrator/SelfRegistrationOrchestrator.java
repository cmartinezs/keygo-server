package io.cmartinezs.keygo.app.user.orchestrator;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.command.CreateMembershipCommand;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.usecase.CreateMembershipUseCase;
import io.cmartinezs.keygo.app.user.command.VerifyEmailCommand;
import io.cmartinezs.keygo.app.user.usecase.VerifyEmailUseCase;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.RegistrationPolicy;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.User;

import java.util.Optional;
import java.util.Set;

/**
 * Orchestrator for the self-registration flow: email verification + conditional membership
 * creation.
 *
 * <p>Orquestador del flujo de auto-registro: verificación de email + creación condicional de
 * membresía.
 *
 * <p>Coordinates multiple use cases to implement the policy-driven post-verification behavior.
 * Ensures single responsibility of each use case while enabling complex flows.
 *
 * <p>Coordina múltiples casos de uso para implementar el comportamiento post-verificación
 * controlado por política. Asegura responsabilidad única de cada caso de uso mientras habilita
 * flujos complejos.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class SelfRegistrationOrchestrator {

  private final VerifyEmailUseCase verifyEmailUseCase;
  private final CreateMembershipUseCase createMembershipUseCase;
  private final ClientAppRepositoryPort clientAppRepositoryPort;
  private final AppRoleRepositoryPort appRoleRepositoryPort;

  public SelfRegistrationOrchestrator(
      VerifyEmailUseCase verifyEmailUseCase,
      CreateMembershipUseCase createMembershipUseCase,
      ClientAppRepositoryPort clientAppRepositoryPort,
      AppRoleRepositoryPort appRoleRepositoryPort) {
    this.verifyEmailUseCase = verifyEmailUseCase;
    this.createMembershipUseCase = createMembershipUseCase;
    this.clientAppRepositoryPort = clientAppRepositoryPort;
    this.appRoleRepositoryPort = appRoleRepositoryPort;
  }

  /**
   * Execute the self-registration verification flow with policy-driven membership creation.
   *
   * <p>Ejecuta el flujo de verificación de auto-registro con creación de membresía controlada
   * por política.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Verify email using VerifyEmailUseCase (activates user)
   *   <li>Retrieve ClientApp and its registrationPolicy
   *   <li>If policy requires automatic membership (OPEN_AUTO_ACTIVE or OPEN_AUTO_PENDING),
   *       create membership with the corresponding status
   *   <li>Return the activated User
   * </ol>
   *
   * @param tenantSlug      the tenant slug
   * @param clientId       the client app ID
   * @param email         the email to verify (optional if registrationId provided)
   * @param registrationId the registration ID (optional if email provided)
   * @param code           the verification code
   * @return the activated User with optional auto-created Membership
   * @throws Exception if verification fails, app not found, or membership creation fails
   */
  public User execute(String tenantSlug, String clientId, String email, String registrationId, String code) {
    // Step 1: Verify email (activates user)
    VerifyEmailCommand verifyCmd = new VerifyEmailCommand(tenantSlug, clientId, email, registrationId, code);
    User activatedUser = verifyEmailUseCase.execute(verifyCmd);

    // Step 2: Retrieve ClientApp and its registrationPolicy
    var clientApp = clientAppRepositoryPort
        .findByClientIdAndTenantId(ClientId.of(clientId), activatedUser.getTenantId())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "ClientApp not found for clientId " + clientId + " in tenant " + tenantSlug));

    RegistrationPolicy policy = clientApp.getRegistrationPolicy();

    // Step 3: Create membership based on policy
    if (RegistrationPolicy.OPEN_AUTO_ACTIVE.equals(policy)
        || RegistrationPolicy.OPEN_AUTO_PENDING.equals(policy)) {
      createMembershipForSelfRegistration(tenantSlug, activatedUser, clientApp, policy);
    }

    // Step 4: Return the activated user
    return activatedUser;
  }

  /**
   * Create an automatic membership for self-registered user based on registration policy.
   *
   * <p>Crea una membresía automática para usuario auto-registrado basada en la política de
   * registro.
   *
   * @param tenantSlug the tenant slug
   * @param user the activated user
   * @param clientApp the client app with registration policy
   * @param policy the registration policy (must be OPEN_AUTO_ACTIVE or OPEN_AUTO_PENDING)
   */
  private void createMembershipForSelfRegistration(
      String tenantSlug,
      User user,
      ClientApp clientApp,
      RegistrationPolicy policy) {
    Optional<AppRole> defaultRole = appRoleRepositoryPort.findDefaultByClientApp(clientApp.getId().value());
    Set<String> roleCodes = defaultRole.map(r -> Set.of(r.getCode().value())).orElse(Set.of());

    CreateMembershipCommand cmd =
        new CreateMembershipCommand(
            tenantSlug,
            user.getId().value(),
            clientApp.getId().value(),
            roleCodes);

    createMembershipUseCase.execute(cmd);
  }
}
