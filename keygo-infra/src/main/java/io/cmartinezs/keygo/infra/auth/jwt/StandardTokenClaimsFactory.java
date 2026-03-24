package io.cmartinezs.keygo.infra.auth.jwt;

import io.cmartinezs.keygo.app.auth.port.TokenClaimsFactoryPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación estándar de {@link TokenClaimsFactoryPort}.
 *
 * <p>Construye los claim maps para access_token (RFC 9068) e id_token (OIDC Core 1.0).
 * El {@code at_hash} del id_token se calcula como los primeros 16 bytes del SHA-256
 * del access_token, codificado en base64url sin padding (según OIDC Core §3.3.2.11).
 */
public class StandardTokenClaimsFactory implements TokenClaimsFactoryPort {

  @Override
  public Map<String, Object> buildAccessTokenClaims(
      String issuer,
      String subject,
      String audience,
      String scope,
      String jti,
      Instant issuedAt,
      Instant expiresAt,
      List<String> roles) {

    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("iss", issuer);
    claims.put("sub", subject);
    claims.put("aud", audience);
    claims.put("scope", scope);
    claims.put("jti", jti);
    claims.put("iat", issuedAt.getEpochSecond());
    claims.put("exp", expiresAt.getEpochSecond());
    if (roles != null && !roles.isEmpty()) {
      claims.put("roles", roles);
    }
    return claims;
  }

  @Override
  public Map<String, Object> buildIdTokenClaims(
      String issuer,
      String subject,
      String audience,
      String jti,
      Instant issuedAt,
      Instant expiresAt,
      String nonce,
      String email,
      String name,
      String accessToken,
      List<String> roles) {

    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("iss", issuer);
    claims.put("sub", subject);
    claims.put("aud", audience);
    claims.put("jti", jti);
    claims.put("iat", issuedAt.getEpochSecond());
    claims.put("exp", expiresAt.getEpochSecond());

    // at_hash: SHA-256 del access_token → primeros 16 bytes → base64url sin padding
    if (accessToken != null) {
      claims.put("at_hash", computeAtHash(accessToken));
    }
    if (nonce != null && !nonce.isBlank()) {
      claims.put("nonce", nonce);
    }
    if (email != null && !email.isBlank()) {
      claims.put("email", email);
    }
    if (name != null && !name.isBlank()) {
      claims.put("name", name);
    }
    if (roles != null && !roles.isEmpty()) {
      claims.put("roles", roles);
    }
    return claims;
  }

  /**
   * Calcula el {@code at_hash} del access_token según OIDC Core §3.3.2.11.
   *
   * <p>SHA-256(accessToken) → primeros 16 bytes → base64url sin padding.
   */
  private String computeAtHash(String accessToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(accessToken.getBytes(StandardCharsets.US_ASCII));
      byte[] leftHalf = new byte[16];
      System.arraycopy(hash, 0, leftHalf, 0, 16);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(leftHalf);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
