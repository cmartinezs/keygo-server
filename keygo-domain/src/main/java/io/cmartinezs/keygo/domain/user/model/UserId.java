package io.cmartinezs.keygo.domain.user.model;

import java.util.UUID;

/**
 * Value object representing a unique user identifier.
 * <p>Objeto de valor que representa un identificador único de usuario.
 * @author cmartinezs
 * @version 1.0
 */
public record UserId(UUID value) {

  public UserId {
    if (value == null) {
      throw new IllegalArgumentException("UserId value cannot be null");
    }
  }

  /**
   * Generate a new random UserId.
   * <p>Genera un nuevo UserId aleatorio.
   * @return a new UserId
   */
  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }

  /**
   * Create a UserId from an existing UUID.
   * <p>Crea un UserId a partir de un UUID existente.
   * @param value the UUID value
   * @return a UserId wrapping the given UUID
   */
  public static UserId of(UUID value) {
    return new UserId(value);
  }

  /**
   * Create a UserId from a string UUID.
   * <p>Crea un UserId a partir de un UUID en formato string.
   * @param value the UUID string
   * @return a UserId wrapping the parsed UUID
   * @throws IllegalArgumentException if the string is not a valid UUID
   */
  public static UserId of(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("UserId string value cannot be null or blank");
    }
    return new UserId(UUID.fromString(value));
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public String toString() {
    return value.toString();
  }
}

