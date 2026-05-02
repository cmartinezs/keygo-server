package io.cmartinezs.keygo.app.billing.contracting.port;

import io.cmartinezs.keygo.domain.billing.contracting.model.ContractEmailVerification;

import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT for contract onboarding email verification codes.
 */
public interface ContractEmailVerificationRepositoryPort {

  ContractEmailVerification upsert(ContractEmailVerification verification);

  Optional<ContractEmailVerification> findByContractId(UUID contractId);

  void markUsed(ContractEmailVerification verification);
}
