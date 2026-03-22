package io.cmartinezs.keygo.domain.auth.model;

/** Algoritmo de firma soportado para tokens JWT. */
public enum SigningKeyAlgorithm {
  /** RSA 2048-bit con SHA-256 (RS256). Algoritmo por defecto. */
  RS256,
  /** RSA 2048-bit con SHA-384 (RS384). */
  RS384,
  /** RSA 2048-bit con SHA-512 (RS512). */
  RS512
}

