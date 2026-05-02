package io.cmartinezs.keygo.app.billing.platform.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

import java.util.UUID;

/**
 * Thrown when a contractor cannot be found for the given platform user.
 */
public class ContractorNotFoundException extends UseCaseException {

  public ContractorNotFoundException(UUID platformUserId) {
    super("Contractor not found for platform user: %s".formatted(platformUserId));
  }
}
