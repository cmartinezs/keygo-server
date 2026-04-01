package io.cmartinezs.keygo.app.billing.catalog.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a plan with the same code already exists for the given client app.
 */
public class DuplicatePlanCodeException extends UseCaseException {

  public DuplicatePlanCodeException(String code) {
    super("Plan code already exists: %s".formatted(code));
  }
}
