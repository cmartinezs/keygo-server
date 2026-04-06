package io.cmartinezs.keygo.app.user.command;

/**
 * Command to authenticate a global platform user.
 * <p>Comando para autenticar un usuario global de la plataforma.
 *
 * @param email       the user's email address
 * @param rawPassword the raw password to verify
 * @author cmartinezs
 * @version 1.0
 */
public record AuthenticatePlatformUserCommand(
    String email,
    String rawPassword
) {

  public AuthenticatePlatformUserCommand {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email cannot be null or blank");
    }
    if (rawPassword == null || rawPassword.isBlank()) {
      throw new IllegalArgumentException("rawPassword cannot be null or blank");
    }
  }
}
