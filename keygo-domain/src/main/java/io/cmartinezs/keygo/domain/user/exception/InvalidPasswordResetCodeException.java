package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Se lanza cuando el código de verificación de reset de contraseña es incorrecto o no existe.
 * <p>Thrown when the 6-digit password-reset code provided by the user does not match the stored code.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidPasswordResetCodeException extends DomainException {

  public InvalidPasswordResetCodeException() {
    super("The password reset verification code is invalid or does not exist");
  }
}

