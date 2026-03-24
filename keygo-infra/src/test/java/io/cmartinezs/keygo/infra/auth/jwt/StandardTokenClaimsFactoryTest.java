package io.cmartinezs.keygo.infra.auth.jwt;

import java.time.Instant;
import java.util.List;
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
            "http://issuer", "user-1", "client-1", "openid profile", "jti-abc", now, exp, null);

    // Then
    assertThat(claims)
        .containsKey("iss")
        .containsKey("sub")
        .containsKey("aud")
        .containsKey("scope")
        .containsKey("jti")
        .containsKey("iat")
        .containsKey("exp")
        .containsEntry("scope", "openid profile")
        .doesNotContainKey("roles");
  }

  @Test
  void givenRoles_whenBuildAccessTokenClaims_thenRolesClaimPresent() {
    // Given
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(3600);

    // When
    Map<String, Object> claims =
        factory.buildAccessTokenClaims(
            "http://issuer", "user-1", "client-1", "openid", "jti-xyz", now, exp,
            List.of("ADMIN", "USER"));

    // Then
    assertThat(claims).containsKey("roles");
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.get("roles");
    assertThat(roles).containsExactly("ADMIN", "USER");
  }

  @Test
  void givenEmptyRoles_whenBuildAccessTokenClaims_thenRolesClaimAbsent() {
    // Given / When
    Map<String, Object> claims =
        factory.buildAccessTokenClaims(
            "http://issuer", "user-1", "client-1", "openid", "jti-xyz",
            Instant.now(), Instant.now().plusSeconds(3600), List.of());

    // Then
    assertThat(claims).doesNotContainKey("roles");
  }

  @Test
  void givenAccessToken_whenBuildIdTokenClaims_thenAtHashIsPresent() {
    // Given
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(3600);

    // When
    Map<String, Object> claims =
        factory.buildIdTokenClaims(
            "http://issuer", "user-1", "client-1", "jti-id",
            now, exp, null, "user@example.com", "John Doe", "access.token.value", null);

    // Then
    assertThat(claims).containsKey("at_hash");
    assertThat(claims.get("at_hash")).isNotNull().isInstanceOf(String.class);
    assertThat((String) claims.get("at_hash")).isNotBlank();
    assertThat(claims).doesNotContainKey("roles");
  }

  @Test
  void givenRolesInIdToken_whenBuildIdTokenClaims_thenRolesPresent() {
    // Given
    Instant now = Instant.now();

    // When
    Map<String, Object> claims =
        factory.buildIdTokenClaims(
            "http://issuer", "user-1", "client-1", "jti-id",
            now, now.plusSeconds(3600), null, "u@e.com", "User",
            "access.token", List.of("EDITOR"));

    // Then
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.get("roles");
    assertThat(roles).containsExactly("EDITOR");
  }

  @Test
  void givenNonce_whenBuildIdTokenClaims_thenNonceIncluded() {
    // Given
    Instant now = Instant.now();

    // When
    Map<String, Object> claims =
        factory.buildIdTokenClaims(
            "http://issuer", "user-1", "client-1", "jti-id",
            now, now.plusSeconds(3600), "my-nonce", null, null, "token", null);

    // Then
    assertThat(claims).containsEntry("nonce", "my-nonce");
  }

  @Test
  void givenNullNonce_whenBuildIdTokenClaims_thenNonceAbsent() {
    // Given / When
    Map<String, Object> claims =
        factory.buildIdTokenClaims(
            "http://issuer", "user-1", "client-1", "jti-id",
            Instant.now(), Instant.now().plusSeconds(3600),
            null, null, null, "token", null);

    // Then
    assertThat(claims).doesNotContainKey("nonce");
  }
}
