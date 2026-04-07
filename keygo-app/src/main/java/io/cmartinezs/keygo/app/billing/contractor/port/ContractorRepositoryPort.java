package io.cmartinezs.keygo.app.billing.contractor.port;

import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;

import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for Contractor.
 * @author cmartinezs
 * @version 1.0
 */
public interface ContractorRepositoryPort {
  Contractor save(Contractor contractor);
  Optional<Contractor> findById(UUID id);
  Optional<Contractor> findByPlatformUserId(UUID platformUserId);

  /**
   * Find a contractor by the email of its linked PlatformUser.
   * Email is globally unique in platform_users, so no tenantId is needed.
   *
   * @param email the email address
   * @return optional contractor if found
   */
  Optional<Contractor> findByPlatformUserEmail(String email);
}

