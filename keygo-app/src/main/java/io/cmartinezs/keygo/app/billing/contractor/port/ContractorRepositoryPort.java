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
}

