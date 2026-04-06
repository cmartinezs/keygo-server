package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.RevokeTokenCommand;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.*;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevokeTokenUseCaseTest {

  @Mock RefreshTokenRepositoryPort refreshTokenRepository;

  RevokeTokenUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new RevokeTokenUseCase(refreshTokenRepository);
  }

  private RefreshToken activeToken(String hash) {
    return RefreshToken.issue(hash,
        new TenantId(UUID.randomUUID()),
        new ClientAppId(UUID.randomUUID()),
        new UserId(UUID.randomUUID()),
        SessionId.generate(),
        "openid", Instant.now().plusSeconds(86400), Instant.now(), null);
  }

  @Test
  void givenActiveToken_whenRevoke_thenTokenIsRevoked() {
    // Given
    String rawToken = "someRawToken123";
    String expectedHash = RevokeTokenUseCase.hashTokenStatic(rawToken);
    RefreshToken token = activeToken(expectedHash);

    when(refreshTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(token));

    // When
    useCase.execute(new RevokeTokenCommand("tenant", "client", rawToken, "refresh_token"));

    // Then
    assertThat(token.getStatus()).isEqualTo(RefreshTokenStatus.REVOKED);
    verify(refreshTokenRepository).update(token);
  }

  @Test
  void givenNonExistentToken_whenRevoke_thenNoException() {
    // Given
    when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatCode(() ->
        useCase.execute(new RevokeTokenCommand("tenant", "client", "unknownToken", null)))
        .doesNotThrowAnyException();
    verify(refreshTokenRepository, never()).update(any());
  }

  @Test
  void givenAlreadyRevokedToken_whenRevoke_thenIdempotentNoException() {
    // Given
    String rawToken = "revokedToken";
    String expectedHash = RevokeTokenUseCase.hashTokenStatic(rawToken);
    RefreshToken token = activeToken(expectedHash);
    token.revoke();

    when(refreshTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(token));

    // When / Then
    assertThatCode(() ->
        useCase.execute(new RevokeTokenCommand("tenant", "client", rawToken, null)))
        .doesNotThrowAnyException();
    verify(refreshTokenRepository, never()).update(any());
  }
}

