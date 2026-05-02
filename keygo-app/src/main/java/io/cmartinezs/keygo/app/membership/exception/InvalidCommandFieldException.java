package io.cmartinezs.keygo.app.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a required command field is null or blank.
 */
public class InvalidCommandFieldException extends UseCaseException {

  public InvalidCommandFieldException(String field) {
    super("Command field cannot be null or blank: %s".formatted(field));
  }
}
