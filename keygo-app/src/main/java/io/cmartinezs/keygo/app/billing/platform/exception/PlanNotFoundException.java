package io.cmartinezs.keygo.app.billing.platform.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a platform plan cannot be found by code.
 */
public class PlanNotFoundException extends UseCaseException {

  public PlanNotFoundException(String planCode) {
    super("Platform plan not found: %s".formatted(planCode));
  }
}
