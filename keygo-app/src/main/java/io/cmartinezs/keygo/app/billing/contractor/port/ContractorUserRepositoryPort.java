package io.cmartinezs.keygo.app.billing.contractor.port;

import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorUser;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorUserRole;
import java.util.UUID;

/**
 * Port OUT — persistence contract for contractor/platform-user associations.
 */
public interface ContractorUserRepositoryPort {
  ContractorUser assign(UUID contractorId, UUID platformUserId, ContractorUserRole role);

  boolean hasRole(UUID contractorId, UUID platformUserId, ContractorUserRole role);
}
