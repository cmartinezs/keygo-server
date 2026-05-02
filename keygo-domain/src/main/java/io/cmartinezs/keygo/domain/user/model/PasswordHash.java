package io.cmartinezs.keygo.domain.user.model;

/**
 * Value object representing a hashed password.
 * <p>Objeto de valor que representa una contraseña hasheada.
 * This value object never stores raw passwords — only the hashed form.
 * <p>Este objeto de valor nunca almacena contraseñas en texto plano, solo el hash.
 * @author cmartinezs
 * @version 1.0
 */
public record PasswordHash(String value) {

  public PasswordHash {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("PasswordHash value cannot be null or blank");
    }
  }

  /**
   * Create a PasswordHash from an already-hashed string.
   * <p>Crea un PasswordHash a partir de un string ya hasheado.
   * @param value the hashed password string
   * @return a PasswordHash wrapping the given value
   */
  public static PasswordHash of(String value) {
    return new PasswordHash(value);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public String toString() {
    // Never expose hash in toString to avoid accidental logging
    return "PasswordHash[REDACTED]";
  }
}

