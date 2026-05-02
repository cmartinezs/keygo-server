package io.cmartinezs.keygo.app.auth.port;

import java.time.Instant;

/**
 * Puerto OUT (salida) para obtener la hora actual del sistema.
 *
 * <p>Abstrae la dependencia de System.currentTimeMillis() permitiendo testabilidad.
 */
public interface ClockPort {

  /**
   * Obtiene el instante actual.
   *
   * @return Instant del momento actual
   */
  Instant now();

  /**
   * Suma una duración al instante actual.
   *
   * @param durationSeconds segundos a sumar
   * @return Instant futuro
   */
  Instant futureSeconds(long durationSeconds);
}

