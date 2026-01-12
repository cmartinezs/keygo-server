package io.cmartinezs.keygo.app.port.out;

/**
 * Port for retrieving service public information
 * Puerto para obtener información pública del servicio
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface ServiceInfoProvider {

  /**
   * Get service title
   * @return service title
   */
  String getTitle();

  /**
   * Get service name
   * @return service name
   */
  String getName();

  /**
   * Get service version
   * @return service version
   */
  String getVersion();
}

