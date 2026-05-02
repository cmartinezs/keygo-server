package io.cmartinezs.keygo.infra.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StandardTokenClaimsFactoryTest {

  private final StandardTokenClaimsFactory factory = new StandardTokenClaimsFactory();

  @Test
  void buildAccessTokenClaims_withRoles_shouldIncludeRolesClaim() {
    Map<String, Object> claims = factory.buildAccessTokenClaims(
        "http://localhost",
        "user-1",
        "client-1",
        "openid profile",
        "jti-1",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        List.of("ROLE_ADMIN", "ROLE_USER")
    );

    assertEquals("http://localhost", claims.get("iss"));
    assertEquals("user-1", claims.get("sub"));
    assertEquals("client-1", claims.get("aud"));
    assertEquals("openid profile", claims.get("scope"));
    assertEquals("jti-1", claims.get("jti"));
    assertNotNull(claims.get("iat"));
    assertNotNull(claims.get("exp"));
    assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), claims.get("roles"));
  }

  @Test
  void buildAccessTokenClaims_withoutRoles_shouldNotIncludeRolesClaim() {
    Map<String, Object> claims = factory.buildAccessTokenClaims(
        "http://localhost",
        "user-1",
        "client-1",
        "openid",
        "jti-1",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        null
    );

    assertNull(claims.get("roles"));
  }

  @Test
  void buildAccessTokenClaims_withEmptyRoles_shouldNotIncludeRolesClaim() {
    Map<String, Object> claims = factory.buildAccessTokenClaims(
        "http://localhost",
        "user-1",
        "client-1",
        "openid",
        "jti-1",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        List.of()
    );

    assertNull(claims.get("roles"));
  }

  @Test
  void buildIdTokenClaims_withAllOptionalFields_shouldIncludeAll() {
    Map<String, Object> claims = factory.buildIdTokenClaims(
        "http://localhost",
        "user-1",
        "client-1",
        "jti-1",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        "nonce-value",
        "user@test.com",
        "User Name",
        "access-token-value",
        List.of("ROLE_ADMIN")
    );

    assertEquals("http://localhost", claims.get("iss"));
    assertEquals("user-1", claims.get("sub"));
    assertEquals("client-1", claims.get("aud"));
    assertEquals("jti-1", claims.get("jti"));
    assertNotNull(claims.get("iat"));
    assertNotNull(claims.get("exp"));
    assertNotNull(claims.get("at_hash"));
    assertEquals("nonce-value", claims.get("nonce"));
    assertEquals("user@test.com", claims.get("email"));
    assertEquals("User Name", claims.get("name"));
    assertEquals(List.of("ROLE_ADMIN"), claims.get("roles"));
  }

  @Test
  void buildIdTokenClaims_withoutOptionalFields_shouldExcludeThem() {
    Map<String, Object> claims = factory.buildIdTokenClaims(
        "http://localhost",
        "user-1",
        "client-1",
        "jti-1",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        null,
        null,
        null,
        "access-token",
        null
    );

    assertNull(claims.get("nonce"));
    assertNull(claims.get("email"));
    assertNull(claims.get("name"));
    assertNull(claims.get("roles"));
    assertNotNull(claims.get("at_hash"));
  }

  @Test
  void buildIdTokenClaims_withBlankOptionalFields_shouldExcludeThem() {
    Map<String, Object> claims = factory.buildIdTokenClaims(
        "http://localhost",
        "user-1",
        "client-1",
        "jti-1",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        "  ",
        "  ",
        "  ",
        "access-token",
        List.of()
    );

    assertNull(claims.get("nonce"));
    assertNull(claims.get("email"));
    assertNull(claims.get("name"));
    assertNull(claims.get("roles"));
  }

  @Test
  void buildIdTokenClaims_withNullAccessToken_shouldNotComputeAtHash() {
    Map<String, Object> claims = factory.buildIdTokenClaims(
        "http://localhost",
        "user-1",
        "client-1",
        "jti-1",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        null,
        "email@test.com",
        "Name",
        null,
        null
    );

    assertNull(claims.get("at_hash"));
  }
}