package io.cmartinezs.keygo.domain.user.exception;

/**
 * Excepción lanzada cuando no se encuentra la solicitud de reset de contraseña
 * con el {@code requestId} proporcionado.
 *
 * <p>HTTP: 404 Not Found → {@code RESOURCE_NOT_FOUND}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PasswordResetRequestNotFoundException extends RuntimeException {

  public PasswordResetRequestNotFoundException(String requestId) {
    super("Password reset request not found: " + requestId);
  }
}

