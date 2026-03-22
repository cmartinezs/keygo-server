package io.cmartinezs.keygo.app.user.port;

/**
 * Port OUT — contract for hashing and verifying user passwords.
 * <p>Puerto de salida — contrato para hashear y verificar contraseñas de usuarios.
 * @author cmartinezs
 * @version 1.0
 */
public interface PasswordHasherPort {

  /**
   * Hash a raw password into its stored representation.
   * <p>Hashea una contraseña en texto plano a su representación almacenada.
   * @param rawPassword the raw password to hash
   * @return the hashed password
   */
  String hash(String rawPassword);

  /**
   * Verify whether a raw password matches a stored hash.
   * <p>Verifica si una contraseña en texto plano coincide con un hash almacenado.
   * @param rawPassword    the raw password to verify
   * @param hashedPassword the stored hash to compare against
   * @return true if the password matches the hash
   */
  boolean matches(String rawPassword, String hashedPassword);
}

