package io.cmartinezs.keygo.app.platform.port;

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

  /**
   * Get the active environment / profile name (e.g. "local", "desa", "prod", "default").
   * @return active environment name
   */
  String getEnvironment();

  /**
   * Get service operational status.
   * Implementations always return "UP" — the service cannot respond when DOWN.
   * @return service status string
   */
  String getStatus();
}

