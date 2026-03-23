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
    assertThatThrownBy(() -> Session.open(null, CLIENT_APP_ID, USER_ID, EXPIRES_AT, NOW, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

