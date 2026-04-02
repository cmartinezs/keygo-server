package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RevokeUserSessionCommand;
import io.cmartinezs.keygo.app.user.result.RevokeUserSessionResult;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserId;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RevokeUserSessionUseCase.
 */
@ExtendWith(MockitoExtension.class)
class RevokeUserSessionUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String BEARER_TOKEN = "valid.jwt.token";

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock AccessTokenVerifierPort accessTokenVerifier;
  @Mock TenantRepositoryPort tenantRepository;
  @Mock SessionRepositoryPort sessionRepository;
  @Mock RefreshTokenRepositoryPort refreshTokenRepository;

  private RevokeUserSessionUseCase useCase;
  private Tenant tenant;
  private UUID userId;
  private UUID sessionUuid;
  private TenantId tenantId;

  @BeforeEach
  void setUp() {
    useCase = new RevokeUserSessionUseCase(
        signingKeyRepository, accessTokenVerifier, tenantRepository,
        sessionRepository, refreshTokenRepository);

    userId = UUID.randomUUID();
    sessionUuid = UUID.randomUUID();
    tenantId = TenantId.of(UUID.randomUUID());

    tenant = Tenant.builder()
        .id(tenantId)
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE).build();

    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(mock(SigningKey.class)));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
  }

  // ─── Escenario 1: revocación exitosa ─────────────────────────────────────

  @Test
  void execute_shouldRevokeSession_whenSessionIsActive() {
    // Given
    var session = Session.reconstitute(
        SessionId.from(sessionUuid), tenantId, ClientAppId.of(UUID.randomUUID()),
        new UserId(userId), SessionStatus.ACTIVE,
        Instant.now().plusSeconds(3600), Instant.now(), "ua", "1.1.1.1", Instant.now());

    when(sessionRepository.findById(any())).thenReturn(Optional.of(session));

    // When
    RevokeUserSessionResult result = useCase.execute(
        new RevokeUserSessionCommand(TENANT_SLUG, BEARER_TOKEN, sessionUuid.toString()));

    // Then
    assertThat(result.alreadyClosed()).isFalse();
    assertThat(result.sessionId()).isEqualTo(sessionUuid);
    verify(refreshTokenRepository).revokeAllForSession(any());
    verify(sessionRepository).update(any());
  }

  // ─── Escenario 2: sesión no encontrada → idempotente ─────────────────────

  @Test
  void execute_shouldReturnAlreadyClosed_whenSessionNotFound() {
    // Given
    when(sessionRepository.findById(any())).thenReturn(Optional.empty());

    // When
    RevokeUserSessionResult result = useCase.execute(
        new RevokeUserSessionCommand(TENANT_SLUG, BEARER_TOKEN, sessionUuid.toString()));

    // Then
    assertThat(result.alreadyClosed()).isTrue();
    verifyNoInteractions(refreshTokenRepository);
  }

  // ─── Escenario 3: sesión ya terminada → idempotente ──────────────────────

  @Test
  void execute_shouldReturnAlreadyClosed_whenSessionIsTerminated() {
    // Given
    var session = Session.reconstitute(
        SessionId.from(sessionUuid), tenantId, ClientAppId.of(UUID.randomUUID()),
        new UserId(userId), SessionStatus.TERMINATED,
        Instant.now().plusSeconds(3600), Instant.now(), "ua", "1.1.1.1", Instant.now());

    when(sessionRepository.findById(any())).thenReturn(Optional.of(session));

    // When
    RevokeUserSessionResult result = useCase.execute(
        new RevokeUserSessionCommand(TENANT_SLUG, BEARER_TOKEN, sessionUuid.toString()));

    // Then
    assertThat(result.alreadyClosed()).isTrue();
    verifyNoInteractions(refreshTokenRepository);
  }

  // ─── Escenario 4: sesión pertenece a otro usuario ────────────────────────

  @Test
  void execute_shouldThrowSecurityException_whenSessionBelongsToOtherUser() {
    // Given
    var otherUserId = UUID.randomUUID();
    var session = Session.reconstitute(
        SessionId.from(sessionUuid), tenantId, ClientAppId.of(UUID.randomUUID()),
        new UserId(otherUserId), SessionStatus.ACTIVE,
        Instant.now().plusSeconds(3600), Instant.now(), "ua", "1.1.1.1", Instant.now());

    when(sessionRepository.findById(any())).thenReturn(Optional.of(session));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new RevokeUserSessionCommand(TENANT_SLUG, BEARER_TOKEN, sessionUuid.toString())))
        .isInstanceOf(SecurityException.class);

    verifyNoInteractions(refreshTokenRepository);
  }
}
