package io.cmartinezs.keygo.app.user.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Lanzada cuando se intenta restablecer la contraseña de un usuario que no está en estado
 * {@code RESET_PASSWORD}.
 *
 * <p>Mapeada en {@code GlobalExceptionHandler} → HTTP 403 {@code BUSINESS_RULE_VIOLATION}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class UserNotInResetPasswordStatusException extends UseCaseException {

  public UserNotInResetPasswordStatusException(String email) {
    super("User account is not pending password reset: %s".formatted(email));
  }
}
