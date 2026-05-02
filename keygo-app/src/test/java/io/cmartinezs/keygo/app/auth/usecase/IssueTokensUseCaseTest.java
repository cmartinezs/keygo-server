package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenClaimsFactoryPort;
import io.cmartinezs.keygo.app.auth.port.TokenSignerPort;
import io.cmartinezs.keygo.domain.auth.exception.NoActiveSigningKeyException;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueTokensUseCaseTest {

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock TokenSignerPort tokenSigner;
  @Mock TokenClaimsFactoryPort tokenClaimsFactory;
  @Mock ClockPort clock;

  IssueTokensUseCase useCase;

  private SigningKey activeKey;

  @BeforeEach
  void setUp() {
    useCase = new IssueTokensUseCase(signingKeyRepository, tokenSigner, tokenClaimsFactory, clock);
    activeKey = SigningKey.builder()
        .id(new SigningKeyId("key-1"))
        .kid("kid-1")
        .algorithm(SigningKeyAlgorithm.RS256)
        .status(SigningKeyStatus.ACTIVE)
        .publicMaterial("PUBLIC_PEM")
        .privateMaterial("PRIVATE_PEM")
        .activatedAt(Instant.now())
        .build();
  }

  @Test
  void givenActiveKey_whenExecute_thenReturnsBothTokens() {
    // Given
    when(signingKeyRepository.findActiveKey()).thenReturn(Optional.of(activeKey));
    when(clock.now()).thenReturn(Instant.now());
    when(tokenClaimsFactory.buildAccessTokenClaims(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Map.of("iss", "http://localhost"));
    when(tokenClaimsFactory.buildIdTokenClaims(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Map.of("iss", "http://localhost", "sub", "user-1"));
    when(tokenSigner.signJwt(any(), eq(activeKey)))
        .thenReturn("access.jwt.token", "id.jwt.token");

    // When
    var result = useCase.execute(
        null,
        "http://localhost/keygo-server/api/v1/tenants/my-tenant",
        "user-uuid",
        "client-id",
        "openid profile",
        null, null, null,
        "code-id-123",
        List.of("ADMIN", "USER"));

    // Then
    assertThat(result.accessToken()).isEqualTo("access.jwt.token");
    assertThat(result.idToken()).isEqualTo("id.jwt.token");
    assertThat(result.tokenType()).isEqualTo("Bearer");
    assertThat(result.expiresIn()).isEqualTo(3600L);
    assertThat(result.scope()).isEqualTo("openid profile");
    assertThat(result.authorizationCodeId()).isEqualTo("code-id-123");
  }

  @Test
  void givenActiveKeyAndEmptyRoles_whenExecute_thenReturnsBothTokens() {
    // Given
    when(signingKeyRepository.findActiveKey()).thenReturn(Optional.of(activeKey));
    when(clock.now()).thenReturn(Instant.now());
    when(tokenClaimsFactory.buildAccessTokenClaims(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Map.of("iss", "http://localhost"));
    when(tokenClaimsFactory.buildIdTokenClaims(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Map.of("iss", "http://localhost", "sub", "user-1"));
    when(tokenSigner.signJwt(any(), eq(activeKey)))
        .thenReturn("access.jwt.token", "id.jwt.token");

    // When
    var result = useCase.execute(
        null,
        "http://localhost/keygo-server/api/v1/tenants/my-tenant",
        "user-uuid", "client-id", "openid profile",
        null, null, null, "code-id-456", List.of());

    // Then
    assertThat(result.accessToken()).isEqualTo("access.jwt.token");
    assertThat(result.idToken()).isEqualTo("id.jwt.token");
  }

  @Test
  void givenNoActiveKey_whenExecute_thenThrowsNoActiveSigningKeyException() {
    // Given
    when(signingKeyRepository.findActiveKey()).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        null, "http://localhost", "user", "client", "openid", null, null, null, "code-1", null))
        .isInstanceOf(NoActiveSigningKeyException.class);
  }
}
