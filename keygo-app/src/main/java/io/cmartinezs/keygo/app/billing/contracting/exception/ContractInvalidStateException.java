package io.cmartinezs.keygo.app.billing.contracting.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import java.util.UUID;

/**
 * Thrown when a contract operation cannot proceed because the contract is in an invalid state.
 */
public class ContractInvalidStateException extends UseCaseException {

  public ContractInvalidStateException(UUID contractId, ContractStatus status) {
    super("Contract %s cannot be processed in state: %s".formatted(contractId, status));
  }

  public ContractInvalidStateException(UUID contractId, ContractStatus status, String reason) {
    super("Contract %s cannot be processed in state %s: %s".formatted(contractId, status, reason));
  }
}
