package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.domain.shared.exception.ExceptionLayer;
import io.cmartinezs.keygo.domain.shared.exception.KeyGoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when authentication is required or invalid.
 * Sets {@link ExceptionLayer#CONTROLLER} as the originating layer.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends KeyGoException {

  public UnauthorizedException(String reason) {
    super(ExceptionLayer.CONTROLLER, reason);
  }

  public UnauthorizedException(String reason, Throwable cause) {
    super(ExceptionLayer.CONTROLLER, reason, cause);
  }
}
