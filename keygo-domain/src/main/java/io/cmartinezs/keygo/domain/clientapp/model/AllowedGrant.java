package io.cmartinezs.keygo.domain.clientapp.model;

/**
 * Enum representing supported OAuth2 grant types for a client application.
 * <p>Enum que representa los grant types OAuth2 soportados para una aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
public enum AllowedGrant {
  /** Standard OAuth2 Authorization Code flow. */
  AUTHORIZATION_CODE,
  /** OAuth2 Client Credentials flow — machine-to-machine. */
  CLIENT_CREDENTIALS,
  /** OAuth2 Refresh Token grant. */
  REFRESH_TOKEN,
  /** OAuth2 Implicit flow (legacy). */
  IMPLICIT
}

