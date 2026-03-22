package io.cmartinezs.keygo.infra.auth.jwks;

import static org.assertj.core.api.Assertions.*;

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

class JwkSetBuilderTest {

  static SigningKey activeKey;
  static SigningKey retiredKey;
  static final JwkSetBuilder builder = new JwkSetBuilder();

  @BeforeAll
  static void generateKeys() throws Exception {
    activeKey = buildKey("kid-active", SigningKeyStatus.ACTIVE);
    retiredKey = buildKey("kid-retired", SigningKeyStatus.RETIRED);
  }

  private static SigningKey buildKey(String kid, SigningKeyStatus status) throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();

    String publicPem = toPem("PUBLIC KEY", kp.getPublic().getEncoded());

    return SigningKey.builder()
        .id(new SigningKeyId(kid))
        .kid(kid)
        .algorithm(SigningKeyAlgorithm.RS256)
        .status(status)
        .publicMaterial(publicPem)
        .privateMaterial(null)
        .activatedAt(Instant.now())
        .build();
  }

  private static String toPem(String label, byte[] encoded) {
    String b64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded);
    return "-----BEGIN " + label + "-----\n" + b64 + "\n-----END " + label + "-----\n";
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenActiveKey_whenBuildJwkSet_thenContainsKeyWithRequiredFields() {
    // Given / When
    Map<String, Object> jwkSet = builder.buildJwkSet(List.of(activeKey));

    // Then
    assertThat(jwkSet).containsKey("keys");
    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwkSet.get("keys");
    assertThat(keys).hasSize(1);

    Map<String, Object> jwk = keys.getFirst();
    assertThat(jwk)
        .containsEntry("kid", "kid-active")
        .containsKey("kty")
        .containsEntry("kty", "RSA")
        .containsKey("n")
        .containsKey("e")
        .containsKey("use");
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenMultiplePublishableKeys_whenBuildJwkSet_thenAllKeysIncluded() {
    // Given / When
    Map<String, Object> jwkSet = builder.buildJwkSet(List.of(activeKey, retiredKey));

    // Then
    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwkSet.get("keys");
    assertThat(keys).hasSize(2);
    assertThat(keys.stream().map(k -> k.get("kid")))
        .containsExactlyInAnyOrder("kid-active", "kid-retired");
  }

  @Test
  void givenEmptyList_whenBuildJwkSet_thenKeysArrayIsEmpty() {
    // Given / When
    Map<String, Object> jwkSet = builder.buildJwkSet(List.of());

    // Then
    assertThat(jwkSet).containsKey("keys");
    List<?> keys = (List<?>) jwkSet.get("keys");
    assertThat(keys).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void givenKeyWithBlankPublicMaterial_whenBuildJwkSet_thenKeyIsSkipped() {
    // Given
    SigningKey badKey =
        SigningKey.builder()
            .id(new SigningKeyId("bad-key"))
            .kid("kid-bad")
            .algorithm(SigningKeyAlgorithm.RS256)
            .status(SigningKeyStatus.ACTIVE)
            .publicMaterial("")
            .privateMaterial(null)
            .activatedAt(Instant.now())
            .build();

    // When
    Map<String, Object> jwkSet = builder.buildJwkSet(List.of(badKey, activeKey));

    // Then: la clave inválida se omite, la activa se incluye
    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwkSet.get("keys");
    assertThat(keys).hasSize(1);
    assertThat(keys.getFirst()).containsEntry("kid", "kid-active");
  }
}
