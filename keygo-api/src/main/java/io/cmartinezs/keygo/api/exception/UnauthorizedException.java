package io.cmartinezs.keygo.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when authentication is required or invalid
 * Excepción lanzada cuando la autenticación es requerida o inválida
 *
 * @author cmartinezs
 * @version 1.0
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}

