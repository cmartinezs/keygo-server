package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: verify the email of a contract using the code sent during creation.
 * Advances contract status from PENDING_EMAIL_VERIFICATION → PENDING_PAYMENT.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class VerifyContractEmailUseCase {

  private final AppContractRepositoryPort contractRepo;

  public VerifyContractEmailUseCase(AppContractRepositoryPort contractRepo) {
    this.contractRepo = contractRepo;
  }

  public AppContractResult execute(UUID contractId, String inputCode) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado: " + contractId));

    OffsetDateTime now = OffsetDateTime.now();
    contract.verifyCode(inputCode, now);
    contract = contractRepo.save(contract);

    return new AppContractResult(contract, null);
  }
}

