package io.cmartinezs.keygo.domain.user.exception;

/**
 * Thrown when credential validation fails (wrong password or user not found by credential).
 * <p>Se lanza cuando la validación de credenciales falla (contraseña incorrecta).
 * Note: this exception is intentionally generic to avoid credential enumeration.
 * <p>Nota: esta excepción es genérica intencionalmente para evitar enumeración de credenciales.
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Invalid credentials provided");
  }
}

