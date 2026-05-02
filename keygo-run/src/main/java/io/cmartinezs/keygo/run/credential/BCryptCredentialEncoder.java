package io.cmartinezs.keygo.run.credential;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt-based unified implementation of CredentialEncoderPort.
 * <p>Implementación unificada basada en BCrypt de CredentialEncoderPort.
 * Encodes both user passwords and OAuth2 client secrets.
 * <p>Codifica tanto contraseñas de usuario como secrets de cliente OAuth2.
 * @author cmartinezs
 * @version 1.0
 */
public class BCryptCredentialEncoder implements CredentialEncoderPort {

  private final BCryptPasswordEncoder encoder;

  public BCryptCredentialEncoder() {
    this.encoder = new BCryptPasswordEncoder();
  }

  @Override
  public String encode(String rawCredential) {
    return encoder.encode(rawCredential);
  }

  @Override
  public boolean matches(String rawCredential, String encodedCredential) {
    return encoder.matches(rawCredential, encodedCredential);
  }
}

