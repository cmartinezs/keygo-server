package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.user.exception.InvalidPasswordException;

/**
 * Política de contraseñas del dominio — DEPRECATED.
 *
 * <p>Este clase se mantiene por compatibilidad con código existente.
 * Usa {@link PasswordValidationHelper} para nueva validación — más flexible y mantenible.
 *
 * <p>Reglas (permanentes):
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
 * @deprecated use {@link PasswordValidationHelper#validate(String, boolean)} instead
 */
@Deprecated(since = "1.0", forRemoval = false)
public final class PasswordPolicy {

  private PasswordPolicy() {}

  /**
   * Valida que la contraseña cumpla la política de seguridad (permanente).
   *
   * @param password contraseña a validar
   * @throws InvalidPasswordException si la contraseña no cumple alguna regla
   * @deprecated use {@link PasswordValidationHelper#validate(String, boolean)} with {@code false}
   */
  @Deprecated(since = "1.0", forRemoval = false)
  public static void validate(String password) {
    PasswordValidationHelper.validate(password, false);
  }
}
