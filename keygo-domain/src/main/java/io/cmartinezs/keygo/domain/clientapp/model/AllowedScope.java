package io.cmartinezs.keygo.domain.clientapp.model;

/**
 * Value object representing an OAuth2 scope.
 * <p>Objeto de valor que representa un scope OAuth2.
 * @author cmartinezs
 * @version 1.0
 */
public record AllowedScope(String value) {

  public AllowedScope {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("AllowedScope value cannot be null or blank");
    }
  }

  /**
   * Create an AllowedScope from a string.
   * <p>Crea un AllowedScope a partir de un string.
   * @param value the scope string
   * @return a new AllowedScope
   */
  public static AllowedScope of(String value) {
    return new AllowedScope(value);
  }

  @Override
  public String toString() {
    return value;
  }
}

