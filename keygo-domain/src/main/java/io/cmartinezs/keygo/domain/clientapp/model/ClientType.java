package io.cmartinezs.keygo.domain.clientapp.model;

/**
 * Enum representing the OAuth2 client type.
 * <p>Enum que representa el tipo de cliente OAuth2.
 * @author cmartinezs
 * @version 1.0
 */
public enum ClientType {
  /** Public client — no client secret (e.g., SPA, mobile app). */
  PUBLIC,
  /** Confidential client — holds a client secret (e.g., server-side app). */
  CONFIDENTIAL
}

