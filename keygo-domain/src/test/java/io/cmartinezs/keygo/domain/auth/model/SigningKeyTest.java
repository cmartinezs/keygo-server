package io.cmartinezs.keygo.domain.auth.model;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SigningKeyTest {

  private SigningKey buildKey(SigningKeyStatus status) {
    return SigningKey.builder()
        .id(new SigningKeyId("test-id"))
        .kid("test-kid")
        .algorithm(SigningKeyAlgorithm.RS256)
        .status(status)
        .publicMaterial("PUBLIC_PEM")
        .privateMaterial("PRIVATE_PEM")
        .activatedAt(Instant.now())
        .build();
  }

  @Test
  void givenActiveStatus_whenIsActive_thenTrue() {
    // Given
    SigningKey key = buildKey(SigningKeyStatus.ACTIVE);
    // When / Then
    assertThat(key.isActive()).isTrue();
  }

  @Test
  void givenRetiredStatus_whenIsActive_thenFalse() {
    // Given
    SigningKey key = buildKey(SigningKeyStatus.RETIRED);
    // When / Then
    assertThat(key.isActive()).isFalse();
  }

  @Test
  void givenActiveStatus_whenIsPublishable_thenTrue() {
    // Given
    SigningKey key = buildKey(SigningKeyStatus.ACTIVE);
    // When / Then
    assertThat(key.isPublishable()).isTrue();
  }

  @Test
  void givenRetiredStatus_whenIsPublishable_thenTrue() {
    // Given
    SigningKey key = buildKey(SigningKeyStatus.RETIRED);
    // When / Then
    assertThat(key.isPublishable()).isTrue();
  }

  @Test
  void givenRevokedStatus_whenIsPublishable_thenFalse() {
    // Given
    SigningKey key = buildKey(SigningKeyStatus.REVOKED);
    // When / Then
    assertThat(key.isPublishable()).isFalse();
  }

  @Test
  void givenRevokedStatus_whenIsActive_thenFalse() {
    // Given
    SigningKey key = buildKey(SigningKeyStatus.REVOKED);
    // When / Then
    assertThat(key.isActive()).isFalse();
  }
}

