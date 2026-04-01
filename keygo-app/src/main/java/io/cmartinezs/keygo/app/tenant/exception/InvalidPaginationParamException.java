package io.cmartinezs.keygo.app.tenant.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a pagination parameter violates its constraints.
 */
public class InvalidPaginationParamException extends UseCaseException {

  public InvalidPaginationParamException(String param, String constraint) {
    super("Pagination parameter '%s' is invalid: %s".formatted(param, constraint));
  }
}
