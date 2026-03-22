package io.cmartinezs.keygo.run.user;

import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt-based implementation of PasswordHasherPort.
 * <p>Implementación de PasswordHasherPort basada en BCrypt.
 * @author cmartinezs
 * @version 1.0
 */
public class BCryptPasswordHasher implements PasswordHasherPort {

  private final BCryptPasswordEncoder encoder;

  public BCryptPasswordHasher() {
    this.encoder = new BCryptPasswordEncoder();
  }

  @Override
  public String hash(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String hashedPassword) {
    return encoder.matches(rawPassword, hashedPassword);
  }
}

