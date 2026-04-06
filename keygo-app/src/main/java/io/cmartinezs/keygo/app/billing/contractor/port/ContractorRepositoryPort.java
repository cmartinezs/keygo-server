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
  Optional<Contractor> findByTenantUserId(UUID tenantUserId);

  /**
   * Find a contractor by the email of its linked TenantUser within a specific tenant.
   * Useful for checking if an email is already registered as a contractor before creating a contract.
   *
   * @param tenantId the tenant ID (usually the provider tenant)
   * @param email    the email address
   * @return optional contractor if found
   */
  Optional<Contractor> findByTenantUserEmail(UUID tenantId, String email);
}

