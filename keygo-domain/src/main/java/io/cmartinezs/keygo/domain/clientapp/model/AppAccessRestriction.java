package io.cmartinezs.keygo.domain.clientapp.model;

/**
 * Enum that controls token-time access to a ClientApp.
 * <p>Controla quién puede obtener tokens OAuth2 para esta aplicación.
 */
public enum AppAccessRestriction {

  /**
   * Only users with an ACTIVE membership can get tokens. Default for new apps.
   */
  CLOSED,

  /**
   * Any tenant user can get tokens without explicit membership.
   */
  OPEN_JOIN,

  /**
   * Users can self-register and automatically receive membership (not implemented in current release).
   */
  SELF_SIGNUP
}
