package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Se lanza cuando el código de verificación de reset de contraseña ha expirado.
 * <p>Thrown when the 6-digit password-reset code has passed its TTL.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PasswordResetCodeExpiredException extends DomainException {

  public PasswordResetCodeExpiredException() {
    super("The password reset verification code has expired. Please log in again to receive a new code.");
  }
}

