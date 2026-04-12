package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.ContractEmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractStateViolationException;
import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractVerificationCodeInvalidException;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractEmailVerification;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: verify the email code of a contract before payment approval.
 * <ol>
 *   <li>Validates the 6-digit code.</li>
 *   <li>Marks the verification as used.</li>
 *   <li>Advances contract status from PENDING_EMAIL_VERIFICATION to PENDING_PAYMENT.</li>
 * </ol>
 *
 * @author cmartinezs
 * @version 3.0
 */
public class VerifyContractEmailUseCase {

  private final AppContractRepositoryPort contractRepo;
  private final ContractEmailVerificationRepositoryPort contractVerificationRepo;

  public VerifyContractEmailUseCase(
      AppContractRepositoryPort contractRepo,
      ContractEmailVerificationRepositoryPort contractVerificationRepo) {
    this.contractRepo = contractRepo;
    this.contractVerificationRepo = contractVerificationRepo;
  }

  public AppContractResult execute(UUID contractId, String inputCode) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new ContractNotFoundException(contractId));

    OffsetDateTime now = OffsetDateTime.now();
    if (!ContractStatus.PENDING_EMAIL_VERIFICATION.equals(contract.getStatus())) {
      throw new ContractStateViolationException(contract.getId(), contract.getStatus(), "verifyContractEmail");
    }

    ContractEmailVerification verification = contractVerificationRepo.findByContractId(contractId)
        .orElseThrow(() -> new ContractVerificationCodeInvalidException(contractId, "verification code not found"));

    if (verification.isUsed() || !verification.matches(inputCode)) {
      throw new ContractVerificationCodeInvalidException(contractId);
    }
    if (verification.isExpired(now)) {
      throw new ContractVerificationCodeInvalidException(contractId, "code has expired");
    }

    verification.markUsed(now);
    contractVerificationRepo.markUsed(verification);
    contract.markEmailVerified(now);
    contract = contractRepo.save(contract);

    return new AppContractResult(contract, null);
  }
}
