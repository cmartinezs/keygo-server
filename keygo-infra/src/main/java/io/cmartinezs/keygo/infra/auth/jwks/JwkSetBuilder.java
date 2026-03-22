package io.cmartinezs.keygo.infra.auth.jwks;

import com.nimbusds.jose.jwk.RSAKey;
import io.cmartinezs.keygo.app.auth.port.JwksBuilderPort;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link JwksBuilderPort} usando Nimbus JOSE+JWT.
 *
 * <p>Convierte claves de dominio al formato JWK Set (RFC 7517). Solo se incluyen claves
 * con material público válido. Las claves con material inválido se omiten sin fallar todo el set.
 */
public class JwkSetBuilder implements JwksBuilderPort {

  @Override
  public Map<String, Object> buildJwkSet(List<SigningKey> signingKeys) {
    List<Map<String, Object>> jwkList = new ArrayList<>();

    for (SigningKey key : signingKeys) {
      if (key.getPublicMaterial() == null || key.getPublicMaterial().isBlank()) {
        continue;
      }
      try {
        RSAPublicKey publicKey = loadPublicKey(key.getPublicMaterial());
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .keyID(key.getKid())
            .algorithm(resolveAlgorithm(key))
            .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
            .build();
        jwkList.add(new LinkedHashMap<>(rsaKey.toJSONObject()));
      } catch (Exception ignored) {
        // Clave con material inválido: omitir sin fallar todo el set
      }
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("keys", jwkList);
    return result;
  }

  private static RSAPublicKey loadPublicKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
    String stripped = pem
        .replaceAll("-----BEGIN.*?-----", "")
        .replaceAll("-----END.*?-----", "")
        .replaceAll("\\s", "");
    byte[] decoded = Base64.getDecoder().decode(stripped);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) kf.generatePublic(spec);
  }

  private static com.nimbusds.jose.Algorithm resolveAlgorithm(SigningKey key) {
    return switch (key.getAlgorithm()) {
      case RS384 -> com.nimbusds.jose.JWSAlgorithm.RS384;
      case RS512 -> com.nimbusds.jose.JWSAlgorithm.RS512;
      default -> com.nimbusds.jose.JWSAlgorithm.RS256;
    };
  }
}
