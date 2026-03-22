package io.cmartinezs.keygo.domain.auth.exception;

/**
 * Excepción lanzada cuando no existe ninguna clave de firma activa en el sistema.
 *
 * <p>Indica que el servidor no puede emitir tokens JWT porque no hay una {@code SigningKey} con
 * estado {@code ACTIVE} disponible.
 */
public class NoActiveSigningKeyException extends RuntimeException {

  public NoActiveSigningKeyException(String message) {
    super(message);
  }

  public NoActiveSigningKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}

