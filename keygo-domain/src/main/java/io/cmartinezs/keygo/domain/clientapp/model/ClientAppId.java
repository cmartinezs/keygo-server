package io.cmartinezs.keygo.domain.clientapp.model;

import java.util.UUID;

/**
 * Value object representing a unique client application identifier.
 * <p>Objeto de valor que representa un identificador único de aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
public record ClientAppId(UUID value) {

  public ClientAppId {
    if (value == null) {
      throw new IllegalArgumentException("ClientAppId value cannot be null");
    }
  }

  /**
   * Generate a new random ClientAppId.
   * <p>Genera un nuevo ClientAppId aleatorio.
   * @return a new ClientAppId
   */
  public static ClientAppId generate() {
    return new ClientAppId(UUID.randomUUID());
  }

  /**
   * Create a ClientAppId from an existing UUID.
   * <p>Crea un ClientAppId a partir de un UUID existente.
   * @param value the UUID value
   * @return a ClientAppId wrapping the given UUID
   */
  public static ClientAppId of(UUID value) {
    return new ClientAppId(value);
  }

  /**
   * Create a ClientAppId from a string UUID.
   * <p>Crea un ClientAppId a partir de un UUID en formato string.
   * @param value the UUID string
   * @return a ClientAppId wrapping the parsed UUID
   * @throws IllegalArgumentException if the string is not a valid UUID
   */
  public static ClientAppId of(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ClientAppId string value cannot be null or blank");
    }
    return new ClientAppId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}

