package io.cmartinezs.keygo.domain.user.model;

/**
 * Política de contraseñas del dominio.
 *
 * <p>Clase utilitaria sin dependencias externas ni Spring.
 * Valida que una contraseña cumpla los requisitos mínimos de seguridad.
 *
 * <p>Reglas:
 * <ul>
 *   <li>Longitud mínima: 12 caracteres</li>
 *   <li>Al menos una letra mayúscula</li>
 *   <li>Al menos una letra minúscula</li>
 *   <li>Al menos un dígito</li>
 *   <li>Al menos un carácter especial (no alfanumérico)</li>
 * </ul>
 *
 * @author cmartinezs
 * @version 1.0
 */
public final class PasswordPolicy {

  private PasswordPolicy() {}

  /**
   * Valida que la contraseña cumpla la política de seguridad.
   *
   * @param password contraseña a validar
   * @throws IllegalArgumentException si la contraseña no cumple alguna regla
   */
  public static void validate(String password) {
    if (password == null || password.length() < 12) {
      throw new IllegalArgumentException(
          "new_password: debe tener al menos 12 caracteres");
    }
    if (!password.matches(".*[A-Z].*")) {
      throw new IllegalArgumentException(
          "new_password: debe contener al menos una letra mayúscula");
    }
    if (!password.matches(".*[a-z].*")) {
      throw new IllegalArgumentException(
          "new_password: debe contener al menos una letra minúscula");
    }
    if (!password.matches(".*\\d.*")) {
      throw new IllegalArgumentException(
          "new_password: debe contener al menos un dígito");
    }
    if (!password.matches(".*[^A-Za-z0-9].*")) {
      throw new IllegalArgumentException(
          "new_password: debe contener al menos un carácter especial");
    }
  }
}
