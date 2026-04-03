package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Lanzada cuando se intenta usar un token de recuperación de contraseña que ya fue utilizado.
 *
 * <p>Mapeada en {@code GlobalExceptionHandler} → HTTP 422 {@code BUSINESS_RULE_VIOLATION}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PasswordRecoveryTokenAlreadyUsedException extends DomainException {

  public PasswordRecoveryTokenAlreadyUsedException() {
    super("Password recovery token has already been used");
  }
}
