package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an email verification code is invalid (wrong or already used).
 * <p>Se lanza cuando un código de verificación de email es inválido (incorrecto o ya utilizado).
 * @author cmartinezs
 * @version 1.0
 */
public class EmailVerificationInvalidException extends DomainException {

  public EmailVerificationInvalidException(String email) {
    super("Email verification code is invalid or already used for: " + email);
  }
}

