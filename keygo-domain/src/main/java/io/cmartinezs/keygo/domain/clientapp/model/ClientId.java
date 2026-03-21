package io.cmartinezs.keygo.domain.clientapp.model;

/**
 * Value object representing the OAuth2 client_id.
 * <p>Objeto de valor que representa el client_id OAuth2.
 * @author cmartinezs
 * @version 1.0
 */
public record ClientId(String value) {

  public ClientId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ClientId value cannot be null or blank");
    }
    if (value.length() > 255) {
      throw new IllegalArgumentException("ClientId value cannot exceed 255 characters");
    }
  }

  /**
   * Create a ClientId from a string value.
   * <p>Crea un ClientId a partir de un valor string.
   * @param value the string value
   * @return a new ClientId
   */
  public static ClientId of(String value) {
    return new ClientId(value);
  }

  @Override
  public String toString() {
    return value;
  }
}

