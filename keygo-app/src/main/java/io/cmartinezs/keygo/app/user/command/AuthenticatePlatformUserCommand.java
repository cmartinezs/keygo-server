package io.cmartinezs.keygo.app.user.command;

/**
 * Command to authenticate a global platform user.
 * <p>Comando para autenticar un usuario global de la plataforma.
 *
 * @param emailOrUsername the user's email address or username
 * @param rawPassword    the raw password to verify
 * @author cmartinezs
 * @version 1.0
 */
public record AuthenticatePlatformUserCommand(
    String emailOrUsername,
    String rawPassword
) {

  public AuthenticatePlatformUserCommand {
    if (emailOrUsername == null || emailOrUsername.isBlank()) {
      throw new IllegalArgumentException("emailOrUsername cannot be null or blank");
    }
    if (rawPassword == null || rawPassword.isBlank()) {
      throw new IllegalArgumentException("rawPassword cannot be null or blank");
    }
  }
}
