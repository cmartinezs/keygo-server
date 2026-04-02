package io.cmartinezs.keygo.domain.billing.contracting.exception;

import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.shared.exception.DomainException;

import java.util.UUID;

/**
 * Thrown when a contract operation is attempted in an invalid or terminal state.
 */
public class ContractStateViolationException extends DomainException {

  public ContractStateViolationException(UUID contractId, ContractStatus current, String operation) {
    super("Contract %s cannot perform '%s' in state: %s".formatted(contractId, operation, current));
  }

  public ContractStateViolationException(UUID contractId, String reason) {
    super("Contract %s: %s".formatted(contractId, reason));
  }
}
