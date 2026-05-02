package io.cmartinezs.keygo.domain.billing.contracting.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

import java.util.UUID;

/**
 * Thrown when the email verification code provided for a contract does not match or has expired.
 */
public class ContractVerificationCodeInvalidException extends DomainException {

  public ContractVerificationCodeInvalidException(UUID contractId) {
    super("Verification code is invalid for contract: %s".formatted(contractId));
  }

  public ContractVerificationCodeInvalidException(UUID contractId, String reason) {
    super("Verification code is invalid for contract %s: %s".formatted(contractId, reason));
  }
}
