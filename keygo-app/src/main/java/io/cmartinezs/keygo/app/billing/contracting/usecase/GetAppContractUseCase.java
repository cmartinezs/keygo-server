package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;

import java.util.UUID;

/**
 * Use case: retrieve a contract by ID.
 * @author cmartinezs
 * @version 1.0
 */
public class GetAppContractUseCase {

  private final AppContractRepositoryPort contractRepo;

  public GetAppContractUseCase(AppContractRepositoryPort contractRepo) {
    this.contractRepo = contractRepo;
  }

  public AppContractResult execute(UUID contractId) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new ContractNotFoundException(contractId));
    return new AppContractResult(contract, null);
  }
}

