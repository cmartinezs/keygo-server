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

  /**
   * Resolve a contractor accessible by a platform user through contractor_users.
   */
  Optional<Contractor> findByPlatformUserId(UUID platformUserId);

  /**
   * Resolve a contractor accessible by a platform user email through contractor_users.
   *
   * @param email the email address
   * @return optional contractor if found
   */
  Optional<Contractor> findByPlatformUserEmail(String email);
}

