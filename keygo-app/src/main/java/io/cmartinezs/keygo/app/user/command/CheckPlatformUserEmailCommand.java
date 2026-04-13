package io.cmartinezs.keygo.app.user.command;

/**
 * Command to check whether a platform user exists by email.
 *
 * @param email platform user email to check
 * @author cmartinezs
 * @version 1.0
 */
public record CheckPlatformUserEmailCommand(String email) {

  public CheckPlatformUserEmailCommand {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email cannot be null or blank");
    }
  }
}
