package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.*;

import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  private static final ClientAppId CLIENT_APP_ID = new ClientAppId(UUID.randomUUID());
  private static final UUID TENANT_USER_ID = UUID.randomUUID();
  private static final SessionId SESSION_ID = SessionId.generate();
  private static final Instant NOW = Instant.now();
  private static final Instant EXPIRES_FUTURE = NOW.plusSeconds(86400);
  private static final Instant EXPIRES_PAST = NOW.minusSeconds(1);

  private RefreshToken activeToken() {
    return RefreshToken.issue(
        "somehash",
        CLIENT_APP_ID,
        TENANT_USER_ID,
        SESSION_ID,
        "openid profile",
        EXPIRES_FUTURE,
        NOW,
        null);
  }

  private String nullString() {
    return null;
  }

  private SessionId nullSessionId() {
    return null;
  }

  private Instant nullInstant() {
    return null;
  }

  @Test
  void issue_createsTokenInActiveStatus() {
    // Given / When
    RefreshToken token = activeToken();

    // Then
    assertThat(token.getId()).isNotNull();
    assertThat(token.getStatus()).isEqualTo(RefreshTokenStatus.ACTIVE);
    assertThat(token.getTokenHash()).isEqualTo("somehash");
    assertThat(token.getScopes()).isEqualTo("openid profile");
    assertThat(token.isValid()).isTrue();
    assertThat(token.isExpired()).isFalse();
    assertThat(token.getClientAppId()).isEqualTo(CLIENT_APP_ID);
    assertThat(token.getTenantUserId()).isEqualTo(TENANT_USER_ID);
    assertThat(token.isPlatformToken()).isFalse();
  }

  @Test
  void issue_withNullClientAppId_createsPlatformToken() {
    // Given / When — null clientAppId = platform session RT
    RefreshToken token = RefreshToken.issue(
        "somehash", null, TENANT_USER_ID, SESSION_ID, "openid", EXPIRES_FUTURE, NOW, null);

    // Then
    assertThat(token.getClientAppId()).isNull();
    assertThat(token.isPlatformToken()).isTrue();
  }

  @Test
  void issue_withNullTenantUserId_isAllowed() {
    // Given / When — null tenantUserId = platform session RT
    RefreshToken token = RefreshToken.issue(
        "somehash", CLIENT_APP_ID, null, SESSION_ID, "openid", EXPIRES_FUTURE, NOW, null);

    // Then
    assertThat(token.getTenantUserId()).isNull();
  }

  @Test
  void isExpired_whenExpiresInPast_returnsTrue() {
    // Given
    RefreshToken token =
        RefreshToken.issue("hash", CLIENT_APP_ID, TENANT_USER_ID, SESSION_ID, "openid", EXPIRES_PAST, NOW, null);

    // When / Then
    assertThat(token.isExpired()).isTrue();
    assertThat(token.isValid()).isFalse();
  }

  @Test
  void markAsUsed_changesStatusToUsed() {
    // Given
    RefreshToken token = activeToken();
    RefreshTokenId newId = RefreshTokenId.generate();

    // When
    token.markAsUsed(NOW, newId);

    // Then
    assertThat(token.getStatus()).isEqualTo(RefreshTokenStatus.USED);
    assertThat(token.getUsedAt()).isEqualTo(NOW);
    assertThat(token.getReplacedByTokenId()).isEqualTo(newId);
  }

  @Test
  void markAsUsed_whenAlreadyUsed_throwsException() {
    // Given
    RefreshToken token = activeToken();
    RefreshTokenId generate = RefreshTokenId.generate();
    token.markAsUsed(NOW, generate);

    // When / Then
    assertThatThrownBy(() -> token.markAsUsed(NOW, generate))
        .isInstanceOf(InvalidRefreshTokenException.class)
        .hasMessageContaining("ACTIVE");
  }

  @Test
  void revoke_changesStatusToRevoked() {
    // Given
    RefreshToken token = activeToken();

    // When
    token.revoke();

    // Then
    assertThat(token.getStatus()).isEqualTo(RefreshTokenStatus.REVOKED);
  }

  @Test
  void revoke_whenAlreadyUsed_throwsException() {
    // Given
    RefreshTokenId replacedBy = RefreshTokenId.generate();
    RefreshToken token =
        RefreshToken.reconstitute(
            RefreshTokenId.generate(),
            "somehash",
            CLIENT_APP_ID,
            TENANT_USER_ID,
            SESSION_ID,
            "openid profile",
            RefreshTokenStatus.USED,
            EXPIRES_FUTURE,
            NOW,
            NOW,
            replacedBy,
            null);

    // When / Then
    assertThatThrownBy(token::revoke).isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void issue_withNullHash_throwsException() {
    assertThatThrownBy(
            () -> RefreshToken.issue(nullString(), CLIENT_APP_ID, TENANT_USER_ID, SESSION_ID, "openid", EXPIRES_FUTURE, NOW, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void issue_withBlankHash_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> RefreshToken.issue("   ", CLIENT_APP_ID, TENANT_USER_ID, SESSION_ID, "openid", EXPIRES_FUTURE, NOW, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("tokenHash");
  }

  @Test
  void issue_withNullSessionId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> RefreshToken.issue("somehash", CLIENT_APP_ID, TENANT_USER_ID, nullSessionId(), "openid", EXPIRES_FUTURE, NOW, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SessionId");
  }

  @Test
  void issue_withNullScopes_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> RefreshToken.issue("somehash", CLIENT_APP_ID, TENANT_USER_ID, SESSION_ID, nullString(), EXPIRES_FUTURE, NOW, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Scopes");
  }

  @Test
  void issue_withNullExpiresAt_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> RefreshToken.issue("somehash", CLIENT_APP_ID, TENANT_USER_ID, SESSION_ID, "openid", nullInstant(), NOW, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ExpiresAt");
  }

  @Test
  void issue_withNullNow_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> RefreshToken.issue("somehash", CLIENT_APP_ID, TENANT_USER_ID, SESSION_ID, "openid", EXPIRES_FUTURE, nullInstant(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Now");
  }

  @Test
  void markAsUsed_whenRevoked_throwsException() {
    // Given
    RefreshToken token = activeToken();
    RefreshTokenId replacedById = RefreshTokenId.generate();
    token.revoke();

    // When / Then
    assertThatThrownBy(() -> token.markAsUsed(NOW, replacedById))
        .isInstanceOf(InvalidRefreshTokenException.class)
        .hasMessageContaining("not ACTIVE");
  }

  @Test
  void revoke_whenAlreadyRevoked_isIdempotent() {
    // Given
    RefreshToken token = activeToken();

    // When
    token.revoke();
    token.revoke();

    // Then
    assertThat(token.getStatus()).isEqualTo(RefreshTokenStatus.REVOKED);
  }
}
