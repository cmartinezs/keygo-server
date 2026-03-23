package io.cmartinezs.keygo.app.auth.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.cmartinezs.keygo.app.auth.command.RotateRefreshTokenCommand;
import io.cmartinezs.keygo.app.auth.port.*;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.exception.RefreshTokenExpiredException;
import io.cmartinezs.keygo.domain.auth.model.*;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RotateRefreshTokenUseCaseTest {

  @Mock RefreshTokenRepositoryPort refreshTokenRepository;
  @Mock SessionRepositoryPort sessionRepository;
  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock TokenSignerPort tokenSigner;
  @Mock TokenClaimsFactoryPort tokenClaimsFactory;
  @Mock TenantRepositoryPort tenantRepository;
  @Mock ClientAppRepositoryPort clientAppRepository;
  @Mock UserRepositoryPort userRepository;
  @Mock ClockPort clock;

  RotateRefreshTokenUseCase useCase;

  private final TenantId tenantId = new TenantId(UUID.randomUUID());
  private final ClientAppId clientAppId = new ClientAppId(UUID.randomUUID());
  private final UserId userId = new UserId(UUID.randomUUID());
  private final SessionId sessionId = SessionId.generate();
  private final Instant now = Instant.now();

  @BeforeEach
  void setUp() {
    useCase = new RotateRefreshTokenUseCase(
        refreshTokenRepository, sessionRepository,
        signingKeyRepository, tokenSigner, tokenClaimsFactory,
        tenantRepository, clientAppRepository, userRepository,
        clock, "http://localhost:8080/keygo-server");
  }

  private RefreshToken buildActiveToken(String hash) {
    return RefreshToken.issue(hash, tenantId, clientAppId, userId, sessionId,
        "openid profile", now.plusSeconds(86400), now);
  }

  private void setupCommonMocks(String rawToken) {
    String hash = RotateRefreshTokenUseCase.sha256Hex(rawToken);
    RefreshToken rt = buildActiveToken(hash);
    RefreshToken newRt = buildActiveToken("newhash");

    when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(rt));
    when(clock.now()).thenReturn(now);

    Tenant tenant = mock(Tenant.class);
    when(tenant.getId()).thenReturn(tenantId);
    when(tenantRepository.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));

    ClientApp clientApp = mock(ClientApp.class);
    when(clientApp.getId()).thenReturn(clientAppId);
    when(clientAppRepository.findByClientIdAndTenantId(any(ClientId.class), eq(tenantId)))
        .thenReturn(Optional.of(clientApp));

    when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.empty());

    SigningKey key = SigningKey.builder()
        .id(new SigningKeyId("k1")).kid("kid1")
        .algorithm(SigningKeyAlgorithm.RS256)
        .status(SigningKeyStatus.ACTIVE)
        .publicMaterial("pub").privateMaterial("priv")
        .activatedAt(now).build();
    when(signingKeyRepository.findActiveKey()).thenReturn(Optional.of(key));

    when(tokenClaimsFactory.buildAccessTokenClaims(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Map.of());
    when(tokenClaimsFactory.buildIdTokenClaims(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Map.of());
    when(tokenSigner.signJwt(any(), any())).thenReturn("signed.jwt.token");
    when(refreshTokenRepository.save(any())).thenReturn(newRt);
  }

  @Test
  void givenValidRefreshToken_whenRotate_thenReturnsNewTokens() {
    // Given
    String rawToken = "validRawToken";
    setupCommonMocks(rawToken);

    // When
    var result = useCase.execute(new RotateRefreshTokenCommand("tenant-slug", "my-client", rawToken, null));

    // Then
    assertThat(result.accessToken()).isEqualTo("signed.jwt.token");
    assertThat(result.rawRefreshToken()).isNotNull().isNotBlank();
    assertThat(result.tokenType()).isEqualTo("Bearer");
    verify(refreshTokenRepository).save(any());
    verify(refreshTokenRepository).update(any());
  }

  @Test
  void givenExpiredToken_whenRotate_thenThrowsRefreshTokenExpiredException() {
    // Given
    String rawToken = "expiredToken";
    String hash = RotateRefreshTokenUseCase.sha256Hex(rawToken);
    RefreshToken expiredToken = RefreshToken.issue(hash, tenantId, clientAppId, userId, sessionId,
        "openid", now.minusSeconds(1), now.minusSeconds(3600));

    when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expiredToken));
    var command = new RotateRefreshTokenCommand("tenant-slug", "my-client", rawToken, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(RefreshTokenExpiredException.class);
  }

  @Test
  void givenRevokedToken_whenRotate_thenThrowsInvalidRefreshTokenException() {
    // Given
    String rawToken = "revokedToken";
    String hash = RotateRefreshTokenUseCase.sha256Hex(rawToken);
    RefreshToken revokedToken = buildActiveToken(hash);
    revokedToken.revoke();

    when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(revokedToken));
    var command = new RotateRefreshTokenCommand("tenant-slug", "my-client", rawToken, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(InvalidRefreshTokenException.class)
        .hasMessageContaining("revoked");
  }

  @Test
  void givenUnknownToken_whenRotate_thenThrowsInvalidRefreshTokenException() {
    // Given
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());
    var command = new RotateRefreshTokenCommand("tenant-slug", "my-client", "unknown", null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(InvalidRefreshTokenException.class);
  }
}


