package io.cmartinezs.keygo.app.billing.contracting.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when a plan version cannot be found during contract activation.
 */
public class PlanVersionNotFoundException extends UseCaseException {

  public PlanVersionNotFoundException(UUID planVersionId) {
    super("Plan version not found by id: %s".formatted(planVersionId));
  }
}
