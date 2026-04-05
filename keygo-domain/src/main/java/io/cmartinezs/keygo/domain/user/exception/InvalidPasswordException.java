package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a password violates the security policy.
 * <p>Se lanza cuando una contraseña viola la política de seguridad.
 *
 * <p>Reglas de la política:
 * <ul>
 *   <li>Longitud mínima: 12 caracteres (para contraseñas definitivas)</li>
 *   <li>Longitud mínima: 8 caracteres (para contraseñas temporarias)</li>
 *   <li>Al menos una letra mayúscula</li>
 *   <li>Al menos una letra minúscula</li>
 *   <li>Al menos un dígito</li>
 *   <li>Al menos un carácter especial (no alfanumérico)</li>
 * </ul>
 *
 * <p>HTTP response: 400 Bad Request → {@code INVALID_INPUT}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidPasswordException extends DomainException {

  public InvalidPasswordException(String reason) {
    super("Password validation failed: %s".formatted(reason));
  }
}

