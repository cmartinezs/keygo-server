package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.cmartinezs.keygo.domain.auth.exception.AuthorizationCodeExpiredException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidAuthorizationCodeException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthorizationCodeTest {

  private static final TenantId TENANT_ID = new TenantId(UUID.randomUUID());
  private static final ClientAppId CLIENT_APP_ID = new ClientAppId(UUID.randomUUID());
  private static final UserId USER_ID = new UserId(UUID.randomUUID());
  private static final ScopeSet SCOPES = ScopeSet.of(Set.of("openid", "profile"));
  private static final CodeChallenge CHALLENGE = CodeChallenge.s256("challenge");

  private AuthorizationCode issuedCode() {
    return AuthorizationCode.issued(
        "auth-code",
        TENANT_ID,
        CLIENT_APP_ID,
        USER_ID,
        "https://example.com/callback",
        SCOPES,
        CHALLENGE,
        Instant.now().plusSeconds(120));
  }

  private String nullString() {
    return null;
  }

  private TenantId nullTenantId() {
    return null;
  }

  private ClientAppId nullClientAppId() {
    return null;
  }

  private UserId nullUserId() {
    return null;
  }

  private ScopeSet nullScopes() {
    return null;
  }

  private CodeChallenge nullChallenge() {
    return null;
  }

  private Instant nullInstant() {
    return null;
  }

  @Test
  void issued_withValidData_createsPendingCode() {
    // Given / When
    AuthorizationCode code = issuedCode();

    // Then
    assertThat(code.getId()).isNotNull();
    assertThat(code.getStatus()).isEqualTo(AuthorizationCodeStatus.PENDING);
    assertThat(code.getCode()).isEqualTo("auth-code");
  }

  @Test
  void issued_withNullCode_throwsException() {
    // Given / When / Then
    Instant expiresAt = Instant.now().plusSeconds(60);
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    nullString(),
                    TENANT_ID,
                    CLIENT_APP_ID,
                    USER_ID,
                    "https://example.com/callback",
                    SCOPES,
                    CHALLENGE,
                    expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Code");
  }

  @Test
  void issued_withNullTenantId_throwsException() {
    // Given / When / Then
    Instant expiresAt = Instant.now().plusSeconds(60);
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    "auth-code",
                    nullTenantId(),
                    CLIENT_APP_ID,
                    USER_ID,
                    "https://example.com/callback",
                    SCOPES,
                    CHALLENGE,
                    expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void issued_withNullClientAppId_throwsException() {
    // Given / When / Then
    Instant expiresAt = Instant.now().plusSeconds(60);
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    "auth-code",
                    TENANT_ID,
                    nullClientAppId(),
                    USER_ID,
                    "https://example.com/callback",
                    SCOPES,
                    CHALLENGE,
                    expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ClientAppId");
  }

  @Test
  void issued_withNullUserId_throwsException() {
    // Given / When / Then
    Instant expiresAt = Instant.now().plusSeconds(60);
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    "auth-code",
                    TENANT_ID,
                    CLIENT_APP_ID,
                    nullUserId(),
                    "https://example.com/callback",
                    SCOPES,
                    CHALLENGE,
                    expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void issued_withBlankRedirectUri_throwsException() {
    // Given / When / Then
    Instant expiresAt = Instant.now().plusSeconds(60);
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    "auth-code",
                    TENANT_ID,
                    CLIENT_APP_ID,
                    USER_ID,
                    "   ",
                    SCOPES,
                    CHALLENGE,
                    expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("RedirectUri");
  }

  @Test
  void issued_withNullScopes_throwsException() {
    // Given / When / Then
    Instant expiresAt = Instant.now().plusSeconds(60);
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    "auth-code",
                    TENANT_ID,
                    CLIENT_APP_ID,
                    USER_ID,
                    "https://example.com/callback",
                    nullScopes(),
                    CHALLENGE,
                    expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Scopes");
  }

  @Test
  void issued_withNullCodeChallenge_throwsException() {
    // Given / When / Then
    Instant expiresAt = Instant.now().plusSeconds(60);
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    "auth-code",
                    TENANT_ID,
                    CLIENT_APP_ID,
                    USER_ID,
                    "https://example.com/callback",
                    SCOPES,
                    nullChallenge(),
                    expiresAt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CodeChallenge");
  }

  @Test
  void issued_withNullExpiresAt_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () ->
                AuthorizationCode.issued(
                    "auth-code",
                    TENANT_ID,
                    CLIENT_APP_ID,
                    USER_ID,
                    "https://example.com/callback",
                    SCOPES,
                    CHALLENGE,
                    nullInstant()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ExpiresAt");
  }

  @Test
  void isNotExpired_withFutureExpiration_returnsTrue() {
    // Given
    AuthorizationCode code = issuedCode();

    // When / Then
    assertThat(code.isNotExpired()).isTrue();
  }

  @Test
  void isNotExpired_withPastExpiration_returnsFalse() {
    // Given
    AuthorizationCode code =
        AuthorizationCode.issued(
            "auth-code",
            TENANT_ID,
            CLIENT_APP_ID,
            USER_ID,
            "https://example.com/callback",
            SCOPES,
            CHALLENGE,
            Instant.now().minusSeconds(1));

    // When / Then
    assertThat(code.isNotExpired()).isFalse();
  }

  @Test
  void markAsUsed_fromPending_setsUsedStatus() {
    // Given
    AuthorizationCode code = issuedCode();
    Instant usedAt = Instant.now();

    // When
    code.markAsUsed(usedAt);

    // Then
    assertThat(code.getStatus()).isEqualTo(AuthorizationCodeStatus.USED);
    assertThat(code.getUsedAt()).isEqualTo(usedAt);
  }

  @Test
  void markAsUsed_whenAlreadyUsed_throwsException() {
    // Given
    AuthorizationCode code = issuedCode();
    Instant firstUsedAt = Instant.now();
    Instant secondUsedAt = firstUsedAt.plusSeconds(1);
    code.markAsUsed(firstUsedAt);

    // When / Then
    assertThatThrownBy(() -> code.markAsUsed(secondUsedAt))
        .isInstanceOf(InvalidAuthorizationCodeException.class)
        .hasMessageContaining("already used");
  }

  @Test
  void markAsUsed_whenRevoked_throwsException() {
    // Given
    AuthorizationCode code = issuedCode();
    Instant usedAt = Instant.now();
    code.revoke();

    // When / Then
    assertThatThrownBy(() -> code.markAsUsed(usedAt))
        .isInstanceOf(InvalidAuthorizationCodeException.class)
        .hasMessageContaining("revoked");
  }

  @Test
  void markAsUsed_whenExpired_throwsException() {
    // Given
    AuthorizationCode code = issuedCode();
    Instant usedAt = Instant.now();
    code.markAsExpired();

    // When / Then
    assertThatThrownBy(() -> code.markAsUsed(usedAt))
        .isInstanceOf(AuthorizationCodeExpiredException.class)
        .hasMessageContaining("expired");
  }

  @Test
  void revoke_fromPending_setsRevokedStatus() {
    // Given
    AuthorizationCode code = issuedCode();

    // When
    code.revoke();

    // Then
    assertThat(code.getStatus()).isEqualTo(AuthorizationCodeStatus.REVOKED);
  }

  @Test
  void revoke_whenAlreadyRevoked_throwsException() {
    // Given
    AuthorizationCode code = issuedCode();
    code.revoke();

    // When / Then
    assertThatThrownBy(code::revoke)
        .isInstanceOf(InvalidAuthorizationCodeException.class)
        .hasMessageContaining("already revoked");
  }

  @Test
  void markAsExpired_fromPending_setsExpiredStatus() {
    // Given
    AuthorizationCode code = issuedCode();

    // When
    code.markAsExpired();

    // Then
    assertThat(code.getStatus()).isEqualTo(AuthorizationCodeStatus.EXPIRED);
  }

  @Test
  void markAsExpired_whenUsed_throwsException() {
    // Given
    AuthorizationCode code = issuedCode();
    code.markAsUsed(Instant.now());

    // When / Then
    assertThatThrownBy(code::markAsExpired)
        .isInstanceOf(InvalidAuthorizationCodeException.class)
        .hasMessageContaining("already used");
  }

  @Test
  void toString_doesNotExposeRawCode() {
    // Given
    AuthorizationCode code = issuedCode();

    // When
    String printed = code.toString();

    // Then
    assertThat(printed).contains("AuthorizationCode{").doesNotContain("auth-code");
  }
}


