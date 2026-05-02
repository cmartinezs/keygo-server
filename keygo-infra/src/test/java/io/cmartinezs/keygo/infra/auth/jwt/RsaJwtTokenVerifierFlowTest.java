package io.cmartinezs.keygo.infra.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;

import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RsaJwtTokenVerifierFlowTest {

  static SigningKey signingKey;
  static RsaJwtTokenVerifier verifier = new RsaJwtTokenVerifier();
  static String validToken;

  @BeforeAll
  static void setUp() throws Exception {
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

    validToken = createSignedToken(kp, privatePem);
  }

  private static String toPem(String label, byte[] encoded) {
    String b64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded);
    return "-----BEGIN " + label + "-----\n" + b64 + "\n-----END " + label + "-----\n";
  }

  private static String createSignedToken(KeyPair kp, String privatePem) throws Exception {
    RsaJwtTokenSigner signer = new RsaJwtTokenSigner();
    Map<String, Object> claims =
        Map.of(
            "iss", "http://localhost",
            "sub", "user-1",
            "iat", Instant.now().getEpochSecond(),
            "exp", Instant.now().plusSeconds(3600).getEpochSecond());
    return signer.signJwt(claims, signingKey);
  }

  @Test
  void verify_withValidToken_shouldReturnClaims() {
    Map<String, Object> claims = verifier.verify(validToken, List.of(signingKey));

    assertNotNull(claims);
    assertEquals("user-1", claims.get("sub"));
  }

  @Test
  void verify_withExpiredToken_shouldThrowException() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();

    String privatePem = toPem("PRIVATE KEY", kp.getPrivate().getEncoded());

    Map<String, Object> expiredClaims =
        Map.of(
            "iss", "http://localhost",
            "sub", "user-expired",
            "iat", Instant.now().minusSeconds(7200).getEpochSecond(),
            "exp", Instant.now().minusSeconds(3600).getEpochSecond());

    RsaJwtTokenSigner signer = new RsaJwtTokenSigner();
    String expiredToken = signer.signJwt(expiredClaims, signingKey);

    assertThrows(InvalidRefreshTokenException.class, () -> verifier.verify(expiredToken, List.of(signingKey)));
  }

  @Test
  void verify_withInvalidSignature_shouldThrowException() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();

    String publicPem = toPem("PUBLIC KEY", kp.getPublic().getEncoded());

    SigningKey otherKey =
        SigningKey.builder()
            .id(new SigningKeyId("other-key"))
            .kid("other-kid")
            .algorithm(SigningKeyAlgorithm.RS256)
            .status(SigningKeyStatus.ACTIVE)
            .publicMaterial(publicPem)
            .privateMaterial(toPem("PRIVATE KEY", kp.getPrivate().getEncoded()))
            .activatedAt(Instant.now())
            .build();

    assertThrows(InvalidRefreshTokenException.class, () -> verifier.verify(validToken, List.of(otherKey)));
  }
}