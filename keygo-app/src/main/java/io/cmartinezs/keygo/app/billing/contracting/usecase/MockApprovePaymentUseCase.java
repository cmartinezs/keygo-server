package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: simulate payment approval (dev/test only).
 * Reads keygo.billing.mock-payment-enabled flag. If false, returns 404.
 * @author cmartinezs
 * @version 1.0
 */
public class MockApprovePaymentUseCase {

  private final AppContractRepositoryPort contractRepo;
  private final boolean mockPaymentEnabled;

  public MockApprovePaymentUseCase(AppContractRepositoryPort contractRepo, boolean mockPaymentEnabled) {
    this.contractRepo = contractRepo;
    this.mockPaymentEnabled = mockPaymentEnabled;
  }

  public boolean isMockEnabled() {
    return mockPaymentEnabled;
  }

  public AppContractResult execute(UUID contractId) {
    if (!mockPaymentEnabled) {
      throw new UnsupportedOperationException("Mock payment is disabled in this environment");
    }

    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

    if (ContractStatus.ACTIVATED.equals(contract.getStatus()) ||
        ContractStatus.CANCELLED.equals(contract.getStatus())) {
      throw new IllegalStateException("Contract cannot be approved in current state: " + contract.getStatus());
    }

    contract.markPaymentApproved(OffsetDateTime.now());
    contract = contractRepo.save(contract);
    return new AppContractResult(contract, null);
  }
}

