package io.cmartinezs.keygo.infra.auth.security;

import io.cmartinezs.keygo.app.auth.exception.HashingUnavailableException;
import io.cmartinezs.keygo.app.auth.exception.UnsupportedPkceMethodException;
import lombok.experimental.UtilityClass;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utilidad para validación de PKCE (Proof Key for Public Clients).
 *
 * <p>Verifica que el code_verifier coincida con el code_challenge según el método especificado
 * (plain o S256).
 */
@UtilityClass
public class PkceVerifier {

  /**
   * Valida que el verifier corresponda al challenge usando el método especificado.
   *
   * @param method "plain" o "S256"
   * @param verifier valor enviado por el cliente
   * @param challenge valor almacenado
   * @return true si coincide, false en caso contrario
   * @throws IllegalArgumentException si el método es desconocido
   */
  public static boolean verify(String method, String verifier, String challenge) {
    if (verifier == null || challenge == null) {
      return false;
    }

    if ("plain".equals(method)) {
      return verifier.equals(challenge);
    } else if ("S256".equals(method)) {
      String verifierHash = hashAndEncode(verifier);
      return verifierHash.equals(challenge);
    } else {
      throw new UnsupportedPkceMethodException(method);
    }
  }

  /**
   * Genera el hash SHA256 base64url de un verifier.
   *
   * <p>Esto se utiliza cuando el cliente envía un code_challenge usando el método S256.
   *
   * @param verifier verifier en texto plano
   * @return hash SHA256 base64url
   */
  public static String hashAndEncode(String verifier) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new HashingUnavailableException("SHA-256", e);
    }
  }
}

