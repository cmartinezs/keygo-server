package io.cmartinezs.keygo.infra.auth.jwt;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link AccessTokenVerifierPort} usando Nimbus JOSE+JWT con RSA.
 *
 * <p>Verifica la firma RSA de un access_token JWT contra las claves públicas disponibles (ACTIVE +
 * RETIRED). Valida también la expiración del token.
 */
public class RsaJwtTokenVerifier implements AccessTokenVerifierPort {

  /** {@inheritDoc} */
  @Override
  public Map<String, Object> verify(String accessToken, List<SigningKey> publicKeys) {
    SignedJWT signedJWT;
    try {
      signedJWT = SignedJWT.parse(accessToken);
    } catch (ParseException e) {
      throw new InvalidRefreshTokenException("Cannot parse access token: " + e.getMessage(), e);
    }

    // Determinar kid del header para seleccionar la clave correcta
    String kid = signedJWT.getHeader().getKeyID();

    boolean verified = false;
    for (SigningKey signingKey : publicKeys) {
      // Si el token tiene kid, usar solo la clave correspondiente
      if (kid != null && !kid.equals(signingKey.getKid())) {
        continue;
      }
      try {
        RSAPublicKey publicKey = loadPublicKey(signingKey.getPublicMaterial());
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        if (signedJWT.verify(verifier)) {
          verified = true;
          break;
        }
      } catch (Exception e) {
        // Intentar con la siguiente clave
      }
    }

    if (!verified) {
      throw new InvalidRefreshTokenException("Access token signature verification failed");
    }

    // Validar expiración
    try {
      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expiration != null && expiration.before(new Date())) {
        throw new InvalidRefreshTokenException("Access token has expired");
      }
    } catch (ParseException e) {
      throw new InvalidRefreshTokenException(
          "Cannot read access token claims: " + e.getMessage(), e);
    }

    // Extraer y retornar claims como mapa
    try {
      var claimsSet = signedJWT.getJWTClaimsSet();
      Map<String, Object> claims = new HashMap<>(claimsSet.getClaims());
      // Normalizar: convertir Date → epoch seconds para consistencia
      if (claimsSet.getExpirationTime() != null) {
        claims.put("exp", claimsSet.getExpirationTime().toInstant().getEpochSecond());
      }
      if (claimsSet.getIssueTime() != null) {
        claims.put("iat", claimsSet.getIssueTime().toInstant().getEpochSecond());
      }
      return claims;
    } catch (ParseException e) {
      throw new InvalidRefreshTokenException("Cannot extract claims from access token", e);
    }
  }

  /** Carga una clave pública RSA desde PEM (X.509/SubjectPublicKeyInfo). */
  private RSAPublicKey loadPublicKey(String pem)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
      return getRsaPublicKey(pem);
  }

    public static RSAPublicKey getRsaPublicKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String stripped =
            pem.replaceAll("-----BEGIN.*?-----", "")
                .replaceAll("-----END.*?-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(stripped);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }
}
