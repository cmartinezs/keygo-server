package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a resend of a verification code is requested but the current code is still active.
 * <p>Se lanza cuando se solicita reenvío de código de verificación pero el código actual aún está vigente.
 * @author cmartinezs
 * @version 1.0
 */
public class EmailVerificationStillActiveException extends DomainException {

  public EmailVerificationStillActiveException(String email) {
    super("Email verification code is still active for: " + email
          + ". A new code can only be requested after the current one expires.");
  }
}

