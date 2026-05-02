package io.cmartinezs.keygo.app.shared.exception;

import io.cmartinezs.keygo.domain.shared.exception.ExceptionLayer;
import io.cmartinezs.keygo.domain.shared.exception.KeyGoException;

/**
 * Base exception for outbound port failures (keygo-app).
 * Sets {@link ExceptionLayer#PORT} automatically.
 *
 * <p>Use this when a port call (repository, email, external service) fails
 * at the infrastructure boundary. Maps to HTTP 503 in {@code GlobalExceptionHandler}.
 */
public abstract class PortException extends KeyGoException {

  protected PortException(String message) {
    super(ExceptionLayer.PORT, message);
  }

  protected PortException(String message, Throwable cause) {
    super(ExceptionLayer.PORT, message, cause);
  }
}
