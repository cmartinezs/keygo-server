package io.cmartinezs.keygo.run.config.auth;

import io.cmartinezs.keygo.app.auth.port.ClockPort;
import java.time.Instant;

/**
 * Implementación del puerto ClockPort usando el reloj del sistema.
 */
public class SystemClockProvider implements ClockPort {

  @Override
  public Instant now() {
    return Instant.now();
  }

  @Override
  public Instant futureSeconds(long durationSeconds) {
    return Instant.now().plusSeconds(durationSeconds);
  }
}

