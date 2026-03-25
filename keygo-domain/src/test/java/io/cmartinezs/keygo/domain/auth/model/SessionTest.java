package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SessionTest {

  private static final TenantId TENANT_ID = new TenantId(UUID.randomUUID());
  private static final ClientAppId CLIENT_APP_ID = new ClientAppId(UUID.randomUUID());
  private static final UserId USER_ID = new UserId(UUID.randomUUID());
  private static final Instant NOW = Instant.now();
  private static final Instant EXPIRES_AT = NOW.plusSeconds(3600);

  private Session newActiveSession() {
    return Session.open(TENANT_ID, CLIENT_APP_ID, USER_ID, EXPIRES_AT, NOW, "agent", "127.0.0.1");
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
    assertThat(session.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(session.getClientAppId()).isEqualTo(CLIENT_APP_ID);
    assertThat(session.getUserId()).isEqualTo(USER_ID);
    assertThat(session.getUserAgent()).isEqualTo("agent");
    assertThat(session.getIpAddress()).isEqualTo("127.0.0.1");
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
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("TERMINATED");
  }

  @Test
  void terminate_whenSessionIsExpired_throwsException() {
    // Given
    Session session =
        Session.reconstitute(
            SessionId.generate(),
            TENANT_ID,
            CLIENT_APP_ID,
            USER_ID,
            SessionStatus.EXPIRED,
            EXPIRES_AT,
            NOW,
            "agent",
            "127.0.0.1",
            NOW);

    // When / Then
    assertThatThrownBy(session::terminate)
        .isInstanceOf(IllegalStateException.class)
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
  void open_withNullTenantId_throwsException() {
    assertThatThrownBy(
            () ->
                Session.open(
                    nullTenantId(),
                    CLIENT_APP_ID,
                    USER_ID,
                    EXPIRES_AT,
                    NOW,
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void open_withNullClientAppId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> Session.open(TENANT_ID, nullClientAppId(), USER_ID, EXPIRES_AT, NOW, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ClientAppId");
  }

  @Test
  void open_withNullUserId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> Session.open(TENANT_ID, CLIENT_APP_ID, nullUserId(), EXPIRES_AT, NOW, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void open_withNullExpiresAt_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> Session.open(TENANT_ID, CLIENT_APP_ID, USER_ID, nullInstant(), NOW, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ExpiresAt");
  }

  @Test
  void open_withNullNow_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () -> Session.open(TENANT_ID, CLIENT_APP_ID, USER_ID, EXPIRES_AT, nullInstant(), null, null))
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
