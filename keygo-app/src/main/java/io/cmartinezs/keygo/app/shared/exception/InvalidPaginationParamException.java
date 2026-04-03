package io.cmartinezs.keygo.app.shared.exception;

/**
 * Thrown when a pagination or sort parameter violates its constraints.
 * Lanzada cuando un parámetro de paginación u ordenamiento viola sus restricciones.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidPaginationParamException extends UseCaseException {

  public InvalidPaginationParamException(String param, String constraint) {
    super("Pagination parameter '%s' is invalid: %s".formatted(param, constraint));
  }
}
