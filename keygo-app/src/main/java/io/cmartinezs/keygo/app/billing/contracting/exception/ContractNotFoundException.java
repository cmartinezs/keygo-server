package io.cmartinezs.keygo.app.billing.contracting.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when a contract cannot be found by its ID during use-case execution.
 */
public class ContractNotFoundException extends UseCaseException {

  public ContractNotFoundException(UUID contractId) {
    super("Contract not found by id: %s".formatted(contractId));
  }
}
