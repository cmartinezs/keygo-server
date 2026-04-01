package io.cmartinezs.keygo.app.shared.exception;

import io.cmartinezs.keygo.domain.shared.exception.ExceptionLayer;
import io.cmartinezs.keygo.domain.shared.exception.KeyGoException;

/**
 * Base exception for all use-case layer errors (keygo-app).
 * Sets {@link ExceptionLayer#USE_CASE} automatically.
 *
 * <p>Subclasses must define their own message template and accept only
 * typed values — never a free-form string built by the caller.
 */
public abstract class UseCaseException extends KeyGoException {

  protected UseCaseException(String message) {
    super(ExceptionLayer.USE_CASE, message);
  }

  protected UseCaseException(String message, Throwable cause) {
    super(ExceptionLayer.USE_CASE, message, cause);
  }
}
