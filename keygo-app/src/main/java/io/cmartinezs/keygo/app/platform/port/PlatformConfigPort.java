package io.cmartinezs.keygo.app.platform.port;

import java.util.List;

/**
 * Port for retrieving platform-level configuration (redirect URIs, app name, etc.).
 *
 * <p>Implemented in {@code keygo-run} via {@code KeyGoPlatformProperties}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface PlatformConfigPort {

  /** Allowed OAuth2 redirect URIs for the platform client. */
  List<String> getAllowedRedirectUris();

  /** Human-readable application name shown in the authorize screen. */
  String getApplicationName();
}
