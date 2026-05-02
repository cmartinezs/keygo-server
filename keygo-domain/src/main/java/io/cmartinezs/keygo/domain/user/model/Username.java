package io.cmartinezs.keygo.domain.user.model;

/**
 * Value object representing a unique username within a tenant.
 * <p>Objeto de valor que representa un nombre de usuario único dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public record Username(String value) {

  /* 3–100 characters: letters, digits, underscore, dash, dot */
  private static final String USERNAME_REGEX = "^[a-zA-Z0-9_.\\-]{3,100}$";

  public Username {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Username value cannot be null or blank");
    }
    if (!value.matches(USERNAME_REGEX)) {
      throw new IllegalArgumentException(
          "Username must be 3-100 characters and contain only letters, digits, '_', '-' or '.'");
    }
  }

  /**
   * Create a Username from a string.
   * <p>Crea un Username a partir de un string.
   * @param value the username string
   * @return a Username wrapping the given value
   */
  public static Username of(String value) {
    return new Username(value);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public String toString() {
    return value;
  }
}

