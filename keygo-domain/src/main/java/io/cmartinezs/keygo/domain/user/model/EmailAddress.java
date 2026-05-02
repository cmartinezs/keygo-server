package io.cmartinezs.keygo.domain.user.model;

/**
 * Value object representing a valid email address.
 * <p>Objeto de valor que representa una dirección de correo electrónico válida.
 * @author cmartinezs
 * @version 1.0
 */
public record EmailAddress(String value) {

  /* Simplified RFC-5322 email regex */
  private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

  public EmailAddress {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("EmailAddress value cannot be null or blank");
    }
    if (!value.matches(EMAIL_REGEX)) {
      throw new IllegalArgumentException("EmailAddress has invalid format: " + value);
    }
  }

  /**
   * Create an EmailAddress from a string.
   * <p>Crea un EmailAddress a partir de un string.
   * @param value the email address string
   * @return an EmailAddress wrapping the given value
   */
  public static EmailAddress of(String value) {
    return new EmailAddress(value);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public String toString() {
    return value;
  }
}

