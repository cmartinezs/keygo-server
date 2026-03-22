package io.cmartinezs.keygo.infra.auth.jwt;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StandardTokenClaimsFactoryTest {

  private final StandardTokenClaimsFactory factory = new StandardTokenClaimsFactory();

  @Test
  void givenValidInputs_whenBuildAccessTokenClaims_thenContainsRequiredClaims() {
    // Given
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(3600);

    // When
    Map<String, Object> claims =
        factory.buildAccessTokenClaims(
            "http://issuer", "user-1", "client-1", "openid profile", "jti-abc", now, exp);

    // Then
    assertThat(claims)
        .containsKey("iss")
        .containsKey("sub")
        .containsKey("aud")
        .containsKey("scope")
        .containsKey("jti")
        .containsKey("iat")
        .containsKey("exp")
        .containsEntry("scope", "openid profile");
  }

  @Test
  void givenAccessToken_whenBuildIdTokenClaims_thenAtHashIsPresent() {
    // Given
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(3600);

    // When
    Map<String, Object> claims =
        factory.buildIdTokenClaims(
            "http://issuer",
            "user-1",
            "client-1",
            "jti-id",
            now,
            exp,
            null,
            "user@example.com",
            "John Doe",
            "access.token.value");

    // Then
    assertThat(claims).containsKey("at_hash");
    assertThat(claims.get("at_hash")).isNotNull().isInstanceOf(String.class);
    assertThat((String) claims.get("at_hash")).isNotBlank();
  }

  @Test
  void givenNonce_whenBuildIdTokenClaims_thenNonceIncluded() {
    // Given
    Instant now = Instant.now();

    // When
    Map<String, Object> claims =
        factory.buildIdTokenClaims(
            "http://issuer",
            "user-1",
            "client-1",
            "jti-id",
            now,
            now.plusSeconds(3600),
            "my-nonce",
            null,
            null,
            "token");

    // Then
    assertThat(claims).containsEntry("nonce", "my-nonce");
  }

  @Test
  void givenNullNonce_whenBuildIdTokenClaims_thenNonceAbsent() {
    // Given / When
    Map<String, Object> claims =
        factory.buildIdTokenClaims(
            "http://issuer",
            "user-1",
            "client-1",
            "jti-id",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            null,
            null,
            null,
            "token");

    // Then
    assertThat(claims).doesNotContainKey("nonce");
  }
}
