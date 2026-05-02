package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.auth.exception.SessionInvalidStateException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SessionTest {

  private static final UUID PLATFORM_USER_ID = UUID.randomUUID();
  private static final ClientAppId CLIENT_APP_ID = new ClientAppId(UUID.randomUUID());
  private static final Instant NOW = Instant.now();
  private static final Instant EXPIRES_AT = NOW.plusSeconds(3600);

  private Session newActiveSession() {
    return Session.open(PLATFORM_USER_ID, CLIENT_APP_ID, EXPIRES_AT, NOW, "agent", "127.0.0.1", null);
  }

  private Instant nullInstant() {
    return null;
  }

  @Test
  void open_createsSessionInActiveStatus() {
    // Given / When
    Session session = newActiveSession();

    // Then
    assertThat(session.getId()).isNotNull();
    assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
    assertThat(session.isActive()).isTrue();
    assertThat(session.getPlatformUserId()).isEqualTo(PLATFORM_USER_ID);
    assertThat(session.getClientAppId()).isEqualTo(CLIENT_APP_ID);
    assertThat(session.getUserAgent()).isEqualTo("agent");
    assertThat(session.getIpAddress()).isEqualTo("127.0.0.1");
    assertThat(session.isPlatformSession()).isFalse();
  }

  @Test
  void open_withNullPlatformUserId_createsSession() {
    // Given / When — platformUserId is nullable for MVP
    Session session = Session.open(null, CLIENT_APP_ID, EXPIRES_AT, NOW, "agent", "127.0.0.1", null);

    // Then
    assertThat(session.getPlatformUserId()).isNull();
    assertThat(session.isActive()).isTrue();
  }

  @Test
  void open_withNullClientAppId_createsPlatformSession() {
    // Given / When — null clientAppId = platform session
    Session session = Session.open(PLATFORM_USER_ID, null, EXPIRES_AT, NOW, "agent", "127.0.0.1", null);

    // Then
    assertThat(session.getClientAppId()).isNull();
    assertThat(session.isPlatformSession()).isTrue();
  }

  @Test
  void terminate_changesStatusToTerminated() {
    // Given
    Session session = newActiveSession();

    // When
    session.terminate();

    // Then
    assertThat(session.getStatus()).isEqualTo(SessionStatus.TERMINATED);
    assertThat(session.isActive()).isFalse();
  }

  @Test
  void terminate_whenAlreadyTerminated_throwsException() {
    // Given
    Session session = newActiveSession();
    session.terminate();

    // When / Then
    assertThatThrownBy(session::terminate)
        .isInstanceOf(SessionInvalidStateException.class)
        .hasMessageContaining("TERMINATED");
  }

  @Test
  void terminate_whenSessionIsExpired_throwsException() {
    // Given
    Session session =
        Session.reconstitute(
            SessionId.generate(),
            PLATFORM_USER_ID,
            CLIENT_APP_ID,
            SessionStatus.EXPIRED,
            EXPIRES_AT,
            NOW,
            "agent",
            "127.0.0.1",
            NOW,
            null);

    // When / Then
    assertThatThrownBy(session::terminate)
        .isInstanceOf(SessionInvalidStateException.class)
        .hasMessageContaining("EXPIRED");
  }

  @Test
  void updateLastAccessed_updatesTimestamp() {
    // Given
    Session session = newActiveSession();
    Instant newTime = NOW.plusSeconds(60);

    // When
    session.updateLastAccessed(newTime);

    // Then
    assertThat(session.getLastAccessedAt()).isEqualTo(newTime);
  }

  @Test
  void open_withNullExpiresAt_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> Session.open(PLATFORM_USER_ID, CLIENT_APP_ID, nullInstant(), NOW, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ExpiresAt");
  }

  @Test
  void open_withNullNow_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> Session.open(PLATFORM_USER_ID, CLIENT_APP_ID, EXPIRES_AT, nullInstant(), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Now");
  }

  @Test
  void updateLastAccessed_withNull_throwsException() {
    // Given
    Session session = newActiveSession();

    // When / Then
    assertThatThrownBy(() -> session.updateLastAccessed(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("AccessedAt");
  }
}
