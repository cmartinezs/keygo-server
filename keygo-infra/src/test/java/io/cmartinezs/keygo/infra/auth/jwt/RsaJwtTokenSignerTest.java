package io.cmartinezs.keygo.infra.auth.jwt;

import static org.assertj.core.api.Assertions.*;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RsaJwtTokenSignerTest {

  static SigningKey signingKey;
  static RsaJwtTokenSigner signer = new RsaJwtTokenSigner();

  @BeforeAll
  static void generateKey() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();

    String publicPem = toPem("PUBLIC KEY", kp.getPublic().getEncoded());
    String privatePem = toPem("PRIVATE KEY", kp.getPrivate().getEncoded());

    signingKey =
        SigningKey.builder()
            .id(new SigningKeyId("test-key-id"))
            .kid("test-kid")
            .algorithm(SigningKeyAlgorithm.RS256)
            .status(SigningKeyStatus.ACTIVE)
            .publicMaterial(publicPem)
            .privateMaterial(privatePem)
            .activatedAt(Instant.now())
            .build();
  }

  private static String toPem(String label, byte[] encoded) {
    String b64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded);
    return "-----BEGIN " + label + "-----\n" + b64 + "\n-----END " + label + "-----\n";
  }

  @Test
  void givenValidClaims_whenSignJwt_thenProducesThreePartToken() {
    // Given
    Map<String, Object> claims =
        Map.of(
            "iss", "http://localhost",
            "sub", "user-1",
            "aud", "client-1",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond(),
            "jti", "jti-1");

    // When
    String jwt = signer.signJwt(claims, signingKey);

    // Then
    assertThat(jwt).isNotBlank();
    String[] parts = jwt.split("\\.");
    assertThat(parts).hasSize(3);
  }

  @Test
  void givenValidClaims_whenSignJwt_thenHeaderContainsKidAndAlgorithm() {
    // Given
    Map<String, Object> claims =
        Map.of(
            "iss", "http://localhost",
            "sub", "user-1",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond(),
            "jti", "jti-2");

    // When
    String jwt = signer.signJwt(claims, signingKey);

    // Then: decodificar header
    String headerJson = new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[0]));
    assertThat(headerJson)
        .contains("\"kid\"")
        .contains("test-kid")
        .contains("\"alg\"")
        .contains("RS256");
  }
}
