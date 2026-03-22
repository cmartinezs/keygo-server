package io.cmartinezs.keygo.domain.auth.exception;

/**
 * Excepción lanzada cuando un scope solicitado no ha sido otorgado al usuario.
 */
public class ScopeNotGrantedException extends RuntimeException {
  public ScopeNotGrantedException(String message) {
    super(message);
  }

  public ScopeNotGrantedException(String message, Throwable cause) {
    super(message, cause);
  }
}

