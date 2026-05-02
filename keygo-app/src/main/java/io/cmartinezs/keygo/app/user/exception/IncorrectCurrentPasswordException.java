package io.cmartinezs.keygo.app.user.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Lanzada cuando el usuario proporciona un {@code current_password} incorrecto
 * al intentar cambiar su contraseña (self-service).
 *
 * <p>Mapeada en {@code GlobalExceptionHandler} → HTTP 403 {@code BUSINESS_RULE_VIOLATION}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class IncorrectCurrentPasswordException extends UseCaseException {

  public IncorrectCurrentPasswordException() {
    super("Current password is incorrect");
  }
}
