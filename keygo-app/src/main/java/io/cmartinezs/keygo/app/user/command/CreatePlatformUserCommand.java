package io.cmartinezs.keygo.app.user.command;

/**
 * Command to create a new global platform user.
 * <p>Comando para crear un nuevo usuario global de la plataforma.
 *
 * @param username    the desired username (globally unique)
 * @param email       the user's email address (globally unique)
 * @param rawPassword the raw password (will be hashed before storage)
 * @param firstName   optional first name
 * @param lastName    optional last name
 * @author cmartinezs
 * @version 1.0
 */
public record CreatePlatformUserCommand(
    String username,
    String email,
    String rawPassword,
    String firstName,
    String lastName
) {

  public CreatePlatformUserCommand {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username cannot be null or blank");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email cannot be null or blank");
    }
    if (rawPassword == null || rawPassword.isBlank()) {
      throw new IllegalArgumentException("rawPassword cannot be null or blank");
    }
  }
}
