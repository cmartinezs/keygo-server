package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ListUserSessionsCommand;
import io.cmartinezs.keygo.app.user.result.ListUserSessionsResult;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para ListUserSessionsUseCase.
 */
@ExtendWith(MockitoExtension.class)
class ListUserSessionsUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String BEARER_TOKEN = "valid.jwt.token";
  private static final String USER_AGENT = "Mozilla/5.0";
  private static final String IP_ADDRESS = "192.168.1.1";

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock AccessTokenVerifierPort accessTokenVerifier;
  @Mock TenantRepositoryPort tenantRepository;
  @Mock SessionRepositoryPort sessionRepository;

  private ListUserSessionsUseCase useCase;
  private Tenant tenant;
  private UUID userId;
  private TenantId tenantId;

  @BeforeEach
  void setUp() {
    useCase = new ListUserSessionsUseCase(
        signingKeyRepository, accessTokenVerifier, tenantRepository, sessionRepository);

    userId = UUID.randomUUID();
    tenantId = TenantId.of(UUID.randomUUID());

    tenant = Tenant.builder()
        .id(tenantId)
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME")
        .status(TenantStatus.ACTIVE).build();

    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(mock(SigningKey.class)));
    lenient().when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
  }

  @Test
  void execute_shouldReturnOnlyActiveSessions() {
    // Given
    var activeSession = Session.reconstitute(
        SessionId.from(UUID.randomUUID()), userId, ClientAppId.of(UUID.randomUUID()),
        SessionStatus.ACTIVE,
        Instant.now().plusSeconds(3600), Instant.now(), USER_AGENT, IP_ADDRESS, Instant.now(), null);
    var terminatedSession = Session.reconstitute(
        SessionId.from(UUID.randomUUID()), userId, ClientAppId.of(UUID.randomUUID()),
        SessionStatus.TERMINATED,
        Instant.now().plusSeconds(3600), Instant.now(), "other-ua", "10.0.0.1", Instant.now(), null);

    when(sessionRepository.findAllByPlatformUserId(any()))
        .thenReturn(List.of(activeSession, terminatedSession));

    // When
    ListUserSessionsResult result = useCase.execute(
        new ListUserSessionsCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.sessions()).hasSize(1);
    assertThat(result.sessions().get(0).status()).isEqualTo("ACTIVE");
  }

  @Test
  void execute_shouldMarkCurrentSession_whenSidClaimMatchesSessionId() {
    // Given — the JWT carries a 'sid' claim pointing to the current session
    UUID currentSessionId = UUID.randomUUID();
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString(), "sid", currentSessionId.toString()));

    var currentSession = Session.reconstitute(
        SessionId.from(currentSessionId), userId, ClientAppId.of(UUID.randomUUID()),
        SessionStatus.ACTIVE,
        Instant.now().plusSeconds(3600), Instant.now(), USER_AGENT, IP_ADDRESS, Instant.now(), null);
    var otherSession = Session.reconstitute(
        SessionId.from(UUID.randomUUID()), userId, ClientAppId.of(UUID.randomUUID()),
        SessionStatus.ACTIVE,
        Instant.now().plusSeconds(3600), Instant.now(), "other-ua", "10.0.0.2", Instant.now(), null);

    when(sessionRepository.findAllByPlatformUserId(any()))
        .thenReturn(List.of(currentSession, otherSession));

    // When
    ListUserSessionsResult result = useCase.execute(
        new ListUserSessionsCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.sessions()).hasSize(2);
    assertThat(result.sessions().stream().filter(s -> s.isCurrent()).count()).isEqualTo(1);
    assertThat(result.sessions().get(0).isCurrent()).isTrue();
    assertThat(result.sessions().get(1).isCurrent()).isFalse();
  }

  @Test
  void execute_shouldReturnEmptyList_whenNoActiveSessions() {
    // Given
    when(sessionRepository.findAllByPlatformUserId(any())).thenReturn(List.of());

    // When
    ListUserSessionsResult result = useCase.execute(
        new ListUserSessionsCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.sessions()).isEmpty();
  }
}
