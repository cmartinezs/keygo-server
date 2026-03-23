package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.*;

import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  private static final TenantId TENANT_ID = new TenantId(UUID.randomUUID());
  private static final ClientAppId CLIENT_APP_ID = new ClientAppId(UUID.randomUUID());
  private static final UserId USER_ID = new UserId(UUID.randomUUID());
  private static final SessionId SESSION_ID = SessionId.generate();
  private static final Instant NOW = Instant.now();
  private static final Instant EXPIRES_FUTURE = NOW.plusSeconds(86400);
  private static final Instant EXPIRES_PAST = NOW.minusSeconds(1);

  private RefreshToken activeToken() {
    return RefreshToken.issue(
        "somehash",
        TENANT_ID,
        CLIENT_APP_ID,
        USER_ID,
        SESSION_ID,
        "openid profile",
        EXPIRES_FUTURE,
        NOW);
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
  }

  @Test
  void isExpired_whenExpiresInPast_returnsTrue() {
    // Given
    RefreshToken token =
        RefreshToken.issue(
            "hash", TENANT_ID, CLIENT_APP_ID, USER_ID, SESSION_ID, "openid", EXPIRES_PAST, NOW);

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
    RefreshToken token = activeToken();
    token.markAsUsed(NOW, RefreshTokenId.generate());

    // When / Then
    assertThatThrownBy(token::revoke).isInstanceOf(InvalidRefreshTokenException.class);
  }

  @Test
  void issue_withNullHash_throwsException() {
    assertThatThrownBy(
            () ->
                RefreshToken.issue(
                    null,
                    TENANT_ID,
                    CLIENT_APP_ID,
                    USER_ID,
                    SESSION_ID,
                    "openid",
                    EXPIRES_FUTURE,
                    NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
