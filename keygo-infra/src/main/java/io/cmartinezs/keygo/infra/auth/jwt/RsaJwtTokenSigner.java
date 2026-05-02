package io.cmartinezs.keygo.infra.auth.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.cmartinezs.keygo.app.auth.exception.JwtSigningException;
import io.cmartinezs.keygo.app.auth.port.TokenSignerPort;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Implementación de {@link TokenSignerPort} usando Nimbus JOSE+JWT con RSA.
 *
 * <p>Firma tokens JWT con la clave privada RSA almacenada en {@link SigningKey}.
 * El header incluye el {@code kid} para que los consumidores puedan encontrar la clave
 * pública correcta en el JWKS endpoint.
 */
public class RsaJwtTokenSigner implements TokenSignerPort {

  /** {@inheritDoc} */
  @Override
  public String signJwt(Map<String, Object> claims, SigningKey signingKey) {
    try {
      RSAPrivateKey privateKey = loadPrivateKey(signingKey.getPrivateMaterial());
      JWSSigner signer = new RSASSASigner(privateKey);

      JWSAlgorithm algorithm = resolveAlgorithm(signingKey.getAlgorithm());
      JWSHeader header = new JWSHeader.Builder(algorithm)
          .keyID(signingKey.getKid())
          .build();

      JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
      for (Map.Entry<String, Object> entry : claims.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        switch (key) {
          case "iss" -> claimsBuilder.issuer((String) value);
          case "sub" -> claimsBuilder.subject((String) value);
          case "jti" -> claimsBuilder.jwtID((String) value);
          case "iat" -> claimsBuilder.issueTime(new Date((Long) value * 1000L));
          case "exp" -> claimsBuilder.expirationTime(new Date((Long) value * 1000L));
          case "aud" -> claimsBuilder.audience((String) value);
          default -> claimsBuilder.claim(key, value);
        }
      }

      SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
      signedJWT.sign(signer);
      return signedJWT.serialize();

    } catch (Exception e) {
      throw new JwtSigningException(e.getMessage(), e);
    }
  }

  /** Carga una clave privada RSA desde un string PEM (PKCS#8 o PKCS#1 wrapeado). */
  private RSAPrivateKey loadPrivateKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
    String stripped = pem
        .replaceAll("-----BEGIN.*?-----", "")
        .replaceAll("-----END.*?-----", "")
        .replaceAll("\\s", "");
    byte[] decoded = Base64.getDecoder().decode(stripped);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) kf.generatePrivate(spec);
  }

  private JWSAlgorithm resolveAlgorithm(SigningKeyAlgorithm algorithm) {
    return switch (algorithm) {
      case RS384 -> JWSAlgorithm.RS384;
      case RS512 -> JWSAlgorithm.RS512;
      default -> JWSAlgorithm.RS256;
    };
  }
}

