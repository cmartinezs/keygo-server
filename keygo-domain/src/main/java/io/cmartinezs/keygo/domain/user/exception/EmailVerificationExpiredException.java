package io.cmartinezs.keygo.domain.user.exception;

/**
 * Thrown when an email verification code has expired.
 * <p>Se lanza cuando un código de verificación de email ha expirado.
 * @author cmartinezs
 * @version 1.0
 */
public class EmailVerificationExpiredException extends RuntimeException {

  public EmailVerificationExpiredException(String email) {
    super("Email verification code has expired for: " + email);
  }
}

