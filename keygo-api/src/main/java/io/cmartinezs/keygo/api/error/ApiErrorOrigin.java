package io.cmartinezs.keygo.api.error;

/**
 * Clasifica el origen principal del error para facilitar diagnóstico en cliente.
 */
public enum ApiErrorOrigin {
  CLIENT_REQUEST,
  BUSINESS_RULE,
  SERVER_PROCESSING
}

