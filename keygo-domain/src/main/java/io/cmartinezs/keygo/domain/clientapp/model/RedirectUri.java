package io.cmartinezs.keygo.domain.clientapp.model;

import io.cmartinezs.keygo.domain.clientapp.exception.InvalidRedirectUriException;

/**
 * Value object representing an OAuth2 redirect URI.
 * <p>Objeto de valor que representa un URI de redirección OAuth2.
 * @author cmartinezs
 * @version 1.0
 */
public record RedirectUri(String value) {

  public RedirectUri {
    if (value == null || value.isBlank()) {
      throw new InvalidRedirectUriException(value);
    }
    if (!value.startsWith("https://") && !value.startsWith("http://localhost") && !value.startsWith("http://127.0.0.1")) {
      throw new InvalidRedirectUriException(value);
    }
  }

  /**
   * Create a RedirectUri from a string.
   * <p>Crea un RedirectUri a partir de un string.
   * @param value the URI string
   * @return a new RedirectUri
   */
  public static RedirectUri of(String value) {
    return new RedirectUri(value);
  }

  @Override
  public String toString() {
    return value;
  }
}

