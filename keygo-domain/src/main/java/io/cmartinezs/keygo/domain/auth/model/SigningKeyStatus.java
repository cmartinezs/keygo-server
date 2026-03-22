package io.cmartinezs.keygo.domain.auth.model;

/** Estado del ciclo de vida de una clave de firma RSA. */
public enum SigningKeyStatus {
  /** Clave activa: usada para firmar nuevos tokens. */
  ACTIVE,
  /** Clave retirada: no firma nuevos tokens, pero su clave pública se sigue publicando en JWKS. */
  RETIRED,
  /** Clave revocada: ya no firma ni se publica en JWKS. */
  REVOKED
}

