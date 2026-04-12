package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractInvalidStateException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.ContractEmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Use case: retrieve a contract to resume the onboarding flow.
 * <p>Validates that the contract is not in a terminal state before returning it.
 * Allows the frontend to restore the current onboarding step when the user
 * returns after closing the page.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ResumeContractOnboardingUseCase {

  private static final Set<ContractStatus> TERMINAL_STATUSES = Set.of(
      ContractStatus.EXPIRED,
      ContractStatus.CANCELLED,
      ContractStatus.FAILED,
      ContractStatus.SUPERSEDED,
      ContractStatus.FINALIZED
  );

  private final AppContractRepositoryPort contractRepo;
  private final ContractEmailVerificationRepositoryPort contractVerificationRepo;

  public ResumeContractOnboardingUseCase(
      AppContractRepositoryPort contractRepo,
      ContractEmailVerificationRepositoryPort contractVerificationRepo) {
    this.contractRepo = contractRepo;
    this.contractVerificationRepo = contractVerificationRepo;
  }

  /**
   * Executes the use case.
   *
   * @param contractId the contract UUID provided by the frontend
   * @return {@link AppContractResult} with the current contract state
   * @throws ContractNotFoundException     if the contract does not exist
   * @throws ContractInvalidStateException if the contract is in a terminal state (cannot be resumed)
   */
  public AppContractResult execute(UUID contractId) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new ContractNotFoundException(contractId));

    if (TERMINAL_STATUSES.contains(contract.getStatus())) {
      throw new ContractInvalidStateException(contractId, contract.getStatus(),
          "contract is in a terminal state and cannot be resumed");
    }

    boolean verificationExpired = ContractStatus.PENDING_EMAIL_VERIFICATION.equals(contract.getStatus())
        && contractVerificationRepo.findByContractId(contractId)
            .map(v -> v.isExpired(OffsetDateTime.now()))
            .orElse(true);

    return new AppContractResult(contract, null, verificationExpired);
  }
}

