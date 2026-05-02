package io.cmartinezs.keygo.infra.auth.security;

import static org.junit.jupiter.api.Assertions.*;

import io.cmartinezs.keygo.app.auth.exception.UnsupportedPkceMethodException;
import org.junit.jupiter.api.Test;

class PkceVerifierTest {

  @Test
  void verify_plainMethod_shouldReturnTrueWhenVerifierMatchesChallenge() {
    assertTrue(PkceVerifier.verify("plain", "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk", "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
  }

  @Test
  void verify_plainMethod_shouldReturnFalseWhenVerifierDoesNotMatchChallenge() {
    assertFalse(PkceVerifier.verify("plain", "verifier", "different-challenge"));
  }

  @Test
  void verify_plainMethod_shouldReturnFalseWhenVerifierIsNull() {
    assertFalse(PkceVerifier.verify("plain", null, "challenge"));
  }

  @Test
  void verify_plainMethod_shouldReturnFalseWhenChallengeIsNull() {
    assertFalse(PkceVerifier.verify("plain", "verifier", null));
  }

  @Test
  void verify_S256Method_shouldReturnTrueWhenHashMatches() {
    String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
    String expectedChallenge = PkceVerifier.hashAndEncode(verifier);

    assertTrue(PkceVerifier.verify("S256", verifier, expectedChallenge));
  }

  @Test
  void verify_S256Method_shouldReturnFalseWhenHashDoesNotMatch() {
    assertFalse(PkceVerifier.verify("S256", "verifier", "invalid-challenge"));
  }

  @Test
  void verify_S256Method_shouldReturnFalseWhenVerifierIsNull() {
    assertFalse(PkceVerifier.verify("S256", null, "challenge"));
  }

  @Test
  void verify_S256Method_shouldReturnFalseWhenChallengeIsNull() {
    assertFalse(PkceVerifier.verify("S256", "verifier", null));
  }

  @Test
  void verify_unknownMethod_shouldThrowUnsupportedPkceMethodException() {
    assertThrows(UnsupportedPkceMethodException.class, () ->
        PkceVerifier.verify("unknown", "verifier", "challenge")
    );
  }

  @Test
  void hashAndEncode_shouldReturnBase64UrlEncodedSha256() {
    String verifier = "test-verifier";
    String result = PkceVerifier.hashAndEncode(verifier);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertFalse(result.contains("+"));
    assertFalse(result.contains("/"));
    assertFalse(result.contains("="));
  }

  @Test
  void hashAndEncode_shouldBeDeterministic() {
    String verifier = "test-verifier";
    String result1 = PkceVerifier.hashAndEncode(verifier);
    String result2 = PkceVerifier.hashAndEncode(verifier);

    assertEquals(result1, result2);
  }
}