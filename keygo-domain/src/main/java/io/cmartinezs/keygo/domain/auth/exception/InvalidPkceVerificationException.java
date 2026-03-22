package io.cmartinezs.keygo.domain.auth.exception;

/**
 * Excepción lanzada cuando la verificación PKCE falla.
 *
 * <p>Causas posibles:
 * <ul>
 *   <li>Code verifier no coincide con code challenge
 *   <li>Code verifier falta cuando code challenge está presente
 *   <li>Método PKCE no es soportado
 * </ul>
 */
public class InvalidPkceVerificationException extends RuntimeException {
  public InvalidPkceVerificationException(String message) {
    super(message);
  }

  public InvalidPkceVerificationException(String message, Throwable cause) {
    super(message, cause);
  }
}

