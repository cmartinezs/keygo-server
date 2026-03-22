package io.cmartinezs.keygo.domain.auth.model;

/**
 * Value object que encapsula el PKCE code challenge.
 *
 * <p>En PKCE (Proof Key for Public Clients), se envía:
 * <ul>
 *   <li>En la solicitud de autorización: code_challenge (derivado de un verifier)
 *   <li>En el intercambio de código: code_verifier (el original)
 * </ul>
 *
 * <p>Métodos soportados:
 * <ul>
 *   <li><b>plain</b>: code_challenge = code_verifier (solo dev/localhost)
 *   <li><b>S256</b>: code_challenge = BASE64URL(SHA256(code_verifier)) (recomendado en producción)
 * </ul>
 */
public final class CodeChallenge {
  private final String challenge;
  private final String method;

  private CodeChallenge(String challenge, String method) {
    this.challenge = challenge;
    this.method = method;
  }

  /**
   * Crea un CodeChallenge usando el método S256 (SHA256 URL-safe base64).
   *
   * @param challenge valor del desafío
   * @return CodeChallenge con método S256
   * @throws IllegalArgumentException si challenge es null o vacío
   */
  public static CodeChallenge s256(String challenge) {
    if (challenge == null || challenge.trim().isEmpty()) {
      throw new IllegalArgumentException("Code challenge cannot be null or empty");
    }
    return new CodeChallenge(challenge.trim(), "S256");
  }

  /**
   * Crea un CodeChallenge usando el método plain (sin transformación).
   *
   * <p>Solo debe usarse en desarrollo o con clientes públicos en localhost.
   *
   * @param challenge valor del desafío (igual al verifier)
   * @return CodeChallenge con método plain
   * @throws IllegalArgumentException si challenge es null o vacío
   */
  public static CodeChallenge plain(String challenge) {
    if (challenge == null || challenge.trim().isEmpty()) {
      throw new IllegalArgumentException("Code challenge cannot be null or empty");
    }
    return new CodeChallenge(challenge.trim(), "plain");
  }

  /**
   * Obtiene el valor del challenge.
   *
   * @return challenge
   */
  public String getChallenge() {
    return challenge;
  }

  /**
   * Obtiene el método PKCE utilizado.
   *
   * @return "S256" o "plain"
   */
  public String getMethod() {
    return method;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CodeChallenge that)) return false;
    return challenge.equals(that.challenge) && method.equals(that.method);
  }

  @Override
  public int hashCode() {
    return 31 * challenge.hashCode() + method.hashCode();
  }

  @Override
  public String toString() {
    return "CodeChallenge{" + "method='" + method + '\'' + '}';
  }
}

