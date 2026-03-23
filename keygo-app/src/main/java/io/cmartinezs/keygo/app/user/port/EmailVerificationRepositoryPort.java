package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Optional;

/**
 * Port OUT — persistence contract for EmailVerification.
 * <p>Puerto de salida — contrato de persistencia para EmailVerification.
 * @author cmartinezs
 * @version 1.0
 */
public interface EmailVerificationRepositoryPort {

  /**
   * Persist a new email verification.
   * <p>Persiste una nueva verificación de email.
   * @param verification the verification to save
   * @return the saved verification
   */
  EmailVerification save(EmailVerification verification);

  /**
   * Find the most recent verification for a user within a tenant.
   * <p>Busca la verificación más reciente para un usuario dentro de un tenant.
   * @param userId   the user identifier
   * @param tenantId the tenant identifier
   * @return an Optional containing the latest verification if found
   */
  Optional<EmailVerification> findLatestByUserIdAndTenantId(UserId userId, TenantId tenantId);
}

