package io.cmartinezs.keygo.app.user.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when two passwords that should match do not match (e.g., newPassword ≠ confirmNewPassword,
 * or newPassword = currentPassword).
 * <p>Se lanza cuando dos contraseñas que deberían coincidir no lo hacen.
 *
 * <p>HTTP response: 400 Bad Request → {@code INVALID_INPUT}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PasswordMismatchException extends UseCaseException {

  public PasswordMismatchException(String reason) {
    super("Password mismatch: %s".formatted(reason));
  }
}

