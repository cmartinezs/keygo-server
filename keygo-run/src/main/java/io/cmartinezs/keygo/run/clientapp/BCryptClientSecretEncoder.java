package io.cmartinezs.keygo.run.clientapp;

import io.cmartinezs.keygo.app.clientapp.port.ClientSecretEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt-based implementation of ClientSecretEncoderPort.
 * <p>Implementación de ClientSecretEncoderPort basada en BCrypt.
 * @author cmartinezs
 * @version 1.0
 */
public class BCryptClientSecretEncoder implements ClientSecretEncoderPort {

  private final BCryptPasswordEncoder encoder;

  public BCryptClientSecretEncoder() {
    this.encoder = new BCryptPasswordEncoder();
  }

  @Override
  public String encode(String rawSecret) {
    return encoder.encode(rawSecret);
  }

  @Override
  public boolean matches(String rawSecret, String encodedSecret) {
    return encoder.matches(rawSecret, encodedSecret);
  }
}

