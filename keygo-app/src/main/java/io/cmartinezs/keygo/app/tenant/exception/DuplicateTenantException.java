package io.cmartinezs.keygo.app.tenant.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a tenant with the same slug already exists.
 */
public class DuplicateTenantException extends UseCaseException {

  public DuplicateTenantException(String slug) {
    super("A tenant with slug '%s' already exists".formatted(slug));
  }
}
