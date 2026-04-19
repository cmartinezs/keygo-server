package io.cmartinezs.keygo.infra.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;

class RsaJwtTokenVerifierTest {

  private static final String TEST_PEM = """
      -----BEGIN PUBLIC KEY-----
      MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw7xLtXNsN4G8M2/VFWcV1
      Qd2kJ3rW6z1T3j5M5L8qCj6d7m9n2o5p4q1r2s3t4u5v6w7x8y9z0A1B2C3D4E5F6
      G7H8I9J0K1L2M3N4O5P6Q7R8S9T0U1V2W3X4Y5Z6a7b8c9d0e1f2g3h4i5j6k7l8m9n
      0o1p2q3r4s5t6u7v8w9x0y1z2A3B4C5D6E7F8G9H0I1J2K3L4M5N6O7P8Q9R0S1T2U3
      V4W5X6Y7Z8a9b0c1d2e3f4g5h6i7j8k9l0m1n2o3p4q5r6s7t8u9v0w1x2y3z4A5B6C
      7D8E9F0G1H2I3J4K5L6M7N8O9P0Q1R2S3T4U5V6W7X8Y9Z0a1b2c3d4e5f6g7h8i9j0
      k1l2m
      -----END PUBLIC KEY-----
      """;

  @Test
  void getRsaPublicKey_shouldThrowWhenInvalidPem() {
    assertThrows(IllegalArgumentException.class, () ->
        RsaJwtTokenVerifier.getRsaPublicKey("invalid-pem-data")
    );
  }

  @Test
  void getRsaPublicKey_shouldStripPemHeaders() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair keyPair = gen.generateKeyPair();

    byte[] encoded = keyPair.getPublic().getEncoded();
    String encodedBase64 = java.util.Base64.getEncoder().encodeToString(encoded);
    String pem = "-----BEGIN PUBLIC KEY-----\n" + encodedBase64 + "\n-----END PUBLIC KEY-----";

    RSAPublicKey publicKey = RsaJwtTokenVerifier.getRsaPublicKey(pem);

    assertNotNull(publicKey);
  }
}